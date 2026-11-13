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
        pending.add(new PendingCast(CastKind.SKILL, attacker, right,
                defender.getX(), skillDelay(attacker.getHero().id())));
    }

    public void castSpecial(Fighter attacker, Fighter defender) {
        boolean right = defender.getX() >= attacker.getX();
        pending.add(new PendingCast(CastKind.SPECIAL, attacker, right,
                defender.getX(), specialDelay(attacker.getHero().id())));
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
            case "final_boss" -> 0.36 * CombatConstants.SPECIAL_CAST_LOCK;
            default -> 0.16;
        };
    }

    private void spawnSkill(Fighter attacker, boolean right) {
        impacts.add(new Impact(attacker, false));
        switch (attacker.getHero().id()) {
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
            default -> {
            }
        }
    }

    private void spawnSpecial(Fighter attacker, boolean right, double targetX) {
        impacts.add(new Impact(attacker, true));
        switch (attacker.getHero().id()) {
            case "rizal" -> effects.add(AreaEffect.giantBook(attacker, targetX));
            case "bonifacio" -> effects.add(AreaEffect.shockwave(attacker));
            case "luna" -> {
                effects.add(AreaEffect.explosion(attacker, targetX - 60, 0.08));
                effects.add(AreaEffect.explosion(attacker, targetX, 0.22));
                effects.add(AreaEffect.explosion(attacker, targetX + 60, 0.36));
            }
            case "mabini" -> effects.add(AreaEffect.lightBeam(attacker, right));
            case "lapulapu" -> projectiles.add(Projectile.tidalWave(attacker, right));
            case "final_boss" -> effects.add(AreaEffect.firingSquad(attacker, right));
            default -> {
            }
        }
    }

    public void castRizalBook(Fighter attacker, Fighter defender) {
        boolean right = defender.getX() >= attacker.getX();
        pending.add(new PendingCast(CastKind.BOOK, attacker, right,
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
                case SPECIAL -> spawnSpecial(cast.attacker, cast.right, cast.defenderX);
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
                        projectile.getDamage(), blocked);
            }
        }
        projectiles.removeIf(projectile -> !projectile.isAlive());

        for (AreaEffect effect : List.copyOf(effects)) {
            effect.update(dt);
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

            Rectangle2D hitbox = effect.getHitbox();
            if (hitbox != null && hitbox.intersects(defender.getHurtbox())) {
                boolean blocked = defender.isBlocking()
                        && defender.isFacing(effect.getOwner());
                effect.markHit();
                combat.applyHit(effect.getOwner(), defender,
                        effect.getDamage(), blocked);
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
        private final boolean right;
        private final double defenderX;
        private double remaining;

        private PendingCast(CastKind kind, Fighter attacker, boolean right,
                            double defenderX, double remaining) {
            this.kind = kind;
            this.attacker = attacker;
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
