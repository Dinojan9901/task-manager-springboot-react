package com.dinojan.taskmanager.task;

/** Thrown by the service layer; translated to a 404 by the global exception handler. */
public class TaskNotFoundException extends RuntimeException {

	public TaskNotFoundException(Long id) {
		super("Task with id " + id + " not found");
	}
}
