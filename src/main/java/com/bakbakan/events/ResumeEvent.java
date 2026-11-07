package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class ResumeEvent extends Event {

    public static final EventType<ResumeEvent> RESUME = new EventType<>(Event.ANY, "RESUME");

    public ResumeEvent() {
        super(RESUME);
    }
}
