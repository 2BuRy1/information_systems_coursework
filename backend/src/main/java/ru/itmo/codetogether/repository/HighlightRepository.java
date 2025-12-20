package ru.itmo.codetogether.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.codetogether.model.HighlightEntity;
import ru.itmo.codetogether.model.HighlightId;

public interface HighlightRepository extends JpaRepository<HighlightEntity, HighlightId> {
    List<HighlightEntity> findBySession_Id(Long sessionId);
}
