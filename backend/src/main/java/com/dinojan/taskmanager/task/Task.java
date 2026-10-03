package com.dinojan.taskmanager.task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * JPA entity mapped to the {@code tasks} table.
 * <p>
 * This class is purely a persistence model: it is never serialized to or from JSON.
 * The API contract lives in the {@code dto} package, so the table can evolve without
 * breaking clients (and clients can't set fields like {@code id} or {@code createdAt}).
 */
@Entity
@Table(name = "tasks")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private boolean completed;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected Task() {
		// required by JPA
	}

	public Task(String title) {
		this.title = title;
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
