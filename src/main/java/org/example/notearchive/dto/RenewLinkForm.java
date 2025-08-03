package org.example.notearchive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.notearchive.domain.Link;

import java.time.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class RenewLinkForm {
    @NotBlank
    private String expiryTime;

    @NotBlank
    private String expiryDate;

    @NotNull
    private Link link;

    public Date getDate() throws DateTimeException {
        LocalDate date = LocalDate.parse(expiryDate);
        LocalDateTime time = date.atTime(LocalTime.parse(expiryTime));
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }
}

