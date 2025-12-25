package ru.itmo.codetogether.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.codetogether.model.TaskDataEntity;
import ru.itmo.codetogether.model.TaskEntity;

public interface TaskDataRepository extends JpaRepository<TaskDataEntity, Long> {
  Optional<TaskDataEntity> findByTask(TaskEntity task);
}
