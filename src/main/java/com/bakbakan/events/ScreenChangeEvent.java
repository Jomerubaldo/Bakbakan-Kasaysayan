package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class ScreenChangeEvent extends Event {

    public static final EventType<ScreenChangeEvent> SCREEN_CHANGE =
            new EventType<>(Event.ANY, "SCREEN_CHANGE");

    private final String fromScreen;
    private final String toScreen;

    public ScreenChangeEvent(String fromScreen, String toScreen) {
        super(SCREEN_CHANGE);
        this.fromScreen = fromScreen;
        this.toScreen = toScreen;
    }

    public String getFromScreen() {
        return fromScreen;
    }

    public String getToScreen() {
        return toScreen;
    }
}
