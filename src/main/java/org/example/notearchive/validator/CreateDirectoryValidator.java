package org.example.notearchive.validator;

import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.dto.CreateDirectoryForm;
import org.example.notearchive.repository.StorageEntryRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
public class CreateDirectoryValidator implements Validator {
    private final StorageEntryRepository storageEntryRepository;

    public CreateDirectoryValidator(StorageEntryRepository storageEntryRepository) {
        this.storageEntryRepository = storageEntryRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return CreateDirectoryForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) {
            return;
        }

        CreateDirectoryForm form = (CreateDirectoryForm) target;
        if (form.getDirectoryName().matches(".*[\\\\/:*?\"<>|].*")) {
            errors.rejectValue(
                    "directoryName",
                    "contains.file.separator",
                    "Name contains illegal characters."
            );
        }
        Optional<StorageEntry> parent = storageEntryRepository.findById(form.getParentId());
        if (parent.isEmpty()) {
            errors.rejectValue(
                    "parentId",
                    "wrong.id",
                    "Parent id contains a file separator."
            );
        }
        if (parent.isPresent() &&
                parent.get().getChildren().stream().anyMatch(entry -> form.getDirectoryName().equals(entry.getName()))) {
            errors.rejectValue(
                    "directoryName",
                    "directory.already_exists",
                    form.getDirectoryName() + " already exists."
            );
        }
    }
}
