package ru.itmo.codetogether.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.codetogether.model.DocumentSnapshotEntity;

public interface DocumentSnapshotRepository extends JpaRepository<DocumentSnapshotEntity, Long> {
  List<DocumentSnapshotEntity> findByDocumentIdOrderByVersionDesc(Long documentId);
}
