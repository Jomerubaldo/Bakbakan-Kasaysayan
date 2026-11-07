package com.bakbakan.app;

import com.bakbakan.events.GameEventBus;
import com.bakbakan.screens.ScreenManager;
import com.bakbakan.screens.TitleScreen;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    private ScreenManager screenManager;

    @Override
    public void start(Stage stage) {
        screenManager = new ScreenManager(stage, GameEventBus.getInstance());
        screenManager.showInitial(new TitleScreen());
        stage.show();
        stage.getScene().getRoot().requestFocus();
    }

    @Override
    public void stop() {
        if (screenManager != null) {
            screenManager.stop();
        }
    }
}
