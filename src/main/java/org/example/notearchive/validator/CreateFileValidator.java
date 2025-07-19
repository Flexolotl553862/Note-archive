package org.example.notearchive.validator;

import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.dto.FileForm;
import org.example.notearchive.repository.StorageEntryRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
public class CreateFileValidator implements Validator {
    private final StorageEntryRepository storageEntryRepository;

    public CreateFileValidator(StorageEntryRepository storageEntryRepository) {
        this.storageEntryRepository = storageEntryRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FileForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        FileForm fileForm = (FileForm) target;
        if (errors.hasErrors()) {
            return;
        }
        if (fileForm.getFileCreate().isEmpty()) {
            errors.rejectValue("fileCreate", "fileCreate.empty", "File is empty.");
        }
        Optional<StorageEntry> parent = storageEntryRepository.findById(fileForm.getFileParentId());
        String fileName = fileForm.getFileCreate().getOriginalFilename();
        if (parent.isEmpty()) {
            errors.rejectValue("parentId", "parentFolder.not.found", "Could not add file.");
        } else if (parent.get().getChildren().stream().anyMatch(entry -> entry.getName().equals(fileName))) {
            errors.rejectValue("fileCreate", "fileCreate.exists", "File already exists.");
        }
    }
}
