package com.whitewoodcity.fxcityeditor;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Screen;

import java.io.File;

public class GameApp extends GameApplication {
  final int HEIGHT = 1000;
  final int WIDTH = (int) (Screen.getPrimary().getBounds().getWidth() / Screen.getPrimary().getBounds().getHeight() * 1000);

  @Override
  protected void initSettings(GameSettings settings) {
    settings.setHeight(HEIGHT);
    settings.setWidth(WIDTH);
    settings.setMainMenuEnabled(false);
    settings.setGameMenuEnabled(false);
  }

  @Override
  protected void initUI() {
    FXGL.getGameScene().setCursor(Cursor.DEFAULT);

    var menu = new Menu("File");

    var loadImage = new MenuItem("Load Image");
    menu.getItems().add(loadImage);

    var menubar = new MenuBar(menu);
    menubar.setPrefWidth(WIDTH);

    var treeview = new TreeView<String>();
    treeview.translateYProperty().bind(menubar.heightProperty());

    var entityTreeItem = new TreeItem<>("Entity");

    treeview.setRoot(entityTreeItem);

    loadImage.setOnAction(_ -> {
      FileChooser fileChooser = new FileChooser();

      //Set extension filter
      FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
      FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
      fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

      //Show open file dialog
      File file = fileChooser.showOpenDialog(null);

      if (file != null) {
        var image = new Image(file.toURI().toString());
        var view = new ImageView(image);
        var entity = new Entity();
        entity.getViewComponent().addChild(view);
        var treeItem = new TreeItem<>(file.getName());
        entityTreeItem.getChildren().add(treeItem);
        treeview.getSelectionModel().select(treeItem);
        FXGL.getGameWorld().addEntities(entity);
      }
    });

    FXGL.getGameScene().addUINodes(menubar,treeview);
  }

}
