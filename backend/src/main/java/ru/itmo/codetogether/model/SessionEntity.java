package ru.itmo.codetogether.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "session")
public class SessionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  private String language;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity owner;

  @Setter
  @Column(nullable = false, unique = true)
  private String link;

  @Setter
  @Column(name = "link_expires_at", nullable = false)
  private Instant linkExpiresAt;

  @Transient private Instant updatedAt = Instant.now();

  @Setter
  @OneToOne(mappedBy = "session", fetch = FetchType.LAZY)
  private DocumentEntity document;

  public void setName(String name) {
    this.name = name;
    this.updatedAt = Instant.now();
  }

  public void setLanguage(String language) {
    this.language = language;
    this.updatedAt = Instant.now();
  }

  public void touch() {
    this.updatedAt = Instant.now();
  }

  @PostLoad
  public void hydrate() {
    if (this.updatedAt == null) {
      this.updatedAt = Instant.now();
    }
  }
}
