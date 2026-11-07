package com.bakbakan.screens;

import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.ScreenChangeEvent;
import com.bakbakan.events.SoundRequestEvent;
import com.bakbakan.util.GameConfig;
import javafx.application.Platform;
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

public class MainMenuScreen implements Screen {

    private static final double BUTTON_WIDTH = 320;
    private static final double BUTTON_HEIGHT = 64;
    private static final double BUTTON_X = (GameConfig.WIDTH - BUTTON_WIDTH) / 2.0;
    private static final double PLAY_Y = 380;
    private static final double QUIT_Y = 474;

    private final GameEventBus bus;
    private final StackPane root;
    private final Canvas canvas;

    private int selected;
    private double elapsed;

    public MainMenuScreen(GameEventBus bus) {
        this.bus = bus;
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
        return "MainMenu";
    }

    @Override
    public void onShow() {
        selected = 0;
        bus.fire(new SoundRequestEvent("menu", true));
        render();
    }

    @Override
    public void update(double dt) {
        elapsed += dt;
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#101a3a")),
                new Stop(1, Color.web("#1d0b12"))));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);

        gc.setFill(Color.web("#f4c542", 0.10));
        gc.fillOval(GameConfig.WIDTH / 2.0 - 300, 40, 600, 600);

        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 68));
        gc.fillText("Bakbakan-Kasaysayan", GameConfig.WIDTH / 2.0, 250);

        gc.setFill(Color.web("#e8e8f0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 22));
        gc.fillText("Philippine Heroes Fighting Game", GameConfig.WIDTH / 2.0, 300);

        drawButton(gc, BUTTON_X, PLAY_Y, "Play", selected == 0);
        drawButton(gc, BUTTON_X, QUIT_Y, "Quit", selected == 1);

        double pulse = 0.55 + 0.45 * Math.sin(elapsed * 3.0);
        gc.setFill(Color.web("#ffffff", pulse));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        gc.fillText("Use Up/Down or the mouse, then press Enter", GameConfig.WIDTH / 2.0, 600);

        gc.setFill(Color.web("#6d7796"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        gc.fillText("Family-friendly fan tribute. All art and audio are original or procedurally generated.",
                GameConfig.WIDTH / 2.0, 660);
    }

    private void drawButton(GraphicsContext gc, double x, double y, String label, boolean highlighted) {
        if (highlighted) {
            gc.setFill(Color.web("#f4c542", 0.18));
            gc.fillRoundRect(x - 8, y - 8, BUTTON_WIDTH + 16, BUTTON_HEIGHT + 16, 22, 22);
        }

        gc.setFill(highlighted ? Color.web("#f4c542") : Color.web("#1c2745"));
        gc.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, 16, 16);

        gc.setStroke(highlighted ? Color.web("#ffe89a") : Color.web("#34405f"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, 16, 16);

        gc.setFill(highlighted ? Color.web("#0b1020") : Color.web("#e8e8f0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(label, x + BUTTON_WIDTH / 2.0, y + 42);
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.UP || code == KeyCode.W) {
            move(-1);
        } else if (code == KeyCode.DOWN || code == KeyCode.S) {
            move(1);
        } else if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
            activate();
        }
    }

    private void move(int delta) {
        selected = Math.floorMod(selected + delta, 2);
    }

    private void activate() {
        bus.fire(new SoundRequestEvent("ui_click", false));
        if (selected == 0) {
            bus.fire(new ScreenChangeEvent(getName(), "HeroSelect", true));
        } else {
            Platform.exit();
        }
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
        if (x >= BUTTON_X && x <= BUTTON_X + BUTTON_WIDTH) {
            if (y >= PLAY_Y && y <= PLAY_Y + BUTTON_HEIGHT) {
                return 0;
            }
            if (y >= QUIT_Y && y <= QUIT_Y + BUTTON_HEIGHT) {
                return 1;
            }
        }
        return -1;
    }
}
