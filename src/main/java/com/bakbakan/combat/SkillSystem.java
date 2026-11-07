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

    public SkillSystem(CombatSystem combat) {
        this.combat = combat;
    }

    public void clear() {
        projectiles.clear();
        effects.clear();
    }

    public void castSkill(Fighter attacker, Fighter defender) {
        boolean right = defender.getX() >= attacker.getX();
        switch (attacker.getHero().id()) {
            case "rizal" -> projectiles.add(Projectile.ink(attacker, right));
            case "bonifacio" -> {
                double targetX = attacker.getX() + (right ? 220 : -220);
                effects.add(AreaEffect.flagDash(attacker, targetX));
            }
            case "luna" -> projectiles.add(Projectile.rifle(attacker, right));
            case "mabini" -> projectiles.add(Projectile.quill(attacker, right));
            case "lapulapu" -> projectiles.add(Projectile.kampilanWave(attacker, right));
            default -> {
            }
        }
    }

    public void castSpecial(Fighter attacker, Fighter defender) {
        boolean right = defender.getX() >= attacker.getX();
        switch (attacker.getHero().id()) {
            case "rizal" -> effects.add(AreaEffect.giantBook(attacker, defender.getX()));
            case "bonifacio" -> effects.add(AreaEffect.shockwave(attacker));
            case "luna" -> {
                double targetX = defender.getX();
                effects.add(AreaEffect.explosion(attacker, targetX - 60, 0.20));
                effects.add(AreaEffect.explosion(attacker, targetX, 0.42));
                effects.add(AreaEffect.explosion(attacker, targetX + 60, 0.64));
            }
            case "mabini" -> effects.add(AreaEffect.lightBeam(attacker, right));
            case "lapulapu" -> projectiles.add(Projectile.tidalWave(attacker, right));
            default -> {
            }
        }
    }

    public void castRizalBook(Fighter attacker, Fighter defender) {
        boolean right = defender.getX() >= attacker.getX();
        projectiles.add(Projectile.book(attacker, right));
    }

    public void update(double dt, Fighter player, Fighter opponent) {
        if (player == null || opponent == null) {
            return;
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

    public void render(GraphicsContext gc) {
        for (AreaEffect effect : effects) {
            effect.render(gc);
        }
        for (Projectile projectile : projectiles) {
            projectile.render(gc);
        }
    }
}
