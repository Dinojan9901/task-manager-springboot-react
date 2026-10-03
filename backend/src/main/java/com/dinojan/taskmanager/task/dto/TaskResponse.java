package com.dinojan.taskmanager.task.dto;

import com.dinojan.taskmanager.task.Task;

import java.time.Instant;

/** What the API returns for a task. */
public record TaskResponse(Long id, String title, boolean completed, Instant createdAt) {

	public static TaskResponse from(Task task) {
		return new TaskResponse(task.getId(), task.getTitle(), task.isCompleted(), task.getCreatedAt());
	}
}
