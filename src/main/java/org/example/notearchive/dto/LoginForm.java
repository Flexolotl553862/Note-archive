package org.example.notearchive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginForm {
    @NotBlank
    private String loginOrEmail;

    @NotBlank
    private String password;
}
