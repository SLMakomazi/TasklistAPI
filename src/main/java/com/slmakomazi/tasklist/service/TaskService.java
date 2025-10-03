package com.slmakomazi.tasklist.service;

import com.slmakomazi.tasklist.model.Task;
import com.slmakomazi.tasklist.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task create(Task task) {
        log.info("Creating task with description='{}' dueDate='{}'", task.getDescription(), task.getDueDate());
        return repository.save(task);
    }

    public List<Task> list(Boolean completed) {
        if (completed == null) {
            log.debug("Listing all tasks");
            return repository.findAll();
        }
        log.debug("Listing tasks with completed={}", completed);
        return repository.findByCompleted(completed);
    }

    @Transactional
    public Task markCompleted(Long id) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
        if (!task.isCompleted()) {
            task.setCompleted(true);
            log.info("Marked task id={} as completed", id);
        } else {
            log.info("Task id={} already completed", id);
        }
        return task; // JPA dirty checking will persist change
    }
}
