package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class SpecialReadyEvent extends Event {

    public static final EventType<SpecialReadyEvent> SPECIAL_READY =
            new EventType<>(Event.ANY, "SPECIAL_READY");

    private final String fighterId;

    public SpecialReadyEvent(String fighterId) {
        super(SPECIAL_READY);
        this.fighterId = fighterId;
    }

    public String getFighterId() {
        return fighterId;
    }
}
