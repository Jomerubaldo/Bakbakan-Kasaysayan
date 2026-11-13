package com.bakbakan.screens;

import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.ScreenChangeEvent;
import com.bakbakan.events.SoundRequestEvent;
import com.bakbakan.ui.BackdropArt;
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

        drawBackdrop(gc);

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

    private void drawBackdrop(GraphicsContext gc) {
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#081228")),
                new Stop(0.45, Color.web("#2a1a38")),
                new Stop(0.75, Color.web("#7a3a32")),
                new Stop(1, Color.web("#d98555"))));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);

        double cx = GameConfig.WIDTH / 2.0;
        double cy = 400;
        gc.save();
        gc.translate(cx, cy);
        gc.rotate(elapsed * 4);
        gc.translate(-cx, -cy);
        BackdropArt.sunRays(gc, cx, cy, 420, Color.web("#f4c542", 0.06));
        gc.restore();

        gc.setFill(Color.web("#f4c542", 0.10));
        gc.fillOval(cx - 170, cy - 170, 340, 340);
        gc.setFill(Color.web("#ffd97a", 0.16));
        gc.fillOval(cx - 100, cy - 100, 200, 200);
        gc.setFill(Color.web("#ffe6a0", 0.26));
        gc.fillOval(cx - 60, cy - 60, 120, 120);

        BackdropArt.star(gc, 240, 120, 16, Color.web("#f4c542", 0.55));
        BackdropArt.star(gc, 1040, 120, 16, Color.web("#f4c542", 0.55));
        BackdropArt.star(gc, 640, 92, 11, Color.web("#f4c542", 0.50));

        double drift = (elapsed * 12) % (GameConfig.WIDTH + 320);
        BackdropArt.cloud(gc, 40 + drift, 210, 260, Color.web("#ffffff", 0.09));
        BackdropArt.cloud(gc, 520 + drift * 0.7, 300, 220, Color.web("#ffffff", 0.07));

        gc.setFill(Color.web("#2a2138"));
        gc.fillPolygon(new double[]{0, 170, 340, 520, 700, 900, 1090, 1280},
                new double[]{560, 400, 520, 360, 500, 420, 540, 470}, 8);
        gc.setFill(Color.web("#1c1828"));
        gc.fillPolygon(new double[]{0, 220, 430, 700, 950, 1280},
                new double[]{600, 500, 600, 460, 590, 600}, 6);

        gc.setFill(new LinearGradient(0, 600, 0, GameConfig.HEIGHT, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#5a2a28")),
                new Stop(0.5, Color.web("#2a1820")),
                new Stop(1, Color.web("#0b1220"))));
        gc.fillRect(0, 600, GameConfig.WIDTH, GameConfig.HEIGHT - 600);

        gc.setStroke(Color.web("#f4c542", 0.18));
        gc.setLineWidth(2);
        for (int i = 0; i < 4; i++) {
            double y = 614 + i * 16;
            gc.strokeLine(cx - 120 - i * 22, y, cx + 120 + i * 22, y);
        }

        gc.setFill(Color.web("#0b1220"));
        gc.fillPolygon(new double[]{0, 160, 340, 520, 720, 980, 1280},
                new double[]{666, 638, 660, 634, 666, 626, 654}, 7);

        drawHeroSilhouette(gc, "bonifacio", 110, 666, 1.05);
        drawHeroSilhouette(gc, "lapulapu", 230, 660, 1.08);
        drawHeroSilhouette(gc, "rizal", 352, 668, 1.00);
        drawHeroSilhouette(gc, "luna", 928, 662, 1.05);
        drawHeroSilhouette(gc, "mabini", 1058, 668, 1.00);

        drawTorch(gc, 58, 666);
        drawTorch(gc, 1222, 666);

        for (int i = 0; i < 18; i++) {
            double fx = (i * 173 + elapsed * 18 * (i % 3 + 1)) % GameConfig.WIDTH;
            double fy = 430 + Math.sin(elapsed * 1.5 + i) * 90 + (i % 5) * 18;
            double alpha = 0.22 + 0.14 * Math.sin(elapsed * 2 + i);
            gc.setFill(Color.web("#f4c542", Math.max(0.05, alpha)));
            gc.fillOval(fx, fy, 3, 3);
        }
    }

    private void drawHeroSilhouette(GraphicsContext gc, String id,
                                    double x, double feetY, double scale) {
        Color body = Color.web("#050a14", 0.97);
        Color rim = Color.web("#f4c542", 0.38);

        gc.save();
        gc.translate(x, feetY);
        gc.scale(scale, scale);
        gc.setFill(body);

        gc.fillRoundRect(-20, -54, 15, 54, 7, 7);
        gc.fillRoundRect(5, -54, 15, 54, 7, 7);
        gc.fillRoundRect(-28, -118, 56, 70, 18, 18);
        gc.fillRoundRect(-32, -110, 11, 44, 6, 6);
        gc.fillRoundRect(21, -110, 11, 40, 6, 6);
        gc.fillOval(-17, -156, 36, 36);

        switch (id) {
            case "bonifacio" -> gc.fillRoundRect(-19, -158, 40, 12, 4, 4);
            case "lapulapu" -> {
                gc.fillRoundRect(-24, -150, 11, 38, 5, 5);
                gc.fillRoundRect(15, -150, 11, 38, 5, 5);
            }
            case "luna" -> gc.fillRoundRect(-22, -164, 44, 12, 4, 4);
            case "mabini" -> gc.fillOval(-20, -160, 40, 16);
            default -> gc.fillOval(-19, -159, 38, 18);
        }

        switch (id) {
            case "rizal" -> gc.fillRoundRect(29, -96, 18, 24, 3, 3);
            case "bonifacio" -> gc.fillPolygon(
                    new double[]{27, 62, 66, 33},
                    new double[]{-82, -88, -76, -72}, 4);
            case "luna" -> {
                gc.save();
                gc.translate(28, -80);
                gc.rotate(-28);
                gc.fillRect(0, -3, 66, 6);
                gc.restore();
            }
            case "mabini" -> {
                gc.save();
                gc.translate(26, -58);
                gc.rotate(-28);
                gc.fillRect(0, -3, 58, 6);
                gc.restore();
            }
            case "lapulapu" -> gc.fillPolygon(
                    new double[]{27, 84, 90, 34},
                    new double[]{-80, -96, -88, -74}, 4);
            default -> {
            }
        }

        gc.setStroke(rim);
        gc.setLineWidth(3);
        gc.strokeOval(-17, -156, 36, 36);
        gc.restore();
    }

    private void drawTorch(GraphicsContext gc, double x, double groundY) {
        gc.setFill(Color.web("#3a2414"));
        gc.fillRect(x - 4, groundY - 120, 8, 120);
        gc.setFill(Color.web("#241408"));
        gc.fillRect(x - 12, groundY - 8, 24, 8);

        double flicker = 1 + 0.15 * Math.sin(elapsed * 12 + x);
        gc.setFill(Color.web("#ff8c32", 0.90));
        gc.fillOval(x - 10, groundY - 144 - 10 * flicker, 20, 30 * flicker);
        gc.setFill(Color.web("#ffd97a", 0.90));
        gc.fillOval(x - 6, groundY - 140 - 8 * flicker, 12, 20 * flicker);
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
