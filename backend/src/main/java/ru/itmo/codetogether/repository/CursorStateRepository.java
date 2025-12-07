package ru.itmo.codetogether.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.codetogether.model.CursorStateEntity;
import ru.itmo.codetogether.model.CursorStateId;

public interface CursorStateRepository extends JpaRepository<CursorStateEntity, CursorStateId> {
    List<CursorStateEntity> findBySession_Id(Long sessionId);
}
