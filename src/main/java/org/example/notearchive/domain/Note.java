package org.example.notearchive.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Note extends AbstractEntity {
    @Column(unique = true)
    private String title;

    private Long startSemester;

    private Long endSemester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @OneToOne(orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private StorageEntry content;

    public Note(String title, Long startSemester, Long endSemester, User author) {
        this.title = title;
        this.startSemester = startSemester;
        this.endSemester = endSemester;
        this.author = author;
    }
}
