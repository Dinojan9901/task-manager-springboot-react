package com.dinojan.taskmanager.task;

import com.dinojan.taskmanager.task.dto.CreateTaskRequest;
import com.dinojan.taskmanager.task.dto.TaskResponse;
import com.dinojan.taskmanager.task.dto.UpdateTaskRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer slice test: starts only Spring MVC (controller, exception handler, CORS config, JSON),
 * not the database. The service is replaced by a Mockito mock, so we test HTTP concerns only:
 * routing, status codes, JSON shape, validation and error mapping.
 */
@WebMvcTest(TaskController.class)
class TaskControllerTest {

	private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TaskService service;

	@Test
	void getAllReturnsTasksAsJson() throws Exception {
		given(service.findAll()).willReturn(List.of(
				new TaskResponse(2L, "Second", false, CREATED_AT),
				new TaskResponse(1L, "First", true, CREATED_AT)));

		mockMvc.perform(get("/api/tasks"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(2))
				.andExpect(jsonPath("$[0].title").value("Second"))
				.andExpect(jsonPath("$[1].completed").value(true))
				.andExpect(jsonPath("$[1].createdAt").value("2026-01-01T10:00:00Z"));
	}

	@Test
	void getByIdReturns404ProblemDetailWhenTaskIsMissing() throws Exception {
		given(service.findById(7L)).willThrow(new TaskNotFoundException(7L));

		mockMvc.perform(get("/api/tasks/7"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Not Found"))
				.andExpect(jsonPath("$.detail").value("Task with id 7 not found"))
				.andExpect(jsonPath("$.instance").value("/api/tasks/7"));
	}

	@Test
	void getByIdReturns400WhenIdIsNotANumber() throws Exception {
		mockMvc.perform(get("/api/tasks/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
	}

	@Test
	void createReturns201WithLocationHeader() throws Exception {
		given(service.create(any(CreateTaskRequest.class)))
				.willReturn(new TaskResponse(5L, "Write tests", false, CREATED_AT));

		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "Write tests"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/tasks/5"))
				.andExpect(jsonPath("$.id").value(5))
				.andExpect(jsonPath("$.title").value("Write tests"))
				.andExpect(jsonPath("$.completed").value(false));

		verify(service).create(new CreateTaskRequest("Write tests"));
	}

	@Test
	void createRejectsBlankTitleWithFieldErrors() throws Exception {
		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "   "}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.detail").value("Validation failed"))
				.andExpect(jsonPath("$.errors.title").value("Title must not be blank"));

		verifyNoInteractions(service);
	}

	@Test
	void createRejectsTitleLongerThan255Characters() throws Exception {
		String longTitle = "x".repeat(256);

		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\": \"" + longTitle + "\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.title").value("Title must be at most 255 characters"));
	}

	@Test
	void createRejectsMalformedJson() throws Exception {
		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{not json"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400));

		verifyNoInteractions(service);
	}

	@Test
	void updateReturnsUpdatedTask() throws Exception {
		given(service.update(1L, new UpdateTaskRequest("Done", true)))
				.willReturn(new TaskResponse(1L, "Done", true, CREATED_AT));

		mockMvc.perform(put("/api/tasks/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "Done", "completed": true}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Done"))
				.andExpect(jsonPath("$.completed").value(true));
	}

	@Test
	void updateRequiresCompletedField() throws Exception {
		mockMvc.perform(put("/api/tasks/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "Done"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.completed").value("Completed must be provided"));

		verifyNoInteractions(service);
	}

	@Test
	void deleteReturns204() throws Exception {
		mockMvc.perform(delete("/api/tasks/1"))
				.andExpect(status().isNoContent());

		verify(service).delete(1L);
	}

	@Test
	void deleteReturns404WhenTaskIsMissing() throws Exception {
		willThrow(new TaskNotFoundException(9L)).given(service).delete(9L);

		mockMvc.perform(delete("/api/tasks/9"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Task with id 9 not found"));
	}

	@Test
	void unexpectedErrorsReturn500WithoutLeakingDetails() throws Exception {
		given(service.findAll()).willThrow(new IllegalStateException("database password is hunter2"));

		mockMvc.perform(get("/api/tasks"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
	}

	@Test
	void corsPreflightAllowsConfiguredOrigin() throws Exception {
		mockMvc.perform(options("/api/tasks")
						.header("Origin", "http://localhost:5173")
						.header("Access-Control-Request-Method", "POST"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
	}

	@Test
	void corsPreflightRejectsUnknownOrigin() throws Exception {
		mockMvc.perform(options("/api/tasks")
						.header("Origin", "http://evil.example")
						.header("Access-Control-Request-Method", "POST"))
				.andExpect(status().isForbidden());
	}
}
