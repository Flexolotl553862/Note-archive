package org.example.notearchive.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class FileForm {
    @NotNull(message = "Must not be empty.")
    private MultipartFile fileCreate;

    @NotNull(message = "Parent folder is absent.")
    private Long fileParentId;
}
