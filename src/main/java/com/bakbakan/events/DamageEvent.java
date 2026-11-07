package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventType;

public class DamageEvent extends Event {

    public static final EventType<DamageEvent> DAMAGE = new EventType<>(Event.ANY, "DAMAGE");

    private final String fighterId;
    private final double amount;
    private final double remainingHealth;

    public DamageEvent(String fighterId, double amount, double remainingHealth) {
        super(DAMAGE);
        this.fighterId = fighterId;
        this.amount = amount;
        this.remainingHealth = remainingHealth;
    }

    public String getFighterId() {
        return fighterId;
    }

    public double getAmount() {
        return amount;
    }

    public double getRemainingHealth() {
        return remainingHealth;
    }
}
