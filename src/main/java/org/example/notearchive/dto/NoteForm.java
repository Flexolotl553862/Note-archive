package org.example.notearchive.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class NoteForm {
    @NotBlank
    @Size(min = 2, max = 40)
    private String title;

    @NotNull(message = "must not be empty")
    @Min(1)
    @Max(8)
    private Long startSemester;

    @NotNull(message = "must not be empty")
    @Min(1)
    @Max(8)
    private Long endSemester;

    @NotNull
    private MultipartFile file;
}
