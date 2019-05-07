package io.namjune.basicrestapi.events;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class EventValidator {

    public void validate(EventRequestDto eventRequestDto, Errors errors) {
        if (eventRequestDto.getBasePrice() > eventRequestDto.getMaxPrice()
            && eventRequestDto.getMaxPrice() != 0) {
            errors.rejectValue("basePrice", "wrongValue", "BasePrice is wrong");
            errors.rejectValue("maxPrice", "wrongValue", "maxPrice is wrong");
        }

        LocalDateTime endEventTime = eventRequestDto.getEndEventDateTime();
        if (endEventTime.isBefore(eventRequestDto.getBeginEventDateTime())
            || endEventTime.isBefore(eventRequestDto.getCloseEnrollmentDateTime())
            || endEventTime.isBefore(eventRequestDto.getBeginEnrollmentDateTime())) {
            errors.rejectValue("endEventTime", "wrongValue", "endEventTime is wrong");
        }

        // TODO beginEventDateTime
        // TODO closeEnrollmentDateTime
    }
}
