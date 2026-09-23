package ru.itmo.codetogether.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "document_operation")
public class DocumentOperationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_id", nullable = false)
  private DocumentEntity document;

  @Setter
  @Column(name = "operation_type", nullable = false)
  private String operationType;

  @Setter
  @Column(name = "node_counter", nullable = false)
  private Integer nodeCounter;

  @Setter
  @Column(name = "node_site", nullable = false)
  private Integer nodeSite;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "left_node")
  private DocumentOperationEntity leftNode;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "right_node")
  private DocumentOperationEntity rightNode;

  @Setter private String color;

  @Setter
  @Column(nullable = false)
  private String value;

  @Setter
  @Column(nullable = false)
  private Integer version;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();
}
