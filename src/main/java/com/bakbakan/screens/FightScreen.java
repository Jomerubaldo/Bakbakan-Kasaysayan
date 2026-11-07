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

public class FightScreen implements Screen {

    private static final double BUTTON_W = 170;
    private static final double BUTTON_H = 46;
    private static final double BACK_X = 40;
    private static final double BACK_Y = 646;
    private static final double MENU_X = 228;

    private final GameEventBus bus;
    private final GameSession session;
    private final StackPane root;
    private final Canvas canvas;

    private double elapsed;
    private boolean backHovered;
    private boolean menuHovered;

    public FightScreen(GameEventBus bus, GameSession session) {
        this.bus = bus;
        this.session = session;
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
        return "Fight";
    }

    @Override
    public void onShow() {
        elapsed = 0;
        backHovered = false;
        menuHovered = false;
        bus.fire(new SoundRequestEvent("battle", true));
        render();
    }

    @Override
    public void update(double dt) {
        elapsed += dt;
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        HeroData hero = session.getSelectedHero();
        Color accent = hero == null ? Color.web("#f4c542") : Color.web(hero.accentColor());
        Color primary = hero == null ? Color.web("#2E5E8C") : Color.web(hero.primaryColor());

        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0b1428")),
                new Stop(1, Color.web("#1c0d12"))));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);

        gc.setFill(new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 0.18));
        gc.fillOval(GameConfig.WIDTH / 2.0 - 280, 100, 560, 560);

        gc.setTextAlign(TextAlignment.CENTER);

        if (hero == null) {
            gc.setFill(Color.web("#f4c542"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 46));
            gc.fillText("No hero selected", GameConfig.WIDTH / 2.0, 300);
        } else {
            gc.setFill(accent);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 56));
            gc.fillText(hero.name(), GameConfig.WIDTH / 2.0, 280);

            gc.setFill(Color.web("#e8e8f0"));
            gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 22));
            gc.fillText(hero.weapon(), GameConfig.WIDTH / 2.0, 320);
        }

        double pulse = 0.65 + 0.35 * Math.sin(elapsed * 3.0);
        gc.setFill(Color.web("#ffffff", pulse));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        gc.fillText("Fight screen placeholder", GameConfig.WIDTH / 2.0, 400);

        gc.setFill(Color.web("#9aa4c0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 17));
        gc.fillText("Combat, rounds and CPU opponents arrive in Phase 3.",
                GameConfig.WIDTH / 2.0, 438);

        gc.setFill(Color.web("#7f8aa8"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        gc.fillText("Controls preview:  A/D move   W jump   S duck   J attack   K skill   L block   E special",
                GameConfig.WIDTH / 2.0, 500);

        drawButton(gc, BACK_X, BACK_Y, "Change Hero", backHovered);
        drawButton(gc, MENU_X, BACK_Y, "Main Menu", menuHovered);
    }

    private void drawButton(GraphicsContext gc, double x, double y,
                            String label, boolean hovered) {
        gc.setFill(hovered ? Color.web("#f4c542") : Color.web("#1c2745"));
        gc.fillRoundRect(x, y, BUTTON_W, BUTTON_H, 14, 14);
        gc.setStroke(hovered ? Color.web("#ffe89a") : Color.web("#34405f"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, BUTTON_W, BUTTON_H, 14, 14);

        gc.setFill(hovered ? Color.web("#0b1020") : Color.web("#e8e8f0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(label, x + BUTTON_W / 2.0, y + 30);
    }

    @Override
    public void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.BACK_SPACE) {
            backToHeroSelect();
        }
    }

    @Override
    public void onMouseMoved(MouseEvent event) {
        backHovered = inside(event.getX(), event.getY(), BACK_X, BACK_Y);
        menuHovered = inside(event.getX(), event.getY(), MENU_X, BACK_Y);
    }

    @Override
    public void onMouseClicked(MouseEvent event) {
        if (inside(event.getX(), event.getY(), BACK_X, BACK_Y)) {
            backToHeroSelect();
        } else if (inside(event.getX(), event.getY(), MENU_X, BACK_Y)) {
            bus.fire(new SoundRequestEvent("ui_click", false));
            bus.fire(new ScreenChangeEvent(getName(), "MainMenu", true));
        }
    }

    private boolean inside(double x, double y, double rx, double ry) {
        return x >= rx && x <= rx + BUTTON_W && y >= ry && y <= ry + BUTTON_H;
    }

    private void backToHeroSelect() {
        bus.fire(new SoundRequestEvent("ui_click", false));
        bus.fire(new ScreenChangeEvent(getName(), "HeroSelect", true));
    }
}
