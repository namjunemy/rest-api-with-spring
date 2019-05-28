package io.namjune.basicrestapi.events;

import java.time.LocalDateTime;
import org.junit.Test;
import org.springframework.validation.Errors;

/**
 * Copyright (c) 2018 ZUM Internet, Inc. All right reserved. http://www.zum.com This software is the
 * confidential and proprietary information of ZUM , Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZUM.
 *
 * Revision History Author                    Date                     Description
 * ------------------       --------------            ------------------ njkim
 * 2019-05-28
 */
public class EventValidatorTest {


    @Test
    public void errors(Errors errors) {
        EventValidator eventValidator = new EventValidator();
        EventRequestDto eventRequestDto = EventRequestDto.builder()
            .name("REST API with Spring")
            .description("REST API Basic")
            .beginEnrollmentDateTime(LocalDateTime.of(2019, 5, 9, 17, 0, 0))
            .closeEnrollmentDateTime(LocalDateTime.of(2019, 5, 6, 17, 0, 0))
            .beginEventDateTime(LocalDateTime.of(2019, 5, 10, 17, 0, 0))
            .endEventDateTime(LocalDateTime.of(2019, 5, 13, 17, 0, 0))
            .basePrice(10000)
            .maxPrice(100)
            .limitOfEnrollment(100)
            .location("서울대입구")
            .build();

        boolean result = eventValidator.validate(eventRequestDto, errors);

        assertTrue
    }
}
