package com.bakbakan.screens;

import com.bakbakan.assets.AssetManager;
import com.bakbakan.assets.SpriteProvider;
import com.bakbakan.combat.CombatConstants;
import com.bakbakan.combat.CombatSystem;
import com.bakbakan.combat.InputHandler;
import com.bakbakan.combat.SkillSystem;
import com.bakbakan.entities.Fighter;
import com.bakbakan.entities.GameSession;
import com.bakbakan.entities.HeroData;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.ScreenChangeEvent;
import com.bakbakan.events.SoundRequestEvent;
import com.bakbakan.ui.Hud;
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

    private static final String PLAYER_ID = "player";
    private static final String OPPONENT_ID = "opponent";
    private static final double BUTTON_W = 170;
    private static final double BUTTON_H = 46;
    private static final double BACK_X = 40;
    private static final double BACK_Y = 650;
    private static final double MENU_X = GameConfig.WIDTH - 40 - BUTTON_W;

    private final GameEventBus bus;
    private final GameSession session;
    private final SpriteProvider sprites;
    private final CombatSystem combat;
    private final SkillSystem skills;
    private final InputHandler input = new InputHandler();
    private final StackPane root;
    private final Canvas canvas;

    private Fighter player;
    private Fighter opponent;
    private Hud hud;
    private double elapsed;
    private boolean backHovered;
    private boolean menuHovered;
    private double noManaTimer;
    private double notReadyTimer;

    public FightScreen(GameEventBus bus, GameSession session, AssetManager assets) {
        this.bus = bus;
        this.session = session;
        this.sprites = new SpriteProvider(assets);
        this.combat = new CombatSystem(bus);
        this.skills = new SkillSystem(combat);
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
        HeroData hero = session.hasSelectedHero()
                ? session.getSelectedHero()
                : HeroData.ALL.get(0);

        player = new Fighter(PLAYER_ID, hero, 380, true, bus);
        opponent = new Fighter(OPPONENT_ID, HeroData.DUMMY, 900, false, bus);

        if (hud != null) {
            hud.detach(bus);
        }
        hud = new Hud(PLAYER_ID, OPPONENT_ID, hero.name(), "Training Dummy");
        hud.reset();
        hud.attach(bus);

        skills.clear();
        input.clear();
        elapsed = 0;
        backHovered = false;
        menuHovered = false;
        noManaTimer = 0;
        notReadyTimer = 0;
        bus.fire(new SoundRequestEvent("battle", true));
        render();
    }

    @Override
    public void onHide() {
        if (hud != null) {
            hud.detach(bus);
        }
        skills.clear();
        input.clear();
    }

    @Override
    public void update(double dt) {
        if (player == null || opponent == null) {
            return;
        }

        elapsed += dt;

        double direction = 0;
        if (input.isDown(KeyCode.D) || input.isDown(KeyCode.RIGHT)) {
            direction += 1;
        }
        if (input.isDown(KeyCode.A) || input.isDown(KeyCode.LEFT)) {
            direction -= 1;
        }

        boolean duck = input.isDown(KeyCode.S) || input.isDown(KeyCode.DOWN);
        boolean block = input.isDown(KeyCode.L);
        player.setMoveInput(direction, duck, block);

        if (input.consumePress(KeyCode.W) || input.consumePress(KeyCode.UP)) {
            if (player.tryJump()) {
                bus.fire(new SoundRequestEvent("jump", false));
            }
        }
        if (input.consumePress(KeyCode.J)) {
            if (player.tryAttack() && player.usesProjectileBasicAttack()) {
                skills.castRizalBook(player, opponent);
            }
        }

        if (input.consumePress(KeyCode.K)) {
            if (player.getMana() + 1e-6 < CombatConstants.SKILL_MANA_COST) {
                noManaTimer = 0.9;
                bus.fire(new SoundRequestEvent("no_mana", false));
            } else if (player.trySkill()) {
                skills.castSkill(player, opponent);
            }
        }

        if (input.consumePress(KeyCode.E)) {
            if (player.trySpecial()) {
                skills.castSpecial(player, opponent);
            } else if (!player.isSpecialReady()) {
                notReadyTimer = 0.9;
            }
        }

        player.update(dt, opponent.getX());

        opponent.setMoveInput(0, false, false);
        opponent.update(dt, player.getX());

        separateFighters();
        combat.resolve(player, opponent);
        combat.resolve(opponent, player);
        skills.update(dt, player, opponent);

        noManaTimer = Math.max(0, noManaTimer - dt);
        notReadyTimer = Math.max(0, notReadyTimer - dt);

        input.endFrame();
    }

    private void separateFighters() {
        double minDistance = CombatConstants.FIGHTER_WIDTH * 0.85;
        double dx = opponent.getX() - player.getX();
        if (Math.abs(dx) >= minDistance) {
            return;
        }
        double direction = dx == 0 ? 1 : Math.signum(dx);
        double push = (minDistance - Math.abs(dx)) / 2.0;
        player.setPosition(clampX(player.getX() - direction * push), player.getY());
        opponent.setPosition(clampX(opponent.getX() + direction * push), opponent.getY());
    }

    private double clampX(double x) {
        return Math.max(CombatConstants.MIN_X, Math.min(CombatConstants.MAX_X, x));
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        drawBackground(gc);

        if (player != null && opponent != null) {
            drawShadow(gc, opponent);
            drawShadow(gc, player);
            sprites.render(gc, opponent, elapsed);
            sprites.render(gc, player, elapsed);
            skills.render(gc);
        }

        if (hud != null) {
            hud.render(gc);
        }

        drawFightBanner(gc);
        drawCombatFeedback(gc);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#c7d0e8"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        gc.fillText("A/D Move   W Jump   S Duck   J Attack   K Skill   L Block   E Special   (Esc/P pause in Phase 6)",
                GameConfig.WIDTH / 2.0, 712);

        drawButton(gc, BACK_X, BACK_Y, "Change Hero", backHovered);
        drawButton(gc, MENU_X, BACK_Y, "Main Menu", menuHovered);
    }

    private void drawBackground(GraphicsContext gc) {
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1b2a4a")),
                new Stop(0.55, Color.web("#7a4a5a")),
                new Stop(1, Color.web("#d98555"))));
        gc.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);

        gc.setFill(Color.web("#f4c542", 0.18));
        gc.fillOval(920, 80, 230, 230);
        gc.setFill(Color.web("#f4c542"));
        gc.fillOval(980, 140, 110, 110);

        gc.setFill(Color.web("#ffffff", 0.16));
        gc.fillOval(120, 120, 220, 60);
        gc.fillOval(220, 100, 140, 50);
        gc.fillOval(760, 230, 180, 50);

        gc.setFill(Color.web("#22324a"));
        gc.fillPolygon(
                new double[]{0, 220, 430, 700, 950, GameConfig.WIDTH},
                new double[]{560, 380, 560, 340, 560, 560}, 6);

        gc.setFill(Color.web("#1a2740"));
        gc.fillPolygon(
                new double[]{0, 300, 560, 820, 1080, GameConfig.WIDTH},
                new double[]{600, 470, 600, 430, 600, 600}, 6);

        gc.setFill(Color.web("#3a2a1e"));
        gc.fillRect(0, CombatConstants.GROUND_Y, GameConfig.WIDTH,
                GameConfig.HEIGHT - CombatConstants.GROUND_Y);

        gc.setFill(Color.web("#4c8f4a"));
        gc.fillRect(0, CombatConstants.GROUND_Y - 8, GameConfig.WIDTH, 10);

        gc.setStroke(Color.web("#5a3f2b", 0.5));
        gc.setLineWidth(1);
        for (double x = 80; x < GameConfig.WIDTH; x += 120) {
            gc.strokeLine(x, CombatConstants.GROUND_Y + 10, x, GameConfig.HEIGHT);
        }
    }

    private void drawShadow(GraphicsContext gc, Fighter fighter) {
        double distance = Math.max(0, CombatConstants.GROUND_Y - fighter.getY());
        double scale = Math.max(0.35, 1 - distance / 500.0);
        gc.setFill(Color.web("#000000", 0.25));
        gc.fillOval(fighter.getX() - 40 * scale,
                CombatConstants.GROUND_Y - 6,
                80 * scale,
                16 * scale);
    }

    private void drawFightBanner(GraphicsContext gc) {
        if (elapsed >= 1.2) {
            return;
        }
        double alpha = elapsed < 0.8
                ? Math.min(1, elapsed * 5)
                : Math.max(0, 1 - (elapsed - 0.8) / 0.4);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#f4c542", alpha));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 72));
        gc.fillText("FIGHT!", GameConfig.WIDTH / 2.0, 320);
    }

    private void drawCombatFeedback(GraphicsContext gc) {
        if (player == null) {
            return;
        }
        if (noManaTimer > 0) {
            drawFloatingText(gc, "NO MANA", player.getX(), player.getY() - 190,
                    noManaTimer, Color.web("#ff6b6b"));
        }
        if (notReadyTimer > 0) {
            drawFloatingText(gc, "SPECIAL NOT READY", player.getX(),
                    player.getY() - 214, notReadyTimer, Color.web("#f4c542"));
        }
    }

    private void drawFloatingText(GraphicsContext gc, String text, double x, double y,
                                  double timer, Color color) {
        double alpha = Math.min(1, timer / 0.25);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        gc.setFill(Color.web("#0a0f1e", alpha * 0.8));
        gc.fillText(text, x + 2, y + 2);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        gc.fillText(text, x, y);
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
        input.keyPressed(event.getCode());
    }

    @Override
    public void onKeyReleased(KeyEvent event) {
        input.keyReleased(event.getCode());
    }

    @Override
    public void onMouseMoved(MouseEvent event) {
        backHovered = inside(event.getX(), event.getY(), BACK_X, BACK_Y);
        menuHovered = inside(event.getX(), event.getY(), MENU_X, BACK_Y);
    }

    @Override
    public void onMouseClicked(MouseEvent event) {
        if (inside(event.getX(), event.getY(), BACK_X, BACK_Y)) {
            bus.fire(new SoundRequestEvent("ui_click", false));
            bus.fire(new ScreenChangeEvent(getName(), "HeroSelect", true));
        } else if (inside(event.getX(), event.getY(), MENU_X, BACK_Y)) {
            bus.fire(new SoundRequestEvent("ui_click", false));
            bus.fire(new ScreenChangeEvent(getName(), "MainMenu", true));
        }
    }

    private boolean inside(double x, double y, double rx, double ry) {
        return x >= rx && x <= rx + BUTTON_W && y >= ry && y <= ry + BUTTON_H;
    }
}
