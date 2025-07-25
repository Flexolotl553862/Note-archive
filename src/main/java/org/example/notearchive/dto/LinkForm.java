package org.example.notearchive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LinkForm {
    private String expiryTime;

    private String expiryDate;

    @NotBlank
    @Size(min = 2, max = 100)
    private String description;
}
