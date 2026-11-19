package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class PlayerHeroDownEvent extends Event {

    public static final EventType<PlayerHeroDownEvent> PLAYER_HERO_DOWN =
            new EventType<>(Event.ANY, "PLAYER_HERO_DOWN");

    private final String fighterId;
    private final String heroId;

    public PlayerHeroDownEvent(String fighterId, String heroId) {
        super(PLAYER_HERO_DOWN);
        this.fighterId = fighterId;
        this.heroId = heroId;
    }

    public String getFighterId() {
        return fighterId;
    }

    public String getHeroId() {
        return heroId;
    }
}
