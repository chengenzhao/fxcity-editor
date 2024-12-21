package com.whitewoodcity.fxcityeditor;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.whitewoodcity.fxgl.service.LoadingScene;
import javafx.scene.Cursor;
import javafx.stage.Screen;

public class GameApp extends GameApplication {
  @Override
  protected void initSettings(GameSettings settings) {
    settings.setHeight(1000);
    settings.setWidth((int) (Screen.getPrimary().getBounds().getWidth() / Screen.getPrimary().getBounds().getHeight() * 1000));
    settings.setMainMenuEnabled(false);
    settings.setGameMenuEnabled(false);
  }

  @Override
  protected void initUI() {
    FXGL.getGameScene().setCursor(Cursor.DEFAULT);
  }

}
