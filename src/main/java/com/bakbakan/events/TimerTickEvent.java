package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class TimerTickEvent extends Event {

    public static final EventType<TimerTickEvent> TIMER_TICK =
            new EventType<>(Event.ANY, "TIMER_TICK");

    private final int secondsRemaining;

    public TimerTickEvent(int secondsRemaining) {
        super(TIMER_TICK);
        this.secondsRemaining = secondsRemaining;
    }

    public int getSecondsRemaining() {
        return secondsRemaining;
    }
}
