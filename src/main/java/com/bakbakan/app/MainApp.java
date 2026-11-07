package com.bakbakan.app;

import com.bakbakan.assets.AssetManager;
import com.bakbakan.audio.AudioManager;
import com.bakbakan.entities.GameSession;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.screens.FightScreen;
import com.bakbakan.screens.HeroSelectScreen;
import com.bakbakan.screens.LoadingScreen;
import com.bakbakan.screens.MainMenuScreen;
import com.bakbakan.screens.ResultScreen;
import com.bakbakan.screens.ScreenManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    private ScreenManager screenManager;
    private AudioManager audioManager;

    @Override
    public void start(Stage stage) {
        GameEventBus bus = GameEventBus.getInstance();
        AssetManager assets = new AssetManager();
        screenManager = new ScreenManager(stage, bus);
        audioManager = new AudioManager(assets, bus);
        GameSession session = new GameSession();

        screenManager.registerScreen("Loading", () -> new LoadingScreen(assets, bus));
        screenManager.registerScreen("MainMenu", () -> new MainMenuScreen(bus));
        screenManager.registerScreen("HeroSelect", () -> new HeroSelectScreen(bus, session));
        screenManager.registerScreen("Fight", () -> new FightScreen(bus, session, assets));
        screenManager.registerScreen("RoundWin",
                () -> new ResultScreen(bus, session, ResultScreen.Mode.ROUND_WIN));
        screenManager.registerScreen("GameOver",
                () -> new ResultScreen(bus, session, ResultScreen.Mode.GAME_OVER));
        screenManager.registerScreen("Victory",
                () -> new ResultScreen(bus, session, ResultScreen.Mode.VICTORY));

        screenManager.showInitial("Loading");
        stage.show();
        stage.getScene().getRoot().requestFocus();
    }

    @Override
    public void stop() {
        if (screenManager != null) {
            screenManager.stop();
        }
        if (audioManager != null) {
            audioManager.dispose();
        }
    }
}
