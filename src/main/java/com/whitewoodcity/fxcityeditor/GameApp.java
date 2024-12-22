package com.whitewoodcity.fxcityeditor;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Screen;

import java.io.File;

public class GameApp extends GameApplication {
  final int HEIGHT = 1000;
  final int WIDTH = (int) (Screen.getPrimary().getBounds().getWidth() / Screen.getPrimary().getBounds().getHeight() * 1000);

  final Entity entity = new Entity();

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

    entity.getViewComponent().addChild(new Circle(10, Color.RED));
    entity.setX((double) WIDTH /2);
    entity.setY((double) HEIGHT /2);

    var menu = new Menu("File");

    var loadImage = new MenuItem("Load Image");
    menu.getItems().add(loadImage);

    var menubar = new MenuBar(menu);
    menubar.setPrefWidth(WIDTH);

    var treeview = new TreeView<String>();
    treeview.translateYProperty().bind(menubar.heightProperty());
    treeview.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,null,null)));

    var entityTreeItem = new TreeItem<>("Game World");
    var entityTree = new TreeItem<>("Entity0");

    entityTreeItem.getChildren().add(entityTree);

    treeview.setRoot(entityTreeItem);

    var rightPane = new GridPane(20,20);
    rightPane.setPadding(new Insets(20));
    rightPane.setPrefWidth(300);
    rightPane.layoutYProperty().bind(menubar.heightProperty());
    rightPane.setLayoutX(WIDTH - rightPane.getPrefWidth());

    rightPane.add(new Label("X:"),0,0);
    rightPane.add(new Label("Y:"),0,1);
    var x = new NumberTextField(WIDTH);
    var y = new NumberTextField(HEIGHT);
    rightPane.add(x,1,0);
    rightPane.add(y,1,1);

    x.promptTextProperty().bind(entity.xProperty().asString());
    y.promptTextProperty().bind(entity.yProperty().asString());

    x.setText((int)entity.getX()+"");
    y.setText((int)entity.getY()+"");

    x.setOnAction(_ -> entity.setX(Integer.parseInt(x.getText().trim())));
    y.setOnAction(_ -> entity.setY(Integer.parseInt(y.getText().trim())));

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
        entity.getViewComponent().addChild(view);
        var treeItem = new TreeItem<>(file.getName());
        entityTree.getChildren().add(treeItem);
        treeview.getSelectionModel().select(treeItem);
      }

    });

    FXGL.getGameScene().addUINodes(menubar,treeview,rightPane);
  }

  @Override
  protected void initGame() {
    FXGL.getGameWorld().addEntities(entity);
  }
}
