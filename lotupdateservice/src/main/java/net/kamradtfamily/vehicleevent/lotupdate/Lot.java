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
package net.kamradtfamily.vehicleevent.lotupdate;


import net.kamradtfamily.contextlogging.ContextLogger;
import net.kamradtfamily.vehicleevent.lot.api.LotCreateCommand;
import net.kamradtfamily.vehicleevent.lot.api.LotCreateEvent;
import net.kamradtfamily.vehicleevent.lot.api.LotUpdateCommand;
import net.kamradtfamily.vehicleevent.lot.api.LotUpdateEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.Instant;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 *
 * @author randalkamradt
 */
@Aggregate(cache = "lotCache")
public class Lot {
    @AggregateIdentifier
    String id;
    String name;
    String manager;
    String createTime;
    String updateTime;
    @CommandHandler
    public Lot(LotCreateCommand command) {
        apply(LotCreateEvent.builder()
                .id(command.getId())
                .name(command.getName())
                .manager(command.getManager())
                .time(Instant.now().toString())
                .context(command.getContext())
                .build());
    }

    public Lot() {

    }
    @CommandHandler
    public void handle(LotUpdateCommand command) {
        apply(LotUpdateEvent.builder()
                .id(command.getId())
                .name(command.getName())
                .manager(command.getManager())
                .time(Instant.now().toString())
                .context(command.getContext())
                .build());

    }

    @EventSourcingHandler
    public void on(LotCreateEvent event) {
        ContextLogger.logWithContext(event.getContext(), "handling " + event.getClass().getSimpleName());
        id = event.getId();
        name = event.getName();
        manager = event.getManager();
        createTime = event.getTime();
    }

    @EventSourcingHandler
    public void on(LotUpdateEvent event) {
        ContextLogger.logWithContext(event.getContext(), "handling " + event.getClass().getSimpleName());
        name = event.getName();
        manager = event.getManager();
        updateTime = event.getTime();
    }

}
