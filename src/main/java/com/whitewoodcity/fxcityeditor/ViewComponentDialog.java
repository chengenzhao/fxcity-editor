package com.whitewoodcity.fxcityeditor;

import com.google.common.collect.BiMap;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;

public class ViewComponentDialog extends Dialog<ViewComponentDialog.Parameters> {
  public record Parameters(String image, String password){ }

  public ViewComponentDialog(Set<File> imageFileSet) {
    setTitle("ViewComponent Dialog");
    setHeaderText("What kind of View Component would you like to have?");

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane(10,10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField username = new TextField();
    username.setPromptText("Username");
    PasswordField password = new PasswordField();
    password.setPromptText("Password");

    ComboBox<File> comboBox = new ComboBox<>();

    comboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(File file) {
        return file == null ? "":file.getName();
      }

      @Override
      public File fromString(String s) {
        return comboBox.getItems().stream().filter(ap -> ap.getName().equals(s)).findFirst().orElse(null);
      }
    });

    comboBox.getItems().addAll(imageFileSet);

    grid.add(new Label("Image:"), 0, 0);
    grid.add(comboBox, 1, 0);
    grid.add(new Label("Password:"), 0, 1);
    grid.add(password, 1, 1);

    Node okButton = getDialogPane().lookupButton(ButtonType.OK);
    okButton.setDisable(true);
    comboBox.valueProperty().addListener((_, _, newValue) -> okButton.setDisable(newValue==null));

    getDialogPane().setContent(grid);

    Platform.runLater(username::requestFocus);

    setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        return new ViewComponentDialog.Parameters(comboBox.getValue().toString(), password.getText());
      }
      return null;
    });
  }
}