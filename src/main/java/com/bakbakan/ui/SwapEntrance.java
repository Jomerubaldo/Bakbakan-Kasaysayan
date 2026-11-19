package com.bakbakan.ui;

import com.bakbakan.assets.SpriteProvider;
import com.bakbakan.combat.CombatConstants;
import com.bakbakan.entities.Fighter;
import com.bakbakan.entities.HeroData;
import com.bakbakan.util.GameConfig;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public final class SwapEntrance {

    private static final double IMPACT_AT = 0.38;
    private static final double DURATION = 0.95;

    private boolean active;
    private double time;
    private HeroData hero;
    private Fighter ghost;
    private double startX;
    private double startY;
    private double contactX;
    private double contactY;
    private double landX;
    private boolean feetRight = true;
    private boolean impactPending;
    private boolean impactDone;

    public void start(HeroData hero, Fighter ghost, double startX, double startY,
                      double contactX, double contactY, double landX,
                      boolean feetRight) {
        this.hero = hero;
        this.ghost = ghost;
        this.startX = startX;
        this.startY = startY;
        this.contactX = contactX;
        this.contactY = contactY;
        this.landX = landX;
        this.feetRight = feetRight;
        this.time = 0;
        this.active = true;
        this.impactPending = false;
        this.impactDone = false;
        if (ghost != null) {
            ghost.setFacingRight(feetRight);
            ghost.setPosition(startX, startY);
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isComplete() {
        return active && time >= DURATION;
    }

    public boolean consumeImpact() {
        if (!impactPending) {
            return false;
        }
        impactPending = false;
        return true;
    }

    public double getLandX() {
        return landX;
    }

    public void stop() {
        active = false;
        time = 0;
        hero = null;
        ghost = null;
        impactPending = false;
        impactDone = false;
    }

    public void update(double dt) {
        if (!active || ghost == null) {
            return;
        }
        time += dt;

        if (!impactDone && time >= IMPACT_AT) {
            impactDone = true;
            impactPending = true;
        }

        if (time < IMPACT_AT) {
            double u = clamp01(time / IMPACT_AT);
            double x = lerp(startX, contactX, easeOutCubic(u));
            double y = lerp(startY, contactY, u * u);
            ghost.setPosition(x, y);
        } else {
            double u = clamp01((time - IMPACT_AT) / (DURATION - IMPACT_AT));
            double x = lerp(contactX, landX, easeOutCubic(u));
            double y = lerp(contactY, CombatConstants.GROUND_Y, easeInQuad(u));
            ghost.setPosition(x, y);
        }
    }

    public void render(GraphicsContext gc, SpriteProvider sprites) {
        if (!active || ghost == null) {
            return;
        }
        Color accent = Color.web(hero.accentColor());
        drawShadow(gc);
        drawStreaks(gc, accent);
        double pivotY = ghost.getY() - CombatConstants.FIGHTER_HEIGHT * 0.5;
        gc.save();
        gc.translate(ghost.getX(), pivotY);
        gc.rotate(currentAngle());
        gc.translate(-ghost.getX(), -pivotY);
        sprites.render(gc, ghost, time);
        gc.restore();
        drawImpact(gc, accent);
        drawDust(gc, accent);
    }

    public void renderOverlay(GraphicsContext gc) {
        if (!active || hero == null) {
            return;
        }
        double alpha = Math.min(clamp01(time / 0.16),
                clamp01((DURATION - time) / 0.24));
        if (alpha <= 0) {
            return;
        }
        Color accent = Color.web(hero.accentColor());
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        gc.setFill(new Color(0.03, 0.05, 0.12, 0.7 * alpha));
        gc.fillText(hero.name(), GameConfig.WIDTH / 2.0 + 2, 207);
        gc.setFill(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), alpha));
        gc.fillText(hero.name(), GameConfig.WIDTH / 2.0, 205);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        gc.setFill(new Color(0.92, 0.94, 1.0, 0.85 * alpha));
        gc.fillText("TAKES THE FIELD!", GameConfig.WIDTH / 2.0, 230);
    }

    private double currentAngle() {
        if (time < IMPACT_AT) {
            double u = clamp01(time / IMPACT_AT);
            return (feetRight ? -1 : 1) * lerp(8, 62, easeInQuad(u));
        }
        double u = clamp01((time - IMPACT_AT) / (DURATION - IMPACT_AT));
        return (feetRight ? -1 : 1) * lerp(62, 0, easeOutCubic(u));
    }

    private void drawShadow(GraphicsContext gc) {
        double distance = Math.max(0, CombatConstants.GROUND_Y - ghost.getY());
        double scale = Math.max(0.32, 1 - distance / 760.0);
        gc.setFill(new Color(0, 0, 0, 0.26 * scale));
        gc.fillOval(ghost.getX() - 44 * scale, CombatConstants.GROUND_Y - 7,
                88 * scale, 15 * scale);
    }

    private void drawStreaks(GraphicsContext gc, Color accent) {
        double dir = feetRight ? 1 : -1;
        double phase = time < IMPACT_AT
                ? clamp01(time / IMPACT_AT)
                : clamp01(1 - (time - IMPACT_AT) / 0.35);
        if (phase <= 0) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            double alpha = (0.30 - i * 0.06) * phase;
            if (alpha <= 0) {
                continue;
            }
            gc.setStroke(new Color(accent.getRed(), accent.getGreen(),
                    accent.getBlue(), alpha));
            gc.setLineWidth(5 - i * 0.9);
            double offset = 40 + i * 30;
            gc.strokeLine(ghost.getX() - dir * offset,
                    ghost.getY() - 40 - i * 16,
                    ghost.getX() - dir * (offset + 70),
                    ghost.getY() - 100 - i * 22);
        }
    }

    private void drawImpact(GraphicsContext gc, Color accent) {
        if (time < IMPACT_AT) {
            return;
        }
        double p = clamp01((time - IMPACT_AT) / 0.5);
        if (p >= 1) {
            return;
        }
        double alpha = 1 - p;
        double cx = contactX;
        double cy = contactY - 55;
        double radius = 26 + p * 96;
        gc.setFill(new RadialGradient(0, 0, cx, cy, radius + 24, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, new Color(1, 1, 1, 0.9 * alpha)),
                new Stop(0.45, new Color(accent.getRed(), accent.getGreen(),
                        accent.getBlue(), 0.55 * alpha)),
                new Stop(1, new Color(accent.getRed(), accent.getGreen(),
                        accent.getBlue(), 0))));
        gc.fillOval(cx - radius - 24, cy - radius - 24,
                (radius + 24) * 2, (radius + 24) * 2);

        gc.setStroke(new Color(1, 1, 1, 0.85 * alpha));
        gc.setLineWidth(5 * alpha + 1);
        gc.strokeOval(cx - radius, cy - radius * 0.62,
                radius * 2, radius * 1.24);
        gc.setStroke(new Color(accent.getRed(), accent.getGreen(),
                accent.getBlue(), 0.7 * alpha));
        gc.setLineWidth(3 * alpha + 1);
        gc.strokeOval(cx - radius * 1.4, cy - radius * 0.9,
                radius * 2.8, radius * 1.8);

        gc.save();
        gc.translate(cx, cy);
        gc.setStroke(new Color(1, 1, 1, 0.8 * alpha));
        gc.setLineWidth(3);
        for (int i = 0; i < 6; i++) {
            gc.rotate(60);
            double inner = 18 + p * 34;
            double outer = inner + 26 + p * 40;
            gc.strokeLine(inner, 0, outer, 0);
        }
        gc.restore();
    }

    private void drawDust(GraphicsContext gc, Color accent) {
        if (time < IMPACT_AT + 0.2) {
            return;
        }
        double p = clamp01((time - IMPACT_AT - 0.2) / 0.35);
        if (p >= 1) {
            return;
        }
        double alpha = (1 - p) * 0.5;
        gc.setFill(new Color(0.86, 0.82, 0.72, alpha));
        for (int i = 0; i < 5; i++) {
            double spread = (i - 2) * (18 + p * 26);
            double size = 12 + p * 22;
            gc.fillOval(landX + spread - size / 2,
                    CombatConstants.GROUND_Y - size * 0.6,
                    size, size * 0.6);
        }
        gc.setStroke(new Color(accent.getRed(), accent.getGreen(),
                accent.getBlue(), alpha * 0.7));
        gc.setLineWidth(2);
        gc.strokeOval(landX - 44 - p * 20, CombatConstants.GROUND_Y - 8,
                88 + p * 40, 16);
    }

    private static double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double easeInQuad(double t) {
        return t * t;
    }

    private static double easeOutCubic(double t) {
        double inv = 1 - t;
        return 1 - inv * inv * inv;
    }
}
