package ru.itmo.codetogether.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.codetogether.dto.task.Task;
import ru.itmo.codetogether.dto.task.TaskRequest;
import ru.itmo.codetogether.dto.task.TaskUpdateRequest;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.service.SessionService;
import ru.itmo.codetogether.service.TaskService;

@RestController
@RequestMapping("/sessions/{sessionId}/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;
  private final SessionService sessionService;

  @GetMapping
  public List<Task> list(@AuthenticationPrincipal UserEntity user, @PathVariable Long sessionId) {
    sessionService.ensureMember(sessionId, user.getId());
    return taskService.listTasks(sessionId);
  }

  @PostMapping
  public ResponseEntity<Task> create(
      @AuthenticationPrincipal UserEntity user,
      @PathVariable Long sessionId,
      @Valid @RequestBody TaskRequest request) {
    sessionService.ensureEditor(sessionId, user.getId());
    Task task = taskService.createTask(sessionId, user.getId(), request);
    return ResponseEntity.status(201).body(task);
  }

  @PatchMapping("/{taskId}")
  public Task update(
      @AuthenticationPrincipal UserEntity user,
      @PathVariable Long sessionId,
      @PathVariable Long taskId,
      @Valid @RequestBody TaskUpdateRequest request) {
    sessionService.ensureEditor(sessionId, user.getId());
    return taskService.updateTask(sessionId, taskId, request);
  }

  @DeleteMapping("/{taskId}")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal UserEntity user,
      @PathVariable Long sessionId,
      @PathVariable Long taskId) {
    sessionService.ensureEditor(sessionId, user.getId());
    taskService.deleteTask(sessionId, taskId);
    return ResponseEntity.noContent().build();
  }
}
