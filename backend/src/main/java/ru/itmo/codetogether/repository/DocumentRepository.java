package ru.itmo.codetogether.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.codetogether.model.DocumentEntity;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    Optional<DocumentEntity> findBySession_Id(Long sessionId);
}
