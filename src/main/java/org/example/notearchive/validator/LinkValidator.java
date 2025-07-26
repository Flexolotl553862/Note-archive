package org.example.notearchive.validator;

import org.example.notearchive.dto.LinkForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.DateTimeException;
import java.util.Date;

@Component
public class LinkValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return LinkForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) {
            return;
        }
        LinkForm linkForm = (LinkForm) target;
        try {
            if (linkForm.getDate().before(new Date())) {
                errors.rejectValue("expiryDate", "link.valid.date", "You cannot select a past time");
            }
        } catch (DateTimeException ignored) {
            errors.rejectValue("expiryDate", "link.valid.date", "Invalid time or date");
        }
    }
}
