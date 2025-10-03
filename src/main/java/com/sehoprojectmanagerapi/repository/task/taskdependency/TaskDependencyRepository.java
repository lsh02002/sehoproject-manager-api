package com.sehoprojectmanagerapi.repository.task;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, String> {
}
