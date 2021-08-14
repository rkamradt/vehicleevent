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


import net.kamradtfamily.vehicleevent.api.PurchaseOrderAddCommand;
import net.kamradtfamily.vehicleevent.api.PurchaseOrderAddEvent;
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
@Aggregate(cache = "purchaseOrderCache")
public class PurchaseOrder {
    @AggregateIdentifier
    String id;
    BigDecimal price;
    String type;
    String time;
    @CommandHandler
    public PurchaseOrder(PurchaseOrderAddCommand command) {
        if (command.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount <= 0");
        }
        apply(new PurchaseOrderAddEvent(command.getId(),
                command.getPrice(),
                command.getType()));
    }

    @EventSourcingHandler
    public void on(PurchaseOrderAddCommand event) {
        id = event.getId();
        price = event.getPrice();
        type = event.getType();
        time = Instant.now().toString();
    }

}
