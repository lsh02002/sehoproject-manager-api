package com.sehoprojectmanagerapi.repository.task;

public enum TaskPriority {
    LOW, MEDIUM, HIGH, URGENT; // 문자열을 안전하게 enum 으로 변환

    public static TaskPriority from(String value) {
        if (value == null) return null;
        try {
            return TaskPriority.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 우선순위 값입니다: " + value);
        }
    }
}
