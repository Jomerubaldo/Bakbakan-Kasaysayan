package com.bakbakan.screens;

import com.bakbakan.combat.CombatConstants;
import com.bakbakan.entities.GameSession;
import com.bakbakan.entities.HeroData;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.ScreenChangeEvent;
import com.bakbakan.events.SoundRequestEvent;
import com.bakbakan.ui.BackdropArt;
import com.bakbakan.util.GameConfig;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class ResultScreen implements Screen {

    public enum Mode {
        ROUND_WIN, GAME_OVER, VICTORY
    }

    private static final double BUTTON_W = 260;
    private static final double BUTTON_H = 58;
    private static final double BUTTON_GAP = 28;
    private static final double BUTTON_Y = 548;

    private final GameEventBus bus;
    private final GameSession session;
    private final Mode mode;
    private final StackPane root;
    private final Canvas canvas;

    private int selected;
    private double elapsed;

    public ResultScreen(GameEventBus bus, GameSession session, Mode mode) {
        this.bus = bus;
        this.session = session;
        this.mode = mode;
        this.canvas = new Canvas(GameConfig.WIDTH, GameConfig.HEIGHT);
        this.root = new StackPane(canvas);
        this.root.setStyle("-fx-background-color: #0b1020;");
    }

    @Override
    public StackPane getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return switch (mode) {
            case ROUND_WIN -> "RoundWin";
            case GAME_OVER -> "GameOver";
            case VICTORY -> "Victory";
        };
    }

    @Override
    public void onShow() {
        selected = 0;
        elapsed = 0;
        String music = mode == Mode.GAME_OVER ? "gameover" : "victory";
        bus.fire(new SoundRequestEvent(music, true));
        render();
    }

    @Override
    public void update(double dt) {
        elapsed += dt;
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawBackground(gc);

        Color accent = switch (mode) {
            case ROUND_WIN -> Color.web("#4cd97b");
            case GAME_OVER -> Color.web("#ff6b6b");
            case VICTORY -> Color.web("#f4c542");
        };

        gc.setTextAlign(TextAlignment.CENTER);
        double glowPulse = 0.08 + 0.04 * Math.sin(elapsed * 2.0);
        gc.setFill(mode == Mode.GAME_OVER
                ? Color.web("#ff3b3b", 0.03 + 0.015 * Math.sin(elapsed * 2.0))
                : Color.web("#ffffff", glowPulse));
        gc.fillOval(GameConfig.WIDTH / 2.0 - 330, 58, 660, 660);

        gc.setFill(accent);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 74));
        gc.fillText(title(), GameConfig.WIDTH / 2.0, 258);

        gc.setFill(Color.web("#f4f6ff"));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 30));
        gc.fillText(message(), GameConfig.WIDTH / 2.0, 326);

        gc.setFill(Color.web("#b9c2da"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 17));
        gc.fillText(details(), GameConfig.WIDTH / 2.0, 386);

        HeroData hero = session.getSelectedHero();
        if (hero != null) {
            gc.setFill(Color.web("#8f9ab8"));
            gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            gc.fillText("Hero: " + hero.name(), GameConfig.WIDTH / 2.0, 418);
            if (mode == Mode.GAME_OVER
                    && session.getLastRoundNumber() >= CombatConstants.TOTAL_ROUNDS) {
                drawLegacy(gc, hero);
            }
        }

        if (mode == Mode.VICTORY) {
            drawButton(gc, primaryX(), BUTTON_Y, primaryLabel(), selected == 0);
        } else {
            drawButton(gc, primaryX(), BUTTON_Y, primaryLabel(), selected == 0);
            drawButton(gc, secondaryX(), BUTTON_Y, "Main Menu", selected == 1);
        }

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#6d7796"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        gc.fillText(hint(), GameConfig.WIDTH / 2.0, 684);
    }

    private void drawBackground(GraphicsContext gc) {
        if (mode == Mode.GAME_OVER) {
            drawDefeatBackdrop(gc);
            return;
        }
        if (mode == Mode.ROUND_WIN) {
            drawVictoryBackdrop(gc);
            return;
        }

        Color top = switch (mode) {
            case ROUND_WIN -> Color.web("#0d2230");
            case GAME_OVER -> Color.web("#220d16");
            case VICTORY -> Color.web("#241b08");
        };
        Color bottom = switch (mode) {
            case ROUND_WIN -> Color.web("#101a3a");
            case GAME_OVER -> Color.web("#0b1020");
            case VICTORY -> Color.web("#0b1020");
        };
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, top), new Stop(1, bottom)));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);

        gc.setStroke(Color.web("#ffffff", 0.06));
        gc.setLineWidth(1);
        for (double x = 80; x < GameConfig.WIDTH; x += 80) {
            gc.strokeLine(x, 0, x, GameConfig.HEIGHT);
        }
        for (double y = 80; y < GameConfig.HEIGHT; y += 80) {
            gc.strokeLine(0, y, GameConfig.WIDTH, y);
        }
    }

    private void drawVictoryBackdrop(GraphicsContext gc) {
        double width = GameConfig.WIDTH;
        double height = GameConfig.HEIGHT;
        double horizon = 600;

        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#05161f")),
                new Stop(0.34, Color.web("#0b3340")),
                new Stop(0.68, Color.web("#1d5f4f")),
                new Stop(0.88, Color.web("#c98a3e")),
                new Stop(1, Color.web("#f7d488"))));
        gc.fillRect(0, 0, width, horizon);

        double sunX = 980;
        double sunY = 250;
        gc.save();
        gc.translate(sunX, sunY);
        gc.rotate(elapsed * 6);
        gc.translate(-sunX, -sunY);
        BackdropArt.sunRays(gc, sunX, sunY, 430, Color.web("#ffe6a0", 0.09));
        gc.restore();

        gc.setFill(Color.web("#ffe6a0", 0.16));
        gc.fillOval(sunX - 165, sunY - 165, 330, 330);
        gc.setFill(Color.web("#ffd27a", 0.62));
        gc.fillOval(sunX - 92, sunY - 92, 184, 184);
        gc.setFill(Color.web("#fff0bd", 0.95));
        gc.fillOval(sunX - 64, sunY - 64, 128, 128);

        gc.setFill(new RadialGradient(0, 0, GameConfig.WIDTH / 2.0, 250, 420,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#9ff0c0", 0.14)),
                new Stop(1, Color.web("#9ff0c0", 0))));
        gc.fillOval(GameConfig.WIDTH / 2.0 - 420, 250 - 420, 840, 840);

        gc.setFill(Color.web("#ffe6a0", 0.05));
        for (int i = 0; i < 5; i++) {
            double bx = 110 + i * 255;
            gc.fillPolygon(new double[]{bx, bx + 120, bx + 170, bx + 60},
                    new double[]{horizon, horizon, 130, 130}, 4);
        }

        double drift = (elapsed * 10) % (width + 360);
        BackdropArt.cloud(gc, 60 + drift, 150, 300, Color.web("#d9f2ea", 0.10));
        BackdropArt.cloud(gc, 520 + drift * 0.6, 236, 240, Color.web("#e8f5c8", 0.08));

        double twinkle = 0.22 + 0.16 * Math.max(0, Math.sin(elapsed * 2.6));
        BackdropArt.star(gc, 150, 108, 14, Color.web("#ffe6a0", twinkle));
        BackdropArt.star(gc, 320, 168, 9, Color.web("#fff4c8", twinkle * 0.8));
        BackdropArt.star(gc, 1190, 130, 11, Color.web("#ffe6a0", twinkle * 0.9));

        gc.setFill(new LinearGradient(0, horizon - 180, 0, horizon, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#16453a")),
                new Stop(1, Color.web("#0a2620"))));
        gc.fillPolygon(
                new double[]{0, 170, 340, 540, 740, 950, 1150, 1280, 1280, 0},
                new double[]{horizon - 30, horizon - 130, horizon - 70,
                        horizon - 170, horizon - 90, horizon - 150,
                        horizon - 60, horizon - 120, horizon, horizon}, 10);

        gc.setFill(new LinearGradient(0, horizon - 130, 0, horizon, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0e332b")),
                new Stop(1, Color.web("#071d19"))));
        gc.fillPolygon(
                new double[]{0, 230, 460, 700, 940, 1160, 1280, 1280, 0},
                new double[]{horizon - 16, horizon - 84, horizon - 38,
                        horizon - 108, horizon - 46, horizon - 92,
                        horizon - 28, horizon, horizon}, 9);

        gc.setFill(new LinearGradient(0, horizon - 120, 0, horizon, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ffd98a", 0)),
                new Stop(1, Color.web("#ffd98a", 0.16))));
        gc.fillRect(0, horizon - 120, width, 120);

        gc.setFill(Color.web("#1d5a40"));
        gc.fillPolygon(
                new double[]{0, 210, 430, 670, 900, 1120, 1280},
                new double[]{horizon, horizon - 52, horizon - 24, horizon - 60,
                        horizon - 20, horizon - 46, horizon}, 7);
        gc.setFill(Color.web("#2f8a5b"));
        gc.fillPolygon(
                new double[]{0, 260, 520, 780, 1040, 1280},
                new double[]{horizon, horizon - 26, horizon - 12, horizon - 30,
                        horizon - 10, horizon}, 6);

        gc.setFill(new LinearGradient(0, horizon, 0, height, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#123a2a")),
                new Stop(1, Color.web("#07160f"))));
        gc.fillRect(0, horizon, width, height - horizon);
        gc.setFill(Color.web("#4fbf7a"));
        gc.fillRect(0, horizon - 6, width, 8);
        gc.setFill(Color.web("#2a7a4e", 0.7));
        gc.fillRect(0, horizon + 2, width, 5);
        gc.setFill(Color.web("#ffe6a0", 0.16));
        gc.fillOval(width / 2.0 - 220, horizon - 12, 440, 26);

        drawVictoryFlag(gc, 126, horizon + 42);
        drawTriumphWeapons(gc, 1082, horizon + 60);

        for (int i = 0; i < 4; i++) {
            double dx = (elapsed * (26 + i * 9) + i * 260) % (width + 160) - 80;
            double dy = 120 + i * 46 + Math.sin(elapsed * 1.4 + i) * 14;
            drawDove(gc, dx, dy, 8 + (i % 2) * 3,
                    Math.sin(elapsed * 6 + i * 1.7));
        }

        drawBunting(gc);

        for (int i = 0; i < 48; i++) {
            double speed = 46 + (i % 6) * 16;
            double cx = (i * 149 + Math.sin(elapsed * 1.7 + i * 0.9) * 42
                    + elapsed * 12 * (i % 3)) % (width + 80) - 40;
            double cy = (i * 61 + elapsed * speed) % (height + 90) - 45;
            double rot = elapsed * (70 + (i % 5) * 26) + i * 31;
            Color c = switch (i % 5) {
                case 0 -> Color.web("#2b3a63");
                case 1 -> Color.web("#c0392b");
                case 2 -> Color.web("#f4c542");
                case 3 -> Color.web("#ffffff");
                default -> Color.web("#4cd97b");
            };
            gc.save();
            gc.translate(cx, cy);
            gc.rotate(rot);
            gc.setFill(c.deriveColor(0, 1, 1, 0.85));
            gc.fillRect(-4, -2, 8, 4);
            gc.restore();
        }

        for (int i = 0; i < 10; i++) {
            double sx = 100 + (i * 127) % (width - 200);
            double sy = 90 + (i * 83) % 300;
            double alpha = 0.2 + 0.2 * Math.max(0, Math.sin(elapsed * 2.4 + i));
            BackdropArt.star(gc, sx, sy, 4 + (i % 3) * 2,
                    Color.web("#fff4c8", alpha));
        }
    }

    private void drawBunting(GraphicsContext gc) {
        double width = GameConfig.WIDTH;
        gc.setStroke(Color.web("#d9d2c0", 0.35));
        gc.setLineWidth(1.2);
        gc.beginPath();
        gc.moveTo(0, 26);
        gc.quadraticCurveTo(width / 2.0, 62, width, 24);
        gc.stroke();

        Color[] colors = {Color.web("#2b3a63"), Color.web("#c0392b"),
                Color.web("#f4c542")};
        int count = 26;
        for (int i = 0; i < count; i++) {
            double t = (i + 0.5) / count;
            double bx = t * width;
            double by = 26 * (1 - t) * (1 - t) + 120 * t * (1 - t)
                    + 24 * t * t;
            double sway = Math.sin(elapsed * 2.2 + i) * 2;
            gc.setFill(colors[i % 3]);
            gc.fillPolygon(new double[]{bx - 8, bx + 8, bx + sway},
                    new double[]{by, by, by + 18}, 3);
        }
    }

    private void drawVictoryFlag(GraphicsContext gc, double x, double groundY) {
        gc.save();
        gc.translate(x, groundY);

        gc.setFill(Color.web("#04100c", 0.45));
        gc.fillOval(-26, -6, 96, 12);

        gc.setFill(new LinearGradient(-3, 0, 3, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#5a4a2a")),
                new Stop(0.5, Color.web("#d9c07a")),
                new Stop(1, Color.web("#6a5530"))));
        gc.fillRect(-3, -236, 6, 236);
        gc.setFill(Color.web("#3a2f1a"));
        gc.fillRect(-7, -8, 14, 8);

        gc.setFill(Color.web("#f4c542"));
        gc.fillOval(-7, -252, 14, 14);
        gc.fillRect(-2, -240, 4, 12);

        double topY = -224;
        double clothH = 76;
        double length = 132;
        int segments = 12;
        double leftX = 3;
        double[] topXs = new double[segments + 1];
        double[] topYs = new double[segments + 1];
        double[] botYs = new double[segments + 1];

        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            topXs[i] = leftX + t * length;
            topYs[i] = topY + victoryFlagWave(t);
            botYs[i] = topYs[i] + clothH;
        }

        for (int i = 0; i < segments; i++) {
            double t0 = i / (double) segments;
            double t1 = (i + 1) / (double) segments;
            double mid0 = topYs[i] + clothH * 0.5;
            double mid1 = topYs[i + 1] + clothH * 0.5;
            double shade = 0.84 + 0.16 * Math.cos(
                    (t0 - elapsed * 0.6) * Math.PI * 2);

            gc.setFill(shadeColor(Color.web("#2b4f9e"), shade));
            gc.fillPolygon(new double[]{topXs[i], topXs[i + 1],
                            topXs[i + 1], topXs[i]},
                    new double[]{topYs[i], topYs[i + 1], mid1, mid0}, 4);
            gc.setFill(shadeColor(Color.web("#c0392b"), shade));
            gc.fillPolygon(new double[]{topXs[i], topXs[i + 1],
                            topXs[i + 1], topXs[i]},
                    new double[]{mid0, mid1, botYs[i + 1], botYs[i]}, 4);
            gc.setFill(Color.web("#f4c542", 0.8));
            gc.fillRect(topXs[i], botYs[i] - 2, topXs[i + 1] - topXs[i] + 0.5, 2);

            if (i % 3 == 2) {
                gc.setStroke(Color.web("#000000", 0.12));
                gc.setLineWidth(1);
                gc.strokeLine(topXs[i + 1], topYs[i + 1],
                        topXs[i + 1], botYs[i + 1]);
            }
        }

        gc.setFill(Color.web("#f2efe6"));
        gc.fillPolygon(new double[]{leftX, leftX + 46, leftX},
                new double[]{topY, topY + clothH / 2.0, topY + clothH}, 3);

        double sunCX = leftX + 17;
        double sunCY = topY + clothH / 2.0;
        gc.setFill(Color.web("#f4c542"));
        gc.fillOval(sunCX - 7, sunCY - 7, 14, 14);
        gc.setStroke(Color.web("#f4c542"));
        gc.setLineWidth(1.4);
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            gc.strokeLine(sunCX + Math.cos(angle) * 9, sunCY + Math.sin(angle) * 9,
                    sunCX + Math.cos(angle) * 14, sunCY + Math.sin(angle) * 14);
        }

        BackdropArt.star(gc, leftX + 11, topY + 12, 5, Color.web("#f4c542"));
        BackdropArt.star(gc, leftX + 11, topY + clothH - 12, 5,
                Color.web("#f4c542"));
        BackdropArt.star(gc, leftX + 36, topY + clothH / 2.0, 5,
                Color.web("#f4c542"));

        gc.setStroke(Color.web("#0a1730", 0.25));
        gc.setLineWidth(1);
        gc.strokePolyline(topXs, topYs, segments + 1);
        gc.strokePolyline(topXs, botYs, segments + 1);

        gc.restore();
    }

    private double victoryFlagWave(double t) {
        return Math.sin(t * 5.2 + elapsed * 2.6) * 7.0 * t
                + Math.sin(t * 3.1 - elapsed * 1.7 + 1.2) * 2.8 * t;
    }

    private Color shadeColor(Color base, double light) {
        double l = Math.max(0.6, Math.min(1.15, light));
        return new Color(
                Math.min(1, base.getRed() * l),
                Math.min(1, base.getGreen() * l),
                Math.min(1, base.getBlue() * l),
                base.getOpacity());
    }

    private void drawTriumphWeapons(GraphicsContext gc, double x, double groundY) {
        gc.save();
        gc.translate(x, groundY);

        gc.setFill(new RadialGradient(0, 0, 0, -70, 140, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ffe6a0", 0.25)),
                new Stop(1, Color.web("#ffe6a0", 0))));
        gc.fillOval(-140, -210, 280, 280);

        gc.setFill(Color.web("#0a2018"));
        gc.fillOval(-54, -12, 108, 20);
        gc.setFill(Color.web("#1d5a40"));
        gc.fillOval(-48, -9, 96, 14);

        gc.save();
        gc.translate(-22, -8);
        gc.rotate(-38);
        gc.setFill(new LinearGradient(-5, 0, 5, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#8f98a8")),
                new Stop(0.5, Color.web("#f4f7ff")),
                new Stop(1, Color.web("#7d8797"))));
        gc.fillPolygon(new double[]{-5, 5, 5, 0, -5},
                new double[]{0, 0, -142, -158, -142}, 5);
        gc.setStroke(Color.web("#c8d1e0", 0.8));
        gc.setLineWidth(1.4);
        gc.strokeLine(0, -8, 0, -140);
        gc.setFill(Color.web("#d4af37"));
        gc.fillRect(-20, -4, 40, 7);
        gc.setFill(Color.web("#5a3020"));
        gc.fillRect(-4, 3, 8, 26);
        gc.setFill(Color.web("#d4af37"));
        gc.fillOval(-6, 28, 12, 12);
        gc.restore();

        gc.save();
        gc.translate(22, -6);
        gc.rotate(-40);
        gc.setFill(new LinearGradient(0, -8, 0, 8, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#8a5a34")),
                new Stop(1, Color.web("#4a2c18"))));
        gc.fillPolygon(new double[]{-64, -18, -24, -18, -64},
                new double[]{-8, -5, 0, 6, 7}, 5);
        gc.setFill(Color.web("#3a3f48"));
        gc.fillRect(-20, -3, 110, 6);
        gc.setFill(Color.web("#8f98a8"));
        gc.fillRect(72, -4, 20, 8);
        gc.setFill(Color.web("#2a2d34"));
        gc.fillRect(30, -7, 5, 4);
        gc.fillRect(82, -8, 4, 4);
        gc.setStroke(Color.web("#4a515c"));
        gc.setLineWidth(2);
        gc.strokePolyline(new double[]{-4, 0, 8, 5},
                new double[]{4, 11, 10, 5}, 4);
        gc.setStroke(Color.web("#c0392b", 0.8));
        gc.setLineWidth(3);
        gc.strokeLine(-14, -6, 14, 8);
        gc.restore();

        gc.setFill(Color.web("#c0392b"));
        gc.fillOval(-13, -58, 26, 20);
        gc.setFill(Color.web("#f4c542"));
        gc.fillOval(-6, -52, 12, 10);
        gc.setFill(Color.web("#c0392b"));
        gc.fillPolygon(new double[]{-8, -20, -16, -2},
                new double[]{-42, -16, -8, -36}, 4);
        gc.fillPolygon(new double[]{8, 20, 16, 2},
                new double[]{-42, -16, -8, -36}, 4);

        gc.restore();
    }

    private void drawDove(GraphicsContext gc, double x, double y,
                          double size, double flap) {
        double lift = 0.6 + 0.4 * flap;
        gc.setStroke(Color.web("#f5f8ff", 0.85));
        gc.setLineWidth(Math.max(1.2, size * 0.18));
        gc.beginPath();
        gc.moveTo(x - size, y);
        gc.quadraticCurveTo(x - size * 0.5, y - size * lift, x, y);
        gc.quadraticCurveTo(x + size * 0.5, y - size * lift, x + size, y);
        gc.stroke();
        gc.setFill(Color.web("#f5f8ff", 0.85));
        gc.fillOval(x - size * 0.25, y - size * 0.22, size * 0.5, size * 0.34);
    }

    private void drawDefeatBackdrop(GraphicsContext gc) {
        double width = GameConfig.WIDTH;
        double height = GameConfig.HEIGHT;

        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#160810")),
                new Stop(0.45, Color.web("#2a0c16")),
                new Stop(0.75, Color.web("#170a11")),
                new Stop(1, Color.web("#070509"))));
        gc.fillRect(0, 0, width, height);

        double mx = 980;
        double my = 250;
        gc.save();
        gc.translate(mx, my);
        gc.rotate(elapsed * 2.5);
        gc.translate(-mx, -my);
        BackdropArt.sunRays(gc, mx, my, 300, Color.web("#ff3b3b", 0.05));
        gc.restore();

        gc.setFill(Color.web("#ff3b3b", 0.08));
        gc.fillOval(mx - 130, my - 130, 260, 260);
        gc.setFill(Color.web("#8f1f2a", 0.55));
        gc.fillOval(mx - 78, my - 78, 156, 156);
        gc.setFill(Color.web("#3a0b12", 0.96));
        gc.fillOval(mx - 52, my - 58, 118, 122);
        gc.setStroke(Color.web("#c0454c", 0.32));
        gc.setLineWidth(3);
        gc.strokeOval(mx - 76, my - 76, 152, 152);

        BackdropArt.star(gc, 220, 110, 15, Color.web("#9a5560", 0.35));
        BackdropArt.star(gc, 1080, 120, 13, Color.web("#9a5560", 0.30));
        BackdropArt.star(gc, 700, 88, 9, Color.web("#9a5560", 0.25));

        double drift = (elapsed * 8) % (width + 400);
        BackdropArt.cloud(gc, 40 + drift, 205, 300, Color.web("#3a1620", 0.38));
        BackdropArt.cloud(gc, 520 + drift * 0.6, 300, 240, Color.web("#2a1018", 0.38));

        gc.setFill(Color.web("#1a0a14"));
        gc.fillPolygon(
                new double[]{0, 190, 380, 600, 820, 1040, width},
                new double[]{560, 430, 545, 405, 545, 425, 560}, 7);
        gc.setFill(Color.web("#120710"));
        gc.fillPolygon(
                new double[]{0, 260, 500, 760, 1010, width},
                new double[]{600, 505, 595, 480, 590, 600}, 6);

        gc.setFill(Color.web("#0a0609"));
        gc.fillRect(0, 600, width, height - 600);

        gc.setFill(Color.web("#12070d"));
        gc.fillPolygon(
                new double[]{0, 200, 420, 660, 900, 1120, width},
                new double[]{640, 612, 632, 606, 636, 610, 638}, 7);

        gc.setStroke(Color.web("#341018", 0.85));
        gc.setLineWidth(2);
        gc.strokeLine(560, 626, 610, 660);
        gc.strokeLine(610, 660, 590, 694);
        gc.strokeLine(610, 660, 652, 686);
        gc.strokeLine(300, 650, 340, 678);
        gc.strokeLine(340, 678, 322, 714);
        gc.strokeLine(900, 644, 940, 672);
        gc.strokeLine(940, 672, 922, 706);
        gc.strokeLine(1020, 662, 1060, 690);

        drawTornPhilippineFlag(gc, 116, 642);
        drawBrokenSword(gc, 296, 666, 100, -12);
        drawBrokenSword(gc, 336, 672, 74, 8);
        drawBrokenRifle(gc, 986, 664, -6);
        drawBrokenRifle(gc, 1094, 674, 5);
        drawCrackedHelmet(gc, 1214, 682);

        for (int i = 0; i < 36; i++) {
            double ax = (i * 151 + elapsed * 9 * (i % 3 + 1)) % width;
            double ay = (i * 83 + elapsed * (28 + (i % 5) * 9)) % height;
            double alpha = 0.12 + 0.12 * Math.sin(elapsed * 2 + i);
            Color dot = (i % 3 == 0)
                    ? Color.web("#ff6b6b", Math.max(0.03, alpha))
                    : Color.web("#8a8f99", Math.max(0.03, alpha));
            gc.setFill(dot);
            double size = 2 + (i % 2);
            gc.fillOval(ax, ay, size, size);
        }
    }

    private void drawTornPhilippineFlag(GraphicsContext gc, double x, double groundY) {
        gc.save();
        gc.translate(x, groundY);
        gc.rotate(-7);

        gc.setFill(Color.web("#050305", 0.5));
        gc.fillOval(-40, -8, 150, 14);

        gc.setFill(new LinearGradient(-3, 0, 3, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1e151a")),
                new Stop(0.5, Color.web("#4d3d3f")),
                new Stop(1, Color.web("#191116"))));
        gc.fillRect(-3, -196, 6, 196);
        gc.setFill(Color.web("#120b10"));
        gc.fillRect(-6, -10, 12, 10);

        gc.setStroke(Color.web("#5a4a4a", 0.7));
        gc.setLineWidth(1.5);
        gc.strokePolyline(
                new double[]{-3, -8, -2, -10, -3},
                new double[]{-196, -190, -187, -183, -178}, 5);

        gc.setFill(Color.web("#2b3a63"));
        gc.beginPath();
        gc.moveTo(4, -178);
        gc.quadraticCurveTo(58, -176, 116, -174);
        gc.quadraticCurveTo(142, -173, 158, -172);
        gc.lineTo(170, -167);
        gc.lineTo(159, -160);
        gc.lineTo(169, -153);
        gc.lineTo(160, -146);
        gc.lineTo(168, -138);
        gc.lineTo(100, -135);
        gc.lineTo(50, -137);
        gc.lineTo(4, -138);
        gc.closePath();
        gc.fill();

        gc.setFill(Color.web("#7a2530"));
        gc.beginPath();
        gc.moveTo(4, -138);
        gc.lineTo(50, -137);
        gc.lineTo(100, -135);
        gc.lineTo(168, -138);
        gc.lineTo(159, -124);
        gc.lineTo(170, -118);
        gc.lineTo(160, -110);
        gc.lineTo(169, -102);
        gc.lineTo(158, -95);
        gc.lineTo(150, -88);
        gc.lineTo(142, -98);
        gc.lineTo(130, -86);
        gc.lineTo(118, -96);
        gc.lineTo(106, -84);
        gc.lineTo(94, -98);
        gc.lineTo(84, -88);
        gc.lineTo(72, -100);
        gc.lineTo(62, -90);
        gc.lineTo(50, -102);
        gc.lineTo(40, -92);
        gc.lineTo(30, -104);
        gc.lineTo(20, -94);
        gc.lineTo(12, -104);
        gc.lineTo(4, -96);
        gc.closePath();
        gc.fill();

        gc.setFill(Color.web("#000000", 0.16));
        gc.beginPath();
        gc.moveTo(74, -175);
        gc.quadraticCurveTo(66, -140, 74, -102);
        gc.lineTo(92, -99);
        gc.quadraticCurveTo(84, -138, 92, -174);
        gc.closePath();
        gc.fill();

        gc.setFill(Color.web("#d9d6cf", 0.92));
        gc.fillPolygon(new double[]{4, 54, 4}, new double[]{-176, -135, -96}, 3);

        gc.setFill(Color.web("#1a0a14"));
        gc.fillPolygon(new double[]{30, 37, 32}, new double[]{-146, -139, -135}, 3);
        gc.fillPolygon(new double[]{17, 23, 18}, new double[]{-112, -107, -103}, 3);

        gc.setFill(Color.web("#f0c24a", 0.85));
        gc.fillOval(16, -143, 14, 14);
        gc.setStroke(Color.web("#f0c24a", 0.8));
        gc.setLineWidth(1.6);
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            gc.strokeLine(23 + Math.cos(angle) * 9, -136 + Math.sin(angle) * 9,
                    23 + Math.cos(angle) * 15, -136 + Math.sin(angle) * 15);
        }

        BackdropArt.star(gc, 14, -164, 5.5, Color.web("#f0c24a", 0.85));
        BackdropArt.star(gc, 14, -106, 5.5, Color.web("#f0c24a", 0.85));
        BackdropArt.star(gc, 44, -135, 5.5, Color.web("#f0c24a", 0.85));

        gc.setFill(Color.web("#150a12", 0.95));
        gc.fillPolygon(new double[]{86, 92, 95, 90, 85},
                new double[]{-118, -122, -113, -108, -111}, 5);
        gc.fillPolygon(new double[]{128, 134, 137, 131, 126},
                new double[]{-158, -161, -153, -149, -152}, 5);
        gc.fillPolygon(new double[]{112, 117, 119, 114},
                new double[]{-96, -99, -92, -89}, 4);

        gc.setStroke(Color.web("#a5444a", 0.65));
        gc.setLineWidth(1);
        gc.strokeLine(62, -90, 60, -82);
        gc.strokeLine(84, -88, 83, -80);
        gc.strokeLine(130, -86, 129, -77);
        gc.strokeLine(142, -98, 145, -91);
        gc.setStroke(Color.web("#4a5d8f", 0.55));
        gc.strokeLine(159, -160, 166, -156);
        gc.strokeLine(160, -146, 167, -143);

        gc.restore();

        gc.save();
        gc.translate(x + 26, groundY - 4);
        gc.rotate(72);
        gc.setFill(Color.web("#33262a"));
        gc.fillRect(-2, -24, 4, 24);
        gc.restore();
    }

    private void drawBrokenSword(GraphicsContext gc, double x, double groundY,
                                 double length, double tilt) {
        gc.save();
        gc.translate(x, groundY);
        gc.rotate(tilt);

        gc.setFill(new LinearGradient(-4, 0, 4, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#3a3f48")),
                new Stop(0.45, Color.web("#98a1ae")),
                new Stop(1, Color.web("#2c3139"))));
        gc.fillPolygon(
                new double[]{-4, 4, 4, 2, 4, 1, -3, -4, -4},
                new double[]{0, 0, -length + 18, -length + 6,
                        -length + 12, -length, -length + 8, -length + 17, 0}, 9);
        gc.setStroke(Color.web("#101216", 0.55));
        gc.setLineWidth(1);
        gc.strokeLine(0, -4, 0, -length + 14);

        gc.setStroke(Color.web("#341018", 0.9));
        gc.setLineWidth(2);
        gc.strokeLine(-20, 3, -6, 9);
        gc.strokeLine(6, 9, 20, 3);

        gc.save();
        gc.translate(32, -3);
        gc.rotate(104);

        gc.setFill(new LinearGradient(-3, 0, 3, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4a515c")),
                new Stop(0.5, Color.web("#9aa3b0")),
                new Stop(1, Color.web("#333942"))));
        gc.fillPolygon(new double[]{-3, 3, 3, 1, 3, 0, -2, -3},
                new double[]{0, 0, -38, -29, -34, -23, -31, -38}, 8);

        gc.setFill(Color.web("#1c1a20"));
        gc.fillRect(-17, -4, 34, 6);
        gc.setFill(Color.web("#4a2a1c"));
        gc.fillRect(-3, 2, 6, 20);
        gc.setStroke(Color.web("#2a1a10", 0.8));
        gc.setLineWidth(1);
        gc.strokeLine(0, 4, 0, 20);
        gc.setFill(Color.web("#2a2630"));
        gc.fillOval(-5, 20, 10, 10);

        gc.restore();
        gc.restore();
    }

    private void drawBrokenRifle(GraphicsContext gc, double x, double groundY,
                                 double tilt) {
        gc.save();
        gc.translate(x, groundY);
        gc.rotate(tilt);

        gc.setFill(new LinearGradient(0, -9, 0, 9, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#6a4428")),
                new Stop(1, Color.web("#341f12"))));
        gc.fillPolygon(new double[]{-60, -24, -29, -23, -60},
                new double[]{-9, -7, 0, 7, 8}, 5);

        gc.setFill(Color.web("#2a2d34"));
        gc.fillPolygon(new double[]{-25, 16, 44, 54, 54, 16, -25},
                new double[]{-4, -3, -7, -6, 2, 4, 4}, 7);
        gc.setFill(Color.web("#565e6a"));
        gc.fillRect(24, -4, 18, 3);

        gc.setStroke(Color.web("#0c0e12", 0.8));
        gc.setLineWidth(1.4);
        gc.strokeLine(18, -3, 24, 2);
        gc.strokeLine(24, 2, 20, 5);

        gc.setStroke(Color.web("#3a3f48"));
        gc.setLineWidth(2);
        gc.strokePolyline(new double[]{-2, 2, 9, 6},
                new double[]{4, 11, 10, 5}, 4);

        gc.setFill(Color.web("#5a3a22"));
        gc.fillPolygon(new double[]{44, 56, 58, 46},
                new double[]{2, 4, 8, 8}, 4);

        gc.setStroke(Color.web("#8a5a34", 0.7));
        gc.setLineWidth(1);
        gc.strokeLine(-24, -6, -18, -2);
        gc.strokeLine(-25, 4, -19, 1);

        gc.save();
        gc.translate(70, 10);
        gc.rotate(18);
        gc.setFill(Color.web("#2a2d34"));
        gc.fillRoundRect(-8, -3, 16, 6, 2, 2);
        gc.setFill(Color.web("#0c0e12"));
        gc.fillOval(-8, -3, 4, 6);
        gc.restore();

        gc.restore();
    }

    private void drawCrackedHelmet(GraphicsContext gc, double x, double groundY) {
        gc.setFill(Color.web("#3a3f48"));
        gc.fillOval(x - 21, groundY - 24, 42, 28);
        gc.fillRect(x - 25, groundY - 9, 50, 8);
        gc.setFill(Color.web("#22262c"));
        gc.fillOval(x - 15, groundY - 19, 30, 16);
        gc.setStroke(Color.web("#12151a"));
        gc.setLineWidth(1.6);
        gc.strokeLine(x - 4, groundY - 22, x + 2, groundY - 13);
        gc.strokeLine(x + 2, groundY - 13, x - 2, groundY - 4);
        gc.strokeLine(x + 2, groundY - 13, x + 10, groundY - 7);
        gc.setFill(Color.web("#f4c542", 0.25));
        gc.fillOval(x + 10, groundY - 10, 4, 3);
    }

    private String title() {
        return switch (mode) {
            case ROUND_WIN -> "You Win!";
            case GAME_OVER -> "You Lose!";
            case VICTORY -> "Victory!";
        };
    }

    private String message() {
        return switch (mode) {
            case ROUND_WIN -> "Round cleared! Keep the streak alive.";
            case GAME_OVER -> "They got you this time. Get up and run it back!";
            case VICTORY -> "Boss defeated! Your hero's legend is complete.";
        };
    }

    private String details() {
        int round = session.getLastRoundNumber();
        String reason = session.getLastRoundReason();
        String suffix = reason == null || reason.isBlank() ? "" : " - " + reason;
        return switch (mode) {
            case ROUND_WIN -> "Round " + round + " cleared" + suffix
                    + ". Next: Round " + session.getCurrentRound() + ".";
            case GAME_OVER -> "Reached Round " + round + suffix
                    + ". Continue restarts from Round 1.";
            case VICTORY -> "Final round " + round + " cleared" + suffix + ".";
        };
    }

    private void drawLegacy(GraphicsContext gc, HeroData hero) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#f4c542", 0.85));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        gc.fillText("LEGACY OF THE HERO", GameConfig.WIDTH / 2.0, 454);

        gc.setStroke(Color.web("#f4c542", 0.35));
        gc.setLineWidth(1);
        gc.strokeLine(GameConfig.WIDTH / 2.0 - 240, 464,
                GameConfig.WIDTH / 2.0 + 240, 464);

        gc.setFill(Color.web("#ffe89a"));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        gc.fillText(legacyLine(hero), GameConfig.WIDTH / 2.0, 492);
    }

    private static String legacyLine(HeroData hero) {
        return switch (hero.id()) {
            case "rizal" -> "Rizal died, but his words live on in every generation.";
            case "bonifacio" -> "Bonifacio fell, but the cry of the Katipunan lives in the heart of the nation.";
            case "luna" -> "Luna died, but his courage lives in the heart of every Filipino.";
            case "mabini" -> "Mabini died, but his intellect and spirit live on in every generation.";
            default -> "Lapulapu's memory endures in the heart of the nation and the legend of Mactan.";
        };
    }

    private String primaryLabel() {
        return switch (mode) {
            case ROUND_WIN -> "Next Round";
            case GAME_OVER -> "Continue";
            case VICTORY -> "Main Menu";
        };
    }

    private String hint() {
        return mode == Mode.VICTORY
                ? "Enter / click to return to the main menu"
                : "Left/Right or mouse to choose   |   Enter to confirm   |   Esc = Main Menu";
    }

    private void drawButton(GraphicsContext gc, double x, double y,
                            String label, boolean highlighted) {
        if (highlighted) {
            gc.setFill(Color.web("#f4c542", 0.18));
            gc.fillRoundRect(x - 8, y - 8, BUTTON_W + 16, BUTTON_H + 16, 22, 22);
        }

        gc.setFill(highlighted ? Color.web("#f4c542") : Color.web("#1c2745"));
        gc.fillRoundRect(x, y, BUTTON_W, BUTTON_H, 16, 16);
        gc.setStroke(highlighted ? Color.web("#ffe89a") : Color.web("#34405f"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, BUTTON_W, BUTTON_H, 16, 16);

        gc.setFill(highlighted ? Color.web("#0b1020") : Color.web("#e8e8f0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 21));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(label, x + BUTTON_W / 2.0, y + 37);
    }

    private double primaryX() {
        if (mode == Mode.VICTORY) {
            return (GameConfig.WIDTH - BUTTON_W) / 2.0;
        }
        return GameConfig.WIDTH / 2.0 - BUTTON_W - BUTTON_GAP / 2.0;
    }

    private double secondaryX() {
        return GameConfig.WIDTH / 2.0 + BUTTON_GAP / 2.0;
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.LEFT || code == KeyCode.UP) {
            move(-1);
        } else if (code == KeyCode.RIGHT || code == KeyCode.DOWN) {
            move(1);
        } else if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
            activate();
        } else if (code == KeyCode.ESCAPE) {
            goToMainMenu();
        }
    }

    private void move(int delta) {
        int count = mode == Mode.VICTORY ? 1 : 2;
        selected = Math.floorMod(selected + delta, count);
    }

    private void activate() {
        bus.fire(new SoundRequestEvent("ui_click", false));
        if (mode == Mode.ROUND_WIN) {
            if (selected == 0) {
                bus.fire(new ScreenChangeEvent(getName(), "Fight", true));
            } else {
                goToMainMenu();
            }
            return;
        }
        if (mode == Mode.GAME_OVER) {
            if (selected == 0) {
                session.restartProgress();
                bus.fire(new ScreenChangeEvent(getName(), "Fight", true));
            } else {
                goToMainMenu();
            }
            return;
        }
        goToMainMenu();
    }

    private void goToMainMenu() {
        bus.fire(new ScreenChangeEvent(getName(), "MainMenu", true));
    }

    @Override
    public void onMouseMoved(MouseEvent event) {
        int hovered = buttonAt(event.getX(), event.getY());
        if (hovered >= 0) {
            selected = hovered;
        }
    }

    @Override
    public void onMouseClicked(MouseEvent event) {
        int hit = buttonAt(event.getX(), event.getY());
        if (hit >= 0) {
            selected = hit;
            activate();
        }
    }

    private int buttonAt(double x, double y) {
        if (y < BUTTON_Y || y > BUTTON_Y + BUTTON_H) {
            return -1;
        }
        if (inside(x, primaryX())) {
            return 0;
        }
        if (mode != Mode.VICTORY && inside(x, secondaryX())) {
            return 1;
        }
        return -1;
    }

    private boolean inside(double x, double buttonX) {
        return x >= buttonX && x <= buttonX + BUTTON_W;
    }
}
