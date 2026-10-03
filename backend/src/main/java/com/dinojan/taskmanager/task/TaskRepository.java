package com.dinojan.taskmanager.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

	// Spring Data derives the SQL from the method name: ... ORDER BY created_at DESC
	List<Task> findAllByOrderByCreatedAtDesc();
}
