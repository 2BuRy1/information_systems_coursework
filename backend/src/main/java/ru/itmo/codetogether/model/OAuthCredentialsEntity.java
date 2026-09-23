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
@Table(name = "oauth_credentials")
public class OAuthCredentialsEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Setter
  @Column(nullable = false)
  private String provider;

  @Setter
  @Column(name = "access_token", length = 2048)
  private String accessToken;

  @Setter
  @Column(name = "refresh_token", length = 2048)
  private String refreshToken;

  @Setter
  @Column(length = 1024)
  private String scopes;

  @Setter
  @Column(name = "token_expires_at")
  private Instant tokenExpiresAt;
}
