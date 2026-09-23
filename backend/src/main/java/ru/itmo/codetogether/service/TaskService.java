package ru.itmo.codetogether.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.codetogether.dto.task.Task;
import ru.itmo.codetogether.dto.task.TaskRequest;
import ru.itmo.codetogether.dto.task.TaskUpdateRequest;
import ru.itmo.codetogether.exception.CodeTogetherException;
import ru.itmo.codetogether.model.SessionEntity;
import ru.itmo.codetogether.model.TaskDataEntity;
import ru.itmo.codetogether.model.TaskEntity;
import ru.itmo.codetogether.model.TaskStatus;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.repository.SessionRepository;
import ru.itmo.codetogether.repository.TaskDataRepository;
import ru.itmo.codetogether.repository.TaskRepository;
import ru.itmo.codetogether.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class TaskService {

  private final TaskRepository taskRepository;
  private final TaskDataRepository taskDataRepository;
  private final SessionRepository sessionRepository;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  public List<Task> listTasks(Long sessionId) {
    return taskRepository.findBySession_Id(sessionId).stream().map(this::toDto).toList();
  }

  @Transactional
  public Task createTask(Long sessionId, Long userId, TaskRequest request) {
    SessionEntity session =
        sessionRepository
            .findById(sessionId)
            .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Доска не найдена"));
    UserEntity author =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    TaskEntity task = new TaskEntity();
    task.setSession(session);
    task.setAuthor(author);
    task.setText(request.text());
    task.setStatus(TaskStatus.OPEN);
    TaskEntity saved = taskRepository.save(task);
    if (request.metadata() != null && !request.metadata().isEmpty()) {
      TaskDataEntity data = new TaskDataEntity();
      data.setTask(saved);
      data.setPayloadJson(serialize(request.metadata()));
      taskDataRepository.save(data);
      saved.setData(data);
    }
    return toDto(saved);
  }

  @Transactional
  public Task updateTask(Long sessionId, Long taskId, TaskUpdateRequest request) {
    TaskEntity task =
        taskRepository
            .findById(taskId)
            .filter(entity -> entity.getSession().getId().equals(sessionId))
            .orElseThrow(
                () -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Задача не найдена"));
    if (request.text() != null && !request.text().isBlank()) {
      task.setText(request.text());
    }
    if (request.status() != null) {
      task.setStatus(TaskStatus.fromString(request.status()));
    }
    if (request.metadata() != null) {
      TaskDataEntity data =
          taskDataRepository
              .findByTask(task)
              .orElseGet(
                  () -> {
                    TaskDataEntity entity = new TaskDataEntity();
                    entity.setTask(task);
                    return entity;
                  });
      data.setPayloadJson(serialize(request.metadata()));
      taskDataRepository.save(data);
      task.setData(data);
    }
    TaskEntity saved = taskRepository.save(task);
    return toDto(saved);
  }

  @Transactional
  public void deleteTask(Long sessionId, Long taskId) {
    TaskEntity task =
        taskRepository
            .findById(taskId)
            .filter(entity -> entity.getSession().getId().equals(sessionId))
            .orElseThrow(
                () -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Задача не найдена"));
    taskRepository.delete(task);
  }

  private Task toDto(TaskEntity entity) {
    Map<String, String> metadata =
        taskDataRepository
            .findByTask(entity)
            .map(data -> deserialize(data.getPayloadJson()))
            .orElse(Map.of());
    return new Task(
        entity.getId(),
        entity.getSession().getId(),
        entity.getText(),
        entity.getStatus().getValue(),
        entity.getAuthor().getId(),
        metadata,
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  private String serialize(Map<String, String> metadata) {
    try {
      return objectMapper.writeValueAsString(metadata);
    } catch (JsonProcessingException ex) {
      throw new CodeTogetherException(
          HttpStatus.BAD_REQUEST, "Некорректные метаданные", ex.getMessage());
    }
  }

  private Map<String, String> deserialize(String json) {
    if (json == null || json.isBlank()) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(
          json,
          objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
    } catch (JsonProcessingException ex) {
      throw new CodeTogetherException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка чтения метаданных", ex.getMessage());
    }
  }
}
