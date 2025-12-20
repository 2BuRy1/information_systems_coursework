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
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "task")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionEntity session;

    @Column(nullable = false)
    private String text;

    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.OPEN;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    @OneToOne(mappedBy = "task", fetch = FetchType.LAZY)
    private TaskDataEntity data;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public void setText(String text) {
        this.text = text;
        touch();
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        touch();
    }

    public void setData(TaskDataEntity data) {
        this.data = data;
        touch();
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
