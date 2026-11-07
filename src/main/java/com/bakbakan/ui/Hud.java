package com.bakbakan.ui;

import com.bakbakan.combat.CombatConstants;
import com.bakbakan.events.DamageEvent;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.util.GameConfig;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class Hud {

    private static final double BAR_WIDTH = 480;
    private static final double BAR_HEIGHT = 26;
    private static final double BAR_Y = 48;
    private static final double LEFT_X = 60;
    private static final double RIGHT_X = GameConfig.WIDTH - 60 - BAR_WIDTH;

    private final String playerId;
    private final String opponentId;
    private final String playerName;
    private final String opponentName;
    private final EventHandler<DamageEvent> damageHandler = this::onDamage;

    private double playerHealth = CombatConstants.MAX_HEALTH;
    private double opponentHealth = CombatConstants.MAX_HEALTH;
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
        attached = true;
    }

    public void detach(GameEventBus bus) {
        if (!attached) {
            return;
        }
        bus.unregister(DamageEvent.DAMAGE, damageHandler);
        attached = false;
    }

    public void reset() {
        playerHealth = CombatConstants.MAX_HEALTH;
        opponentHealth = CombatConstants.MAX_HEALTH;
    }

    private void onDamage(DamageEvent event) {
        double health = Math.max(0,
                Math.min(CombatConstants.MAX_HEALTH, event.getRemainingHealth()));
        if (playerId.equals(event.getFighterId())) {
            playerHealth = health;
        } else if (opponentId.equals(event.getFighterId())) {
            opponentHealth = health;
        }
    }

    public void render(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        gc.setFill(Color.web("#f4f6ff"));
        gc.fillText(playerName, LEFT_X, 36);

        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(opponentName, RIGHT_X + BAR_WIDTH, 36);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#f4c542"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        gc.fillText("VS", GameConfig.WIDTH / 2.0, 72);

        drawBar(gc, LEFT_X, BAR_Y, playerHealth, Color.web("#4cd97b"), false);
        drawBar(gc, RIGHT_X, BAR_Y, opponentHealth, Color.web("#e74c3c"), true);
    }

    private void drawBar(GraphicsContext gc, double x, double y,
                         double health, Color fill, boolean rightAligned) {
        gc.setFill(Color.web("#0a0f1e", 0.85));
        gc.fillRoundRect(x - 3, y - 3, BAR_WIDTH + 6, BAR_HEIGHT + 6, 12, 12);

        gc.setFill(Color.web("#242d49"));
        gc.fillRoundRect(x, y, BAR_WIDTH, BAR_HEIGHT, 9, 9);

        double ratio = Math.max(0, Math.min(1, health / CombatConstants.MAX_HEALTH));
        double fillWidth = BAR_WIDTH * ratio;
        gc.setFill(fill);
        if (rightAligned) {
            gc.fillRoundRect(x + BAR_WIDTH - fillWidth, y, fillWidth, BAR_HEIGHT, 9, 9);
        } else {
            gc.fillRoundRect(x, y, fillWidth, BAR_HEIGHT, 9, 9);
        }

        gc.setStroke(Color.web("#f4f6ff", 0.35));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, BAR_WIDTH, BAR_HEIGHT, 9, 9);

        gc.setFill(Color.web("#b9c2da"));
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText((int) Math.ceil(health) + " / " + (int) CombatConstants.MAX_HEALTH,
                x + BAR_WIDTH / 2.0, y + BAR_HEIGHT + 16);
    }
}
