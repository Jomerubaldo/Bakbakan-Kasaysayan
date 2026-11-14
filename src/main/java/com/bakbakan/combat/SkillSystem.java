package com.bakbakan.combat;

import com.bakbakan.entities.Fighter;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

public final class SkillSystem {

    private final CombatSystem combat;
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<AreaEffect> effects = new ArrayList<>();
    private final List<PendingCast> pending = new ArrayList<>();
    private final List<Impact> impacts = new ArrayList<>();

    public SkillSystem(CombatSystem combat) {
        this.combat = combat;
    }

    public record Impact(Fighter attacker, boolean special) {
    }

    public List<Impact> drainImpacts() {
        List<Impact> drained = List.copyOf(impacts);
        impacts.clear();
        return drained;
    }

    public void clear() {
        projectiles.clear();
        effects.clear();
        pending.clear();
        impacts.clear();
    }

    public void castSkill(Fighter attacker, Fighter defender) {
        boolean right = defender.getX() >= attacker.getX();
        pending.add(new PendingCast(CastKind.SKILL, attacker, defender, right,
                defender.getX(), skillDelay(attacker.getSpriteId())));
    }

    public void castSpecial(Fighter attacker, Fighter defender) {
        boolean right = defender.getX() >= attacker.getX();
        pending.add(new PendingCast(CastKind.SPECIAL, attacker, defender, right,
                defender.getX(), specialDelay(attacker.getSpriteId())));
    }

    private static double skillDelay(String id) {
        return switch (id) {
            case "rizal" -> 0.38 * CombatConstants.SKILL_CAST_LOCK;
            case "bonifacio" -> 0.22 * CombatConstants.SKILL_CAST_LOCK;
            case "luna" -> 0.31 * CombatConstants.SKILL_CAST_LOCK;
            case "mabini" -> 0.36 * CombatConstants.SKILL_CAST_LOCK;
            case "lapulapu" -> 0.36 * CombatConstants.SKILL_CAST_LOCK;
            case "spanish_soldier", "american_soldier" ->
                    0.30 * CombatConstants.SKILL_CAST_LOCK;
            case "boss_polavieja" -> 0.30 * CombatConstants.SKILL_CAST_LOCK;
            case "boss_makapagal" -> 0.24 * CombatConstants.SKILL_CAST_LOCK;
            case "boss_janolino" -> 0.30 * CombatConstants.SKILL_CAST_LOCK;
            case "boss_macarthur" -> 0.34 * CombatConstants.SKILL_CAST_LOCK;
            case "boss_magellan" -> 0.42 * CombatConstants.SKILL_CAST_LOCK;
            case "final_boss" -> 0.32 * CombatConstants.SKILL_CAST_LOCK;
            default -> 0.12;
        };
    }

    private static double specialDelay(String id) {
        return switch (id) {
            case "rizal" -> 0.72 * CombatConstants.SPECIAL_CAST_LOCK - 0.26;
            case "bonifacio" -> 0.46 * CombatConstants.SPECIAL_CAST_LOCK - 0.05;
            case "luna" -> 0.34 * CombatConstants.SPECIAL_CAST_LOCK - 0.08;
            case "mabini" -> 0.42 * CombatConstants.SPECIAL_CAST_LOCK - 0.05;
            case "lapulapu" -> 0.44 * CombatConstants.SPECIAL_CAST_LOCK;
            case "boss_polavieja" -> 0.20 * CombatConstants.SPECIAL_CAST_LOCK;
            case "boss_makapagal" -> 0.24 * CombatConstants.SPECIAL_CAST_LOCK;
            case "boss_janolino" -> 0.30 * CombatConstants.SPECIAL_CAST_LOCK;
            case "boss_macarthur" -> 0.34 * CombatConstants.SPECIAL_CAST_LOCK;
            case "boss_magellan" -> 0.24 * CombatConstants.SPECIAL_CAST_LOCK;
            case "final_boss" -> 0.36 * CombatConstants.SPECIAL_CAST_LOCK;
            default -> 0.16;
        };
    }

    private void spawnSkill(Fighter attacker, boolean right) {
        impacts.add(new Impact(attacker, false));
        switch (attacker.getSpriteId()) {
            case "rizal" -> projectiles.add(Projectile.ink(attacker, right));
            case "bonifacio" -> {
                double targetX = attacker.getX() + (right ? 220 : -220);
                effects.add(AreaEffect.flagDash(attacker, targetX));
            }
            case "luna" -> projectiles.add(Projectile.rifle(attacker, right));
            case "mabini" -> projectiles.add(Projectile.quill(attacker, right));
            case "lapulapu" -> projectiles.add(Projectile.kampilanWave(attacker, right));
            case "spanish_soldier", "american_soldier", "final_boss" ->
                    projectiles.add(Projectile.rifle(attacker, right));
            case "boss_polavieja" -> projectiles.add(Projectile.saberCommand(attacker, right));
            case "boss_makapagal" -> projectiles.add(Projectile.boloStrike(attacker, right));
            case "boss_janolino" -> {
                projectiles.add(Projectile.rifleBurst(attacker, right, -18));
                projectiles.add(Projectile.rifleBurst(attacker, right, 0));
                projectiles.add(Projectile.rifleBurst(attacker, right, 18));
            }
            case "boss_macarthur" -> projectiles.add(Projectile.heavyRifle(attacker, right));
            case "boss_magellan" -> projectiles.add(Projectile.arquebus(attacker, right));
            default -> {
            }
        }
    }

    private void spawnSpecial(Fighter attacker, Fighter defender, boolean right,
                              double targetX) {
        impacts.add(new Impact(attacker, true));
        switch (attacker.getSpriteId()) {
            case "rizal" -> addSpecialEffect(AreaEffect.giantBook(attacker, defender));
            case "bonifacio" -> addSpecialEffect(AreaEffect.shockwave(attacker));
            case "luna" -> {
                addSpecialEffect(AreaEffect.explosion(attacker, defender, -120, 0.10));
                addSpecialEffect(AreaEffect.explosion(attacker, defender, 0, 0.28));
                addSpecialEffect(AreaEffect.explosion(attacker, defender, 120, 0.46));
            }
            case "mabini" -> addSpecialEffect(AreaEffect.lightBeam(attacker, defender, right));
            case "lapulapu" -> addSpecialProjectile(Projectile.tidalWave(attacker, right));
            case "boss_polavieja" ->
                    addSpecialEffect(AreaEffect.firingSquad(attacker, defender, right));
            case "boss_makapagal" -> {
                addSpecialEffect(AreaEffect.rifleStrike(attacker, defender, -34, 0.06));
                addSpecialEffect(AreaEffect.rifleStrike(attacker, defender, 0, 0.20));
                addSpecialEffect(AreaEffect.rifleStrike(attacker, defender, 34, 0.34));
            }
            case "boss_janolino" -> {
                addSpecialEffect(AreaEffect.saberStrike(attacker, defender, -14, 0.04));
                addSpecialEffect(AreaEffect.saberStrike(attacker, defender, 16, 0.16));
                addSpecialEffect(AreaEffect.saberStrike(attacker, defender, 0, 0.28));
            }
            case "boss_macarthur" ->
                    addSpecialEffect(AreaEffect.bombardment(attacker, defender));
            case "boss_magellan" ->
                    addSpecialEffect(AreaEffect.conquistadorCharge(attacker, defender.getX()));
            case "final_boss" ->
                    addSpecialEffect(AreaEffect.firingSquad(attacker, defender, right));
            default -> {
            }
        }
    }

    private void addSpecialEffect(AreaEffect effect) {
        effect.markSpecial();
        effects.add(effect);
    }

    private void addSpecialProjectile(Projectile projectile) {
        projectile.markSpecial();
        projectiles.add(projectile);
    }

    public void castRizalBook(Fighter attacker, Fighter defender) {
        boolean right = defender.getX() >= attacker.getX();
        pending.add(new PendingCast(CastKind.BOOK, attacker, defender, right,
                defender.getX(), 0.10));
    }

    public void update(double dt, Fighter player, Fighter opponent) {
        if (player == null || opponent == null) {
            return;
        }

        for (int i = pending.size() - 1; i >= 0; i--) {
            PendingCast cast = pending.get(i);
            cast.remaining -= dt;
            if (cast.remaining > 0) {
                continue;
            }
            pending.remove(i);
            switch (cast.kind) {
                case SPECIAL -> spawnSpecial(cast.attacker, cast.defender, cast.right,
                        cast.defenderX);
                case SKILL -> spawnSkill(cast.attacker, cast.right);
                case BOOK -> projectiles.add(Projectile.book(cast.attacker, cast.right));
            }
        }

        for (Projectile projectile : List.copyOf(projectiles)) {
            projectile.update(dt);
            if (!projectile.isAlive() || projectile.hasHit()
                    || !projectile.getOwner().isAlive()) {
                continue;
            }

            Fighter defender = projectile.getOwner() == player ? opponent : player;
            if (!defender.isAlive()) {
                continue;
            }
            if (projectile.isHigh() && defender.isDucking()) {
                continue;
            }
            if (projectile.getHitbox().intersects(defender.getHurtbox())) {
                boolean blocked = defender.isBlocking()
                        && defender.isFacing(projectile.getOwner());
                projectile.markHit();
                projectile.setAlive(false);
                combat.applyHit(projectile.getOwner(), defender,
                        projectile.getDamage(), blocked, projectile.isSpecial());
            }
        }
        projectiles.removeIf(projectile -> !projectile.isAlive());

        for (AreaEffect effect : List.copyOf(effects)) {
            effect.update(dt);
            if (effect.consumeImpactMoment()) {
                impacts.add(new Impact(effect.getOwner(), true));
            }
            if (!effect.isAlive() || effect.hasHit() || !effect.isActive()
                    || !effect.getOwner().isAlive()) {
                continue;
            }

            Fighter defender = effect.getOwner() == player ? opponent : player;
            if (!defender.isAlive()) {
                continue;
            }
            if (effect.isHigh() && defender.isDucking()) {
                continue;
            }

            if (effect.getKind() == AreaEffect.Kind.GIANT_BOOK
                    || effect.getKind() == AreaEffect.Kind.EXPLOSION) {
                if (effect.getTarget() == defender) {
                    boolean blocked = defender.isBlocking()
                            && defender.isFacing(effect.getOwner());
                    effect.markHit();
                    combat.applyHit(effect.getOwner(), defender,
                            effect.getDamage(), blocked, effect.isSpecial());
                    continue;
                }
            }

            Rectangle2D hitbox = effect.getHitbox();
            if (hitbox != null && hitbox.intersects(defender.getHurtbox())) {
                boolean blocked = defender.isBlocking()
                        && defender.isFacing(effect.getOwner());
                effect.markHit();
                combat.applyHit(effect.getOwner(), defender,
                        effect.getDamage(), blocked, effect.isSpecial());
            }
        }
        effects.removeIf(effect -> !effect.isAlive());
    }

    private enum CastKind {
        SKILL, SPECIAL, BOOK
    }

    private static final class PendingCast {
        private final CastKind kind;
        private final Fighter attacker;
        private final Fighter defender;
        private final boolean right;
        private final double defenderX;
        private double remaining;

        private PendingCast(CastKind kind, Fighter attacker, Fighter defender,
                            boolean right, double defenderX, double remaining) {
            this.kind = kind;
            this.attacker = attacker;
            this.defender = defender;
            this.right = right;
            this.defenderX = defenderX;
            this.remaining = remaining;
        }
    }

    public void render(GraphicsContext gc) {
        for (AreaEffect effect : effects) {
            effect.render(gc);
        }
        for (Projectile projectile : projectiles) {
            projectile.render(gc);
        }
    }
}
