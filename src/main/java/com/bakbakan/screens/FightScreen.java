package com.bakbakan.screens;

import com.bakbakan.ai.EnemyAI;
import com.bakbakan.assets.AssetManager;
import com.bakbakan.assets.SpriteProvider;
import com.bakbakan.combat.CombatConstants;
import com.bakbakan.combat.CombatSystem;
import com.bakbakan.combat.InputHandler;
import com.bakbakan.combat.RoundManager;
import com.bakbakan.combat.SkillSystem;
import com.bakbakan.entities.EnemyData;
import com.bakbakan.entities.Fighter;
import com.bakbakan.entities.GameSession;
import com.bakbakan.entities.HeroData;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.GameOverEvent;
import com.bakbakan.events.PauseEvent;
import com.bakbakan.events.ResumeEvent;
import com.bakbakan.events.RoundEndEvent;
import com.bakbakan.events.ScreenChangeEvent;
import com.bakbakan.events.SoundRequestEvent;
import com.bakbakan.events.TimeOutEvent;
import com.bakbakan.events.VictoryEvent;
import com.bakbakan.ui.Hud;
import com.bakbakan.util.GameConfig;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    private static final double PAUSE_BUTTON_W = 300;
    private static final double PAUSE_BUTTON_H = 54;

    private final GameEventBus bus;
    private final GameSession session;
    private final SpriteProvider sprites;
    private final CombatSystem combat;
    private final SkillSystem skills;
    private final InputHandler input = new InputHandler();
    private final StackPane root;
    private final Canvas canvas;
    private final StackPane pauseOverlay;
    private final Button resumeButton;
    private final Button quitButton;

    private final EventHandler<RoundEndEvent> roundEndHandler = this::onRoundEnd;
    private final EventHandler<GameOverEvent> gameOverHandler = this::onGameOver;
    private final EventHandler<VictoryEvent> victoryHandler = this::onVictory;
    private final EventHandler<TimeOutEvent> timeOutHandler = this::onTimeOut;
    private final EventHandler<PauseEvent> pauseHandler = this::onPause;
    private final EventHandler<ResumeEvent> resumeHandler = this::onResume;

    private Fighter player;
    private Fighter opponent;
    private EnemyData enemyData;
    private EnemyAI enemyAI;
    private RoundManager roundManager;
    private Hud hud;
    private double elapsed;
    private boolean backHovered;
    private boolean menuHovered;
    private double noManaTimer;
    private double notReadyTimer;
    private double timeOutTimer;
    private double resultTimer;
    private String resultScreen;
    private boolean resultHandlersAttached;
    private boolean pauseHandlersAttached;
    private boolean paused;
    private int pauseSelection;

    public FightScreen(GameEventBus bus, GameSession session, AssetManager assets) {
        this.bus = bus;
        this.session = session;
        this.sprites = new SpriteProvider(assets);
        this.combat = new CombatSystem(bus);
        this.skills = new SkillSystem(combat);
        this.canvas = new Canvas(GameConfig.WIDTH, GameConfig.HEIGHT);
        this.resumeButton = createPauseButton("Resume");
        this.quitButton = createPauseButton("Quit to Main Menu");
        this.pauseOverlay = buildPauseOverlay();
        this.root = new StackPane(canvas, pauseOverlay);
        this.root.setStyle("-fx-background-color: #0b1020;");
    }

    private StackPane buildPauseOverlay() {
        Label title = new Label("PAUSED");
        title.setStyle("-fx-text-fill: #f4c542; -fx-font-family: 'Segoe UI';"
                + " -fx-font-size: 58px; -fx-font-weight: bold;");

        Label subtitle = new Label("The battle is frozen. Take a breath.");
        subtitle.setStyle("-fx-text-fill: #c7d0e8; -fx-font-family: 'Segoe UI';"
                + " -fx-font-size: 18px;");

        Label hint = new Label("Esc/P to resume   |   Up/Down + Enter to choose");
        hint.setStyle("-fx-text-fill: #8f9ab8; -fx-font-family: 'Segoe UI';"
                + " -fx-font-size: 13px;");

        resumeButton.setOnAction(event -> requestResume());
        quitButton.setOnAction(event -> quitToMainMenu());
        resumeButton.setOnMouseEntered(event -> {
            pauseSelection = 0;
            updatePauseSelection();
        });
        quitButton.setOnMouseEntered(event -> {
            pauseSelection = 1;
            updatePauseSelection();
        });

        VBox box = new VBox(18, title, subtitle, resumeButton, quitButton, hint);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(460);
        box.setPadding(new Insets(32));

        StackPane overlay = new StackPane(box);
        overlay.setStyle("-fx-background-color: rgba(5, 8, 20, 0.80);");
        overlay.setVisible(false);
        return overlay;
    }

    private Button createPauseButton(String text) {
        Button button = new Button(text);
        button.setFocusTraversable(false);
        button.setPrefSize(PAUSE_BUTTON_W, PAUSE_BUTTON_H);
        button.setStyle(pauseButtonStyle(false));
        return button;
    }

    private void updatePauseSelection() {
        resumeButton.setStyle(pauseButtonStyle(pauseSelection == 0));
        quitButton.setStyle(pauseButtonStyle(pauseSelection == 1));
    }

    private String pauseButtonStyle(boolean selected) {
        return "-fx-background-color: " + (selected ? "#f4c542" : "#1c2745") + ";"
                + "-fx-text-fill: " + (selected ? "#0b1020" : "#e8e8f0") + ";"
                + "-fx-font-family: 'Segoe UI'; -fx-font-size: 20px;"
                + " -fx-font-weight: bold; -fx-background-radius: 14;"
                + " -fx-border-radius: 14; -fx-border-width: 2;"
                + " -fx-border-color: " + (selected ? "#ffe89a" : "#34405f") + ";"
                + " -fx-cursor: hand;";
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
        int round = session.getCurrentRound();
        enemyData = EnemyData.forRound(round, hero);

        player = new Fighter(PLAYER_ID, hero, 380, true, bus);
        opponent = new Fighter(OPPONENT_ID, enemyData.moveset(), 900, false, bus,
                enemyData.maxHealth(), enemyData.damageMultiplier());

        if (roundManager != null) {
            roundManager.detach();
            roundManager = null;
        }
        if (hud != null) {
            hud.detach(bus);
        }
        hud = new Hud(PLAYER_ID, OPPONENT_ID, hero.name(), enemyData.name(),
                CombatConstants.MAX_HEALTH, enemyData.maxHealth(), enemyData.boss());
        hud.attach(bus);

        skills.clear();
        input.clear();
        paused = false;
        pauseSelection = 0;
        pauseOverlay.setVisible(false);
        updatePauseSelection();
        attachResultHandlers();
        attachPauseHandlers();

        roundManager = new RoundManager(bus, session);
        roundManager.attach();
        enemyAI = new EnemyAI(opponent, player, skills, round);
        roundManager.startRound(player, opponent, enemyData);

        elapsed = 0;
        backHovered = false;
        menuHovered = false;
        noManaTimer = 0;
        notReadyTimer = 0;
        timeOutTimer = 0;
        resultTimer = 0;
        resultScreen = null;
        bus.fire(new SoundRequestEvent("battle", true));
        render();
    }

    @Override
    public void onHide() {
        paused = false;
        pauseOverlay.setVisible(false);
        detachPauseHandlers();
        detachResultHandlers();
        if (roundManager != null) {
            roundManager.detach();
            roundManager = null;
        }
        if (hud != null) {
            hud.detach(bus);
        }
        skills.clear();
        input.clear();
        enemyAI = null;
    }

    @Override
    public void update(double dt) {
        if (player == null || opponent == null) {
            return;
        }

        if (paused) {
            input.endFrame();
            return;
        }

        elapsed += dt;
        noManaTimer = Math.max(0, noManaTimer - dt);
        notReadyTimer = Math.max(0, notReadyTimer - dt);
        timeOutTimer = Math.max(0, timeOutTimer - dt);

        if (resultScreen != null) {
            resultTimer -= dt;
            input.endFrame();
            if (resultTimer <= 0) {
                String target = resultScreen;
                resultScreen = null;
                bus.fire(new ScreenChangeEvent(getName(), target, true));
            }
            return;
        }

        if (roundManager == null || !roundManager.isRunning()) {
            input.endFrame();
            return;
        }

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

        if (enemyAI != null) {
            enemyAI.update(dt);
        }

        player.update(dt, opponent.getX());
        opponent.update(dt, player.getX());

        separateFighters();
        if (resultScreen == null) {
            combat.resolve(player, opponent);
        }
        if (resultScreen == null) {
            combat.resolve(opponent, player);
        }
        if (resultScreen == null) {
            skills.update(dt, player, opponent);
        }
        if (roundManager != null) {
            roundManager.update(dt);
        }

        input.endFrame();
    }

    private void attachResultHandlers() {
        if (resultHandlersAttached) {
            return;
        }
        bus.register(RoundEndEvent.ROUND_END, roundEndHandler);
        bus.register(GameOverEvent.GAME_OVER, gameOverHandler);
        bus.register(VictoryEvent.VICTORY, victoryHandler);
        bus.register(TimeOutEvent.TIME_OUT, timeOutHandler);
        resultHandlersAttached = true;
    }

    private void detachResultHandlers() {
        if (!resultHandlersAttached) {
            return;
        }
        bus.unregister(RoundEndEvent.ROUND_END, roundEndHandler);
        bus.unregister(GameOverEvent.GAME_OVER, gameOverHandler);
        bus.unregister(VictoryEvent.VICTORY, victoryHandler);
        bus.unregister(TimeOutEvent.TIME_OUT, timeOutHandler);
        resultHandlersAttached = false;
    }

    private void attachPauseHandlers() {
        if (pauseHandlersAttached) {
            return;
        }
        bus.register(PauseEvent.PAUSE, pauseHandler);
        bus.register(ResumeEvent.RESUME, resumeHandler);
        pauseHandlersAttached = true;
    }

    private void detachPauseHandlers() {
        if (!pauseHandlersAttached) {
            return;
        }
        bus.unregister(PauseEvent.PAUSE, pauseHandler);
        bus.unregister(ResumeEvent.RESUME, resumeHandler);
        pauseHandlersAttached = false;
    }

    private void onPause(PauseEvent event) {
        if (paused) {
            return;
        }
        paused = true;
        pauseSelection = 0;
        input.clear();
        if (player != null) {
            player.setMoveInput(0, false, false);
        }
        if (opponent != null) {
            opponent.setMoveInput(0, false, false);
        }
        updatePauseSelection();
        pauseOverlay.setVisible(true);
        pauseOverlay.toFront();
    }

    private void onResume(ResumeEvent event) {
        if (!paused) {
            return;
        }
        paused = false;
        input.clear();
        if (player != null) {
            player.setMoveInput(0, false, false);
        }
        if (opponent != null) {
            opponent.setMoveInput(0, false, false);
        }
        pauseOverlay.setVisible(false);
    }

    private void requestPause() {
        if (!paused) {
            bus.fire(new PauseEvent());
        }
    }

    private void requestResume() {
        if (paused) {
            bus.fire(new SoundRequestEvent("ui_click", false));
            bus.fire(new ResumeEvent());
        }
    }

    private void togglePause() {
        if (paused) {
            requestResume();
        } else {
            requestPause();
        }
    }

    private void movePauseSelection(int delta) {
        pauseSelection = Math.floorMod(pauseSelection + delta, 2);
        updatePauseSelection();
    }

    private void activatePauseSelection() {
        if (pauseSelection == 0) {
            requestResume();
        } else {
            quitToMainMenu();
        }
    }

    private void quitToMainMenu() {
        bus.fire(new SoundRequestEvent("ui_click", false));
        if (paused) {
            bus.fire(new ResumeEvent());
        }
        bus.fire(new ScreenChangeEvent(getName(), "MainMenu", true));
    }

    private void onRoundEnd(RoundEndEvent event) {
        if (resultScreen != null) {
            return;
        }
        if (!event.isPlayerWon()) {
            startResult("GameOver");
        } else if (event.getRoundNumber() >= CombatConstants.TOTAL_ROUNDS) {
            startResult("Victory");
        } else {
            startResult("RoundWin");
        }
    }

    private void onGameOver(GameOverEvent event) {
        startResult("GameOver");
    }

    private void onVictory(VictoryEvent event) {
        startResult("Victory");
    }

    private void onTimeOut(TimeOutEvent event) {
        timeOutTimer = 1.4;
    }

    private void startResult(String screen) {
        if (resultScreen != null) {
            return;
        }
        resultScreen = screen;
        resultTimer = 1.35;
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
        gc.fillText("A/D Move   W Jump   S Duck   J Attack   K Skill   L Block   E Special   Esc/P Pause",
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
        if (enemyData != null && enemyData.boss()) {
            drawBossBanner(gc);
            return;
        }
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

    private void drawBossBanner(GraphicsContext gc) {
        if (elapsed >= 3.2) {
            return;
        }
        double alpha = elapsed < 0.6
                ? Math.min(1, elapsed / 0.35)
                : elapsed < 2.5
                ? 1
                : Math.max(0, 1 - (elapsed - 2.5) / 0.7);

        gc.setFill(Color.web("#0a0f1e", 0.58 * alpha));
        gc.fillRect(0, 178, GameConfig.WIDTH, 168);
        gc.setStroke(Color.web("#ff6b6b", 0.8 * alpha));
        gc.setLineWidth(3);
        gc.strokeLine(240, 346, GameConfig.WIDTH - 240, 346);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#ff6b6b", alpha));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 76));
        gc.fillText("BOSS", GameConfig.WIDTH / 2.0, 262);

        gc.setFill(Color.web("#f4c542", alpha));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 25));
        gc.fillText(enemyData.name(), GameConfig.WIDTH / 2.0, 302);

        gc.setFill(Color.web("#e8e8f0", 0.9 * alpha));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        gc.fillText("ROUND 5 - FINAL BATTLE", GameConfig.WIDTH / 2.0, 330);
    }

    private void drawCombatFeedback(GraphicsContext gc) {
        if (player == null) {
            return;
        }
        if (timeOutTimer > 0) {
            double alpha = Math.min(1, timeOutTimer / 0.25);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 54));
            gc.setFill(Color.web("#0a0f1e", alpha * 0.8));
            gc.fillText("TIME OUT", GameConfig.WIDTH / 2.0 + 3, 303);
            gc.setFill(Color.web("#f4c542", alpha));
            gc.fillText("TIME OUT", GameConfig.WIDTH / 2.0, 300);
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
        KeyCode code = event.getCode();
        if (code == KeyCode.ESCAPE || code == KeyCode.P) {
            togglePause();
            event.consume();
            return;
        }
        if (paused) {
            if (code == KeyCode.UP || code == KeyCode.W) {
                movePauseSelection(-1);
            } else if (code == KeyCode.DOWN || code == KeyCode.S) {
                movePauseSelection(1);
            } else if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
                activatePauseSelection();
            }
            event.consume();
            return;
        }
        input.keyPressed(code);
    }

    @Override
    public void onKeyReleased(KeyEvent event) {
        if (!paused) {
            input.keyReleased(event.getCode());
        }
    }

    @Override
    public void onMouseMoved(MouseEvent event) {
        if (paused) {
            return;
        }
        backHovered = inside(event.getX(), event.getY(), BACK_X, BACK_Y);
        menuHovered = inside(event.getX(), event.getY(), MENU_X, BACK_Y);
    }

    @Override
    public void onMouseClicked(MouseEvent event) {
        if (paused) {
            return;
        }
        if (inside(event.getX(), event.getY(), BACK_X, BACK_Y)) {
            bus.fire(new SoundRequestEvent("ui_click", false));
            bus.fire(new ScreenChangeEvent(getName(), "HeroSelect", true));
        } else if (inside(event.getX(), event.getY(), MENU_X, BACK_Y)) {
            bus.fire(new SoundRequestEvent("ui_click", false));
            bus.fire(new ScreenChangeEvent(getName(), "MainMenu", true));
        }
    }

    @Override
    public void onFocusLost() {
        input.clear();
        if (player != null) {
            player.setMoveInput(0, false, false);
        }
        if (opponent != null) {
            opponent.setMoveInput(0, false, false);
        }
    }

    private boolean inside(double x, double y, double rx, double ry) {
        return x >= rx && x <= rx + BUTTON_W && y >= ry && y <= ry + BUTTON_H;
    }
}
