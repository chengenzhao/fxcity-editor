package com.whitewoodcity.fxcityeditor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class EditorApp extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    stage.setTitle("Hello World!");
    Button btn = new Button();
    btn.setText("Say 'Hello World'");
    btn.setOnAction(e -> System.out.println("hello world"));

    StackPane root = new StackPane();
    root.getChildren().add(btn);
    stage.setScene(new Scene(root, 300, 250));
    stage.show();
  }
}
