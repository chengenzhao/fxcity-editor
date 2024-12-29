package com.whitewoodcity.fxcityeditor;

import com.whitewoodcity.control.IntField;
import com.whitewoodcity.control.NumberField;
import com.whitewoodcity.model.View;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.io.File;
import java.util.Set;

public class ViewComponentDialog extends Dialog<View> {

  public ViewComponentDialog(Set<File> imageFileSet) {
    setTitle("ViewComponent Dialog");
    setHeaderText("What kind of View Component would you like to have?");

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane(10,10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    var framesPerRowField = new IntField(1,100);
    framesPerRowField.setPromptText("How many frames in the row?");

    var durationField = new NumberField(0,100);
    durationField.setPromptText("How long the animation endure?");
    durationField.setText("1");

    ComboBox<File> fileComboBox = new ComboBox<>();

    fileComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(File file) {
        return file == null ? "":file.getName();
      }

      @Override
      public File fromString(String s) {
        return fileComboBox.getItems().stream().filter(file -> file.getName().equals(s)).findFirst().orElse(null);
      }
    });

    fileComboBox.getItems().addAll(imageFileSet);

    grid.add(new Label("Image:"), 0, 0);
    grid.add(fileComboBox, 1, 0);
    grid.add(new Label("Frames per Row:"), 0, 1);
    grid.add(framesPerRowField, 1, 1);
    grid.add(new Label("Duration(in seconds):"), 0, 2);
    grid.add(durationField, 1, 2);

    Node okButton = getDialogPane().lookupButton(ButtonType.OK);
    okButton.setDisable(true);
    fileComboBox.valueProperty().addListener((_, _, newValue) -> okButton.setDisable(newValue==null));

    getDialogPane().setContent(grid);

    Platform.runLater(fileComboBox::requestFocus);

    setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        return new View(fileComboBox.getValue(), framesPerRowField.getInt(), durationField.getDouble());
      }
      return null;
    });
  }
}