package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class ManaChangedEvent extends Event {

    public static final EventType<ManaChangedEvent> MANA_CHANGED =
            new EventType<>(Event.ANY, "MANA_CHANGED");

    private final String fighterId;
    private final double currentMana;
    private final double maxMana;

    public ManaChangedEvent(String fighterId, double currentMana, double maxMana) {
        super(MANA_CHANGED);
        this.fighterId = fighterId;
        this.currentMana = currentMana;
        this.maxMana = maxMana;
    }

    public String getFighterId() {
        return fighterId;
    }

    public double getCurrentMana() {
        return currentMana;
    }

    public double getMaxMana() {
        return maxMana;
    }
}
