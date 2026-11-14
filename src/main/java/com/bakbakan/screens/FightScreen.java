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
import com.bakbakan.events.HitEvent;
import com.bakbakan.events.PauseEvent;
import com.bakbakan.events.ResumeEvent;
import com.bakbakan.events.RoundEndEvent;
import com.bakbakan.events.ScreenChangeEvent;
import com.bakbakan.events.SoundRequestEvent;
import com.bakbakan.events.TimeOutEvent;
import com.bakbakan.events.VictoryEvent;
import com.bakbakan.ui.BackdropArt;
import com.bakbakan.ui.FinalWarningCinematic;
import com.bakbakan.ui.Hud;
import com.bakbakan.ui.ImpactCamera;
import com.bakbakan.ui.SpecialCinematic;
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
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private final SpecialCinematic cinematic = new SpecialCinematic();
    private final FinalWarningCinematic finalWarning = new FinalWarningCinematic();
    private final ImpactCamera impactCamera = new ImpactCamera();
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
    private final EventHandler<HitEvent> hitHandler = this::onHit;
    private final List<BloodParticle> bloodParticles = new ArrayList<>();
    private final Random bloodRandom = new Random();

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
    private boolean hitHandlerAttached;
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
        opponent.setSpriteId(enemyData.spriteId());

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
        bloodParticles.clear();
        input.clear();
        paused = false;
        pauseSelection = 0;
        cinematic.stop();
        finalWarning.stop();
        impactCamera.stop();
        pauseOverlay.setVisible(false);
        updatePauseSelection();
        attachResultHandlers();
        attachPauseHandlers();
        attachHitHandlers();

        roundManager = new RoundManager(bus, session);
        roundManager.attach();
        enemyAI = new EnemyAI(opponent, player, skills, round);
        roundManager.startRound(player, opponent, enemyData);
        if (round >= CombatConstants.TOTAL_ROUNDS) {
            finalWarning.start(hero, enemyData.moveset(), enemyData.spriteId());
        }

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
        detachHitHandlers();
        if (roundManager != null) {
            roundManager.detach();
            roundManager = null;
        }
        if (hud != null) {
            hud.detach(bus);
        }
        skills.clear();
        bloodParticles.clear();
        input.clear();
        cinematic.stop();
        finalWarning.stop();
        impactCamera.stop();
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

        cinematic.update(dt);
        if (cinematic.isActive()) {
            input.endFrame();
            return;
        }

        finalWarning.update(dt);
        if (finalWarning.isActive()) {
            input.endFrame();
            return;
        }

        impactCamera.update(dt);
        double worldDt = dt * impactCamera.timeScale();

        elapsed += worldDt;
        noManaTimer = Math.max(0, noManaTimer - dt);
        notReadyTimer = Math.max(0, notReadyTimer - dt);
        timeOutTimer = Math.max(0, timeOutTimer - dt);

        if (resultScreen != null) {
            resultTimer -= dt;
            updateFallenFighters(dt);
            updateBlood(dt);
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
        if (input.isDown(KeyCode.D)) {
            direction += 1;
        }
        if (input.isDown(KeyCode.A)) {
            direction -= 1;
        }

        boolean duck = input.isDown(KeyCode.S);
        boolean block = input.isDown(KeyCode.L);
        player.setMoveInput(direction, duck, block);

        if (input.consumePress(KeyCode.SPACE)) {
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

        if (input.consumePress(KeyCode.R)) {
            if (player.trySpecial()) {
                skills.castSpecial(player, opponent);
                cinematic.start(player.getHero());
            } else if (!player.isSpecialReady()) {
                notReadyTimer = 0.9;
            }
        }

        if (enemyAI != null) {
            enemyAI.update(worldDt);
        }

        player.update(worldDt, opponent.getX());
        opponent.update(worldDt, player.getX());

        separateFighters();
        if (resultScreen == null) {
            combat.resolve(player, opponent);
        }
        if (resultScreen == null) {
            combat.resolve(opponent, player);
        }
        if (resultScreen == null) {
            skills.update(worldDt, player, opponent);
        }
        updateBlood(worldDt);
        for (SkillSystem.Impact impact : skills.drainImpacts()) {
            Fighter caster = impact.attacker();
            Fighter target = caster == player ? opponent : player;
            double focusX = (caster.getX() + target.getX()) / 2.0;
            impactCamera.trigger(focusX, 470, impact.special(),
                    Color.web(caster.getHero().accentColor()));
        }
        if (roundManager != null) {
            roundManager.update(worldDt);
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

    private void attachHitHandlers() {
        if (hitHandlerAttached) {
            return;
        }
        bus.register(HitEvent.HIT, hitHandler);
        hitHandlerAttached = true;
    }

    private void detachHitHandlers() {
        if (!hitHandlerAttached) {
            return;
        }
        bus.unregister(HitEvent.HIT, hitHandler);
        hitHandlerAttached = false;
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

    private void onHit(HitEvent event) {
        if (player == null || opponent == null || event.getDamage() <= 0) {
            return;
        }
        Fighter target;
        if (player.getId().equals(event.getTargetId())) {
            target = player;
        } else if (opponent.getId().equals(event.getTargetId())) {
            target = opponent;
        } else {
            return;
        }
        Fighter attacker = player.getId().equals(event.getAttackerId())
                ? player
                : opponent;
        boolean special = event.isSpecial();
        double direction = target.getX() >= attacker.getX() ? 1 : -1;
        double baseX = target.getX() - direction * 8;
        double baseY = target.isAlive()
                ? target.getY() - target.getCurrentHeight() * 0.62
                : target.getY() - 22;
        int count = event.isBlocked()
                ? 3 + bloodRandom.nextInt(3)
                : special
                        ? Math.min(90, 34 + (int) (event.getDamage() * 1.6)
                                + bloodRandom.nextInt(9))
                        : Math.min(22, 8 + (int) (event.getDamage() * 0.55)
                                + bloodRandom.nextInt(5));
        for (int i = 0; i < count; i++) {
            BloodParticle drop = new BloodParticle();
            double spreadX = special ? 62 : 24;
            double spreadY = special ? 96 : 44;
            drop.x = baseX + (bloodRandom.nextDouble() - 0.5) * spreadX;
            drop.y = baseY + (bloodRandom.nextDouble() - 0.5) * spreadY;
            if (bloodRandom.nextDouble() < (special ? 0.78 : 0.65)) {
                double speed = special
                        ? 150 + bloodRandom.nextDouble() * 380
                        : 80 + bloodRandom.nextDouble() * 190;
                drop.vx = direction * speed
                        + (bloodRandom.nextDouble() - 0.5) * (special ? 130 : 70);
                drop.vy = -60 - bloodRandom.nextDouble() * (special ? 320 : 150);
            } else {
                drop.vx = (bloodRandom.nextDouble() - 0.5) * (special ? 60 : 26);
                drop.vy = -10 + bloodRandom.nextDouble() * (special ? 70 : 30);
            }
            drop.size = special
                    ? 2.2 + bloodRandom.nextDouble() * 5.2
                    : 1.8 + bloodRandom.nextDouble() * 3.4;
            drop.maxLife = special
                    ? 1.0 + bloodRandom.nextDouble() * 1.0
                    : 0.7 + bloodRandom.nextDouble() * 0.7;
            drop.life = drop.maxLife;
            bloodParticles.add(drop);
        }
        while (bloodParticles.size() > 420) {
            bloodParticles.remove(0);
        }
    }

    private void updateBlood(double dt) {
        for (int i = bloodParticles.size() - 1; i >= 0; i--) {
            BloodParticle drop = bloodParticles.get(i);
            drop.life -= dt;
            if (drop.life <= 0) {
                bloodParticles.remove(i);
                continue;
            }
            if (drop.grounded) {
                continue;
            }
            drop.vy += 950 * dt;
            drop.x += drop.vx * dt;
            drop.y += drop.vy * dt;
            if (drop.y >= CombatConstants.GROUND_Y - 2) {
                drop.y = CombatConstants.GROUND_Y - 2;
                drop.grounded = true;
                drop.vx = 0;
                drop.vy = 0;
            }
        }
    }

    private void renderBlood(GraphicsContext gc) {
        for (BloodParticle drop : bloodParticles) {
            double fade = Math.min(1, drop.life / 0.35);
            gc.setFill(new Color(0.55, 0.03, 0.06, 0.85 * fade));
            if (drop.grounded) {
                gc.fillOval(drop.x - drop.size * 1.5, drop.y - drop.size * 0.4,
                        drop.size * 3.0, drop.size * 0.8);
            } else {
                gc.fillOval(drop.x - drop.size * 0.5, drop.y - drop.size * 0.85,
                        drop.size, drop.size * 1.7);
            }
        }
    }

    private void updateFallenFighters(double dt) {
        if (player != null && !player.isAlive()) {
            player.update(dt, opponent != null ? opponent.getX() : player.getX());
        }
        if (opponent != null && !opponent.isAlive()) {
            opponent.update(dt, player != null ? player.getX() : opponent.getX());
        }
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

        gc.save();
        impactCamera.applyWorldTransform(gc);
        drawBackground(gc);

        if (player != null && opponent != null) {
            drawShadow(gc, opponent);
            drawShadow(gc, player);
            sprites.render(gc, opponent, elapsed);
            sprites.render(gc, player, elapsed);
            skills.render(gc);
            renderBlood(gc);
        }
        gc.restore();

        if (impactCamera.isActive()) {
            impactCamera.renderOverlay(gc);
        }

        if (hud != null) {
            hud.render(gc);
        }

        drawFightBanner(gc);
        drawCombatFeedback(gc);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#c7d0e8"));
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        gc.fillText("A/D Move   SPACE Jump   S Duck   J Attack   K Skill   L Block   R Special   Esc/P Pause",
                GameConfig.WIDTH / 2.0, 712);

        drawButton(gc, BACK_X, BACK_Y, "Change Hero", backHovered);
        drawButton(gc, MENU_X, BACK_Y, "Main Menu", menuHovered);

        if (cinematic.isActive()) {
            cinematic.render(gc, sprites);
        }
        if (finalWarning.isActive()) {
            finalWarning.render(gc, sprites);
        }
    }

    private void drawBackground(GraphicsContext gc) {
        double horizon = CombatConstants.GROUND_Y;

        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a1730")),
                new Stop(0.35, Color.web("#3a2148")),
                new Stop(0.62, Color.web("#a8473a")),
                new Stop(0.85, Color.web("#e8864a")),
                new Stop(1, Color.web("#f2b36a"))));
        gc.fillRect(0, 0, GameConfig.WIDTH, horizon);

        double sunX = 1010;
        double sunY = 455;
        gc.save();
        gc.translate(sunX, sunY);
        gc.rotate(elapsed * 5);
        gc.translate(-sunX, -sunY);
        BackdropArt.sunRays(gc, sunX, sunY, 300, Color.web("#ffd97a", 0.10));
        gc.restore();

        gc.setFill(Color.web("#ffd97a", 0.14));
        gc.fillOval(sunX - 140, sunY - 140, 280, 280);
        gc.setFill(Color.web("#ffcf6a", 0.50));
        gc.fillOval(sunX - 72, sunY - 72, 144, 144);
        gc.setFill(Color.web("#ffe6a0", 0.85));
        gc.fillOval(sunX - 50, sunY - 50, 100, 100);

        BackdropArt.star(gc, 210, 118, 18, Color.web("#f4c542", 0.45));
        BackdropArt.star(gc, 1090, 98, 14, Color.web("#f4c542", 0.40));
        BackdropArt.star(gc, 760, 148, 10, Color.web("#f4c542", 0.30));

        double drift = (elapsed * 16) % (GameConfig.WIDTH + 360);
        BackdropArt.cloud(gc, 40 + drift, 200, 280, Color.web("#ffffff", 0.10));
        BackdropArt.cloud(gc, 560 + drift * 0.6, 290, 220, Color.web("#ffffff", 0.07));

        gc.setFill(new LinearGradient(0, horizon - 220, 0, horizon, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4a3558")),
                new Stop(1, Color.web("#2c2138"))));
        gc.fillPolygon(
                new double[]{0, 150, 300, 470, 640, 820, 1000, 1150, 1280, 1280, 0},
                new double[]{horizon - 40, horizon - 140, horizon - 80,
                        horizon - 210, horizon - 110, horizon - 190,
                        horizon - 70, horizon - 150, horizon - 50,
                        horizon, horizon}, 11);

        gc.setFill(new LinearGradient(0, horizon - 170, 0, horizon, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#32243f")),
                new Stop(1, Color.web("#1d1628"))));
        gc.fillPolygon(
                new double[]{0, 180, 360, 560, 760, 980, 1180, 1280, 1280, 0},
                new double[]{horizon - 20, horizon - 90, horizon - 40,
                        horizon - 150, horizon - 60, horizon - 120,
                        horizon - 35, horizon - 80, horizon, horizon}, 10);

        gc.setFill(Color.web("#241b30"));
        gc.fillPolygon(
                new double[]{250, 360, 430, 470, 520, 620, 700, 700, 250},
                new double[]{horizon - 20, horizon - 200, horizon - 245,
                        horizon - 232, horizon - 250, horizon - 180,
                        horizon - 20, horizon, horizon}, 9);
        gc.setStroke(Color.web("#d98555", 0.22));
        gc.setLineWidth(2);
        gc.strokePolyline(
                new double[]{250, 360, 430, 470, 520, 620, 700},
                new double[]{horizon - 20, horizon - 200, horizon - 245,
                        horizon - 232, horizon - 250, horizon - 180,
                        horizon - 20}, 7);

        gc.setFill(new LinearGradient(0, horizon - 180, 0, horizon, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#e8864a", 0)),
                new Stop(1, Color.web("#f2b36a", 0.14))));
        gc.fillRect(0, horizon - 180, GameConfig.WIDTH, 180);

        gc.setFill(Color.web("#23402e"));
        gc.fillPolygon(
                new double[]{0, 200, 420, 660, 900, 1120, 1280},
                new double[]{horizon, horizon - 70, horizon - 30, horizon - 80,
                        horizon - 25, horizon - 65, horizon}, 7);
        gc.setStroke(Color.web("#3f6b45", 0.45));
        gc.setLineWidth(2);
        for (int i = 1; i <= 3; i++) {
            double y = horizon - i * 16;
            gc.strokeLine(0, y, GameConfig.WIDTH, y - 6);
        }

        drawNipaHut(gc, 70, horizon - 6, 1.0);
        drawNipaHut(gc, 1120, horizon - 6, 0.9);
        drawPalm(gc, 165, horizon - 8, 1.0);
        drawPalm(gc, 1055, horizon - 8, 0.85);
        drawFlag(gc, 40, horizon - 6, 150);

        drawTorch(gc, 250, horizon);
        drawTorch(gc, 1030, horizon);

        for (int i = 0; i < 20; i++) {
            double fx = (i * 149 + elapsed * 14 * (i % 3 + 1)) % GameConfig.WIDTH;
            double fy = 300 + Math.sin(elapsed * 1.3 + i) * 110 + (i % 5) * 24;
            double alpha = 0.16 + 0.12 * Math.sin(elapsed * 2 + i);
            gc.setFill(Color.web("#ffe6a0", Math.max(0.03, alpha)));
            gc.fillOval(fx, fy, 3, 3);
        }

        gc.setFill(Color.web("#3a2a1e"));
        gc.fillRect(0, horizon, GameConfig.WIDTH, GameConfig.HEIGHT - horizon);

        gc.setFill(Color.web("#4c8f4a"));
        gc.fillRect(0, horizon - 8, GameConfig.WIDTH, 10);

        gc.setStroke(Color.web("#5a3f2b", 0.5));
        gc.setLineWidth(1);
        for (double x = 80; x < GameConfig.WIDTH; x += 120) {
            gc.strokeLine(x, horizon + 10, x, GameConfig.HEIGHT);
        }
        gc.setStroke(Color.web("#2a1c12", 0.45));
        for (double x = 40; x < GameConfig.WIDTH; x += 80) {
            gc.strokeLine(x, horizon + 4, x + 40, horizon + 18);
        }
    }

    private void drawNipaHut(GraphicsContext gc, double x, double baseY, double scale) {
        gc.save();
        gc.translate(x, baseY);
        gc.scale(scale, scale);

        gc.setFill(Color.web("#4a2f1c"));
        gc.fillRect(-26, -40, 52, 40);
        gc.setFill(Color.web("#1a1008"));
        gc.fillRect(-8, -26, 16, 26);

        gc.setFill(Color.web("#6b4a2a"));
        gc.fillPolygon(new double[]{-38, 0, 38}, new double[]{-40, -78, -40}, 3);
        gc.setFill(Color.web("#7d5a33"));
        gc.fillPolygon(new double[]{-32, 0, 32}, new double[]{-44, -72, -44}, 3);

        gc.setFill(Color.web("#2a1a10"));
        gc.fillRect(-30, -8, 5, 8);
        gc.fillRect(25, -8, 5, 8);
        gc.restore();
    }

    private void drawPalm(GraphicsContext gc, double x, double baseY, double scale) {
        gc.save();
        gc.translate(x, baseY);
        gc.scale(scale, scale);

        gc.setStroke(Color.web("#2a1f14"));
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineWidth(6);
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.bezierCurveTo(2, -30, 10, -58, 6, -86);
        gc.stroke();
        gc.setStroke(Color.web("#4a3520", 0.7));
        gc.setLineWidth(2);
        gc.beginPath();
        gc.moveTo(-1, -2);
        gc.bezierCurveTo(1, -30, 8, -58, 4, -84);
        gc.stroke();

        gc.setStroke(Color.web("#1a120a", 0.7));
        gc.setLineWidth(1);
        for (int i = 1; i <= 6; i++) {
            double ty = -i * 13;
            gc.strokeLine(-4, ty, 5, ty - 1);
        }

        double sx = 6;
        double sy = -86;
        for (int i = 0; i < 7; i++) {
            double baseAngle = -Math.PI / 2 + (i - 3) * 0.42;
            double sway = Math.sin(elapsed * 1.8 + i * 0.8 + x * 0.01) * 0.10;
            double angle = baseAngle + sway;
            double len = 36;
            double ex = sx + Math.cos(angle) * len;
            double ey = sy + Math.sin(angle) * len;
            gc.setStroke(i % 2 == 0 ? Color.web("#22401f") : Color.web("#2e5228"));
            gc.setLineWidth(4);
            gc.beginPath();
            gc.moveTo(sx, sy);
            gc.quadraticCurveTo(
                    sx + Math.cos(angle) * len * 0.5,
                    sy + Math.sin(angle) * len * 0.5 - 4, ex, ey);
            gc.stroke();

            gc.setStroke(Color.web("#2e5228", 0.8));
            gc.setLineWidth(1.5);
            for (int j = 1; j <= 4; j++) {
                double f = j / 5.0;
                double lx = sx + Math.cos(angle) * len * f;
                double ly = sy + Math.sin(angle) * len * f;
                gc.strokeLine(lx, ly,
                        lx + Math.cos(angle + 0.9) * 7,
                        ly + Math.sin(angle + 0.9) * 7);
                gc.strokeLine(lx, ly,
                        lx + Math.cos(angle - 0.9) * 7,
                        ly + Math.sin(angle - 0.9) * 7);
            }
        }

        gc.setFill(Color.web("#4a3520"));
        gc.fillOval(sx - 5, sy - 2, 5, 5);
        gc.fillOval(sx + 2, sy + 1, 5, 5);
        gc.restore();
    }

    private double flagWave(double t) {
        return Math.sin(t * 5.6 + elapsed * 3.4) * 8.5 * t
                + Math.sin(t * 3.2 - elapsed * 2.1 + 1.1) * 3.2 * t;
    }

    private void drawFlag(GraphicsContext gc, double x, double baseY, double poleHeight) {
        double top = baseY - poleHeight;
        double flagTop = top + 9;
        double flagLen = 92;
        double flagH = 54;
        int segments = 22;

        gc.setFill(new LinearGradient(x - 4, 0, x + 4, 0, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2a1a10")),
                new Stop(0.45, Color.web("#7a522c")),
                new Stop(1, Color.web("#1c1008"))));
        gc.fillRoundRect(x - 3, top, 6, poleHeight + 6, 2, 2);

        gc.setFill(Color.web("#d4af37"));
        gc.fillOval(x - 6, top - 9, 12, 12);
        gc.setFill(Color.web("#f7e7a0", 0.7));
        gc.fillOval(x - 3, top - 6, 4, 4);

        gc.setFill(Color.web("#241408"));
        gc.fillOval(x - 11, baseY - 6, 22, 8);

        for (int i = 0; i < segments; i++) {
            double t0 = i / (double) segments;
            double t1 = (i + 1) / (double) segments;
            double x0 = x + t0 * flagLen;
            double x1 = x + t1 * flagLen;
            double w0 = flagWave(t0);
            double w1 = flagWave(t1);

            double phase = t0 * 5.6 + elapsed * 3.4;
            double light = 0.74 + 0.26 * Math.cos(phase);
            gc.setFill(shadeColor(Color.web("#0b3d8c"), light));
            gc.fillPolygon(
                    new double[]{x0, x1, x1, x0},
                    new double[]{flagTop + w0, flagTop + w1,
                            flagTop + flagH / 2 + w1,
                            flagTop + flagH / 2 + w0}, 4);
            gc.setFill(shadeColor(Color.web("#c0392b"), light));
            gc.fillPolygon(
                    new double[]{x0, x1, x1, x0},
                    new double[]{flagTop + flagH / 2 + w0,
                            flagTop + flagH / 2 + w1,
                            flagTop + flagH + w1,
                            flagTop + flagH + w0}, 4);

            if (i % 3 == 0) {
                gc.setStroke(Color.web("#000000", 0.10));
                gc.setLineWidth(1);
                gc.strokeLine(x0, flagTop + w0, x0, flagTop + flagH + w0);
            }
        }

        double apexT = 0.51;
        double apexX = x + apexT * flagLen;
        double apexY = flagTop + flagH / 2 + flagWave(apexT);
        gc.setFill(Color.web("#f4f0e4"));
        gc.fillPolygon(new double[]{x, apexX, x},
                new double[]{flagTop, apexY, flagTop + flagH}, 3);
        gc.setFill(Color.web("#ffffff", 0.10));
        gc.fillPolygon(new double[]{x, apexX, apexX - 18},
                new double[]{flagTop, apexY, flagTop + flagH * 0.5}, 3);

        double sunT = 0.20;
        double sunX = x + sunT * flagLen;
        double sunY = flagTop + flagH / 2 + flagWave(sunT);
        gc.setStroke(Color.web("#f4c542", 0.9));
        gc.setLineWidth(1.5);
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            gc.strokeLine(sunX + Math.cos(a) * 4, sunY + Math.sin(a) * 4,
                    sunX + Math.cos(a) * 8, sunY + Math.sin(a) * 8);
        }
        gc.setFill(Color.web("#f4c542"));
        gc.fillOval(sunX - 4, sunY - 4, 8, 8);

        BackdropArt.star(gc, x + 9, flagTop + 5, 3.4, Color.web("#f4c542"));
        BackdropArt.star(gc, x + 9, flagTop + flagH - 5, 3.4, Color.web("#f4c542"));
        BackdropArt.star(gc, apexX - 8, apexY, 3.4, Color.web("#f4c542"));

        double[] xs = new double[segments + 1];
        double[] topYs = new double[segments + 1];
        double[] botYs = new double[segments + 1];
        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            xs[i] = x + t * flagLen;
            topYs[i] = flagTop + flagWave(t);
            botYs[i] = flagTop + flagH + flagWave(t);
        }
        gc.setStroke(Color.web("#000000", 0.22));
        gc.setLineWidth(1.5);
        gc.strokePolyline(xs, topYs, segments + 1);
        gc.setStroke(Color.web("#000000", 0.14));
        gc.strokePolyline(xs, botYs, segments + 1);
    }

    private Color shadeColor(Color color, double factor) {
        double f = Math.max(0, Math.min(1, factor));
        return new Color(
                Math.min(1, color.getRed() * f),
                Math.min(1, color.getGreen() * f),
                Math.min(1, color.getBlue() * f),
                color.getOpacity());
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
        if (finalWarning.isActive()) {
            if (code == KeyCode.ENTER) {
                finalWarning.advance();
            }
            event.consume();
            return;
        }
        if (cinematic.isActive()) {
            if (code == KeyCode.ESCAPE || code == KeyCode.ENTER || code == KeyCode.SPACE) {
                cinematic.skip();
            }
            event.consume();
            return;
        }
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
        if (!paused && !cinematic.isActive() && !finalWarning.isActive()) {
            input.keyReleased(event.getCode());
        }
    }

    @Override
    public void onMouseMoved(MouseEvent event) {
        if (paused || cinematic.isActive() || finalWarning.isActive()) {
            return;
        }
        backHovered = inside(event.getX(), event.getY(), BACK_X, BACK_Y);
        menuHovered = inside(event.getX(), event.getY(), MENU_X, BACK_Y);
    }

    @Override
    public void onMouseClicked(MouseEvent event) {
        if (paused || cinematic.isActive() || finalWarning.isActive()) {
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

    private static final class BloodParticle {
        double x;
        double y;
        double vx;
        double vy;
        double size;
        double life;
        double maxLife;
        boolean grounded;
    }
}
