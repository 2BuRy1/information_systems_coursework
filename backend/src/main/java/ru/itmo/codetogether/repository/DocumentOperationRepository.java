package ru.itmo.codetogether.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.codetogether.model.DocumentOperationEntity;

public interface DocumentOperationRepository extends JpaRepository<DocumentOperationEntity, Long> {
  List<DocumentOperationEntity> findByDocumentIdOrderByVersionAsc(Long documentId);
}
