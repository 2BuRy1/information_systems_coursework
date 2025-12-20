package ru.itmo.codetogether.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "cursor_state")
public class CursorStateEntity {

    @Setter
    @EmbeddedId
    private CursorStateId id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sessionId")
    @JoinColumn(name = "session_id")
    private SessionEntity session;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Setter
    @Column(name = "line", nullable = false)
    private Integer line;

    @Setter
    @Column(name = "col", nullable = false)
    private Integer col;

    @Setter
    @Column(name = "color", nullable = false)
    private String color;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
