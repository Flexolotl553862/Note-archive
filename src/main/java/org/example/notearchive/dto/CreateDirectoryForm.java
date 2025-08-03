package org.example.notearchive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.notearchive.domain.StorageEntry;

@Getter
@Setter
@NoArgsConstructor
public class CreateDirectoryForm {
    @NotBlank
    @Size(min = 1, max = 50)
    private String directoryName;

    @NotNull(message = "Parent folder is absent.")
    private StorageEntry parent;
}
