/*
 * The MIT License
 *
 * Copyright 2021 randalkamradt.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.kamradtfamily.vehicleevent.command;


import net.kamradtfamily.vehicleevent.api.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.math.BigDecimal;
import java.time.Instant;

/**
 *
 * @author randalkamradt
 */
@Aggregate(cache = "vehicleCache")
public class Vehicle {
    @AggregateIdentifier
    String id;
    BigDecimal price;
    String type;
    String lot;
    BigDecimal sellPrice;
    String inductTime;
    String toLotTime;
    String sellTime;
    @CommandHandler
    public Vehicle(VehiclePurchaseCommand command) {
        if (command.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount <= 0");
        }
        apply(new VehiclePurchaseEvent(command.getId(),
                command.getPrice(),
                command.getType()));
    }

    public Vehicle() {

    }
    @CommandHandler
    public void handle(VehicleSendToLotCommand command) {
        if (command.getLot().length() != 1 || !"abc".contains(command.getLot().substring(0,1))) {
            throw new IllegalArgumentException("lot must be a, b, or c");
        }
        apply(new VehicleSendToLotEvent(id, command.getLot()));

    }

    @CommandHandler
    public void handle(VehicleSellCommand command) {
        if (sellPrice != null) {
            throw new IllegalArgumentException("vehicle already sold");
        }
        if (command.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount <= 0");
        }
        apply(new VehicleSellEvent(id, command.getPrice()));
    }

    @EventSourcingHandler
    public void on(VehiclePurchaseEvent event) {
        id = event.getId();
        price = event.getPrice();
        type = event.getType();
        inductTime = Instant.now().toString();
    }

    @EventSourcingHandler
    public void on(VehicleSendToLotEvent event) {
        lot = event.getLot();
        toLotTime = Instant.now().toString();
    }

    @EventSourcingHandler
    public void on(VehicleSellEvent event) {
        sellPrice = event.getPrice();
        sellTime = Instant.now().toString();
    }

}
