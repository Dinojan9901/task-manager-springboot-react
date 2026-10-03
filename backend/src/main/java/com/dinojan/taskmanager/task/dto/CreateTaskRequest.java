package com.dinojan.taskmanager.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body of {@code POST /api/tasks}. Only the title can be chosen by the client. */
public record CreateTaskRequest(

		@NotBlank(message = "Title must not be blank")
		@Size(max = 255, message = "Title must be at most 255 characters")
		String title) {
}
