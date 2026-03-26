package com.studyproxy.util;

import cn.hutool.core.util.RandomUtil;

public class UaUtil {

    public static String getMobileUA(String deviceVendor, String appVersion, String build, String imei) {
        return String.format(
                "Mozilla/5.0 (Linux; Android 12; %s Build/OPM1.171019.019; wv) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/71.0.3578.99 Mobile Safari/537.36 " +
                        "(schild:%s) " +
                        "(device:%s) " +
                        "Language/zh_CN " +
                        "com.chaoxing.mobile/ChaoXingStudy_3_%s_android_phone_%s " +
                        "(@Kalimdor)_%s",
                deviceVendor,
                generateMobileSign(deviceVendor, "zh_CN", appVersion, build, imei),
                deviceVendor,
                appVersion,
                build,
                imei
        );
    }

    public static String getWebUA() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 Edg/107.0.1418.35";
    }

    public static String getDefaultUA() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    }

    public static String getDesktopUA() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    }

    private static String generateMobileSign(String model, String locale, String version, String build, String imei) {
        String str = String.format(
                "(schild:ipL$TkeiEmfy1gTXb2XHrdLN0a@7c^vu) " +
                        "(device:%s) " +
                        "Language/%s " +
                        "com.chaoxing.mobile/ChaoXingStudy_3_%s_android_phone_%s " +
                        "(@Kalimdor)_%s",
                model, locale, version, build, imei
        );
        return CryptoUtil.md5(str);
    }

    public static String generateImei() {
        return RandomUtil.randomString("0123456789abcdef", 16);
    }

    public static String generate17DigitNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 17; i++) {
            sb.append(RandomUtil.randomInt(0, 10));
        }
        return sb.toString();
    }
}