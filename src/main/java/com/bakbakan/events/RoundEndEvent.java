package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class RoundEndEvent extends Event {

    public static final EventType<RoundEndEvent> ROUND_END =
            new EventType<>(Event.ANY, "ROUND_END");

    private final int roundNumber;
    private final boolean playerWon;
    private final String reason;

    public RoundEndEvent(int roundNumber, boolean playerWon, String reason) {
        super(ROUND_END);
        this.roundNumber = roundNumber;
        this.playerWon = playerWon;
        this.reason = reason;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public boolean isPlayerWon() {
        return playerWon;
    }

    public String getReason() {
        return reason;
    }
}
