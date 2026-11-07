package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class SoundRequestEvent extends Event {

    public static final EventType<SoundRequestEvent> SOUND_REQUEST =
            new EventType<>(Event.ANY, "SOUND_REQUEST");

    private final String soundId;
    private final boolean music;

    public SoundRequestEvent(String soundId, boolean music) {
        super(SOUND_REQUEST);
        this.soundId = soundId;
        this.music = music;
    }

    public String getSoundId() {
        return soundId;
    }

    public boolean isMusic() {
        return music;
    }
}
