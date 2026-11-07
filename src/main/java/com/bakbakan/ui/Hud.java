package com.bakbakan.ui;

import com.bakbakan.combat.CombatConstants;
import com.bakbakan.events.DamageEvent;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.HitEvent;
import com.bakbakan.events.ManaChangedEvent;
import com.bakbakan.events.SpecialReadyEvent;
import com.bakbakan.events.SpecialUsedEvent;
import com.bakbakan.util.GameConfig;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class Hud {

    private static final double BAR_WIDTH = 480;
    private static final double HEALTH_HEIGHT = 26;
    private static final double SMALL_HEIGHT = 10;
    private static final double HEALTH_Y = 42;
    private static final double MANA_Y = 88;
    private static final double SPECIAL_Y = 104;
    private static final double LEFT_X = 60;
    private static final double RIGHT_X = GameConfig.WIDTH - 60 - BAR_WIDTH;

    private final String playerId;
    private final String opponentId;
    private final String playerName;
    private final String opponentName;
    private final EventHandler<DamageEvent> damageHandler = this::onDamage;
    private final EventHandler<ManaChangedEvent> manaHandler = this::onMana;
    private final EventHandler<HitEvent> hitHandler = this::onHit;
    private final EventHandler<SpecialReadyEvent> specialReadyHandler = this::onSpecialReady;
    private final EventHandler<SpecialUsedEvent> specialUsedHandler = this::onSpecialUsed;

    private double playerHealth = CombatConstants.MAX_HEALTH;
    private double opponentHealth = CombatConstants.MAX_HEALTH;
    private double playerMana = CombatConstants.MAX_MANA;
    private double opponentMana = CombatConstants.MAX_MANA;
    private double playerSpecial;
    private double opponentSpecial;
    private boolean playerSpecialReady;
    private boolean opponentSpecialReady;
    private boolean attached;

    public Hud(String playerId, String opponentId, String playerName, String opponentName) {
        this.playerId = playerId;
        this.opponentId = opponentId;
        this.playerName = playerName;
        this.opponentName = opponentName;
    }

    public void attach(GameEventBus bus) {
        if (attached) {
            return;
        }
        bus.register(DamageEvent.DAMAGE, damageHandler);
        bus.register(ManaChangedEvent.MANA_CHANGED, manaHandler);
        bus.register(HitEvent.HIT, hitHandler);
        bus.register(SpecialReadyEvent.SPECIAL_READY, specialReadyHandler);
        bus.register(SpecialUsedEvent.SPECIAL_USED, specialUsedHandler);
        attached = true;
    }

    public void detach(GameEventBus bus) {
        if (!attached) {
            return;
        }
        bus.unregister(DamageEvent.DAMAGE, damageHandler);
        bus.unregister(ManaChangedEvent.MANA_CHANGED, manaHandler);
        bus.unregister(HitEvent.HIT, hitHandler);
        bus.unregister(SpecialReadyEvent.SPECIAL_READY, specialReadyHandler);
        bus.unregister(SpecialUsedEvent.SPECIAL_USED, specialUsedHandler);
        attached = false;
    }

    public void reset() {
        playerHealth = CombatConstants.MAX_HEALTH;
        opponentHealth = CombatConstants.MAX_HEALTH;
        playerMana = CombatConstants.MAX_MANA;
        opponentMana = CombatConstants.MAX_MANA;
        playerSpecial = 0;
        opponentSpecial = 0;
        playerSpecialReady = false;
        opponentSpecialReady = false;
    }

    private void onDamage(DamageEvent event) {
        double health = clamp(event.getRemainingHealth(), 0, CombatConstants.MAX_HEALTH);
        if (playerId.equals(event.getFighterId())) {
            playerHealth = health;
        } else if (opponentId.equals(event.getFighterId())) {
            opponentHealth = health;
        }
    }

    private void onMana(ManaChangedEvent event) {
        double mana = clamp(event.getCurrentMana(), 0, CombatConstants.MAX_MANA);
        if (playerId.equals(event.getFighterId())) {
            playerMana = mana;
        } else if (opponentId.equals(event.getFighterId())) {
            opponentMana = mana;
        }
    }

    private void onHit(HitEvent event) {
        double dealt = event.getDamage() * CombatConstants.SPECIAL_GAIN_DEALT;
        double taken = event.getDamage() * CombatConstants.SPECIAL_GAIN_TAKEN;

        if (playerId.equals(event.getAttackerId())) {
            addPlayerSpecial(dealt);
        }
        if (opponentId.equals(event.getAttackerId())) {
            addOpponentSpecial(dealt);
        }
        if (playerId.equals(event.getTargetId())) {
            addPlayerSpecial(taken);
        }
        if (opponentId.equals(event.getTargetId())) {
            addOpponentSpecial(taken);
        }
    }

    private void onSpecialReady(SpecialReadyEvent event) {
        if (playerId.equals(event.getFighterId())) {
            playerSpecial = CombatConstants.MAX_SPECIAL;
            playerSpecialReady = true;
        } else if (opponentId.equals(event.getFighterId())) {
            opponentSpecial = CombatConstants.MAX_SPECIAL;
            opponentSpecialReady = true;
        }
    }

    private void onSpecialUsed(SpecialUsedEvent event) {
        if (playerId.equals(event.getFighterId())) {
            playerSpecial = 0;
            playerSpecialReady = false;
        } else if (opponentId.equals(event.getFighterId())) {
            opponentSpecial = 0;
            opponentSpecialReady = false;
        }
    }

    private void addPlayerSpecial(double amount) {
        playerSpecial = clamp(playerSpecial + amount, 0, CombatConstants.MAX_SPECIAL);
        playerSpecialReady = playerSpecial >= CombatConstants.MAX_SPECIAL;
    }

    private void addOpponentSpecial(double amount) {
        opponentSpecial = clamp(opponentSpecial + amount, 0, CombatConstants.MAX_SPECIAL);
        opponentSpecialReady = opponentSpecial >= CombatConstants.MAX_SPECIAL;
    }

    public void render(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        gc.setFill(Color.web("#f4f6ff"));
        gc.fillText(playerName, LEFT_X, 30);

        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(opponentName, RIGHT_X + BAR_WIDTH, 30);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        gc.fillText("VS", GameConfig.WIDTH / 2.0, 66);

        drawBar(gc, LEFT_X, HEALTH_Y, BAR_WIDTH, HEALTH_HEIGHT,
                playerHealth / CombatConstants.MAX_HEALTH,
                Color.web("#4cd97b"), false, false);
        drawBar(gc, RIGHT_X, HEALTH_Y, BAR_WIDTH, HEALTH_HEIGHT,
                opponentHealth / CombatConstants.MAX_HEALTH,
                Color.web("#e74c3c"), true, false);
        drawHealthText(gc, LEFT_X + BAR_WIDTH / 2.0, 80, playerHealth);
        drawHealthText(gc, RIGHT_X + BAR_WIDTH / 2.0, 80, opponentHealth);

        drawBar(gc, LEFT_X, MANA_Y, BAR_WIDTH, SMALL_HEIGHT,
                playerMana / CombatConstants.MAX_MANA,
                Color.web("#3fa9f5"), false, false);
        drawBar(gc, RIGHT_X, MANA_Y, BAR_WIDTH, SMALL_HEIGHT,
                opponentMana / CombatConstants.MAX_MANA,
                Color.web("#3fa9f5"), true, false);

        drawBar(gc, LEFT_X, SPECIAL_Y, BAR_WIDTH, SMALL_HEIGHT,
                playerSpecial / CombatConstants.MAX_SPECIAL,
                Color.web("#f4c542"), false, playerSpecialReady);
        drawBar(gc, RIGHT_X, SPECIAL_Y, BAR_WIDTH, SMALL_HEIGHT,
                opponentSpecial / CombatConstants.MAX_SPECIAL,
                Color.web("#f4c542"), true, opponentSpecialReady);

        drawLabel(gc, "MANA", LEFT_X - 8, MANA_Y + 9, true);
        drawLabel(gc, "SPECIAL", LEFT_X - 8, SPECIAL_Y + 9, true);
        drawLabel(gc, "MANA", RIGHT_X + BAR_WIDTH + 8, MANA_Y + 9, false);
        drawLabel(gc, "SPECIAL", RIGHT_X + BAR_WIDTH + 8, SPECIAL_Y + 9, false);

        if (playerSpecialReady) {
            drawReadyText(gc, LEFT_X + BAR_WIDTH / 2.0);
        }
        if (opponentSpecialReady) {
            drawReadyText(gc, RIGHT_X + BAR_WIDTH / 2.0);
        }
    }

    private void drawBar(GraphicsContext gc, double x, double y,
                         double width, double height, double ratio,
                         Color fill, boolean rightAligned, boolean ready) {
        gc.setFill(Color.web("#0a0f1e", 0.85));
        gc.fillRoundRect(x - 3, y - 3, width + 6, height + 6, 9, 9);

        gc.setFill(Color.web("#242d49"));
        gc.fillRoundRect(x, y, width, height, 6, 6);

        double clamped = clamp(ratio, 0, 1);
        double fillWidth = width * clamped;
        gc.setFill(fill);
        if (rightAligned) {
            gc.fillRoundRect(x + width - fillWidth, y, fillWidth, height, 6, 6);
        } else {
            gc.fillRoundRect(x, y, fillWidth, height, 6, 6);
        }

        gc.setStroke(Color.web("#f4f6ff", 0.35));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(x, y, width, height, 6, 6);

        if (ready) {
            gc.setStroke(Color.web("#fff2b0", 0.9));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x - 4, y - 4, width + 8, height + 8, 8, 8);
        }
    }

    private void drawHealthText(GraphicsContext gc, double centerX, double y, double health) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#b9c2da"));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        gc.fillText((int) Math.ceil(health) + " / " + (int) CombatConstants.MAX_HEALTH,
                centerX, y);
    }

    private void drawLabel(GraphicsContext gc, String text, double x, double y,
                           boolean rightAligned) {
        gc.setTextAlign(rightAligned ? TextAlignment.RIGHT : TextAlignment.LEFT);
        gc.setFill(Color.web("#b9c2da"));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 9));
        gc.fillText(text, x, y);
    }

    private void drawReadyText(GraphicsContext gc, double centerX) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.fillText("SPECIAL READY", centerX, 132);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
