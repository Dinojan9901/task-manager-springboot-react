package com.dinojan.taskmanager.task;

import com.dinojan.taskmanager.task.dto.CreateTaskRequest;
import com.dinojan.taskmanager.task.dto.TaskResponse;
import com.dinojan.taskmanager.task.dto.UpdateTaskRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for tasks. The controller only deals with HTTP; this class owns the rules
 * (trimming, not-found handling) and the transaction boundaries.
 */
@Service
@Transactional(readOnly = true)
public class TaskService {

	private final TaskRepository repository;

	public TaskService(TaskRepository repository) {
		this.repository = repository;
	}

	public List<TaskResponse> findAll() {
		return repository.findAllByOrderByCreatedAtDesc().stream()
				.map(TaskResponse::from)
				.toList();
	}

	public TaskResponse findById(Long id) {
		return TaskResponse.from(getTask(id));
	}

	@Transactional
	public TaskResponse create(CreateTaskRequest request) {
		Task task = new Task(request.title().trim());
		return TaskResponse.from(repository.save(task));
	}

	@Transactional
	public TaskResponse update(Long id, UpdateTaskRequest request) {
		Task task = getTask(id);
		task.setTitle(request.title().trim());
		task.setCompleted(request.completed());
		// No save() needed: the entity is managed, so JPA flushes the changes on commit.
		return TaskResponse.from(task);
	}

	@Transactional
	public void delete(Long id) {
		if (!repository.existsById(id)) {
			throw new TaskNotFoundException(id);
		}
		repository.deleteById(id);
	}

	private Task getTask(Long id) {
		return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
	}
}
