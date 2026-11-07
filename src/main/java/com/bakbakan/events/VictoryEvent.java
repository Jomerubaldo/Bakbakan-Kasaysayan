package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class VictoryEvent extends Event {

    public static final EventType<VictoryEvent> VICTORY =
            new EventType<>(Event.ANY, "VICTORY");

    private final int finalRound;
    private final String heroId;

    public VictoryEvent(int finalRound, String heroId) {
        super(VICTORY);
        this.finalRound = finalRound;
        this.heroId = heroId;
    }

    public int getFinalRound() {
        return finalRound;
    }

    public String getHeroId() {
        return heroId;
    }
}
