package org.example.notearchive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterForm {
    @NotBlank
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^\\p{Lu}\\p{Ll}+( \\p{Lu}\\p{Ll}+)*$", message = "Must be a name")
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 30)
    @Pattern(regexp = "^\\p{Lu}\\p{Ll}+( \\p{Lu}\\p{Ll}+)*$", message = "Must be a name")
    private String lastName;

    @NotBlank
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank
    @Size(min = 5, max = 30)
    private String login;

    @NotBlank
    @Size(min = 8, max = 30)
    private String password;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
