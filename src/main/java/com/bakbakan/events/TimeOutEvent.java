package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class TimeOutEvent extends Event {

    public static final EventType<TimeOutEvent> TIME_OUT =
            new EventType<>(Event.ANY, "TIME_OUT");

    private final double playerHealth;
    private final double opponentHealth;
    private final boolean playerWon;

    public TimeOutEvent(double playerHealth, double opponentHealth, boolean playerWon) {
        super(TIME_OUT);
        this.playerHealth = playerHealth;
        this.opponentHealth = opponentHealth;
        this.playerWon = playerWon;
    }

    public double getPlayerHealth() {
        return playerHealth;
    }

    public double getOpponentHealth() {
        return opponentHealth;
    }

    public boolean isPlayerWon() {
        return playerWon;
    }
}
