package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class SkillUsedEvent extends Event {

    public static final EventType<SkillUsedEvent> SKILL_USED =
            new EventType<>(Event.ANY, "SKILL_USED");

    private final String fighterId;
    private final String skillId;

    public SkillUsedEvent(String fighterId, String skillId) {
        super(SKILL_USED);
        this.fighterId = fighterId;
        this.skillId = skillId;
    }

    public String getFighterId() {
        return fighterId;
    }

    public String getSkillId() {
        return skillId;
    }
}
