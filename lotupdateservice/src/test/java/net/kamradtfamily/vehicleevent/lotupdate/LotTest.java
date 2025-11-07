package net.kamradtfamily.vehicleevent.lotupdate;

import net.kamradtfamily.vehicleevent.lot.api.*;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LotTest {

    private FixtureConfiguration<Lot> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(Lot.class);
    }

    @Test
    void testLotCreateCommand() {
        String lotId = "lot-1";
        String name = "Main Lot";
        String manager = "John Doe";

        fixture.givenNoPriorActivity()
                .when(LotCreateCommand.builder()
                        .id(lotId)
                        .name(name)
                        .manager(manager)
                        .build())
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(Matchers.payloadsMatching(Matchers.exactSequenceOf(
                        Matchers.matches(e -> {
                            LotCreateEvent event = (LotCreateEvent) e;
                            return lotId.equals(event.getId()) &&
                                    name.equals(event.getName()) &&
                                    manager.equals(event.getManager()) &&
                                    event.getTime() != null;
                        })
                )));
    }

    @Test
    void testLotUpdateCommand() {
        String lotId = "lot-1";
        String originalName = "Main Lot";
        String originalManager = "John Doe";
        String newName = "Updated Main Lot";
        String newManager = "Jane Smith";

        fixture.given(LotCreateEvent.builder()
                        .id(lotId)
                        .name(originalName)
                        .manager(originalManager)
                        .time("2024-01-01T10:00:00Z")
                        .build())
                .when(LotUpdateCommand.builder()
                        .id(lotId)
                        .name(newName)
                        .manager(newManager)
                        .build())
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(Matchers.payloadsMatching(Matchers.exactSequenceOf(
                        Matchers.matches(e -> {
                            LotUpdateEvent event = (LotUpdateEvent) e;
                            return lotId.equals(event.getId()) &&
                                    newName.equals(event.getName()) &&
                                    newManager.equals(event.getManager()) &&
                                    event.getTime() != null;
                        })
                )));
    }

    @Test
    void testLotCreateAndUpdate() {
        String lotId = "lot-2";
        String initialName = "Secondary Lot";
        String initialManager = "Manager A";
        String updatedName = "Updated Secondary Lot";
        String updatedManager = "Manager B";

        fixture.given(LotCreateEvent.builder()
                        .id(lotId)
                        .name(initialName)
                        .manager(initialManager)
                        .time("2024-01-01T10:00:00Z")
                        .build())
                .when(LotUpdateCommand.builder()
                        .id(lotId)
                        .name(updatedName)
                        .manager(updatedManager)
                        .build())
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(Matchers.payloadsMatching(Matchers.exactSequenceOf(
                        Matchers.matches(e -> {
                            LotUpdateEvent event = (LotUpdateEvent) e;
                            return lotId.equals(event.getId()) &&
                                    updatedName.equals(event.getName()) &&
                                    updatedManager.equals(event.getManager()) &&
                                    event.getTime() != null;
                        })
                )));
    }

    @Test
    void testMultipleUpdates() {
        String lotId = "lot-3";
        String initialName = "Lot Three";
        String initialManager = "Initial Manager";

        fixture.given(
                        LotCreateEvent.builder()
                                .id(lotId)
                                .name(initialName)
                                .manager(initialManager)
                                .time("2024-01-01T10:00:00Z")
                                .build(),
                        LotUpdateEvent.builder()
                                .id(lotId)
                                .name("First Update")
                                .manager("Manager 1")
                                .time("2024-01-02T10:00:00Z")
                                .build())
                .when(LotUpdateCommand.builder()
                        .id(lotId)
                        .name("Second Update")
                        .manager("Manager 2")
                        .build())
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(Matchers.payloadsMatching(Matchers.exactSequenceOf(
                        Matchers.matches(e -> {
                            LotUpdateEvent event = (LotUpdateEvent) e;
                            return lotId.equals(event.getId()) &&
                                    "Second Update".equals(event.getName()) &&
                                    "Manager 2".equals(event.getManager()) &&
                                    event.getTime() != null;
                        })
                )));
    }
}
