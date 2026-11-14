package com.bakbakan.screens;

import com.bakbakan.assets.AssetManager;
import com.bakbakan.assets.SpriteProvider;
import com.bakbakan.entities.HeroData;
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
import javafx.scene.paint.RadialGradient;
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
    private final SpriteProvider sprites;
    private final StackPane root;
    private final Canvas canvas;

    private int selected;
    private double elapsed;

    public MainMenuScreen(GameEventBus bus, AssetManager assets) {
        this.bus = bus;
        this.sprites = new SpriteProvider(assets);
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
        gc.fillPolygon(
                new double[]{0, 170, 340, 520, 700, 900, 1090, 1280, 1280, 0},
                new double[]{560, 400, 520, 360, 500, 420, 540, 470, 640, 640}, 10);
        gc.setFill(Color.web("#1c1828"));
        gc.fillPolygon(
                new double[]{0, 220, 430, 700, 950, 1280, 1280, 0},
                new double[]{600, 500, 600, 460, 590, 600, 660, 660}, 8);

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
        gc.fillPolygon(
                new double[]{0, 160, 340, 520, 720, 980, 1280, 1280, 0},
                new double[]{666, 638, 660, 634, 666, 626, 654, 720, 720}, 9);

        drawHeroPortrait(gc, HeroData.byId("bonifacio"), 140, 672, 1.00, true);
        drawHeroPortrait(gc, HeroData.byId("lapulapu"), 292, 672, 1.06, true);
        drawHeroPortrait(gc, HeroData.byId("rizal"), 440, 672, 0.98, true);
        drawHeroPortrait(gc, HeroData.byId("luna"), 840, 672, 1.02, false);
        drawHeroPortrait(gc, HeroData.byId("mabini"), 1000, 672, 0.98, false);

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

    private void drawHeroPortrait(GraphicsContext gc, HeroData hero, double x,
                                  double feetY, double scale, boolean facingRight) {
        if (hero == null) {
            return;
        }
        Color accent = Color.web(hero.accentColor());
        Color primary = Color.web(hero.primaryColor());

        gc.setFill(new RadialGradient(0, 0, x, feetY - 44, 150, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, new Color(accent.getRed(), accent.getGreen(),
                        accent.getBlue(), 0.26)),
                new Stop(0.55, new Color(primary.getRed(), primary.getGreen(),
                        primary.getBlue(), 0.12)),
                new Stop(1, new Color(accent.getRed(), accent.getGreen(),
                        accent.getBlue(), 0))));
        gc.fillOval(x - 150, feetY - 194, 300, 300);

        gc.setFill(Color.web("#0b1220", 0.60));
        gc.fillOval(x - 52, feetY - 8, 104, 20);
        gc.setStroke(new Color(accent.getRed(), accent.getGreen(),
                accent.getBlue(), 0.40));
        gc.setLineWidth(2);
        gc.strokeOval(x - 52, feetY - 8, 104, 20);

        sprites.renderPortrait(gc, hero, x, feetY, scale, facingRight);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#0a0f1e", 0.7));
        gc.fillText(hero.name().toUpperCase(), x + 1, feetY + 25);
        gc.setFill(accent);
        gc.fillText(hero.name().toUpperCase(), x, feetY + 24);
    }

    private void drawTorch(GraphicsContext gc, double x, double groundY) {
        double topY = groundY - 118;

        gc.setFill(new LinearGradient(x - 6, 0, x + 6, 0, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#241408")),
                new Stop(0.42, Color.web("#5a3a20")),
                new Stop(0.68, Color.web("#7a522c")),
                new Stop(1, Color.web("#1c1008"))));
        gc.fillPolygon(new double[]{x - 5, x + 5, x + 6, x - 6},
                new double[]{groundY, groundY, topY, topY}, 4);

        gc.setStroke(Color.web("#1c1008", 0.55));
        gc.setLineWidth(1);
        for (int i = 0; i < 3; i++) {
            double gx = x - 3 + i * 3;
            gc.strokeLine(gx, groundY - 10, gx + (i - 1), topY + 18);
        }
        gc.setFill(Color.web("#1a0f08", 0.7));
        gc.fillOval(x - 1, groundY - 42, 4, 7);
        gc.fillOval(x, groundY - 80, 3, 6);

        gc.setStroke(Color.web("#8a6a3a"));
        gc.setLineWidth(2);
        for (int i = 0; i < 3; i++) {
            double ry = topY + 16 + i * 7;
            gc.strokeLine(x - 7, ry + 2, x + 7, ry);
        }

        gc.setFill(Color.web("#3a3a42"));
        gc.fillPolygon(new double[]{x - 12, x + 12, x + 8, x - 8},
                new double[]{topY + 4, topY + 4, topY - 16, topY - 16}, 4);
        gc.setFill(Color.web("#5a5a66"));
        gc.fillRect(x - 8, topY - 18, 16, 3);

        gc.setFill(Color.web("#1a0f08"));
        gc.fillOval(x - 9, topY - 22, 18, 8);

        double flicker = 0.85 + 0.15 * Math.sin(elapsed * 11 + x);
        double sway = Math.sin(elapsed * 6.3 + x * 0.5) * 3
                + Math.sin(elapsed * 13.7) * 1.5;

        gc.setFill(new RadialGradient(0, 0, x, topY - 34, 76 * flicker, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ffb347", 0.34 * flicker)),
                new Stop(0.5, Color.web("#ff7a18", 0.14 * flicker)),
                new Stop(1, Color.web("#ff7a18", 0))));
        gc.fillOval(x - 76, topY - 110, 152, 152);

        gc.setFill(new RadialGradient(0, 0, x, groundY, 100, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ff8c32", 0.20 * flicker)),
                new Stop(1, Color.web("#ff8c32", 0))));
        gc.fillOval(x - 100, groundY - 26, 200, 52);

        flame(gc, x, topY - 14, 24 * flicker, 54 * flicker, sway,
                Color.web("#ff6a00", 0.90));
        flame(gc, x, topY - 16, 16 * flicker, 43 * flicker, sway * 0.8,
                Color.web("#ffb020", 0.95));
        flame(gc, x, topY - 18, 10 * flicker, 31 * flicker, sway * 0.6,
                Color.web("#ffe9a8", 0.95));
        flame(gc, x, topY - 20, 5 * flicker, 18 * flicker, sway * 0.4,
                Color.web("#fffdf2", 0.95));

        for (int i = 0; i < 7; i++) {
            double phase = (elapsed * (0.7 + i * 0.13) + i * 0.37) % 1.0;
            double ex = x + Math.sin(elapsed * 3 + i * 2.3) * (6 + i * 2);
            double ey = topY - 30 - phase * (50 + i * 8);
            double alpha = 1 - phase;
            gc.setFill(Color.web("#ffca63", 0.7 * alpha));
            gc.fillOval(ex, ey, 2 + (i % 2), 2 + (i % 2));
        }
    }

    private void flame(GraphicsContext gc, double cx, double baseY, double width,
                       double height, double sway, Color color) {
        gc.setFill(color);
        gc.beginPath();
        gc.moveTo(cx - width / 2, baseY);
        gc.bezierCurveTo(cx - width * 0.62, baseY - height * 0.34,
                cx - width * 0.18 + sway * 0.5, baseY - height * 0.66,
                cx + sway, baseY - height);
        gc.bezierCurveTo(cx + width * 0.20 + sway * 0.6, baseY - height * 0.62,
                cx + width * 0.60, baseY - height * 0.30,
                cx + width / 2, baseY);
        gc.bezierCurveTo(cx + width * 0.2, baseY + height * 0.06,
                cx - width * 0.2, baseY + height * 0.06,
                cx - width / 2, baseY);
        gc.closePath();
        gc.fill();
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
