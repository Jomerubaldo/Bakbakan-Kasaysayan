package com.bakbakan.screens;

import com.bakbakan.entities.GameSession;
import com.bakbakan.entities.HeroData;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.ScreenChangeEvent;
import com.bakbakan.events.SoundRequestEvent;
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
        gc.setFill(Color.web("#ffffff", 0.08 + 0.04 * Math.sin(elapsed * 2.0)));
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
