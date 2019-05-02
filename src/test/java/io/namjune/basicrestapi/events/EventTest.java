package io.namjune.basicrestapi.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EventTest {

    @Test
    public void builder() {
        Event event = Event.builder()
            .name("REST API with Spring")
            .description("REST API Basic")
            .build();

        assertThat(event).isNotNull();
    }

    @Test
    public void javaBean() {
        //Given
        Event event = new Event();
        String name = "Event";

        //When
        event.setName(name);
        String description = "REST API Basic";
        event.setDescription(description);

        //Then
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }
}
