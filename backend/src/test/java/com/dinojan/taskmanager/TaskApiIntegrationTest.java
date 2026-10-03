package com.dinojan.taskmanager;

import com.dinojan.taskmanager.task.TaskRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack test: the whole application context starts (controller -> service -> JPA -> database),
 * using the "test" profile's in-memory H2 database. Verifies all layers work together.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TaskRepository repository;

	@BeforeEach
	void cleanDatabase() {
		repository.deleteAll();
	}

	@Test
	void taskLifecycle() throws Exception {
		// Create
		String body = mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "  Integration test  "}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Integration test"))
				.andExpect(jsonPath("$.completed").value(false))
				.andExpect(jsonPath("$.createdAt").isNotEmpty())
				.andReturn().getResponse().getContentAsString();
		long id = ((Number) JsonPath.read(body, "$.id")).longValue();

		// It was really written to the database
		assertThat(repository.findById(id)).hasValueSatisfying(task ->
				assertThat(task.getTitle()).isEqualTo("Integration test"));

		// List
		mockMvc.perform(get("/api/tasks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(id));

		// Update
		mockMvc.perform(put("/api/tasks/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "Integration test", "completed": true}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.completed").value(true));
		assertThat(repository.findById(id)).hasValueSatisfying(task ->
				assertThat(task.isCompleted()).isTrue());

		// Delete, then it is gone
		mockMvc.perform(delete("/api/tasks/{id}", id))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/tasks/{id}", id))
				.andExpect(status().isNotFound());
		assertThat(repository.count()).isZero();
	}

	@Test
	void listIsOrderedNewestFirst() throws Exception {
		for (String title : new String[] {"first", "second", "third"}) {
			mockMvc.perform(post("/api/tasks")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\": \"" + title + "\"}"))
					.andExpect(status().isCreated());
			Thread.sleep(5); // make sure createdAt values differ
		}

		mockMvc.perform(get("/api/tasks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("third"))
				.andExpect(jsonPath("$[1].title").value("second"))
				.andExpect(jsonPath("$[2].title").value("first"));
	}
}
