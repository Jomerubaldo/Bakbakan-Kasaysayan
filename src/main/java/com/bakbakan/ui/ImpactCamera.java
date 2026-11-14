package com.bakbakan.ui;

import com.bakbakan.util.BloodFX;
import com.bakbakan.util.GameConfig;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public final class ImpactCamera {

    private static final double WIDTH = GameConfig.WIDTH;
    private static final double HEIGHT = GameConfig.HEIGHT;
    private static final double SKILL_DURATION = 0.42;
    private static final double SPECIAL_DURATION = 1.05;

    private boolean active;
    private boolean special;
    private double timer;
    private double duration;
    private double focusX;
    private double focusY;
    private Color accent = Color.WHITE;

    public void trigger(double focusX, double focusY, boolean special, Color accent) {
        this.focusX = focusX;
        this.focusY = focusY;
        this.special = special;
        this.accent = accent == null ? Color.WHITE : accent;
        this.duration = special ? SPECIAL_DURATION : SKILL_DURATION;
        this.timer = 0;
        this.active = true;
    }

    public void update(double dt) {
        if (!active) {
            return;
        }
        timer += dt;
        if (timer >= duration) {
            active = false;
            timer = 0;
        }
    }

    public void stop() {
        active = false;
        timer = 0;
    }

    public boolean isActive() {
        return active;
    }

    public double timeScale() {
        if (!active) {
            return 1;
        }
        double p = clamp01(timer / duration);
        if (special) {
            double ramp = clamp01((p - 0.55) / 0.45);
            return 0.35 + 0.65 * easeOut(ramp);
        }
        double ramp = clamp01((p - 0.45) / 0.55);
        return 0.72 + 0.28 * easeOut(ramp);
    }

    public void applyWorldTransform(GraphicsContext gc) {
        if (!active) {
            return;
        }
        double p = clamp01(timer / duration);
        double strength = special ? 0.15 : 0.055;
        double zoom = 1 + strength * Math.sin(Math.PI * p);
        double shake = (special ? 9 : 3) * (1 - p);
        double sx = Math.sin(timer * 95) * shake;
        double sy = Math.cos(timer * 77) * shake;
        gc.translate(focusX + sx, focusY + sy);
        gc.scale(zoom, zoom);
        gc.translate(-focusX, -focusY);
    }

    public void renderOverlay(GraphicsContext gc) {
        if (!active) {
            return;
        }
        double p = clamp01(timer / duration);
        double fade = 1 - p;

        if (special) {
            BloodFX.screenSplatter(gc, focusX, focusY, p, 1.0);
        } else {
            double flashWindow = 0.07;
            double flash = Math.max(0, 1 - timer / flashWindow);
            if (flash > 0) {
                gc.setFill(alpha(Color.web("#9c0f1c"), 0.26 * flash));
                gc.fillRect(0, 0, WIDTH, HEIGHT);
            }
        }

        int lines = special ? 30 : 16;
        double spin = timer * (special ? 0.9 : 0.55);
        for (int i = 0; i < lines; i++) {
            double angle = spin + i * (Math.PI * 2 / lines);
            double inner = 130 + p * 120;
            double outer = 900;
            gc.setStroke(alpha(accent, (special ? 0.11 : 0.06) * fade));
            gc.setLineWidth(special ? 2.6 : 1.6);
            gc.strokeLine(focusX + Math.cos(angle) * inner,
                    focusY + Math.sin(angle) * inner,
                    focusX + Math.cos(angle) * outer,
                    focusY + Math.sin(angle) * outer);
        }

        if (!special && p < 0.55) {
            double r = 30 + p * 460;
            double a = (1 - p / 0.55) * 0.30;
            gc.setStroke(alpha(accent, a));
            gc.setLineWidth(3);
            gc.strokeOval(focusX - r, focusY - r * 0.62, r * 2, r * 1.24);
        }

        if (special) {
            double bar = 22 * Math.sin(Math.PI * p);
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, WIDTH, bar);
            gc.fillRect(0, HEIGHT - bar, WIDTH, bar);
            gc.setFill(alpha(accent, 0.7 * fade));
            gc.fillRect(0, bar, WIDTH, 2);
            gc.fillRect(0, HEIGHT - bar - 2, WIDTH, 2);
        }

        gc.setFill(new RadialGradient(0, 0, WIDTH / 2.0, HEIGHT / 2.0, WIDTH * 0.70,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, alpha(Color.BLACK, 0)),
                new Stop(0.68, alpha(Color.BLACK, 0)),
                new Stop(1, alpha(Color.BLACK, 0.42 * fade))));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private static double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private static double easeOut(double t) {
        double v = clamp01(t);
        return 1 - (1 - v) * (1 - v);
    }

    private static Color alpha(Color color, double a) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                clamp01(a));
    }
}
