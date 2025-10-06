package com.slmakomazi.tasklist.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slmakomazi.tasklist.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Task testTask;

    @BeforeEach
    void setUp() {
        testTask = new Task("Integration test task", OffsetDateTime.now().plusDays(1));
    }

    @Test
    void testFullCreateAndRetrieveFlow() throws Exception {
        // Create a task
        String response = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Integration test task"))
                .andExpect(jsonPath("$.completed").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the created task ID
        Task createdTask = objectMapper.readValue(response, Task.class);
        Long taskId = createdTask.getId();

        // Retrieve all tasks
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(taskId))
                .andExpect(jsonPath("$[0].description").value("Integration test task"));

        // Mark as completed
        mockMvc.perform(put("/api/tasks/" + taskId + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.completed").value(true));

        // Verify it's now in completed list
        mockMvc.perform(get("/api/tasks")
                .param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(taskId))
                .andExpect(jsonPath("$[0].completed").value(true));

        // Verify it's not in pending list
        mockMvc.perform(get("/api/tasks")
                .param("completed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCreateTaskWithInvalidData() throws Exception {
        // Test with blank description
        Task invalidTask = new Task("", OffsetDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMarkNonExistentTaskAsCompleted() throws Exception {
        mockMvc.perform(put("/api/tasks/999/complete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testMultipleTasksOperations() throws Exception {
        // Create first task
        Task task1 = new Task("First task", OffsetDateTime.now().plusDays(1));
        String task1Response = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Task createdTask1 = objectMapper.readValue(task1Response, Task.class);

        // Create second task
        Task task2 = new Task("Second task", OffsetDateTime.now().plusDays(2));
        String task2Response = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Task createdTask2 = objectMapper.readValue(task2Response, Task.class);

        // Verify both tasks exist
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Mark first task as completed
        mockMvc.perform(put("/api/tasks/" + createdTask1.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        // Check completed tasks
        mockMvc.perform(get("/api/tasks")
                .param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(createdTask1.getId()));

        // Check pending tasks
        mockMvc.perform(get("/api/tasks")
                .param("completed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(createdTask2.getId()));
    }
}
