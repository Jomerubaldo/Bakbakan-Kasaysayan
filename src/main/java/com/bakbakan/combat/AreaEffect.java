package com.bakbakan.combat;

import com.bakbakan.entities.Fighter;
import com.bakbakan.util.GameConfig;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public final class AreaEffect {

    public enum Kind {
        FLAG_DASH, GIANT_BOOK, SHOCKWAVE, EXPLOSION, LIGHT_BEAM, FIRING_SQUAD
    }

    private final Kind kind;
    private final Fighter owner;
    private final double damage;
    private final boolean high;
    private final double delay;
    private final double duration;
    private final double targetX;
    private final double direction;
    private final double dashStartX;
    private final double dashEndX;

    private double elapsed;
    private boolean hit;
    private boolean alive = true;

    private AreaEffect(Kind kind, Fighter owner, double damage, boolean high,
                       double delay, double duration, double targetX,
                       double direction, double dashStartX, double dashEndX) {
        this.kind = kind;
        this.owner = owner;
        this.damage = damage;
        this.high = high;
        this.delay = delay;
        this.duration = duration;
        this.targetX = targetX;
        this.direction = direction;
        this.dashStartX = dashStartX;
        this.dashEndX = dashEndX;
    }

    public static AreaEffect flagDash(Fighter owner, double targetX) {
        double end = Math.max(CombatConstants.MIN_X,
                Math.min(CombatConstants.MAX_X, targetX));
        return new AreaEffect(Kind.FLAG_DASH, owner,
                owner.scaleDamage(CombatConstants.SKILL_DAMAGE_FLAG_DASH), false,
                0, 0.28, end, end >= owner.getX() ? 1 : -1,
                owner.getX(), end);
    }

    public static AreaEffect giantBook(Fighter owner, double targetX) {
        double x = Math.max(CombatConstants.MIN_X + 80,
                Math.min(CombatConstants.MAX_X - 80, targetX));
        return new AreaEffect(Kind.GIANT_BOOK, owner,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_GIANT_BOOK), false,
                0.26, 0.35, x, 1, x, x);
    }

    public static AreaEffect shockwave(Fighter owner) {
        return new AreaEffect(Kind.SHOCKWAVE, owner,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_SHOCKWAVE), false,
                0.05, 0.55, owner.getX(), 1, owner.getX(), owner.getX());
    }

    public static AreaEffect explosion(Fighter owner, double targetX, double delay) {
        double x = Math.max(CombatConstants.MIN_X,
                Math.min(CombatConstants.MAX_X, targetX));
        return new AreaEffect(Kind.EXPLOSION, owner,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_ARTILLERY_HIT), false,
                delay, 0.3, x, 1, x, x);
    }

    public static AreaEffect lightBeam(Fighter owner, boolean facingRight) {
        return new AreaEffect(Kind.LIGHT_BEAM, owner,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_BEAM), true,
                0.05, 0.45, owner.getX(), facingRight ? 1 : -1,
                owner.getX(), owner.getX());
    }

    public static AreaEffect firingSquad(Fighter owner, boolean facingRight) {
        return new AreaEffect(Kind.FIRING_SQUAD, owner,
                owner.scaleDamage(CombatConstants.SPECIAL_DAMAGE_FIRING_SQUAD), true,
                0.15, 0.40, owner.getX(), facingRight ? 1 : -1,
                owner.getX(), owner.getX());
    }

    public void update(double dt) {
        elapsed += dt;

        if (kind == Kind.FLAG_DASH) {
            double progress = Math.min(1, elapsed / duration);
            double eased = 1 - Math.pow(1 - progress, 2);
            owner.setPosition(dashStartX + (dashEndX - dashStartX) * eased, owner.getY());
            if (elapsed >= duration) {
                alive = false;
            }
            return;
        }

        double extra = kind == Kind.GIANT_BOOK ? 0.2 : 0;
        if (elapsed >= delay + duration + extra) {
            alive = false;
        }
    }

    public boolean isActive() {
        if (kind == Kind.FLAG_DASH) {
            return elapsed <= duration;
        }
        return elapsed >= delay && elapsed <= delay + duration;
    }

    public Rectangle2D getHitbox() {
        if (!isActive()) {
            return null;
        }
        return switch (kind) {
            case FLAG_DASH -> new Rectangle2D(
                    owner.getX() - 40, owner.getY() - 120, 80, 120);
            case GIANT_BOOK -> new Rectangle2D(
                    targetX - 90, CombatConstants.GROUND_Y - 130, 180, 130);
            case SHOCKWAVE -> {
                double progress = Math.min(1, elapsed / duration);
                double radius = 50 + progress * 200;
                yield new Rectangle2D(targetX - radius,
                        CombatConstants.GROUND_Y - 110, radius * 2, 110);
            }
            case EXPLOSION -> new Rectangle2D(
                    targetX - 60, CombatConstants.GROUND_Y - 120, 120, 120);
            case LIGHT_BEAM -> {
                double start = direction > 0 ? targetX : 0;
                double width = direction > 0 ? GameConfig.WIDTH - targetX : targetX;
                yield new Rectangle2D(start, CombatConstants.GROUND_Y - 140, width, 40);
            }
            case FIRING_SQUAD -> {
                double start = direction > 0 ? targetX + 30 : 0;
                double width = direction > 0
                        ? GameConfig.WIDTH - targetX - 30
                        : targetX - 30;
                yield new Rectangle2D(start, CombatConstants.GROUND_Y - 95,
                        Math.max(0, width), 55);
            }
        };
    }

    public void render(GraphicsContext gc) {
        gc.save();
        switch (kind) {
            case FLAG_DASH -> drawFlagDash(gc);
            case GIANT_BOOK -> drawGiantBook(gc);
            case SHOCKWAVE -> drawShockwave(gc);
            case EXPLOSION -> drawExplosion(gc);
            case LIGHT_BEAM -> drawLightBeam(gc);
            case FIRING_SQUAD -> drawFiringSquad(gc);
        }
        gc.restore();
    }

    private void drawFlagDash(GraphicsContext gc) {
        Color accent = Color.web(owner.getHero().accentColor());
        double x = owner.getX();
        double y = owner.getY();

        gc.setStroke(Color.web("#f5f0e1", 0.35));
        gc.setLineWidth(3);
        for (int i = 0; i < 4; i++) {
            double offset = 40 + i * 18;
            gc.strokeLine(x - direction * offset, y - 150 + i * 26,
                    x - direction * (offset + 34), y - 150 + i * 26);
        }

        gc.setFill(Color.web("#5a3f2b"));
        gc.fillRoundRect(x - direction * 8 - 3, y - 150, 6, 120, 3, 3);

        gc.setFill(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.9));
        gc.fillPolygon(
                new double[]{x - direction * 8, x - direction * 8,
                        x - direction * 78, x - direction * 78},
                new double[]{y - 146, y - 92, y - 88, y - 142}, 4);

        gc.setFill(Color.web("#f4c542"));
        gc.fillOval(x - direction * 40 - 9, y - 126, 18, 18);
        gc.setStroke(Color.web("#f4c542"));
        gc.setLineWidth(2);
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * 2 * i / 8;
            gc.strokeLine(x - direction * 40,
                    y - 117,
                    x - direction * 40 + Math.cos(angle) * 12,
                    y - 117 + Math.sin(angle) * 12);
        }
    }

    private void drawGiantBook(GraphicsContext gc) {
        Color accent = Color.web(owner.getHero().accentColor());
        double progress = Math.min(1, elapsed / delay);
        double eased = progress * progress;
        double bookY = -190 + (CombatConstants.GROUND_Y - 90 + 190) * eased;
        double alpha = elapsed >= delay ? Math.max(0, 1 - (elapsed - delay) / duration) : 1;

        if (elapsed >= delay) {
            double impact = Math.min(1, (elapsed - delay) / duration);
            gc.setFill(Color.web("#f4c542", 0.25 * (1 - impact)));
            gc.fillOval(targetX - 150, CombatConstants.GROUND_Y - 40, 300, 70);
            gc.setStroke(Color.web("#f5f0e1", 0.5 * (1 - impact)));
            gc.setLineWidth(4);
            gc.strokeArc(targetX - 130, CombatConstants.GROUND_Y - 70,
                    260, 80, 200, 140, ArcType.OPEN);
        }

        gc.save();
        gc.translate(targetX, bookY);
        gc.setGlobalAlpha(alpha);
        gc.setFill(Color.web("#000000", 0.25));
        gc.fillRoundRect(-84, -54, 176, 116, 12, 12);
        gc.setFill(accent);
        gc.fillRoundRect(-88, -60, 176, 116, 12, 12);
        gc.setFill(Color.web("#f5f0e1"));
        gc.fillRoundRect(-76, -48, 152, 92, 6, 6);
        gc.setStroke(Color.web("#b8b0a0"));
        gc.setLineWidth(2);
        for (int i = 0; i < 4; i++) {
            gc.strokeLine(-62, -30 + i * 18, 62, -30 + i * 18);
        }
        gc.setFill(Color.web(owner.getHero().primaryColor()));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        gc.fillText("NOLI", 0, 8);
        gc.restore();
    }

    private void drawShockwave(GraphicsContext gc) {
        Color accent = Color.web(owner.getHero().accentColor());
        double progress = Math.min(1, elapsed / duration);
        double radius = 50 + progress * 200;
        double alpha = Math.max(0, 1 - progress);

        gc.setStroke(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), alpha));
        gc.setLineWidth(10);
        gc.strokeOval(targetX - radius, CombatConstants.GROUND_Y - radius * 0.55,
                radius * 2, radius * 1.1);
        gc.setStroke(Color.web("#f5f0e1", alpha * 0.8));
        gc.setLineWidth(3);
        gc.strokeOval(targetX - radius * 0.72,
                CombatConstants.GROUND_Y - radius * 0.4,
                radius * 1.44, radius * 0.8);

        gc.setFill(Color.web("#f4c542", alpha));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        gc.fillText("SIGAW!", targetX, CombatConstants.GROUND_Y - radius - 20);
    }

    private void drawExplosion(GraphicsContext gc) {
        if (elapsed < delay) {
            double fall = Math.max(0, Math.min(1, elapsed / delay));
            gc.setFill(Color.web("#f4c542", 0.7));
            gc.fillOval(targetX - 6,
                    CombatConstants.GROUND_Y - 260 * (1 - fall), 12, 22);
            return;
        }

        double progress = Math.min(1, (elapsed - delay) / duration);
        double radius = 24 + progress * 66;
        double alpha = Math.max(0, 1 - progress);

        gc.setFill(Color.web("#f4c542", alpha * 0.45));
        gc.fillOval(targetX - radius * 1.5,
                CombatConstants.GROUND_Y - radius * 1.2,
                radius * 3, radius * 2.4);
        gc.setFill(Color.web("#e67e22", alpha * 0.85));
        gc.fillOval(targetX - radius,
                CombatConstants.GROUND_Y - radius * 0.9,
                radius * 2, radius * 1.8);
        gc.setFill(Color.web("#fff2b0", alpha));
        gc.fillOval(targetX - radius * 0.5,
                CombatConstants.GROUND_Y - radius * 0.55,
                radius, radius);
    }

    private void drawLightBeam(GraphicsContext gc) {
        double progress = Math.min(1, elapsed / duration);
        double alpha = Math.max(0, 1 - Math.max(0, (progress - 0.6) / 0.4));
        double start = direction > 0 ? targetX : 0;
        double width = direction > 0 ? GameConfig.WIDTH - targetX : targetX;
        double y = CombatConstants.GROUND_Y - 140;

        gc.setFill(Color.web("#f4c542", 0.18 * alpha));
        gc.fillRect(start, y - 24, width, 88);
        gc.setFill(Color.web("#f4c542", 0.45 * alpha));
        gc.fillRect(start, y - 10, width, 60);
        gc.setFill(Color.web("#fff8d6", 0.9 * alpha));
        gc.fillRect(start, y, width, 40);

        gc.setStroke(Color.web(owner.getHero().accentColor(), alpha));
        gc.setLineWidth(3);
        for (double x = start + 20; x < start + width; x += 54) {
            gc.strokeOval(x, y + 6, 28, 28);
        }

        gc.setFill(Color.web("#2b1b12", alpha));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        gc.fillText("DECALOGUE", start + width / 2,
                CombatConstants.GROUND_Y - 200);
    }

    private void drawFiringSquad(GraphicsContext gc) {
        if (elapsed < delay) {
            double charge = Math.max(0, Math.min(1, elapsed / delay));
            gc.setFill(Color.web("#e74c3c", 0.5 * charge));
            gc.fillOval(targetX - 10, CombatConstants.GROUND_Y - 132, 20, 20);
            return;
        }

        double progress = Math.min(1, (elapsed - delay) / duration);
        double alpha = Math.max(0, 1 - progress);
        double muzzleX = targetX + direction * 34;
        double y = CombatConstants.GROUND_Y - 95;
        double endX = direction > 0 ? GameConfig.WIDTH : 0;

        gc.setFill(Color.web("#f4c542", 0.28 * alpha));
        gc.fillOval(muzzleX - 34, y - 30, 78, 60);

        for (int i = 0; i < 3; i++) {
            double offset = (i - 1) * 13;
            gc.setStroke(Color.web("#fff2b0", 0.85 * alpha));
            gc.setLineWidth(3 - i * 0.6);
            gc.strokeLine(muzzleX, y + offset, endX, y + offset);
            gc.setStroke(Color.web("#e74c3c", 0.35 * alpha));
            gc.setLineWidth(7 - i);
            gc.strokeLine(muzzleX, y + offset, endX, y + offset);
        }

        gc.setFill(Color.web("#e74c3c", 0.75 * alpha));
        gc.fillOval(muzzleX - 22, y - 22, 46, 46);
        gc.setFill(Color.web("#fff8d6", alpha));
        gc.fillOval(muzzleX - 12, y - 13, 28, 28);

        gc.setFill(Color.web("#f4c542", alpha));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        gc.fillText("SENTENCIA!", targetX + direction * 130, y - 86);
    }

    public boolean isHigh() {
        return high;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean hasHit() {
        return hit;
    }

    public void markHit() {
        hit = true;
    }

    public Fighter getOwner() {
        return owner;
    }

    public double getDamage() {
        return damage;
    }

    public Kind getKind() {
        return kind;
    }
}
