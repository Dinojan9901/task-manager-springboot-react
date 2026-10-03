package com.dinojan.taskmanager.task;

import com.dinojan.taskmanager.task.dto.CreateTaskRequest;
import com.dinojan.taskmanager.task.dto.TaskResponse;
import com.dinojan.taskmanager.task.dto.UpdateTaskRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Pure unit test: no Spring context, no database. The repository is a Mockito mock,
 * so we only test the service's own logic. Runs in milliseconds.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

	@Mock
	private TaskRepository repository;

	@InjectMocks
	private TaskService service;

	@Test
	void findAllMapsEntitiesToResponsesInRepositoryOrder() {
		Task newer = new Task("Newer");
		Task older = new Task("Older");
		older.setCompleted(true);
		given(repository.findAllByOrderByCreatedAtDesc()).willReturn(List.of(newer, older));

		List<TaskResponse> result = service.findAll();

		assertThat(result).extracting(TaskResponse::title).containsExactly("Newer", "Older");
		assertThat(result).extracting(TaskResponse::completed).containsExactly(false, true);
	}

	@Test
	void findByIdThrowsWhenTaskDoesNotExist() {
		given(repository.findById(42L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById(42L))
				.isInstanceOf(TaskNotFoundException.class)
				.hasMessageContaining("42");
	}

	@Test
	void createTrimsTitleAndSavesIncompleteTask() {
		given(repository.save(any(Task.class))).willAnswer(invocation -> invocation.getArgument(0));

		TaskResponse result = service.create(new CreateTaskRequest("  Write tests  "));

		ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
		verify(repository).save(saved.capture());
		assertThat(saved.getValue().getTitle()).isEqualTo("Write tests");
		assertThat(saved.getValue().isCompleted()).isFalse();
		assertThat(result.title()).isEqualTo("Write tests");
	}

	@Test
	void updateChangesTitleAndCompletedOnExistingTask() {
		Task existing = new Task("Old title");
		given(repository.findById(1L)).willReturn(Optional.of(existing));

		TaskResponse result = service.update(1L, new UpdateTaskRequest(" New title ", true));

		assertThat(existing.getTitle()).isEqualTo("New title");
		assertThat(existing.isCompleted()).isTrue();
		assertThat(result.title()).isEqualTo("New title");
		assertThat(result.completed()).isTrue();
	}

	@Test
	void updateThrowsWhenTaskDoesNotExist() {
		given(repository.findById(99L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> service.update(99L, new UpdateTaskRequest("Title", false)))
				.isInstanceOf(TaskNotFoundException.class);
		verify(repository, never()).save(any());
	}

	@Test
	void deleteRemovesExistingTask() {
		given(repository.existsById(1L)).willReturn(true);

		service.delete(1L);

		verify(repository).deleteById(1L);
	}

	@Test
	void deleteThrowsAndDeletesNothingWhenTaskDoesNotExist() {
		given(repository.existsById(99L)).willReturn(false);

		assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(TaskNotFoundException.class);
		verify(repository, never()).deleteById(any());
	}
}
