package com.studyproxy.enums;

public enum QuestionTypeEnum {
    SINGLE_CHOICE("单选题", 0),
    MULTIPLE_CHOICE("多选题", 1),
    FILL_IN_THE_BLANK("填空题", 2),
    TRUE_OR_FALSE("判断题", 3),
    SHORT_ANSWER("简答题", 4),
    TERM_EXPLANATION("名词解释", 5),
    ESSAY("论述题", 6),
    CALCULATION("计算题", 7),
    OTHER("其它", 8),
    JOURNAL_ENTRY("分录题", 9),
    DOCUMENT_BASED("资料题", 10),
    MATCHING("连线题", 11),
    ORDERING("排序题", 12),
    CLOZE("完型填空", 13),
    READING_COMPREHENSION("阅读理解", 14);

    private final String name;
    private final int index;

    QuestionTypeEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public static QuestionTypeEnum fromName(String name) {
        for (QuestionTypeEnum type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return OTHER;
    }

    public static QuestionTypeEnum fromIndex(int index) {
        for (QuestionTypeEnum type : values()) {
            if (type.index == index) {
                return type;
            }
        }
        return OTHER;
    }
}
