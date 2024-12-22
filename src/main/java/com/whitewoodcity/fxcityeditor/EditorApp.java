package com.whitewoodcity.fxcityeditor;

import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;


public class EditorApp extends javafx.application.Application {
  @Override
  public void start(Stage stage) throws Exception {
    stage.setTitle("FXCity Editor");

    StackPane stackPane = new StackPane();

    var gamePane = GameApp.embeddedLaunch(new GameApp());
    gamePane.setRenderFill(Color.TRANSPARENT);
    stackPane.getChildren().add(gamePane);

    stage.setScene(new Scene(stackPane, Screen.getPrimary().getBounds().getWidth() * .75, Screen.getPrimary().getBounds().getHeight() * .75));
    stage.show();

    gamePane.prefWidthProperty().bind(stage.getScene().widthProperty());
    gamePane.prefHeightProperty().bind(stage.getScene().heightProperty());
    gamePane.renderWidthProperty().bind(stage.getScene().widthProperty());
    gamePane.renderHeightProperty().bind(stage.getScene().heightProperty());

  }

  public static void main(String... args) {
    System.setProperty("prism.lcdtext", "false");
    EditorApp.launch(EditorApp.class, args);
  }
}
