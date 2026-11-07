package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class RoundStartEvent extends Event {

    public static final EventType<RoundStartEvent> ROUND_START =
            new EventType<>(Event.ANY, "ROUND_START");

    private final int roundNumber;
    private final String playerHeroId;
    private final String opponentId;

    public RoundStartEvent(int roundNumber, String playerHeroId, String opponentId) {
        super(ROUND_START);
        this.roundNumber = roundNumber;
        this.playerHeroId = playerHeroId;
        this.opponentId = opponentId;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public String getPlayerHeroId() {
        return playerHeroId;
    }

    public String getOpponentId() {
        return opponentId;
    }
}
