package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class GameOverEvent extends Event {

    public static final EventType<GameOverEvent> GAME_OVER =
            new EventType<>(Event.ANY, "GAME_OVER");

    private final int roundReached;
    private final String reason;

    public GameOverEvent(int roundReached, String reason) {
        super(GAME_OVER);
        this.roundReached = roundReached;
        this.reason = reason;
    }

    public int getRoundReached() {
        return roundReached;
    }

    public String getReason() {
        return reason;
    }
}
