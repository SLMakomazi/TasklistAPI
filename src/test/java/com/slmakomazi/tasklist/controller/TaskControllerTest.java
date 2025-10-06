package com.slmakomazi.tasklist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slmakomazi.tasklist.model.Task;
import com.slmakomazi.tasklist.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private Task testTask;
    private Task completedTask;

    @BeforeEach
    void setUp() {
        OffsetDateTime now = OffsetDateTime.now();
        testTask = new Task("Test task", now.plusDays(1));
        testTask.setId(1L);

        completedTask = new Task("Completed task", now.plusDays(2));
        completedTask.setId(2L);
        completedTask.setCompleted(true);
    }

    @Test
    void testCreate_ValidTask() throws Exception {
        // Given
        when(taskService.create(any(Task.class))).thenReturn(testTask);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test task"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(taskService, times(1)).create(any(Task.class));
    }

    @Test
    void testCreate_InvalidTask() throws Exception {
        // Given
        Task invalidTask = new Task("", null); // Invalid: blank description and null due date

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).create(any(Task.class));
    }

    @Test
    void testList_AllTasks() throws Exception {
        // Given
        List<Task> allTasks = Arrays.asList(testTask, completedTask);
        when(taskService.list(null)).thenReturn(allTasks);

        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Test task"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value("Completed task"));

        verify(taskService, times(1)).list(null);
    }

    @Test
    void testList_CompletedTasksOnly() throws Exception {
        // Given
        when(taskService.list(true)).thenReturn(Arrays.asList(completedTask));

        // When & Then
        mockMvc.perform(get("/api/tasks")
                .param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].completed").value(true));

        verify(taskService, times(1)).list(true);
    }

    @Test
    void testList_PendingTasksOnly() throws Exception {
        // Given
        when(taskService.list(false)).thenReturn(Arrays.asList(testTask));

        // When & Then
        mockMvc.perform(get("/api/tasks")
                .param("completed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].completed").value(false));

        verify(taskService, times(1)).list(false);
    }

    @Test
    void testComplete_ValidId() throws Exception {
        // Given
        Task completedTask = new Task("Test task", OffsetDateTime.now().plusDays(1));
        completedTask.setId(1L);
        completedTask.setCompleted(true); // This is the key fix - task should be completed after marking

        when(taskService.markCompleted(1L)).thenReturn(completedTask);

        // When & Then
        mockMvc.perform(put("/api/tasks/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test task"))
                .andExpect(jsonPath("$.completed").value(true));

        verify(taskService, times(1)).markCompleted(1L);
    }

    @Test
    void testComplete_InvalidId() throws Exception {
        // Given
        when(taskService.markCompleted(999L)).thenThrow(new IllegalArgumentException("Task not found: 999"));

        // When & Then
        mockMvc.perform(put("/api/tasks/999/complete"))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).markCompleted(999L);
    }

    @Test
    void testComplete_TaskAlreadyCompleted() throws Exception {
        // Given - Task is already completed but service still returns it
        when(taskService.markCompleted(2L)).thenReturn(completedTask);

        // When & Then
        mockMvc.perform(put("/api/tasks/2/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.completed").value(true));

        verify(taskService, times(1)).markCompleted(2L);
    }
}
