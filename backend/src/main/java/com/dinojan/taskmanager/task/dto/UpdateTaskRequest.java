package com.dinojan.taskmanager.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code PUT /api/tasks/{id}}. PUT replaces the editable state, so both fields are required.
 * {@code Boolean} (not {@code boolean}) lets {@code @NotNull} catch a missing field instead of
 * silently defaulting it to {@code false}.
 */
public record UpdateTaskRequest(

		@NotBlank(message = "Title must not be blank")
		@Size(max = 255, message = "Title must be at most 255 characters")
		String title,

		@NotNull(message = "Completed must be provided")
		Boolean completed) {
}
