package ru.itmo.codetogether.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.codetogether.model.TaskEntity;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
  List<TaskEntity> findBySession_Id(Long sessionId);
}
