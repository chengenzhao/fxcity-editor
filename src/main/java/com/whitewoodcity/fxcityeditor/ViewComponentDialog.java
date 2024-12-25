package com.whitewoodcity.fxcityeditor;

import com.whitewoodcity.control.IntTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.io.File;
import java.util.Set;

public class ViewComponentDialog extends Dialog<ViewComponentDialog.Parameters> {
  public record Parameters(File image, int framesPerRow){ }

  public ViewComponentDialog(Set<File> imageFileSet) {
    setTitle("ViewComponent Dialog");
    setHeaderText("What kind of View Component would you like to have?");

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane(10,10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    var framesPerRowField = new IntTextField(1,500);
    framesPerRowField.setPromptText("How many frames in the row?");

    ComboBox<File> fileComboBox = new ComboBox<>();

    fileComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(File file) {
        return file == null ? "":file.getName();
      }

      @Override
      public File fromString(String s) {
        return fileComboBox.getItems().stream().filter(ap -> ap.getName().equals(s)).findFirst().orElse(null);
      }
    });

    fileComboBox.getItems().addAll(imageFileSet);

    grid.add(new Label("Image:"), 0, 0);
    grid.add(fileComboBox, 1, 0);
    grid.add(new Label("Frames/Row:"), 0, 1);
    grid.add(framesPerRowField, 1, 1);

    Node okButton = getDialogPane().lookupButton(ButtonType.OK);
    okButton.setDisable(true);
    fileComboBox.valueProperty().addListener((_, _, newValue) -> okButton.setDisable(newValue==null));

    getDialogPane().setContent(grid);

    Platform.runLater(fileComboBox::requestFocus);

    setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        return new ViewComponentDialog.Parameters(fileComboBox.getValue(), framesPerRowField.getInt());
      }
      return null;
    });
  }
}