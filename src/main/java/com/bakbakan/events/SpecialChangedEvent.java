package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class SpecialChangedEvent extends Event {

    public static final EventType<SpecialChangedEvent> SPECIAL_CHANGED =
            new EventType<>(Event.ANY, "SPECIAL_CHANGED");

    private final String fighterId;
    private final double currentSpecial;
    private final double maxSpecial;
    private final boolean ready;

    public SpecialChangedEvent(String fighterId, double currentSpecial,
                               double maxSpecial, boolean ready) {
        super(SPECIAL_CHANGED);
        this.fighterId = fighterId;
        this.currentSpecial = currentSpecial;
        this.maxSpecial = maxSpecial;
        this.ready = ready;
    }

    public String getFighterId() {
        return fighterId;
    }

    public double getCurrentSpecial() {
        return currentSpecial;
    }

    public double getMaxSpecial() {
        return maxSpecial;
    }

    public boolean isReady() {
        return ready;
    }
}
