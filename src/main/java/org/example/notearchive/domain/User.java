package org.example.notearchive.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "app_user", indexes = @Index(name = "multi-index", columnList = "login, email, name"))
public class User extends AbstractEntity implements UserDetails {
    public enum Role {
        ROLE_ADMIN,
        ROLE_READER,
        ROLE_WRITER
    }

    @Column(unique = true)
    private String login;

    @Column(unique = true)
    private String email;

    private String passwordSha;

    private String name;

    @NotNull
    private Role role;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Note> notes;

    @Override
    public String getPassword() {
        return passwordSha;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }
}
