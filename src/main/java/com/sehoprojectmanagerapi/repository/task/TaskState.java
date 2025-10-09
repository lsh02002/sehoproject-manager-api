package com.sehoprojectmanagerapi.repository.task;

public enum TaskState {
    TODO, IN_PROGRESS, BLOCKED, DONE;

    public static TaskState from(String value) {
        if (value == null) return null;
        try {
            return TaskState.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 TaskType 값입니다: " + value);
        }
    }
}
