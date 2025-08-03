package org.example.notearchive.validator;

import org.example.notearchive.dto.CreateDirectoryForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CreateDirectoryValidator implements Validator {

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
        if (form.getParent().getChildren().stream().anyMatch(entry -> form.getDirectoryName().equals(entry.getName()))) {
            errors.rejectValue(
                    "directoryName",
                    "directory.already_exists",
                    form.getDirectoryName() + " already exists."
            );
        }
    }
}
