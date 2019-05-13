package io.namjune.basicrestapi.events;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
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
    @Parameters
    public void testFree(int basePrice, int maxPrice, boolean isFree) {
        //Given
        Event event = Event.builder()
            .basePrice(basePrice)
            .maxPrice(maxPrice)
            .build();

        //When
        event.updateFree();

        //Then
        assertThat(event.isFree()).isEqualTo(isFree);
    }

    // @Parameters method convention -> parametersFor + 테스트 메서드
    private Object[] parametersForTestFree() {
        return new Object[]{
            new Object[]{0, 0, true},
            new Object[]{100, 0, false},
            new Object[]{0, 100, false},
            new Object[]{100, 200, false}
        };
    }

    @Test
    @Parameters
    public void testOffline(String location, boolean isOffline) {
        //Given
        Event event = Event.builder()
            .location(location)
            .build();

        //When
        event.updateOffline();

        //Then
        assertThat(event.isOffline()).isEqualTo(isOffline);
    }

    // @Parameters method convention -> parametersFor + 테스트 메서드
    private Object[] parametersForTestOffline() {
        return new Object[]{
            new Object[]{"starbucks", true},
            new Object[]{"", false},
            new Object[]{"          ", false},
            new Object[]{null, false}
        };
    }
}
