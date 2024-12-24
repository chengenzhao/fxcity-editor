package com.whitewoodcity.fxcityeditor;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class ViewComponentDialog extends Dialog<ViewComponentDialog.Parameters> {
  public record Parameters(String username, String password){ }

  public ViewComponentDialog() {
    setTitle("ViewComponent Dialog");
    setHeaderText("What kind of View Component would you like to have?");

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane(10,10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField username = new TextField();
    username.setPromptText("Username");
    PasswordField password = new PasswordField();
    password.setPromptText("Password");

    grid.add(new Label("Username:"), 0, 0);
    grid.add(username, 1, 0);
    grid.add(new Label("Password:"), 0, 1);
    grid.add(password, 1, 1);

    Node loginButton = getDialogPane().lookupButton(ButtonType.OK);
    loginButton.setDisable(true);

    username.textProperty().addListener((_, _, newValue) -> {
      loginButton.setDisable(newValue.trim().isEmpty());
    });

    getDialogPane().setContent(grid);

    Platform.runLater(username::requestFocus);

    setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        return new ViewComponentDialog.Parameters(username.getText(), password.getText());
      }
      return null;
    });
  }
}