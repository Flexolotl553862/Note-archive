package org.example.notearchive.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> editors;

    @OneToOne(orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "content_id")
    private StorageEntry content;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Link> links;

    public Note(String title, Long startSemester, Long endSemester, User author) {
        this.title = title;
        this.startSemester = startSemester;
        this.endSemester = endSemester;
        this.author = author;
        this.editors = new HashSet<>();
        this.editors.add(author);
    }

    public boolean canEdit(Authentication authentication) {
        if (authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        User user = (User) authentication.getPrincipal();
        return editors.stream().anyMatch(u -> Objects.equals(u.getId(), user.getId())
                || user.getRole().equals(User.Role.ROLE_ADMIN));
    }
}
