package ru.itmo.codetogether.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.codetogether.model.OAuthCredentialsEntity;

public interface OAuthCredentialsRepository extends JpaRepository<OAuthCredentialsEntity, Long> {
  List<OAuthCredentialsEntity> findByUser_Id(Long userId);
}
