package com.sehoprojectmanagerapi.repository.activity.logger;

import com.sehoprojectmanagerapi.repository.activity.ActivityEntityType;
import com.sehoprojectmanagerapi.repository.project.Project;

// Loggable.java
public interface Loggable {
    Project logProject();
    ActivityEntityType logTargetType(); // "Project", "Task" 등
    Long logTargetId();   // 보통 getId().toString()
    default String logMessage() { return null; }
}

