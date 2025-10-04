package com.sehoprojectmanagerapi.config.keygenerator;

import com.sehoprojectmanagerapi.repository.project.Project;
import org.springframework.stereotype.Component;

@Component
public class TaskKeyGenerator {
    public String generate(Project project, Long taskId) {
        // 프로젝트 prefix 가져오기 (없으면 ID 기반 fallback)
        String prefix = project.getKey();
        if (prefix == null || prefix.isBlank()) {
            prefix = "PRJ" + project.getId();
        }

        // TaskId 를 그대로 붙이는 방식
        return prefix.toUpperCase() + "-" + taskId;
    }
}
