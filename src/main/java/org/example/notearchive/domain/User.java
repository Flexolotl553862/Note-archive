package org.example.notearchive.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "app_user")
public class User extends AbstractEntity implements UserDetails {
    public enum Role {
        ROLE_ADMIN,
        ROLE_READER,
        ROLE_WRITER
    }

    @NotBlank
    @Column(unique = true)
    private String login;

    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(min = 5)
    private String passwordSha;

    @NotBlank
    @Size(min = 5, max = 20)
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

    public Long getId() {
        return super.getId();
    }
}
