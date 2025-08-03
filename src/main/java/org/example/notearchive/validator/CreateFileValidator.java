package org.example.notearchive.validator;

import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.dto.FileForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CreateFileValidator implements Validator {

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
        StorageEntry parent = fileForm.getParent();
        String fileName = fileForm.getFileCreate().getOriginalFilename();
        if (parent.getChildren().stream().anyMatch(entry -> entry.getName().equals(fileName))) {
            errors.rejectValue("fileCreate", "fileCreate.exists", "File already exists.");
        }
    }
}
