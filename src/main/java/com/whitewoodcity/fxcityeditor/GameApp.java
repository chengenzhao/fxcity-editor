package com.whitewoodcity.fxcityeditor;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.whitewoodcity.fxgl.texture.AnimatedTexture;
import com.whitewoodcity.fxgl.texture.AnimationChannel;
import com.whitewoodcity.fxgl.texture.Texture;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;

public class GameApp extends GameApplication implements GameAppDecorator {

  Entity entity;
  final BiMap<Label, File> fileBiMap = HashBiMap.create();
  final BiMap<Label, Texture> textureBiMap = HashBiMap.create();

  @Override
  protected void initSettings(GameSettings settings) {
    settings.setHeight(HEIGHT);
    settings.setWidth(WIDTH);
    settings.setMainMenuEnabled(false);
    settings.setGameMenuEnabled(false);
  }

  @Override
  protected void initGame() {
    entity = new Entity();
    FXGL.getGameWorld().addEntities(entity);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void initUI() {
    FXGL.getGameScene().setCursor(Cursor.DEFAULT);

    entity.getViewComponent().addDevChild(new Circle(10, Color.RED));
    entity.setX((double) WIDTH / 2);
    entity.setY(300);

    var menu = new Menu("Editor");

    var exit = new MenuItem("Exit");

    var menubar = new MenuBar(menu);
    menubar.setPrefWidth(WIDTH);

    var treeview = new TreeView<Node>();
    treeview.translateYProperty().bind(menubar.heightProperty());
    treeview.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));

    var rightPane = new GridPane(20, 20);
    rightPane.setPadding(new Insets(20));
    rightPane.setPrefWidth(300);
    rightPane.layoutYProperty().bind(menubar.heightProperty());
    rightPane.setLayoutX(WIDTH - rightPane.getPrefWidth());

    var bottomPane = new Pane();
    bottomPane.setPrefWidth(WIDTH);
    bottomPane.setLayoutY(600);

    var treeviewRoot = new TreeItem<Node>();

    var resourceHBox = new HBox(10);
    var resourceTree = new TreeItem<Node>(resourceHBox);
    var addImageButton = new Button("+");
    resourceHBox.setAlignment(Pos.BASELINE_LEFT);

    var resourceLabel = new Label("Resources");
    resourceHBox.getChildren().addAll(resourceLabel, addImageButton);
    resourceHBox.setOnMousePressed(_ -> decorateRightPane(resourceHBox, rightPane));
    fireEvent(resourceHBox);

    var entityHBox = new HBox(10);
    var entityTree = new TreeItem<Node>(entityHBox);
    var addViewComponentButton = new Button("+");
    addViewComponentButton.setOnAction(_ ->
      new ViewComponentDialog(fileBiMap.values()).showAndWait().ifPresent(view -> {
        var image = new Image(view.image().toURI().toString());//file -> image
        var imageChannel = new AnimationChannel(image, view.framesPerRow(), Duration.seconds(view.duration()));
        var animatedTexture = new AnimatedTexture(imageChannel);
        animatedTexture.loop();

        entity.getViewComponent().addChild(animatedTexture);

        var region = new Region();
        region.prefWidthProperty().bind(animatedTexture.fitWidthProperty());
        region.prefHeightProperty().bind(animatedTexture.fitHeightProperty());
        region.translateXProperty().bind(animatedTexture.translateXProperty());
        region.translateYProperty().bind(animatedTexture.translateYProperty());
        String cssBordering = "-fx-border-color:#039ED3;";
        region.setStyle(cssBordering);
        decorateRightPane(animatedTexture, rightPane);

        var textureItem = new TreeItem<Node>();
        var textureName = view.image().getName();
        textureName = textureName.substring(0, textureName.indexOf("."));
        var textureLabel = new Label(textureName);
        var delTextureButton = new Button("×");
        var textureHBox = new HBox(20, textureLabel, delTextureButton);
        textureHBox.setAlignment(Pos.BASELINE_LEFT);
        textureItem.setValue(textureHBox);
        entityTree.getChildren().add(textureItem);
        fireEvent(textureHBox, treeview);

        animatedTexture.setOnMousePressed(originalE -> {
          entity.getViewComponent().addChild(region);

          var ox = originalE.getSceneX();
          var oy = originalE.getSceneY();
          var tx = animatedTexture.getTranslateX();
          var ty = animatedTexture.getTranslateY();
          animatedTexture.setOnMouseDragged(e -> {
            double changeInX = e.getSceneX() - ox;
            double changeInY = e.getSceneY() - oy;
            animatedTexture.setTranslateX(tx + changeInX);
            animatedTexture.setTranslateY(ty + changeInY);
          });
          decorateRightPane(animatedTexture, rightPane);
        });
        animatedTexture.setOnMouseReleased(_ -> entity.getViewComponent().removeChild(region));
      })
    );
    entityHBox.setAlignment(Pos.BASELINE_LEFT);
    entityHBox.getChildren().addAll(new Label("Entity0"), addViewComponentButton);
    entityHBox.setOnMousePressed(_ -> decorateRightPane(entity, rightPane));

    addImageButton.setOnAction(_ -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files", "*.PNG", "*.JPG"));
      File file = fileChooser.showOpenDialog(null);

      if (file != null) {
        var label = new Label(file.getName());
        try {
          fileBiMap.put(label, file);
        } catch (Exception e) {
          var node = fileBiMap.inverse().get(file);
          fireEvent(node, treeview);
          return;
        }
        var image = new Image(file.toURI().toString());
        label.setOnMousePressed(_ -> {
          decorateRightPane(image, rightPane);
          decorateBottomPane(image, bottomPane);
        });
        var treeItem = new TreeItem<Node>(label);
        resourceTree.getChildren().add(treeItem);
        fireEvent(label, treeview);
      }
    });

    treeviewRoot.getChildren().addAll(resourceTree, entityTree);

    treeview.setRoot(treeviewRoot);
    treeview.setShowRoot(false);

    treeview.getSelectionModel().selectedItemProperty().addListener(
      (_, oldValue, newValue) -> {
        if (oldValue != null) freezeEvent(oldValue.getValue());
        if (newValue != null) fireEvent(newValue.getValue());
      });

    exit.setOnAction(_ -> System.exit(0));

    FXGL.getGameScene().addUINodes(menubar, treeview, rightPane, bottomPane);
  }
}
