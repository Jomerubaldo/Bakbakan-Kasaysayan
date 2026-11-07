package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class PauseEvent extends Event {

    public static final EventType<PauseEvent> PAUSE = new EventType<>(Event.ANY, "PAUSE");

    public PauseEvent() {
        super(PAUSE);
    }
}
