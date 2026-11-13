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

        drawBrokenBanner(gc, 122, 642);
        drawStuckBlade(gc, 470, 664, 92, -10);
        drawStuckBlade(gc, 520, 668, 76, 8);
        drawStuckBlade(gc, 1000, 664, 84, -7);
        drawStuckBlade(gc, 1080, 668, 70, 12);

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

    private void drawBrokenBanner(GraphicsContext gc, double x, double groundY) {
        gc.setStroke(Color.web("#241820"));
        gc.setLineWidth(7);
        gc.strokeLine(x, groundY, x - 26, groundY - 140);

        gc.setFill(Color.web("#5a1a1f", 0.95));
        gc.fillPolygon(
                new double[]{x - 26, x + 46, x + 36, x + 22, x + 8, x - 8},
                new double[]{groundY - 140, groundY - 134, groundY - 112,
                        groundY - 124, groundY - 98, groundY - 116}, 6);
        gc.setFill(Color.web("#3a0f14", 0.9));
        gc.fillPolygon(
                new double[]{x - 26, x + 18, x + 10, x - 6},
                new double[]{groundY - 140, groundY - 126, groundY - 108,
                        groundY - 124}, 4);
    }

    private void drawStuckBlade(GraphicsContext gc, double x, double groundY,
                                double length, double tilt) {
        gc.save();
        gc.translate(x, groundY);
        gc.rotate(tilt);

        gc.setFill(Color.web("#3a3440"));
        gc.fillRect(-3, -length, 6, length);
        gc.setFill(Color.web("#2a2630"));
        gc.fillPolygon(new double[]{-3, 3, 0}, new double[]{-12, -12, 0}, 3);

        gc.setFill(Color.web("#1a161e"));
        gc.fillRect(-11, -length, 22, 6);
        gc.fillRect(-2, -length - 24, 4, 24);
        gc.fillOval(-5, -length - 32, 10, 10);
        gc.restore();
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
            case ROUND_WIN -> "Congratulations!";
            case GAME_OVER -> "Better luck next time.";
            case VICTORY -> "Congrats! You finally defeated the boss!";
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
        gc.fillText("PAMANA NG BAYANI", GameConfig.WIDTH / 2.0, 454);

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
            case "rizal" -> "Namatay si Rizal, ngunit ang kanyang mga salita ay buhay sa bawat henerasyon.";
            case "bonifacio" -> "Namatay si Bonifacio, ngunit ang sigaw ng Katipunan ay mananatili sa puso ng bayan.";
            case "luna" -> "Namatay si Luna, ngunit ang kanyang tapang ay mananatili sa puso ng bawat Pilipino.";
            case "mabini" -> "Namatay si Mabini, ngunit ang kanyang talino at diwa ay buhay sa bawat henerasyon.";
            default -> "Namatay man si Lapulapu, ang alaala ng Mactan ay mananatili sa puso ng bayan.";
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
