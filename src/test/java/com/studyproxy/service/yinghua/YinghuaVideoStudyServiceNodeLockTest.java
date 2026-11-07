package com.studyproxy.service.yinghua;

import com.studyproxy.api.yinghua.YinghuaCourseApi;
import com.studyproxy.api.yinghua.YinghuaLoginApi;
import com.studyproxy.api.yinghua.YinghuaStudyApi;
import com.studyproxy.entity.yinghua.YinghuaCourse;
import com.studyproxy.entity.yinghua.YinghuaNode;
import com.studyproxy.entity.yinghua.YinghuaUserCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证 nodeLock 快照刷新的修复效果。
 *
 * <p>模拟英华学堂「按顺序解锁」的服务端状态：第 N 个视频在第 N-1 个达到 100% 后才解锁，
 * 而 chapter.json 返回的是「请求那一刻」的锁定快照。
 *
 * <p>修复前：只用首次拉取的快照，第一个视频完成后，其余节点全部被判「已锁定」跳过，
 * 最终只完成 1 个视频。
 *
 * <p>修复后：完成一个视频后再遇到锁定节点会自动刷新快照，从而依次刷完全部视频。
 */
class YinghuaVideoStudyServiceNodeLockTest {

    private static final int TOTAL = 5;
    private static final int DURATION = 5;      // 缩短单视频耗时
    private static final int SUBMIT_STEP = 5;   // 与服务内部 SUBMIT_INTERVAL_SECONDS 保持一致

    private YinghuaVideoStudyService service;

    /** 服务端真实进度：index -> 已观看秒数 */
    private int[] watched;
    /** 每次 chapter.json 被请求时累加，用于断言确实发生了刷新 */
    private int chapterCalls;

    @BeforeEach
    void setUp() {
        watched = new int[TOTAL];
        chapterCalls = 0;

        service = new YinghuaVideoStudyService();

        YinghuaCourseApi courseApi = new YinghuaCourseApi() {
            @Override
            public List<YinghuaNode> getVideoList(YinghuaUserCache userCache, String courseId) {
                return snapshot();
            }
        };
        YinghuaStudyApi studyApi = new YinghuaStudyApi() {
            @Override
            public String submitStudyTime(YinghuaUserCache userCache, String nodeId,
                                          String studyId, Integer time) {
                int idx = Integer.parseInt(nodeId) - 1;
                if (!unlocked(idx)) {
                    return "{\"status\":false,\"msg\":\"尚未解锁\"}";
                }
                if (time > watched[idx]) {
                    watched[idx] = Math.min(time, DURATION);
                }
                return "{\"status\":true,\"msg\":\"提交成功\","
                        + "\"result\":{\"data\":{\"studyId\":\"1001\"}}}";
            }

            @Override
            public String getVideoWatchRecord(YinghuaUserCache userCache, String courseId, int page) {
                return "{\"status\":true,\"msg\":\"无记录\",\"result\":{\"list\":[]}}";
            }

            @Override
            public String getVideoWatchRecordPC(YinghuaUserCache userCache, String courseId, int page) {
                return "{\"status\":true,\"list\":[]}";
            }
        };
        YinghuaLoginApi loginApi = new YinghuaLoginApi() {
            @Override
            public String keepAlive(YinghuaUserCache userCache) {
                return "ok";
            }
        };

        ReflectionTestUtils.setField(service, "yinghuaCourseApi", courseApi);
        ReflectionTestUtils.setField(service, "yinghuaStudyApi", studyApi);
        ReflectionTestUtils.setField(service, "yinghuaLoginApi", loginApi);
    }

    /** 构造「当前真实状态」的章节快照 */
    private List<YinghuaNode> snapshot() {
        chapterCalls++;
        List<YinghuaNode> nodes = new ArrayList<>();
        for (int i = 0; i < TOTAL; i++) {
            YinghuaNode n = new YinghuaNode();
            n.setId(String.valueOf(i + 1));
            n.setCourseId("1");
            n.setName("[1." + (i + 1) + "]--测试视频" + (i + 1) + ".mp4");
            n.setVideoDuration(DURATION);
            n.setViewedDuration(watched[i]);
            n.setProgress(watched[i] >= DURATION ? 100f : (float) watched[i] / DURATION * 100f);
            n.setNodeLock(unlocked(i) ? 0 : 1);
            n.setTabVideo(true);
            // 故意保留考试/作业标记，验证它们已被完全忽略
            n.setTabExam(true);
            n.setTabWork(true);
            nodes.add(n);
        }
        return nodes;
    }

    private boolean unlocked(int idx) {
        return idx == 0 || watched[idx - 1] >= DURATION;
    }

    private YinghuaCourse course() {
        YinghuaCourse c = new YinghuaCourse();
        c.setId("1");
        c.setName("测试课程");
        return c;
    }

    @Test
    @DisplayName("按顺序解锁的课程应能依次刷完全部视频")
    void shouldStudyAllVideosWhenUnlockedProgressively() {
        service.studyCourse(new YinghuaUserCache(), course());

        for (int i = 0; i < TOTAL; i++) {
            assertEquals(DURATION, watched[i],
                    "第 " + (i + 1) + " 个视频应被刷到 100%");
        }
        assertTrue(chapterCalls > 1,
                "完成视频后应重新拉取章节快照以判断解锁状态，实际请求次数: " + chapterCalls);
    }

    @Test
    @DisplayName("已锁定且确实无法解锁时不应死循环，也不应被误判为完成")
    void shouldNotLoopForeverOnPermanentlyLockedVideo() {
        // 第 2 个视频永远锁定（模拟前置条件不满足）
        YinghuaCourseApi courseApi = new YinghuaCourseApi() {
            @Override
            public List<YinghuaNode> getVideoList(YinghuaUserCache userCache, String courseId) {
                List<YinghuaNode> nodes = snapshot();
                nodes.get(1).setNodeLock(1);
                return nodes;
            }
        };
        // 第 2 个之后不再解锁
        ReflectionTestUtils.setField(service, "yinghuaCourseApi", courseApi);

        long start = System.currentTimeMillis();
        service.studyCourse(new YinghuaUserCache(), course());
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(DURATION, watched[0], "第 1 个视频应正常完成");
        assertEquals(0, watched[1], "被锁定的第 2 个视频不应有进度");
        // 5 个视频 * (最多刷新若干次) 不该耗时过久；给出宽松上限防止回归成死循环
        assertTrue(elapsed < 60_000, "遍历不应超时，实际耗时 " + elapsed + "ms");
    }

    @Test
    @DisplayName("已达 100% 的视频直接返回完成，不重复提交")
    void shouldSkipAlreadyFinishedVideo() {
        watched[0] = DURATION;
        List<YinghuaNode> nodes = snapshot();
        YinghuaNode first = nodes.get(0);

        assertTrue(service.studyVideo(new YinghuaUserCache(), first));
    }

    @Test
    @DisplayName("非视频节点不参与学习")
    void shouldIgnoreNonVideoNode() {
        YinghuaNode node = new YinghuaNode();
        node.setId("99");
        node.setTabVideo(false);
        node.setVideoDuration(DURATION);

        assertFalse(service.studyVideo(new YinghuaUserCache(), node));
    }
}
