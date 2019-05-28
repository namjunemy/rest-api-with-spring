package io.namjune.basicrestapi.events;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Copyright (c) 2018 ZUM Internet, Inc. All right reserved. http://www.zum.com This software is the
 * confidential and proprietary information of ZUM , Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZUM.
 *
 * Revision History Author                    Date                     Description
 * ------------------       --------------            ------------------ njkim 2019-05-28
 */
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;

    public Event add(EventRequestDto eventRequestDto) {
        Event event = modelMapper.map(eventRequestDto, Event.class);
        event.updateDynamicField();
        return this.eventRepository.save(event);
    }

    public Page<Event> findAll(Pageable pageable) {
        return this.eventRepository.findAll(pageable);
    }
}
