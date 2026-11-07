package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class FighterDefeatedEvent extends Event {

    public static final EventType<FighterDefeatedEvent> FIGHTER_DEFEATED =
            new EventType<>(Event.ANY, "FIGHTER_DEFEATED");

    private final String fighterId;
    private final String defeatedBy;

    public FighterDefeatedEvent(String fighterId, String defeatedBy) {
        super(FIGHTER_DEFEATED);
        this.fighterId = fighterId;
        this.defeatedBy = defeatedBy;
    }

    public String getFighterId() {
        return fighterId;
    }

    public String getDefeatedBy() {
        return defeatedBy;
    }
}
