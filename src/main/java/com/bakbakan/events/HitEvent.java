package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class HitEvent extends Event {

    public static final EventType<HitEvent> HIT = new EventType<>(Event.ANY, "HIT");

    private final String attackerId;
    private final String targetId;
    private final double damage;
    private final boolean blocked;
    private final boolean special;

    public HitEvent(String attackerId, String targetId, double damage, boolean blocked) {
        this(attackerId, targetId, damage, blocked, false);
    }

    public HitEvent(String attackerId, String targetId, double damage, boolean blocked,
                    boolean special) {
        super(HIT);
        this.attackerId = attackerId;
        this.targetId = targetId;
        this.damage = damage;
        this.blocked = blocked;
        this.special = special;
    }

    public String getAttackerId() {
        return attackerId;
    }

    public String getTargetId() {
        return targetId;
    }

    public double getDamage() {
        return damage;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isSpecial() {
        return special;
    }
}
