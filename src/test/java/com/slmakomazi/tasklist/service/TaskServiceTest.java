package com.slmakomazi.tasklist.service;

import com.slmakomazi.tasklist.model.Task;
import com.slmakomazi.tasklist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

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
    void testCreate() {
        // Given
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.create(testTask);

        // Then
        assertNotNull(result);
        assertEquals(testTask.getId(), result.getId());
        assertEquals(testTask.getDescription(), result.getDescription());
        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    void testList_AllTasks() {
        // Given
        List<Task> allTasks = Arrays.asList(testTask, completedTask);
        when(taskRepository.findAll()).thenReturn(allTasks);

        // When
        List<Task> result = taskService.list(null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findAll();
        verify(taskRepository, never()).findByCompleted(anyBoolean());
    }

    @Test
    void testList_CompletedTasksOnly() {
        // Given
        when(taskRepository.findByCompleted(true)).thenReturn(Arrays.asList(completedTask));

        // When
        List<Task> result = taskService.list(true);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isCompleted());
        verify(taskRepository, times(1)).findByCompleted(true);
        verify(taskRepository, never()).findAll();
    }

    @Test
    void testList_PendingTasksOnly() {
        // Given
        when(taskRepository.findByCompleted(false)).thenReturn(Arrays.asList(testTask));

        // When
        List<Task> result = taskService.list(false);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isCompleted());
        verify(taskRepository, times(1)).findByCompleted(false);
        verify(taskRepository, never()).findAll();
    }

    @Test
    void testMarkCompleted_TaskExistsAndNotCompleted() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.markCompleted(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isCompleted());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testMarkCompleted_TaskExistsAndAlreadyCompleted() {
        // Given
        when(taskRepository.findById(2L)).thenReturn(Optional.of(completedTask));

        // When
        Task result = taskService.markCompleted(2L);

        // Then
        assertNotNull(result);
        assertTrue(result.isCompleted());
        verify(taskRepository, times(1)).findById(2L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testMarkCompleted_TaskNotFound() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.markCompleted(999L)
        );

        assertEquals("Task not found: 999", exception.getMessage());
        verify(taskRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testMarkCompleted_TaskBecomesCompleted() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When
        Task result = taskService.markCompleted(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isCompleted());
        assertEquals("Test task", result.getDescription());
    }
}
