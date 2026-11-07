package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class SpecialUsedEvent extends Event {

    public static final EventType<SpecialUsedEvent> SPECIAL_USED =
            new EventType<>(Event.ANY, "SPECIAL_USED");

    private final String fighterId;
    private final String heroId;

    public SpecialUsedEvent(String fighterId, String heroId) {
        super(SPECIAL_USED);
        this.fighterId = fighterId;
        this.heroId = heroId;
    }

    public String getFighterId() {
        return fighterId;
    }

    public String getHeroId() {
        return heroId;
    }
}
