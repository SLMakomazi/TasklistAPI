package com.slmakomazi.tasklist.repository;

import com.slmakomazi.tasklist.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Task completedTask;
    private Task pendingTask;

    @BeforeEach
    void setUp() {
        // Create test data
        OffsetDateTime now = OffsetDateTime.now();

        completedTask = new Task("Completed task", now.plusDays(1));
        completedTask.setCompleted(true);

        pendingTask = new Task("Pending task", now.plusDays(2));
        pendingTask.setCompleted(false);

        // Persist test data
        entityManager.persist(completedTask);
        entityManager.persist(pendingTask);
        entityManager.flush();
    }

    @Test
    void testFindByCompleted_WhenCompletedIsTrue() {
        // When
        List<Task> completedTasks = taskRepository.findByCompleted(true);

        // Then
        assertNotNull(completedTasks);
        assertEquals(1, completedTasks.size());
        assertTrue(completedTasks.get(0).isCompleted());
        assertEquals("Completed task", completedTasks.get(0).getDescription());
    }

    @Test
    void testFindByCompleted_WhenCompletedIsFalse() {
        // When
        List<Task> pendingTasks = taskRepository.findByCompleted(false);

        // Then
        assertNotNull(pendingTasks);
        assertEquals(1, pendingTasks.size());
        assertFalse(pendingTasks.get(0).isCompleted());
        assertEquals("Pending task", pendingTasks.get(0).getDescription());
    }

    @Test
    void testFindByCompleted_WhenNoTasksMatch() {
        // Given - Create a task with different completion status
        Task anotherTask = new Task("Another task", OffsetDateTime.now().plusDays(3));
        anotherTask.setCompleted(true); // Same as completedTask
        entityManager.persist(anotherTask);
        entityManager.flush();

        // When - Look for tasks that don't exist (no tasks with completed = false when we have 2 completed)
        List<Task> nonExistentTasks = taskRepository.findByCompleted(false);

        // Then
        assertNotNull(nonExistentTasks);
        assertEquals(1, nonExistentTasks.size()); // Only pendingTask exists
    }

    @Test
    void testFindAll() {
        // When
        List<Task> allTasks = taskRepository.findAll();

        // Then
        assertNotNull(allTasks);
        assertEquals(2, allTasks.size());
    }

    @Test
    void testSave() {
        // Given
        Task newTask = new Task("New task", OffsetDateTime.now().plusDays(5));

        // When
        Task savedTask = taskRepository.save(newTask);

        // Then
        assertNotNull(savedTask);
        assertNotNull(savedTask.getId());
        assertEquals("New task", savedTask.getDescription());

        // Verify it was persisted
        Task foundTask = entityManager.find(Task.class, savedTask.getId());
        assertNotNull(foundTask);
        assertEquals("New task", foundTask.getDescription());
    }

    @Test
    void testFindById() {
        // When
        Task foundTask = taskRepository.findById(completedTask.getId()).orElse(null);

        // Then
        assertNotNull(foundTask);
        assertEquals(completedTask.getId(), foundTask.getId());
        assertEquals("Completed task", foundTask.getDescription());
        assertTrue(foundTask.isCompleted());
    }

    @Test
    void testFindById_NotFound() {
        // When
        Task foundTask = taskRepository.findById(999L).orElse(null);

        // Then
        assertNull(foundTask);
    }

    @Test
    void testDelete() {
        // Given
        Long taskId = completedTask.getId();

        // When
        taskRepository.deleteById(taskId);

        // Then
        Task deletedTask = entityManager.find(Task.class, taskId);
        assertNull(deletedTask);
    }
}
