package com.bakbakan.ui;

import com.bakbakan.assets.SpriteProvider;
import com.bakbakan.entities.HeroData;
import com.bakbakan.util.GameConfig;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public final class SpecialCinematic {

    private static final double WIDTH = GameConfig.WIDTH;
    private static final double HEIGHT = GameConfig.HEIGHT;
    private static final double DURATION = 2.9;
    private static final double OUTRO = 0.5;
    private static final double BAR = 64;

    private HeroData hero;
    private double time;
    private boolean active;

    public void start(HeroData hero) {
        this.hero = hero;
        this.time = 0;
        this.active = hero != null;
    }

    public void update(double dt) {
        if (!active) {
            return;
        }
        time += dt;
        if (time >= DURATION) {
            active = false;
            time = 0;
        }
    }

    public boolean isActive() {
        return active;
    }

    public void skip() {
        active = false;
        time = 0;
    }

    public void stop() {
        active = false;
        time = 0;
        hero = null;
    }

    public void render(GraphicsContext gc, SpriteProvider sprites) {
        if (!active || hero == null) {
            return;
        }

        double intro = easeOut(clamp01(time / 0.28));
        double outro = clamp01((time - (DURATION - OUTRO)) / OUTRO);
        double presence = 1 - easeOut(outro);
        double alpha = clamp01(time / 0.10) * presence;

        Color accent = Color.web(hero.accentColor());
        Color primary = Color.web(hero.primaryColor());
        boolean heroLeft = heroOnLeft(hero.id());
        double heroX = heroLeft ? 310 : 1000;

        gc.save();
        gc.setGlobalAlpha(alpha);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.BASELINE);
        gc.setLineCap(StrokeLineCap.ROUND);

        drawBackdrop(gc, accent, primary, heroX);
        drawThemeBackdrop(gc, accent, primary, heroX, presence);
        drawHeroAura(gc, accent, heroX);
        drawHeroShadow(gc, heroX);
        sprites.renderPortrait(gc, hero, heroX, 686 + Math.sin(time * 2.4) * 5,
                2.9, heroLeft);
        drawThemeForeground(gc, accent, primary, heroX, presence);
        drawVignette(gc);
        drawTitles(gc, accent, heroLeft, intro, presence);
        drawLetterbox(gc, accent, intro, outro);
        drawFlash(gc, accent, outro);

        gc.setGlobalAlpha(1);
        gc.restore();
    }

    private void drawBackdrop(GraphicsContext gc, Color accent, Color primary, double heroX) {
        Color deep = mix(primary, Color.BLACK, 0.82);
        Color mid = mix(primary, Color.BLACK, 0.44);
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, deep),
                new Stop(0.55, mid),
                new Stop(1, mix(accent, Color.BLACK, 0.72))));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setFill(new RadialGradient(0, 0, heroX, 430, 540, false, CycleMethod.NO_CYCLE,
                new Stop(0, alpha(accent, 0.30)),
                new Stop(0.45, alpha(primary, 0.14)),
                new Stop(1, alpha(Color.BLACK, 0))));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        double cx = heroX;
        double cy = 430;
        int rays = 18;
        for (int i = 0; i < rays; i++) {
            double a = time * 0.35 + i * (Math.PI * 2 / rays);
            double x2 = cx + Math.cos(a) * 760;
            double y2 = cy + Math.sin(a) * 760;
            gc.setStroke(alpha(accent, i % 2 == 0 ? 0.07 : 0.035));
            gc.setLineWidth(i % 2 == 0 ? 3 : 1.5);
            gc.strokeLine(cx, cy, x2, y2);
        }

        for (int i = 0; i < 26; i++) {
            double s = rnd(i, 11);
            double y = HEIGHT * s;
            double speed = 260 + s * 420;
            double x = ((time * speed + s * WIDTH * 1.8) % (WIDTH + 400)) - 200;
            gc.setStroke(alpha(Color.WHITE, 0.04 + 0.05 * s));
            gc.setLineWidth(1 + s * 2);
            gc.strokeLine(x, y, x + 130, y - 60);
        }
    }

    private void drawThemeBackdrop(GraphicsContext gc, Color accent, Color primary,
                                   double heroX, double presence) {
        switch (hero.id()) {
            case "rizal" -> drawRizalBackdrop(gc, accent, primary, heroX, presence);
            case "bonifacio" -> drawBonifacioBackdrop(gc, accent, heroX, presence);
            case "luna" -> drawLunaBackdrop(gc, accent, presence);
            case "mabini" -> drawMabiniBackdrop(gc, accent, presence);
            case "lapulapu" -> drawLapulapuBackdrop(gc, accent, presence);
            default -> {
            }
        }
    }

    private void drawRizalBackdrop(GraphicsContext gc, Color accent, Color primary,
                                   double heroX, double presence) {
        double rise = easeOut(clamp01((time - 0.05) / 1.0));
        gc.save();
        gc.translate(500, 640 + (1 - rise) * 240);
        gc.rotate(-7);
        gc.setFill(alpha(mix(primary, Color.BLACK, 0.25), 0.50));
        gc.fillRoundRect(-210, -260, 420, 330, 16, 16);
        gc.setFill(alpha(Color.web("#f3e7cd"), 0.22));
        gc.fillRect(-188, -240, 376, 292);
        gc.setFill(alpha(Color.BLACK, 0.35));
        gc.fillRect(-7, -240, 14, 292);
        gc.setStroke(alpha(Color.web("#e9d8b5"), 0.20));
        gc.setLineWidth(1.5);
        for (int i = 0; i < 7; i++) {
            double y = -210 + i * 34;
            gc.strokeLine(-160, y, -28, y);
            gc.strokeLine(28, y, 160, y);
        }
        gc.restore();

        for (int i = 0; i < 16; i++) {
            double a = rnd(i, 2) * Math.PI * 2 + time * (0.35 + rnd(i, 3) * 0.6);
            double rad = 170 + rnd(i, 4) * 300;
            double px = heroX + Math.cos(a) * rad;
            double py = 410 + Math.sin(a) * rad * 0.55 - time * 18;
            double rot = Math.sin(time * 1.3 + i) * 0.55 + rnd(i, 5) * 0.5;
            double size = 0.65 + rnd(i, 6) * 0.75;
            drawPage(gc, px, py, size, rot, 0.55 * presence);
        }

        for (int i = 0; i < 4; i++) {
            gc.setStroke(alpha(Color.web("#12101a"), 0.30 * presence));
            gc.setLineWidth(2.5 + i);
            gc.strokeArc(heroX - 180 - i * 14, 300 + i * 14, 360 + i * 34,
                    280 + i * 26, 30 + time * 24 + i * 40, 220, ArcType.OPEN);
        }

        for (int i = 0; i < 22; i++) {
            double s = rnd(i, 8);
            double px = heroX - 300 + s * 600;
            double py = 200 + ((s * 900 + time * 60) % 460);
            gc.setFill(alpha(Color.web("#0b0a12"), 0.35 * presence));
            gc.fillOval(px, py, 3 + s * 5, 3 + s * 5);
        }

        for (int i = 0; i < 3; i++) {
            double p = clamp01((time - 0.6 - i * 0.18) / 0.5);
            if (p <= 0) {
                continue;
            }
            gc.setStroke(alpha(accent, 0.55 * presence * p));
            gc.setLineWidth(2.5);
            gc.beginPath();
            gc.moveTo(heroX - 250, 470 + i * 40);
            gc.bezierCurveTo(heroX - 120, 380 + i * 30, heroX + 80, 520 + i * 20,
                    heroX + 240, 420 + i * 40);
            gc.stroke();
        }
    }

    private void drawBonifacioBackdrop(GraphicsContext gc, Color accent,
                                       double heroX, double presence) {
        double sunX = heroX;
        double sunY = 330;
        gc.setFill(alpha(accent, 0.16 * presence));
        gc.fillOval(sunX - 150, sunY - 150, 300, 300);
        for (int i = 0; i < 8; i++) {
            double a = time * 0.25 + i * Math.PI / 4;
            double x1 = sunX + Math.cos(a - 0.10) * 70;
            double y1 = sunY + Math.sin(a - 0.10) * 70;
            double x2 = sunX + Math.cos(a) * 168;
            double y2 = sunY + Math.sin(a) * 168;
            double x3 = sunX + Math.cos(a + 0.10) * 70;
            double y3 = sunY + Math.sin(a + 0.10) * 70;
            gc.setFill(alpha(accent, 0.35 * presence));
            gc.fillPolygon(new double[]{x1, x2, x3}, new double[]{y1, y2, y3}, 3);
        }
        gc.setFill(alpha(accent, 0.55 * presence));
        gc.fillOval(sunX - 58, sunY - 58, 116, 116);
        gc.setFill(alpha(Color.web("#7a1f14"), 0.5 * presence));
        gc.fillOval(sunX - 46, sunY - 46, 92, 92);

        double poleX = 1040;
        gc.setStroke(alpha(Color.web("#2b1b12"), 0.8 * presence));
        gc.setLineWidth(7);
        gc.strokeLine(poleX, 150, poleX, 640);
        int seg = 12;
        double[] xs = new double[(seg + 1) * 2];
        double[] ys = new double[(seg + 1) * 2];
        for (int i = 0; i <= seg; i++) {
            double f = i / (double) seg;
            double wave = Math.sin(time * 3.0 + f * 6.0) * 16 * f;
            xs[i] = poleX + f * 250;
            ys[i] = 170 + wave + f * 10;
            int j = seg + 1 + (seg - i);
            xs[j] = poleX + f * 250;
            ys[j] = 340 + wave + f * 10;
        }
        gc.setFill(alpha(Color.web("#b02a1e"), 0.55 * presence));
        gc.fillPolygon(xs, ys, xs.length);
        gc.setFill(alpha(accent, 0.7 * presence));
        gc.fillOval(poleX + 95, 235, 44, 44);

        for (int i = 0; i < 7; i++) {
            double s = rnd(i, 21);
            double fx = 140 + s * 1000;
            double fh = 90 + s * 160;
            double sway = Math.sin(time * 4 + i) * 12;
            gc.setFill(alpha(mix(Color.web("#ff8a2b"), accent, s), 0.16 * presence));
            gc.fillPolygon(new double[]{fx - 40, fx, fx + 40, fx + sway, fx},
                    new double[]{660, 660 - fh, 660, 620 - fh * 0.35, 660}, 5);
        }
        for (int i = 0; i < 34; i++) {
            double s = rnd(i, 22);
            double px = 120 + s * 1040;
            double py = 650 - ((time * (90 + s * 160) + s * 600) % 520);
            double a = clamp01((py - 140) / 480) * 0.9 * presence;
            gc.setFill(alpha(mix(accent, Color.web("#ffdd88"), s), a));
            gc.fillOval(px, py, 2 + s * 4, 5 + s * 9);
        }

        double p = clamp01((time - 1.45) / 0.35);
        if (p > 0 && p < 1) {
            gc.setStroke(alpha(Color.WHITE, 0.75 * (1 - p) * presence));
            gc.setLineWidth(10);
            gc.strokeArc(120, 120, 1040, 560, 20, 80, ArcType.OPEN);
            gc.setStroke(alpha(accent, 0.65 * (1 - p) * presence));
            gc.setLineWidth(5);
            gc.strokeArc(160, 160, 960, 480, 200, 70, ArcType.OPEN);
        }
    }

    private void drawLunaBackdrop(GraphicsContext gc, Color accent, double presence) {
        for (int i = 0; i < 54; i++) {
            double s = rnd(i, 31);
            double twinkle = 0.35 + 0.65 * Math.abs(Math.sin(time * 2 + i));
            gc.setFill(alpha(Color.web("#e8f0ff"), s * 0.5 * twinkle * presence));
            double size = 1 + rnd(i, 32) * 2.2;
            gc.fillOval(rnd(i, 33) * WIDTH, rnd(i, 34) * 420, size, size);
        }

        double mx = 1000;
        double my = 160;
        gc.setFill(new RadialGradient(0, 0, mx, my, 200, false, CycleMethod.NO_CYCLE,
                new Stop(0, alpha(Color.web("#fff3d6"), 0.35 * presence)),
                new Stop(1, alpha(Color.web("#fff3d6"), 0))));
        gc.fillOval(mx - 200, my - 200, 400, 400);
        gc.setFill(alpha(Color.web("#f7e7c3"), 0.82 * presence));
        gc.fillOval(mx - 86, my - 86, 172, 172);
        gc.setFill(alpha(Color.web("#d9c49a"), 0.35 * presence));
        gc.fillOval(mx - 50, my - 34, 26, 26);
        gc.fillOval(mx + 14, my + 18, 18, 18);
        gc.fillOval(mx + 30, my - 52, 12, 12);

        for (int i = 0; i < 5; i++) {
            double s = rnd(i, 35);
            double y = 130 + s * 210;
            double x = ((time * (12 + s * 20) + s * WIDTH) % (WIDTH + 500)) - 250;
            gc.setFill(alpha(Color.web("#1a2340"), 0.30 * presence));
            gc.fillOval(x, y, 260 + s * 220, 34 + s * 26);
        }

        double reticleX = 320;
        double reticleY = 390;
        gc.setStroke(alpha(Color.web("#d9e2f2"), 0.22 * presence));
        gc.setLineWidth(1.5);
        for (int i = 0; i < 3; i++) {
            double r = 70 + i * 46;
            gc.strokeOval(reticleX - r, reticleY - r, r * 2, r * 2);
        }
        for (int i = 0; i < 8; i++) {
            double a = time * 0.2 + i * Math.PI / 4;
            gc.strokeLine(reticleX + Math.cos(a) * 118, reticleY + Math.sin(a) * 118,
                    reticleX + Math.cos(a) * 140, reticleY + Math.sin(a) * 140);
        }

        for (int k = 0; k < 3; k++) {
            double p = clamp01((time - 0.55 - k * 0.18) / 0.85);
            if (p <= 0) {
                continue;
            }
            double sx = 120 + k * 70;
            double sy = 610;
            double cx = 560 + k * 90;
            double cy = 20;
            double ex = 1040 - k * 60;
            double ey = 560;
            gc.setStroke(alpha(Color.web("#ffd27a"), 0.55 * presence));
            gc.setLineWidth(2.4);
            double prevX = sx;
            double prevY = sy;
            int steps = 48;
            for (int i = 1; i <= steps * p; i++) {
                double f = i / (double) steps;
                double g = 1 - f;
                double x = g * g * sx + 2 * g * f * cx + f * f * ex;
                double y = g * g * sy + 2 * g * f * cy + f * f * ey;
                if (i % 3 != 0) {
                    gc.strokeLine(prevX, prevY, x, y);
                }
                prevX = x;
                prevY = y;
            }
        }
    }

    private void drawMabiniBackdrop(GraphicsContext gc, Color accent, double presence) {
        for (int i = 0; i < 7; i++) {
            double s = rnd(i, 41);
            double x = 80 + i * 180 + Math.sin(time * 0.8 + i) * 18;
            double w = 60 + s * 90;
            gc.setFill(alpha(mix(accent, Color.WHITE, 0.35), 0.05 * presence));
            gc.fillPolygon(new double[]{x, x + w, x + w + 150, x - 40},
                    new double[]{0, 0, HEIGHT, HEIGHT}, 4);
        }

        for (int i = 0; i < 3; i++) {
            double x = 160 + i * 470;
            gc.setFill(alpha(Color.web("#f0e68c"), 0.07 * presence));
            gc.fillRoundRect(x, 210, 150, 300, 14, 14);
            gc.setStroke(alpha(accent, 0.20 * presence));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(x, 210, 150, 300, 14, 14);
            for (int j = 0; j < 7; j++) {
                gc.strokeLine(x + 22, 250 + j * 32, x + 128, 250 + j * 32);
            }
        }

        for (int i = 0; i < 10; i++) {
            double s = rnd(i, 42);
            double px = 120 + s * 1000;
            double py = 210 + rnd(i, 43) * 380 + Math.sin(time * 1.2 + i) * 12;
            double rot = Math.sin(time * 0.9 + i) * 0.25;
            drawDocument(gc, px, py, 0.7 + s * 0.6, rot, 0.30 * presence);
        }

        double p = clamp01((time - 0.7) / 1.1);
        if (p > 0) {
            gc.setStroke(alpha(accent, 0.55 * presence));
            gc.setLineWidth(3);
            gc.beginPath();
            int n = 40;
            int last = (int) (n * p);
            for (int i = 0; i <= last; i++) {
                double f = i / (double) n;
                double x = 180 + f * 430;
                double y = 470 + Math.sin(f * 10 + time * 2) * 18;
                if (i == 0) {
                    gc.moveTo(x, y);
                } else {
                    gc.lineTo(x, y);
                }
            }
            gc.stroke();
        }

        gc.save();
        gc.translate(720, 300);
        gc.rotate(-38 + Math.sin(time * 1.6) * 4);
        gc.setStroke(alpha(Color.web("#f6ecd6"), 0.85 * presence));
        gc.setLineWidth(3);
        gc.strokeLine(0, -10, 0, 170);
        gc.setFill(alpha(mix(accent, Color.WHITE, 0.35), 0.55 * presence));
        gc.fillOval(-26, -150, 52, 120);
        gc.setStroke(alpha(Color.web("#fff6da"), 0.35 * presence));
        gc.setLineWidth(1.2);
        for (int i = 0; i < 6; i++) {
            gc.strokeLine(-18 + i * 7, -140 + i * 18, -6 + i * 4, -60 + i * 10);
        }
        gc.setFill(alpha(Color.web("#d8b25a"), 0.9 * presence));
        gc.fillPolygon(new double[]{-5, 5, 0}, new double[]{165, 165, 196}, 3);
        gc.restore();
    }

    private void drawLapulapuBackdrop(GraphicsContext gc, Color accent, double presence) {
        double mx = 1150;
        double my = 150;
        gc.setFill(new RadialGradient(0, 0, mx, my, 160, false, CycleMethod.NO_CYCLE,
                new Stop(0, alpha(Color.web("#cfe4f2"), 0.28 * presence)),
                new Stop(1, alpha(Color.web("#cfe4f2"), 0))));
        gc.fillOval(mx - 160, my - 160, 320, 320);
        gc.setFill(alpha(Color.web("#dceaf5"), 0.75 * presence));
        gc.fillOval(mx - 66, my - 66, 132, 132);
        gc.setFill(alpha(Color.web("#a8bfd0"), 0.30 * presence));
        gc.fillOval(mx - 26, my - 20, 22, 22);
        gc.fillOval(mx + 22, my + 22, 14, 14);

        for (int i = 0; i < 6; i++) {
            double s = rnd(i, 51);
            double y = 90 + s * 190;
            double x = ((time * (10 + s * 18) + s * WIDTH) % (WIDTH + 600)) - 300;
            gc.setFill(alpha(Color.web("#101a26"), 0.42 * presence));
            gc.fillOval(x, y, 300 + s * 260, 40 + s * 34);
        }

        double flash = Math.max(0, 1 - Math.abs(time - 1.15) / 0.12);
        if (flash > 0) {
            gc.setStroke(alpha(Color.web("#eaf6ff"), 0.85 * flash * presence));
            gc.setLineWidth(4);
            gc.strokePolyline(new double[]{900, 860, 878, 820, 840, 780},
                    new double[]{0, 90, 96, 190, 198, 300}, 6);
            gc.setStroke(alpha(Color.web("#9fd8ff"), 0.45 * flash * presence));
            gc.setLineWidth(2);
            gc.strokePolyline(new double[]{880, 840, 852, 800},
                    new double[]{20, 110, 116, 210}, 4);
        }

        drawWave(gc, 590, 18, 0.012, 1.1, Color.web("#1b4a63"), 0.55 * presence);
        drawWave(gc, 625, 22, 0.010, 1.4, Color.web("#176b7a"), 0.60 * presence);
        drawWave(gc, 660, 26, 0.009, 1.8, Color.web("#0f8a8a"), 0.65 * presence);
        drawWave(gc, 695, 30, 0.008, 2.2, Color.web("#0b5f74"), 0.72 * presence);

        for (int i = 0; i < 30; i++) {
            double s = rnd(i, 52);
            double px = 60 + s * 1160;
            double py = 560 - ((time * (60 + s * 110) + s * 400) % 260);
            double a = clamp01((py - 320) / 260) * 0.5 * presence;
            gc.setFill(alpha(Color.web("#eaf8ff"), a));
            gc.fillOval(px, py, 2 + s * 3, 2 + s * 3);
        }

        double p = clamp01((time - 0.9) / 0.5);
        if (p > 0) {
            gc.save();
            gc.translate(430, 360);
            gc.rotate(-32);
            gc.setFill(alpha(Color.web("#d9e6ef"), 0.18 * p * presence));
            gc.fillPolygon(new double[]{0, 26, 10, -40, -60},
                    new double[]{-180, 20, 160, 120, -140}, 5);
            gc.setStroke(alpha(Color.WHITE, 0.5 * p * presence));
            gc.setLineWidth(2);
            gc.strokeLine(-52, -130, 18, 140);
            gc.restore();
        }
    }

    private void drawThemeForeground(GraphicsContext gc, Color accent, Color primary,
                                     double heroX, double presence) {
        switch (hero.id()) {
            case "rizal" -> {
                for (int i = 0; i < 3; i++) {
                    double s = rnd(i, 61);
                    double px = 120 + s * 1040;
                    double py = 300 + rnd(i, 62) * 340 + Math.sin(time * 1.4 + i) * 16;
                    drawPage(gc, px, py, 1.3 + s * 0.9,
                            Math.sin(time + i) * 0.6, 0.32 * presence);
                }
            }
            case "bonifacio" -> {
                for (int i = 0; i < 12; i++) {
                    double s = rnd(i, 63);
                    double px = 160 + s * 960;
                    double py = 620 - ((time * (240 + s * 220) + s * 500) % 500);
                    gc.setStroke(alpha(Color.web("#ffe6a8"), 0.35 * presence));
                    gc.setLineWidth(1.5);
                    gc.strokeLine(px, py, px - 8 - s * 10, py - 26 - s * 30);
                }
            }
            case "luna" -> {
                for (int k = 0; k < 3; k++) {
                    double impact = 0.55 + k * 0.18 + 0.85;
                    if (time >= impact) {
                        double e = clamp01((time - impact) / 0.55);
                        drawExplosion(gc, accent, 1040 - k * 60, 560, e, presence);
                    }
                }
            }
            case "mabini" -> {
                for (int i = 0; i < 26; i++) {
                    double s = rnd(i, 64);
                    double px = 180 + s * 460 + Math.sin(time * 2 + i) * 30;
                    double py = 380 + rnd(i, 65) * 240 + Math.cos(time * 1.7 + i) * 24;
                    double a = (0.35 + 0.65 * Math.abs(Math.sin(time * 3 + i)))
                            * 0.6 * presence;
                    gc.setFill(alpha(mix(accent, Color.WHITE, 0.5), a));
                    gc.fillOval(px, py, 2 + s * 3, 2 + s * 3);
                }
            }
            case "lapulapu" -> {
                for (int i = 0; i < 16; i++) {
                    double s = rnd(i, 66);
                    double px = heroX - 170 + s * 340;
                    double py = 650 - Math.abs(Math.sin(time * 3 + i)) * 50;
                    gc.setFill(alpha(Color.web("#eaf8ff"), 0.45 * presence));
                    gc.fillOval(px, py, 3 + s * 4, 3 + s * 4);
                }
            }
            default -> {
            }
        }
    }

    private void drawHeroAura(GraphicsContext gc, Color accent, double heroX) {
        double pulse = 0.5 + 0.5 * Math.sin(time * 4.0);
        gc.setFill(new RadialGradient(0, 0, heroX, 470, 260, false, CycleMethod.NO_CYCLE,
                new Stop(0, alpha(accent, 0.30 + 0.10 * pulse)),
                new Stop(0.6, alpha(accent, 0.08)),
                new Stop(1, alpha(accent, 0))));
        gc.fillOval(heroX - 260, 210, 520, 520);

        for (int i = 0; i < 14; i++) {
            double s = rnd(i, 71);
            double x = heroX - 120 + s * 240;
            double y = 650 - ((time * (60 + s * 110) + s * 400) % 430);
            gc.setFill(alpha(accent, 0.14 * (0.4 + s * 0.6)));
            gc.fillRect(x, y, 2, 60 + s * 150);
        }

        gc.setStroke(alpha(accent, 0.35));
        gc.setLineWidth(3);
        gc.strokeOval(heroX - 150, 660, 300, 60);
    }

    private void drawHeroShadow(GraphicsContext gc, double heroX) {
        gc.setFill(alpha(Color.BLACK, 0.35));
        gc.fillOval(heroX - 110, 674, 220, 26);
    }

    private void drawTitles(GraphicsContext gc, Color accent, boolean heroLeft,
                            double intro, double presence) {
        double cx = (heroLeft ? 860 : 420) + (1 - easeOut(intro)) * (heroLeft ? 110 : -110);
        double a = presence * clamp01((time - 0.05) / 0.25);
        String[] lines = titleLines(hero.id());

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        gc.setFill(alpha(accent, 0.85 * a));
        gc.fillText(spaced("SPECIAL MOVE"), cx, 168);

        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        gc.setFill(alpha(Color.WHITE, 0.75 * a));
        gc.fillText(spaced(hero.name().toUpperCase()), cx, 196);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 58));
        double y = 270;
        for (String line : lines) {
            gc.setFill(alpha(Color.BLACK, 0.55 * a));
            gc.fillText(line, cx + 3, y + 3);
            gc.setFill(alpha(accent, a));
            gc.fillText(line, cx, y);
            gc.setStroke(alpha(Color.WHITE, 0.25 * a));
            gc.setLineWidth(1.2);
            gc.strokeText(line, cx, y);
            y += 64;
        }

        gc.setFill(alpha(accent, 0.8 * a));
        gc.fillRect(cx - 130, y - 22, 260, 3);

        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 19));
        gc.setFill(alpha(Color.WHITE, 0.8 * a));
        gc.fillText(tagline(hero.id()), cx, y + 24);
    }

    private void drawLetterbox(GraphicsContext gc, Color accent, double intro, double outro) {
        double h = BAR * intro * (1 - easeOut(outro));
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, h);
        gc.fillRect(0, HEIGHT - h, WIDTH, h);
        gc.setFill(alpha(accent, 0.75));
        gc.fillRect(0, h - 2, WIDTH, 2);
        gc.fillRect(0, HEIGHT - h, WIDTH, 2);

        if (time > 0.8 && outro < 0.5) {
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.setFill(alpha(Color.WHITE, 0.5));
            gc.fillText("ESC / ENTER TO SKIP", WIDTH - 30, HEIGHT - h - 12);
        }
    }

    private void drawVignette(GraphicsContext gc) {
        gc.setFill(new RadialGradient(0, 0, WIDTH / 2.0, HEIGHT / 2.0, WIDTH * 0.72,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, alpha(Color.BLACK, 0)),
                new Stop(0.65, alpha(Color.BLACK, 0)),
                new Stop(1, alpha(Color.BLACK, 0.72))));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawFlash(GraphicsContext gc, Color accent, double outro) {
        double start = clamp01((0.16 - time) / 0.16);
        if (start > 0) {
            gc.setFill(alpha(Color.WHITE, 0.55 * start));
            gc.fillRect(0, 0, WIDTH, HEIGHT);
        }
        double pulse = Math.max(0, 1 - Math.abs(time - 1.5) / 0.12);
        if (pulse > 0) {
            gc.setFill(alpha(accent, 0.18 * pulse));
            gc.fillRect(0, 0, WIDTH, HEIGHT);
        }
        double end = easeOut(outro);
        if (end > 0) {
            gc.setFill(alpha(Color.WHITE, 0.25 * end));
            gc.fillRect(0, 0, WIDTH, HEIGHT);
        }
    }

    private void drawPage(GraphicsContext gc, double x, double y, double scale,
                          double rotation, double a) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(rotation));
        gc.scale(scale, scale);
        gc.setFill(alpha(Color.web("#f6ecd6"), a));
        gc.fillRect(-18, -24, 36, 48);
        gc.setStroke(alpha(Color.web("#5a4630"), a * 0.7));
        gc.setLineWidth(1.2);
        gc.strokeRect(-18, -24, 36, 48);
        gc.setStroke(alpha(Color.web("#2a2118"), a * 0.55));
        for (int i = 0; i < 4; i++) {
            gc.strokeLine(-11, -12 + i * 9, 11, -12 + i * 9);
        }
        gc.setFill(alpha(Color.BLACK, a * 0.12));
        gc.fillRect(-18, -24, 8, 48);
        gc.restore();
    }

    private void drawDocument(GraphicsContext gc, double x, double y, double scale,
                              double rotation, double a) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(rotation));
        gc.scale(scale, scale);
        gc.setFill(alpha(Color.web("#f7f0d5"), a));
        gc.fillRect(-24, -32, 48, 64);
        gc.setStroke(alpha(Color.web("#8a7a4a"), a * 0.7));
        gc.setLineWidth(1.2);
        gc.strokeRect(-24, -32, 48, 64);
        gc.setStroke(alpha(Color.web("#5f5230"), a * 0.5));
        for (int i = 0; i < 5; i++) {
            gc.strokeLine(-16, -20 + i * 10, 16, -20 + i * 10);
        }
        gc.restore();
    }

    private void drawExplosion(GraphicsContext gc, Color accent, double x, double y,
                               double p, double presence) {
        double r = 10 + p * 70;
        gc.setFill(alpha(Color.web("#ffd27a"), 0.6 * (1 - p) * presence));
        gc.fillOval(x - r, y - r, r * 2, r * 2);
        gc.setStroke(alpha(accent, 0.8 * (1 - p) * presence));
        gc.setLineWidth(3);
        gc.strokeOval(x - r * 1.4, y - r * 1.4, r * 2.8, r * 2.8);
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4 + p;
            gc.setStroke(alpha(Color.web("#ffe9b0"), 0.7 * (1 - p) * presence));
            gc.setLineWidth(2);
            gc.strokeLine(x + Math.cos(a) * r, y + Math.sin(a) * r,
                    x + Math.cos(a) * r * 1.8, y + Math.sin(a) * r * 1.8);
        }
    }

    private void drawWave(GraphicsContext gc, double baseY, double amp, double freq,
                          double speed, Color color, double a) {
        int n = 33;
        double[] xs = new double[n + 2];
        double[] ys = new double[n + 2];
        for (int i = 0; i < n; i++) {
            double x = WIDTH * i / (double) (n - 1);
            xs[i] = x;
            ys[i] = baseY + Math.sin(x * freq + time * speed) * amp
                    + Math.sin(x * freq * 2.3 + time * speed * 1.7) * amp * 0.35;
        }
        xs[n] = WIDTH;
        ys[n] = HEIGHT;
        xs[n + 1] = 0;
        ys[n + 1] = HEIGHT;
        gc.setFill(alpha(color, a));
        gc.fillPolygon(xs, ys, n + 2);
        gc.setStroke(alpha(Color.web("#d8f2ff"), a * 0.35));
        gc.setLineWidth(1.5);
        gc.strokePolyline(xs, ys, n);
    }

    private static String[] titleLines(String id) {
        return switch (id) {
            case "rizal" -> new String[]{"NOLI ME", "TANGERE"};
            case "bonifacio" -> new String[]{"SIGAW NG", "PAGSASARILI"};
            case "luna" -> new String[]{"HENERAL", "LUNA"};
            case "mabini" -> new String[]{"SINAG NG", "KATWIRAN"};
            case "lapulapu" -> new String[]{"ALON NG", "MACTAN"};
            default -> new String[]{"SPECIAL", "MOVE"};
        };
    }

    private static String tagline(String id) {
        return switch (id) {
            case "rizal" -> "Ang lakas ng panulat.";
            case "bonifacio" -> "Kapatiran at tapang.";
            case "luna" -> "Disiplina at artilerya.";
            case "mabini" -> "Isip ang sandata.";
            case "lapulapu" -> "Lakas ng dagat at tapang.";
            default -> "";
        };
    }

    private static boolean heroOnLeft(String id) {
        return id.equals("bonifacio") || id.equals("lapulapu");
    }

    private static String spaced(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == ' ') {
                sb.append("   ");
            } else {
                sb.append(c).append(' ');
            }
        }
        return sb.toString().trim();
    }

    private static double rnd(int index, int salt) {
        double v = Math.sin(index * 12.9898 + salt * 78.233) * 43758.5453;
        return v - Math.floor(v);
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

    private static Color mix(Color a, Color b, double t) {
        double f = clamp01(t);
        return new Color(
                a.getRed() + (b.getRed() - a.getRed()) * f,
                a.getGreen() + (b.getGreen() - a.getGreen()) * f,
                a.getBlue() + (b.getBlue() - a.getBlue()) * f,
                1);
    }
}
