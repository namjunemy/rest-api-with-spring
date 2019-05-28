package io.namjune.basicrestapi.events;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class EventValidator {

    public boolean validate(EventRequestDto eventRequestDto, Errors errors) {
        if (eventRequestDto.getBasePrice() > eventRequestDto.getMaxPrice()
            && eventRequestDto.getMaxPrice() != 0) {
            errors.rejectValue("basePrice", "wrongValue", "BasePrice is wrong");
            errors.rejectValue("maxPrice", "wrongValue", "maxPrice is wrong");
        }

        // 이벤트 실제 끝 시간에 대한 validation
        LocalDateTime endEventTime = eventRequestDto.getEndEventDateTime();
        if (endEventTime.isBefore(eventRequestDto.getBeginEventDateTime())
            || endEventTime.isBefore(eventRequestDto.getCloseEnrollmentDateTime())
            || endEventTime.isBefore(eventRequestDto.getBeginEnrollmentDateTime())) {
            errors.rejectValue("endEventTime", "wrongValue", "endEventTime is wrong");
        }

        // 이벤트 모집 종료 시간에 대한 validation
        LocalDateTime closeEnrollmentDateTime = eventRequestDto.getCloseEnrollmentDateTime();
        if (closeEnrollmentDateTime.isBefore(eventRequestDto.getBeginEnrollmentDateTime())
            || closeEnrollmentDateTime.isAfter(eventRequestDto.getBeginEventDateTime())) {
            errors.rejectValue("closeEnrollmentDateTime", "wrongValue",
                "closeEnrollmentDateTime is wrong");
        }

        // 이벤트 실제 시작 시간에 대한 validation
        LocalDateTime beginEventTime = eventRequestDto.getBeginEventDateTime();
        if (beginEventTime.isAfter(eventRequestDto.getEndEventDateTime())
            || beginEventTime.isBefore(eventRequestDto.getBeginEnrollmentDateTime())
            || beginEventTime.isBefore(eventRequestDto.getCloseEnrollmentDateTime())) {
            errors.rejectValue("beginEventDateTime", "wrongValue", "beginEventTime is wrong");
        }

        return errors.getAllErrors().size() <= 0;
    }
}
