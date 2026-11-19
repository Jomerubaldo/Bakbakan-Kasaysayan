package com.bakbakan.combat;

import com.bakbakan.entities.Fighter;
import com.bakbakan.events.DamageEvent;
import com.bakbakan.events.FighterDefeatedEvent;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.HitEvent;
import com.bakbakan.events.SoundRequestEvent;
import javafx.geometry.Rectangle2D;

public final class CombatSystem {

    private final GameEventBus bus;

    public CombatSystem(GameEventBus bus) {
        this.bus = bus;
    }

    public void resolve(Fighter attacker, Fighter defender) {
        if (attacker == null || defender == null) {
            return;
        }
        if (!attacker.isAlive() || !defender.isAlive()) {
            return;
        }

        Rectangle2D hitbox = attacker.getAttackHitbox();
        if (hitbox == null || !hitbox.intersects(defender.getHurtbox())) {
            return;
        }

        attacker.markHitLanded();
        boolean blocked = defender.isBlocking() && defender.isFacing(attacker);
        applyHit(attacker, defender, attacker.getAttackDamage(), blocked);
    }

    public void applyHit(Fighter attacker, Fighter defender, double rawDamage, boolean blocked) {
        applyHit(attacker, defender, rawDamage, blocked, false);
    }

    public void applyHit(Fighter attacker, Fighter defender, double rawDamage,
                         boolean blocked, boolean special) {
        if (attacker == null || defender == null) {
            return;
        }
        if (!attacker.isAlive() || !defender.isAlive()) {
            return;
        }

        double afterDefense = rawDamage * defenseMultiplier(defender);
        double damage = blocked
                ? afterDefense * (1.0 - CombatConstants.BLOCK_DAMAGE_REDUCTION)
                : afterDefense;

        defender.receiveHit(damage, blocked, attacker.getX());

        bus.fire(new HitEvent(attacker.getId(), defender.getId(), damage, blocked,
                special));
        bus.fire(new DamageEvent(defender.getId(), damage, defender.getHealth()));
        bus.fire(new SoundRequestEvent(blocked ? "block" : "hit", false));

        attacker.addSpecial(damage * CombatConstants.SPECIAL_GAIN_DEALT);
        defender.addSpecial(damage * CombatConstants.SPECIAL_GAIN_TAKEN);

        if (!defender.isAlive()) {
            bus.fire(new FighterDefeatedEvent(defender.getId(), attacker.getId()));
        }
    }

    private static double defenseMultiplier(Fighter defender) {
        double defense = Math.max(0, defender.getDefense());
        double multiplier = CombatConstants.DEFENSE_PIVOT
                / (CombatConstants.DEFENSE_PIVOT + defense);
        return Math.max(CombatConstants.MIN_DAMAGE_MULTIPLIER, multiplier);
    }
}
