package com.forgeops.incident;

import com.forgeops.user.UserEntity;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incident_notes")
@EntityListeners(AuditingEntityListener.class)
public class IncidentNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private IncidentEntity incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    @Column(name = "note_body", nullable = false, columnDefinition = "TEXT")
    private String noteBody;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public IncidentNoteEntity() {}

    public IncidentNoteEntity(IncidentEntity incident, UserEntity author, String noteBody) {
        this.incident = incident;
        this.author = author;
        this.noteBody = noteBody;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public IncidentEntity getIncident() { return incident; }
    public void setIncident(IncidentEntity incident) { this.incident = incident; }

    public UserEntity getAuthor() { return author; }
    public void setAuthor(UserEntity author) { this.author = author; }

    public String getNoteBody() { return noteBody; }
    public void setNoteBody(String noteBody) { this.noteBody = noteBody; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
