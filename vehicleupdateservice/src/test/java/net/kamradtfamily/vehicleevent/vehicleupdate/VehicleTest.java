package net.kamradtfamily.vehicleevent.vehicleupdate;

import net.kamradtfamily.vehicleevent.api.*;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VehicleTest {

    private FixtureConfiguration<Vehicle> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(Vehicle.class);
    }

    @Test
    void testVehiclePurchaseCommand_validPrice() {
        String vehicleId = "vehicle-1";
        BigDecimal price = new BigDecimal("25000.00");
        String type = "sedan";

        fixture.givenNoPriorActivity()
                .when(VehiclePurchaseCommand.builder()
                        .id(vehicleId)
                        .price(price)
                        .type(type)
                        .build())
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(Matchers.payloadsMatching(Matchers.exactSequenceOf(
                        Matchers.matches(e -> {
                            VehiclePurchaseEvent event = (VehiclePurchaseEvent) e;
                            return vehicleId.equals(event.getId()) &&
                                    price.equals(event.getPrice()) &&
                                    type.equals(event.getType()) &&
                                    event.getTime() != null;
                        })
                )));
    }

    @Test
    void testVehiclePurchaseCommand_zeroPrice_throwsException() {
        String vehicleId = "vehicle-2";
        BigDecimal zeroPrice = BigDecimal.ZERO;
        String type = "sedan";

        fixture.givenNoPriorActivity()
                .when(VehiclePurchaseCommand.builder()
                        .id(vehicleId)
                        .price(zeroPrice)
                        .type(type)
                        .build())
                .expectException(IllegalArgumentException.class)
                .expectExceptionMessage("amount <= 0");
    }

    @Test
    void testVehiclePurchaseCommand_negativePrice_throwsException() {
        String vehicleId = "vehicle-3";
        BigDecimal negativePrice = new BigDecimal("-1000.00");
        String type = "truck";

        fixture.givenNoPriorActivity()
                .when(VehiclePurchaseCommand.builder()
                        .id(vehicleId)
                        .price(negativePrice)
                        .type(type)
                        .build())
                .expectException(IllegalArgumentException.class)
                .expectExceptionMessage("amount <= 0");
    }

    @Test
    void testVehicleSendToLotCommand() {
        String vehicleId = "vehicle-4";
        BigDecimal price = new BigDecimal("30000.00");
        String type = "suv";
        String lotId = "lot-1";

        fixture.given(VehiclePurchaseEvent.builder()
                        .id(vehicleId)
                        .price(price)
                        .type(type)
                        .time("2024-01-01T10:00:00Z")
                        .build())
                .when(VehicleSendToLotCommand.builder()
                        .id(vehicleId)
                        .lot(lotId)
                        .build())
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(Matchers.payloadsMatching(Matchers.exactSequenceOf(
                        Matchers.matches(e -> {
                            VehicleSendToLotEvent event = (VehicleSendToLotEvent) e;
                            return vehicleId.equals(event.getId()) &&
                                    lotId.equals(event.getLot()) &&
                                    event.getTime() != null;
                        })
                )));
    }

    @Test
    void testVehicleSellCommand_validSale() {
        String vehicleId = "vehicle-5";
        BigDecimal purchasePrice = new BigDecimal("25000.00");
        BigDecimal sellPrice = new BigDecimal("28000.00");
        String type = "sedan";

        fixture.given(VehiclePurchaseEvent.builder()
                        .id(vehicleId)
                        .price(purchasePrice)
                        .type(type)
                        .time("2024-01-01T10:00:00Z")
                        .build())
                .when(VehicleSellCommand.builder()
                        .id(vehicleId)
                        .price(sellPrice)
                        .build())
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(Matchers.payloadsMatching(Matchers.exactSequenceOf(
                        Matchers.matches(e -> {
                            VehicleSellEvent event = (VehicleSellEvent) e;
                            return vehicleId.equals(event.getId()) &&
                                    sellPrice.equals(event.getPrice()) &&
                                    event.getTime() != null;
                        })
                )));
    }

    @Test
    void testVehicleSellCommand_alreadySold_throwsException() {
        String vehicleId = "vehicle-6";
        BigDecimal purchasePrice = new BigDecimal("25000.00");
        BigDecimal firstSellPrice = new BigDecimal("28000.00");
        BigDecimal secondSellPrice = new BigDecimal("30000.00");
        String type = "sedan";

        fixture.given(
                        VehiclePurchaseEvent.builder()
                                .id(vehicleId)
                                .price(purchasePrice)
                                .type(type)
                                .time("2024-01-01T10:00:00Z")
                                .build(),
                        VehicleSellEvent.builder()
                                .id(vehicleId)
                                .price(firstSellPrice)
                                .time("2024-01-02T10:00:00Z")
                                .build())
                .when(VehicleSellCommand.builder()
                        .id(vehicleId)
                        .price(secondSellPrice)
                        .build())
                .expectException(IllegalArgumentException.class)
                .expectExceptionMessage("vehicle already sold");
    }

    @Test
    void testVehicleSellCommand_zeroPrice_throwsException() {
        String vehicleId = "vehicle-7";
        BigDecimal purchasePrice = new BigDecimal("25000.00");
        BigDecimal sellPrice = BigDecimal.ZERO;
        String type = "sedan";

        fixture.given(VehiclePurchaseEvent.builder()
                        .id(vehicleId)
                        .price(purchasePrice)
                        .type(type)
                        .time("2024-01-01T10:00:00Z")
                        .build())
                .when(VehicleSellCommand.builder()
                        .id(vehicleId)
                        .price(sellPrice)
                        .build())
                .expectException(IllegalArgumentException.class)
                .expectExceptionMessage("amount <= 0");
    }

    @Test
    void testCompleteVehicleLifecycle() {
        String vehicleId = "vehicle-8";
        BigDecimal purchasePrice = new BigDecimal("25000.00");
        BigDecimal sellPrice = new BigDecimal("28000.00");
        String type = "sedan";
        String lotId = "lot-1";

        fixture.given(VehiclePurchaseEvent.builder()
                        .id(vehicleId)
                        .price(purchasePrice)
                        .type(type)
                        .time("2024-01-01T10:00:00Z")
                        .build())
                .andGiven(VehicleSendToLotEvent.builder()
                        .id(vehicleId)
                        .lot(lotId)
                        .time("2024-01-02T10:00:00Z")
                        .build())
                .when(VehicleSellCommand.builder()
                        .id(vehicleId)
                        .price(sellPrice)
                        .build())
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(Matchers.payloadsMatching(Matchers.exactSequenceOf(
                        Matchers.matches(e -> {
                            VehicleSellEvent event = (VehicleSellEvent) e;
                            return vehicleId.equals(event.getId()) &&
                                    sellPrice.equals(event.getPrice()) &&
                                    event.getTime() != null;
                        })
                )));
    }
}
