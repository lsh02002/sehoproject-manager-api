package com.sehoprojectmanagerapi.repository.task;

public enum TaskType {
    STORY,
    TASK,
    BUG,
    EPIC;

    public static TaskType from(String value) {
        if (value == null) return null;
        try {
            return TaskType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 TaskType 값입니다: " + value);
        }
    }
}
