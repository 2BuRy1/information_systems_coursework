package ru.itmo.codetogether.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(nullable = false)
  private String name;

  @Setter
  @Column(nullable = false, unique = true)
  private String email;

  @Setter
  @Column(name = "avatar_url")
  private String avatarUrl;

  @Setter
  @Column(nullable = false)
  private String role = "member";
}
