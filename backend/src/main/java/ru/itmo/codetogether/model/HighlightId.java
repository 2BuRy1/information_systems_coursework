package ru.itmo.codetogether.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class HighlightId implements Serializable {

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "user_id")
    private Long userId;
}
