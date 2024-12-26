package com.whitewoodcity.fxcityeditor;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.whitewoodcity.control.NumberField;
import com.whitewoodcity.fxgl.texture.AnimatedTexture;
import com.whitewoodcity.fxgl.texture.AnimationChannel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.io.File;

public class GameApp extends GameApplication {
  final int HEIGHT = 1000;
  final int WIDTH = (int) (Screen.getPrimary().getBounds().getWidth() / Screen.getPrimary().getBounds().getHeight() * 1000);

  Entity entity;
  final BiMap<Label, File> fileBiMap = HashBiMap.create();

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

        animatedTexture.setOnMousePressed(originalE -> {
          entity.getViewComponent().addChild(region);

          var ox = originalE.getSceneX();
          var oy = originalE.getSceneY();
          var tx = animatedTexture.getTranslateX();
          var ty = animatedTexture.getTranslateY();
          animatedTexture.setOnMouseDragged( e -> {
            double changeInX = e.getSceneX() - ox;
            double changeInY = e.getSceneY() - oy;
            animatedTexture.setTranslateX(tx + changeInX);
            animatedTexture.setTranslateY(ty+ changeInY);
          });
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
        treeview.getSelectionModel().select(treeItem);
        fireEvent(label);
      }
    });

    treeviewRoot.getChildren().addAll(resourceTree, entityTree);

    treeview.setRoot(treeviewRoot);
    treeview.setShowRoot(false);

    treeview.setOnKeyPressed(e -> {
      TreeItem<Node> selected = treeview.getSelectionModel().getSelectedItem();
      if (selected != null && e.getCode() == KeyCode.ENTER) {
        fireEvent(selected.getValue());
      }
    });

    exit.setOnAction(_ -> System.exit(0));

    FXGL.getGameScene().addUINodes(menubar, treeview, rightPane, bottomPane);
  }

  private void fireEvent(Node n) {
    n.fireEvent(new MouseEvent(MouseEvent.MOUSE_PRESSED,
      n.getLayoutX(), n.getLayoutY(), n.getLayoutX(), n.getLayoutY(), MouseButton.PRIMARY, 1,
      true, true, true, true, true, true, true,
      true, true, true, null));
  }

  private void fireEvent(Node n, TreeView<Node> treeView) {
    fireEvent(n);
    selectTreeItem(n, treeView);
  }

  private void selectTreeItem(Node n, TreeView<Node> treeView) {
    var treeItem = getTreeItem(n, treeView.getRoot());
    treeView.getSelectionModel().select(treeItem);
  }

  private TreeItem<Node> getTreeItem(Node n, TreeItem<Node> treeItem) {
    if (n == treeItem.getValue())
      return treeItem;
    else {
      for (var item : treeItem.getChildren()) {
        var childTreeItem = getTreeItem(n, item);
        if (childTreeItem != null) return childTreeItem;
      }
      return null;
    }
  }

  private void decorateRightPane(Object object, GridPane rightPane) {
    rightPane.getChildren().clear();
    switch (object) {
      case Image image -> {
        rightPane.add(new Label("Image Width:"), 0, 0);
        rightPane.add(new Label("Image Height:"), 0, 1);
        var width = new Label(image.getWidth() + "");
        var height = new Label(image.getHeight() + "");
        rightPane.add(width, 1, 0);
        rightPane.add(height, 1, 1);
      }
      case Entity e -> {
        rightPane.add(new Label("X:"), 0, 0);
        rightPane.add(new Label("Y:"), 0, 1);
        var x = new NumberField(WIDTH);
        var y = new NumberField(HEIGHT);
        rightPane.add(x, 1, 0);
        rightPane.add(y, 1, 1);

        x.promptTextProperty().bind(e.xProperty().asString());
        y.promptTextProperty().bind(e.yProperty().asString());

        x.setText((int) e.getX() + "");
        y.setText((int) e.getY() + "");

        x.setOnAction(_ -> e.setX(x.getDouble()));
        y.setOnAction(_ -> e.setY(y.getDouble()));
      }
      default -> {
        rightPane.add(new Label("Game Width:"), 0, 0);
        rightPane.add(new Label("Game Height:"), 0, 1);
        var width = new Label(WIDTH + "");
        var height = new Label(HEIGHT + "");
        rightPane.add(width, 1, 0);
        rightPane.add(height, 1, 1);
      }
    }
  }

  private void decorateBottomPane(Object object, Pane pane) {
    pane.getChildren().clear();
    switch (object) {
      case Image image -> {
        var view = new ImageView(image);
        var vbox = new VBox(20, new Label("Image Preview:"), view);
        vbox.setPadding(new Insets(20));
        pane.getChildren().add(vbox);
      }
      default -> {
      }
    }
  }
}
