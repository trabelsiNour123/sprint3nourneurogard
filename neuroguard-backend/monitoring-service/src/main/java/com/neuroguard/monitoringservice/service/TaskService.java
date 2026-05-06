package com.neuroguard.monitoringservice.service;

import com.neuroguard.monitoringservice.entity.TaskEntity;
import com.neuroguard.monitoringservice.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskEntity> getTasks(String assignedUserId, String type) {
        return taskRepository.findByAssignedUserIdAndTypeOrderByCreatedAtDesc(assignedUserId, type);
    }

    public TaskEntity createTask(String assignedUserId, String type, TaskEntity partialTask) {
        partialTask.setAssignedUserId(assignedUserId);
        partialTask.setType(type);
        return taskRepository.save(partialTask);
    }

    public TaskEntity toggleTaskDone(Long taskId) {
        Optional<TaskEntity> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent()) {
            TaskEntity task = optionalTask.get();
            task.setDone(!task.isDone());
            return taskRepository.save(task);
        }
        throw new RuntimeException("Task not found with id: " + taskId);
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }
}
