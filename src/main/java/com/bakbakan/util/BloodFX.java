package com.bakbakan.util;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;

public final class BloodFX {

    private static final Color BLOOD_DARK = Color.web("#43060b");
    private static final Color BLOOD = Color.web("#7d0a12");
    private static final Color BLOOD_BRIGHT = Color.web("#a81422");

    private BloodFX() {
    }

    public static void splatter(GraphicsContext gc, double x, double y,
                                double progress, double intensity,
                                double direction, double groundY) {
        double p = clamp01(progress);
        double power = clamp01(intensity);
        double fade = 1 - clamp01((p - 0.58) / 0.42);
        if (power <= 0 || fade <= 0) {
            return;
        }

        gc.save();
        double spread = (26 + p * 190) * (0.65 + power * 0.6);

        gc.setFill(dark(0.22 * fade * power));
        gc.fillOval(x - spread * 0.62, y - spread * 0.42,
                spread * 1.24, spread * 0.84);

        for (int i = 0; i < 9; i++) {
            double s = rnd(i, 201);
            double a = rnd(i, 202) * Math.PI * 2;
            double rr = spread * (0.10 + s * 0.35);
            double bx = x + Math.cos(a) * rr;
            double by = y + Math.sin(a) * rr * 0.65;
            double w = 6 + s * 26 + p * 20;
            gc.setFill(blood((0.42 + s * 0.28) * fade * power));
            gc.fillOval(bx - w / 2, by - w * 0.32, w, w * (0.55 + s * 0.45));
        }

        gc.setLineCap(StrokeLineCap.ROUND);
        for (int i = 0; i < 14; i++) {
            double s = rnd(i, 203);
            double a = rnd(i, 204) * Math.PI * 2;
            double bias = direction >= 0 ? 0.28 : -0.28;
            double ang = a + (s - 0.5) * bias;
            double start = 4 + rnd(i, 205) * 10;
            double len = (16 + s * 78) * (0.35 + p) * (0.7 + power);
            double sx = x + Math.cos(ang) * start;
            double sy = y + Math.sin(ang) * start * 0.7;
            double ex = x + Math.cos(ang) * (start + len);
            double ey = y + Math.sin(ang) * (start + len) * 0.7 + p * p * 26;
            gc.setStroke(blood((0.40 + s * 0.30) * fade * power));
            gc.setLineWidth(0.8 + s * 2.6);
            gc.strokeLine(sx, sy, ex, ey);
        }

        for (int i = 0; i < 34; i++) {
            double s = rnd(i, 206);
            double a = rnd(i, 207) * Math.PI * 2;
            double dist = (14 + s * 130) * (0.25 + p) * (0.7 + power);
            double px = x + Math.cos(a) * dist * (0.6 + s * 0.8);
            double py = y + Math.sin(a) * dist * 0.55 + p * p * (34 + s * 60);
            double size = 1.3 + s * 4.6;
            gc.setFill(blood((0.45 + s * 0.35) * fade * power));
            gc.fillOval(px, py, size, size * (1.05 + s * 0.9));
        }

        if (groundY > 0) {
            double poolW = (34 + p * 190) * (0.55 + power * 0.55);
            gc.setFill(dark(0.60 * fade * power));
            gc.fillOval(x - poolW / 2, groundY - 5, poolW, 8);
            for (int i = 0; i < 10; i++) {
                double s = rnd(i, 208);
                double px = x - poolW * 0.6 + s * poolW * 1.2;
                double py = groundY - 4 + rnd(i, 209) * 7;
                double rw = 3 + s * 14;
                gc.setFill(blood((0.30 + s * 0.35) * fade * power));
                gc.fillOval(px, py, rw, 2 + s * 4);
            }
        }
        gc.restore();
    }

    public static void ringSpray(GraphicsContext gc, double cx, double cy,
                                 double radius, double progress, double intensity) {
        double p = clamp01(progress);
        double fade = Math.max(0, 1 - p);
        double power = clamp01(intensity);
        if (power <= 0 || fade <= 0) {
            return;
        }

        gc.save();
        gc.setLineCap(StrokeLineCap.ROUND);
        for (int i = 0; i < 34; i++) {
            double s = rnd(i, 301);
            double a = rnd(i, 302) * Math.PI * 2;
            double r = radius * (0.76 + s * 0.36);
            double px = cx + Math.cos(a) * r;
            double py = cy + Math.sin(a) * r * 0.52 - p * 20;
            double size = (2 + s * 7) * (0.55 + p);
            gc.setFill(blood((0.35 + s * 0.35) * fade * power));
            gc.fillOval(px, py, size, size * (1.2 + s));
            if (i % 2 == 0) {
                gc.setStroke(blood((0.25 + s * 0.25) * fade * power));
                gc.setLineWidth(1 + s * 2);
                gc.strokeLine(px, py, px + Math.cos(a) * (8 + s * 18),
                        py + Math.sin(a) * (5 + s * 12));
            }
        }
        gc.restore();
    }

    public static void screenSplatter(GraphicsContext gc, double cx, double cy,
                                      double progress, double intensity) {
        double p = clamp01(progress);
        double fade = 1 - clamp01((p - 0.48) / 0.52);
        if (fade <= 0) {
            return;
        }

        splatter(gc, cx, cy, p * 0.9, intensity, 1, GameConfig.HEIGHT - 40);

        gc.save();
        gc.setFill(new RadialGradient(0, 0, cx, cy, GameConfig.WIDTH * 0.95,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, dark(0)),
                new Stop(0.55, dark(0)),
                new Stop(0.82, dark(0.20 * fade * intensity)),
                new Stop(1, dark(0.52 * fade * intensity))));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);

        for (int i = 0; i < 26; i++) {
            double s = rnd(i, 401);
            double x = rnd(i, 402) * GameConfig.WIDTH;
            double y = rnd(i, 403) * GameConfig.HEIGHT;
            double size = 1.5 + s * 5.5;
            gc.setFill(blood((0.18 + s * 0.30) * fade * intensity));
            gc.fillOval(x, y, size, size * (1.1 + s * 0.8));
        }
        gc.restore();
    }

    private static Color dark(double a) {
        return new Color(BLOOD_DARK.getRed(), BLOOD_DARK.getGreen(),
                BLOOD_DARK.getBlue(), clamp01(a));
    }

    private static Color blood(double a) {
        double t = clamp01(a);
        return new Color(
                BLOOD.getRed() + (BLOOD_BRIGHT.getRed() - BLOOD.getRed()) * t * 0.4,
                BLOOD.getGreen() + (BLOOD_BRIGHT.getGreen() - BLOOD.getGreen()) * t * 0.4,
                BLOOD.getBlue() + (BLOOD_BRIGHT.getBlue() - BLOOD.getBlue()) * t * 0.4,
                t);
    }

    private static double rnd(int index, int salt) {
        double v = Math.sin(index * 12.9898 + salt * 78.233) * 43758.5453;
        return v - Math.floor(v);
    }

    private static double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }
}
