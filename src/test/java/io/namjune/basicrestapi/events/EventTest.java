package io.namjune.basicrestapi.events;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void testFree() {
        //Given
        Event event = Event.builder()
            .basePrice(0)
            .maxPrice(0)
            .build();

        //When
        event.updateFree();

        //Then
        assertThat(event.isFree()).isTrue();
    }

    @Test
    public void testNotFree() {
        //Given
        Event event1 = Event.builder()
            .basePrice(100)
            .maxPrice(0)
            .build();

        Event event2 = Event.builder()
            .basePrice(0)
            .maxPrice(100)
            .build();

        //When
        event1.updateFree();
        event2.updateFree();

        //Then
        assertThat(event1.isFree()).isFalse();
        assertThat(event2.isFree()).isFalse();
    }

    @Test
    public void testOffline() {
        //Given
        Event event = Event.builder()
            .location("starbucks")
            .build();

        //When
        event.updateOffline();

        //Then
        assertThat(event.isOffline()).isTrue();
    }

    @Test
    public void testNotOffline() {
        //Given
        Event event = Event.builder()
            .build();

        //When
        event.updateOffline();

        //Then
        assertThat(event.isOffline()).isFalse();
    }
}
