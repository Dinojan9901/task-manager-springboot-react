package com.dinojan.taskmanager.common;

import com.dinojan.taskmanager.task.TaskNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns exceptions into consistent JSON error bodies using RFC 9457 "Problem Details"
 * ({@code application/problem+json}), e.g.
 * <pre>{"title":"Not Found","status":404,"detail":"Task with id 7 not found","instance":"/api/tasks/7"}</pre>
 * <p>
 * Extending {@link ResponseEntityExceptionHandler} gives Problem Detail responses for all standard
 * Spring MVC errors for free (malformed JSON, wrong HTTP method, type mismatch on {@code /api/tasks/abc}, ...).
 * We add handlers for our own exceptions and enrich validation errors with per-field messages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(TaskNotFoundException.class)
	public ProblemDetail handleTaskNotFound(TaskNotFoundException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	/** Bean Validation failures on {@code @Valid @RequestBody}: add an {@code errors} map of field -> message. */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}
		ProblemDetail problem = ex.getBody();
		problem.setDetail("Validation failed");
		problem.setProperty("errors", errors);
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	/** Last-resort handler: log the real cause, but never leak stack traces or internals to the client. */
	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpected(Exception ex) {
		log.error("Unexpected error", ex);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
	}
}
