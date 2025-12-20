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
@Table(name = "highlight")
public class HighlightEntity {

    @Setter
    @EmbeddedId
    private HighlightId id;

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
    @Column(name = "start_line", nullable = false)
    private Integer startLine;

    @Setter
    @Column(name = "end_line", nullable = false)
    private Integer endLine;

    @Setter
    @Column(name = "start_col", nullable = false)
    private Integer startCol;

    @Setter
    @Column(name = "end_col", nullable = false)
    private Integer endCol;

    @Setter
    @Column(nullable = false)
    private String color;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
