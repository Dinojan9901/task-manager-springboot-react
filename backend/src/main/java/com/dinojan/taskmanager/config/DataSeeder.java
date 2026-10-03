package com.dinojan.taskmanager.config;

import com.dinojan.taskmanager.task.Task;
import com.dinojan.taskmanager.task.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Inserts a few demo tasks into an empty database. Disable with {@code app.seed-data=false}. */
@Configuration
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = true)
public class DataSeeder {

	@Bean
	CommandLineRunner seedTasks(TaskRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				Task learn = new Task("Learn Spring Boot basics");
				learn.setCompleted(true);
				repository.save(learn);
				repository.save(new Task("Build a REST API"));
				repository.save(new Task("Connect a React frontend"));
			}
		};
	}
}
