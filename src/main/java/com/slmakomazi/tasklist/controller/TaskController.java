package com.slmakomazi.tasklist.controller;

import com.slmakomazi.tasklist.model.Task;
import com.slmakomazi.tasklist.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management API")
public class TaskController {
    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @Operation(summary = "Create a new task")
    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody Task task) {
        Task created = service.create(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "List tasks, optionally filter by completed status")
    @GetMapping
    public ResponseEntity<List<Task>> list(@RequestParam(value = "completed", required = false) Boolean completed) {
        return ResponseEntity.ok(service.list(completed));
    }

    @Operation(summary = "Mark a task as completed")
    @PutMapping("/{id}/complete")
    public ResponseEntity<Task> complete(@PathVariable Long id) {
        try {
            Task updated = service.markCompleted(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            log.warn("{}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
