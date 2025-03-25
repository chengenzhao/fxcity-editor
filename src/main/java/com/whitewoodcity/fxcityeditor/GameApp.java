package com.whitewoodcity.fxcityeditor;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.whitewoodcity.control.NumberField;
import com.whitewoodcity.control.RotateTransit2DTexture;
import com.whitewoodcity.control.arrows.Arrow;
import com.whitewoodcity.fxgl.texture.AnimatedTexture;
import com.whitewoodcity.fxgl.texture.AnimationChannel;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameApp extends GameApplication implements GameAppDecorator {

  MenuBar menubar = new MenuBar();
  GridPane rightPane = new GridPane();
  Pane bottomPane = new Pane();
  TreeView<Node> treeView = new TreeView<>();

  Entity entity;
  TreeItem<Node> entityTree;
  final BiMap<Label, File> fileBiMap = HashBiMap.create();
  final List<KeyFrame> keyFrames = new ArrayList<>();
  int currentKeyFrame = 0; //index of current key frame in the above keyFrames
  NumberField maxTime = new NumberField(100);//in seconds

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
    maxTime.setText("1");
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void initUI() {
    FXGL.getGameScene().setCursor(Cursor.DEFAULT);

    var originalPoint = new Circle(3, Color.RED);
    originalPoint.setMouseTransparent(true);
    entity.getViewComponent().addDevChild(originalPoint);
    entity.setX((double) WIDTH / 2);
    entity.setY(300);

    var menu = new Menu("Editor");

    var exit = new MenuItem("Exit");

    menubar.getMenus().add(menu);
    menubar.setPrefWidth(WIDTH);

    treeView = new TreeView<>();
    treeView.translateYProperty().bind(menubar.heightProperty());
    treeView.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));

    rightPane = new GridPane(20, 20);
    rightPane.setPadding(new Insets(20));
    rightPane.setPrefWidth(300);
    rightPane.layoutYProperty().bind(menubar.heightProperty());
    rightPane.setLayoutX(WIDTH - rightPane.getPrefWidth());

    bottomPane = new Pane();
    bottomPane.setPrefWidth(WIDTH);
    bottomPane.setLayoutY(600);

    var treeviewRoot = new TreeItem<Node>();

    var resourceHBox = new HBox(10);
    var resourceTree = new TreeItem<Node>(resourceHBox);
    var addImageButton = new Button("+");
    resourceHBox.setAlignment(Pos.BASELINE_LEFT);

    var resourceLabel = new Label("Resources");
    resourceHBox.getChildren().addAll(resourceLabel, addImageButton);
    resourceHBox.setOnMousePressed(_ -> decorateRightPane(resourceHBox));
    fireEvent(resourceHBox);

    entityTree = new TreeItem<>(new HBox(10));
    var addViewComponentButton = new Button("+");
    addViewComponentButton.setOnAction(_ ->
      new ViewComponentDialog(fileBiMap.values()).showAndWait().ifPresent(view -> {
        var image = new Image(view.image().toURI().toString());

        var name = view.image().getName();
        name = name.substring(0, name.indexOf("."));

        switch (view.textureType()) {
          case TRANSIT -> addTransitTexture(entityTree, name, image);
          case ANIMATED -> addAnimatedTexture(entityTree, name, image);
        }
      })
    );
    var entityHBox = (HBox) entityTree.getValue();
    entityHBox.setAlignment(Pos.BASELINE_LEFT);
    entityHBox.getChildren().addAll(new Label("Entity"), addViewComponentButton);
    entityHBox.setOnMousePressed(_ -> decorateBottomAndRightPane(entity, keyFrames));

    addImageButton.setOnAction(_ -> addImage());

    treeviewRoot.getChildren().addAll(resourceTree, entityTree);

    treeView.setRoot(treeviewRoot);
    treeView.setShowRoot(false);

    treeView.getSelectionModel().selectedItemProperty().addListener(
      (_, oldValue, newValue) -> {
        if (oldValue != null) freezeEvent(oldValue.getValue());
        if (newValue != null) fireEvent(newValue.getValue());
      });

    exit.setOnAction(_ -> System.exit(0));

    FXGL.getGameScene().addUINodes(menubar, treeView, rightPane, bottomPane);

    keyFrames.add(generateKeyFrame(Duration.seconds(0)));
    keyFrames.add(generateKeyFrame(Duration.seconds(1)));

    for (int i = 0; i < keyFrames.size(); i++) {
      var frame = keyFrames.get(i);
      final int j = i;
      frame.setOnMousePressed(_ -> {
        keyFrames.get(currentKeyFrame).deSelect();
        frame.select();
        currentKeyFrame = j;
        decorateMiddlePane(frame);
      });
    }

    keyFrames.getFirst().select();
  }

  private KeyFrame generateKeyFrame(Duration duration) {
    return new KeyFrame(20, 50).setTime(duration).setColor(Color.ORANGE);//LIGHTSEAGREEN
  }

  private void addImage() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files", "*.PNG", "*.JPG"));
    File file = fileChooser.showOpenDialog(null);

    if (file != null) {
      var label = new Label(file.getName());
      try {
        fileBiMap.put(label, file);
      } catch (Exception e) {
        var node = fileBiMap.inverse().get(file);
        selectTreeItem(node);
        return;
      }
      var image = new Image(file.toURI().toString());
      label.setOnMousePressed(_ -> decorateBottomAndRightPane(image));
      var treeItem = new TreeItem<Node>(label);
      treeView.getTreeItem(0).getChildren().add(treeItem);//row 0 is resources tree item
      selectTreeItem(label);
    }
  }

  private void addAnimatedTexture(TreeItem<Node> entityTree, String name, Image image) {
    var texture = new AnimatedTexture(new AnimationChannel(image, 1, Duration.seconds(1)));
    entity.getViewComponent().addChild(texture);

    var rect = createSelectionRectangle(texture);
    var textureItem = createDeletableTreeItem(name, () -> entity.getViewComponent().removeChild(texture));
    var textureHBox = textureItem.getValue();

    textureHBox.setOnMousePressed(_ -> {
      decorateBottomAndRightPane(texture);
      entity.getViewComponent().removeChild(rect);
      entity.getViewComponent().addChild(rect);

      rect.setOnMousePressed(oe -> {
        selectTreeItem(textureHBox);
        var ox = oe.getX();
        var oy = oe.getY();
        var tx = texture.getX();
        var ty = texture.getY();
        rect.setOnMouseDragged(e -> {
          double changeInX = e.getX() - ox;
          double changeInY = e.getY() - oy;
          texture.setX(tx + changeInX);
          texture.setY(ty + changeInY);
        });
      });
    });
    texture.setOnMouseClicked(_ -> selectTreeItem(textureHBox));
    rect.setOnMouseReleased(e -> {//deselect the view component
      if (e.getButton() == MouseButton.SECONDARY)
        freezeEvent(textureHBox);
    });
    textureHBox.setOnMouseReleased(e -> {//freeze event
      if (e.getButton() == MouseButton.SECONDARY) {
        entity.getViewComponent().removeChild(rect);
      }
    });

    entityTree.getChildren().add(textureItem);
    selectTreeItem(textureHBox);
  }

  private void addTransitTexture(TreeItem<Node> treeItem, String name, Image image) {
    var hBox = createDeletableLableBox(name);
    var rectMap = new HashMap<KeyFrame, Rectangle>();
    var arrowMap = new HashMap<KeyFrame, Arrow>();
    for (var keyFrame : keyFrames) {
      var texture = new RotateTransit2DTexture(image);
      keyFrame.getRotateTransit2DTextureBiMap().put(hBox, texture);

      texture.setOnMouseClicked(_ -> selectTreeItem(hBox));
      texture.children().addListener((ListChangeListener<RotateTransit2DTexture>) _ -> selectTreeItem(hBox));

      rectMap.put(keyFrame, createSelectionRectangle(texture));
      arrowMap.put(keyFrame, createRotateArrow(texture));
    }

    var textureItem = createDeletableTreeItem(hBox, () -> {
      for (var keyFrame : keyFrames) {
        var texture = keyFrame.getRotateTransit2DTextureBiMap().remove(hBox);
        texture.setParent(null);
        new ArrayList<>(texture.children()).forEach(e -> e.setParent(null));
      }
      fireEvent(keyFrames.get(currentKeyFrame));
    });
    treeItem.getChildren().add(textureItem);
    selectTreeItem(hBox);
    fireEvent(keyFrames.get(currentKeyFrame));

    var rectangles = new ArrayList<Rectangle>();

    hBox.setOnMousePressed(_ -> {
      var keyFrame = keyFrames.get(currentKeyFrame);
      var texture = keyFrame.getRotateTransit2DTextureBiMap().get(hBox);
      decorateBottomAndRightPane(texture, keyFrame.getRotateTransit2DTextureBiMap());

      var rect = rectMap.get(keyFrame);
      var arrow = arrowMap.get(keyFrame);

      entity.getViewComponent().removeChild(rect);
      entity.getViewComponent().removeChild(arrow);
      entity.getViewComponent().addChild(rect);
      entity.getViewComponent().addChild(arrow);

      rect.getTransforms().clear();
      rect.getTransforms().addAll(texture.getTransforms());
      arrow.getTransforms().clear();
      arrow.getTransforms().addAll(texture.getTransforms());

      for (var r : rectangles)
        entity.getViewComponent().removeChild(r);
      rectangles.clear();
      populateJointSelectionRectanglesExceptThis(texture, rectangles);
      for (var r : rectangles)
        entity.getViewComponent().addChild(r);

      rect.setOnMousePressed(oe -> {
        selectTreeItem(hBox);
        var op = texture.getRotation().transform(new Point2D(oe.getX(), oe.getY()));
        var ox = op.getX();
        var oy = op.getY();
        var rx = rect.getX();
        var ry = rect.getY();
        var ax = arrow.getX1();
        var ay = arrow.getY1();
        rect.setOnMouseDragged(e -> {
          var p = texture.getRotation().transform(new Point2D(e.getX(), e.getY()));
          double changeInX = p.getX() - ox;
          double changeInY = p.getY() - oy;
          texture.setX(rx + changeInX);
          texture.setY(ry + changeInY);
          texture.getRotation().setPivotX(ax + changeInX);
          texture.getRotation().setPivotY(ay + changeInY);
          update(texture, rect, arrow);
        });
      });

      rect.setOnMouseReleased(e -> {//deselect the view component
        if (e.getButton() == MouseButton.SECONDARY)
          freezeEvent(hBox);
      });

      arrow.getOrigin().setOnMousePressed(oe -> {
        selectTreeItem(hBox);
        var op = texture.getRotation().transform(new Point2D(oe.getX(), oe.getY()));
        var ox = op.getX();
        var oy = op.getY();
        var tx = arrow.getX1();
        var ty = arrow.getY1();
        arrow.getOrigin().setOnMouseDragged(e -> {
          var p = texture.getRotation().transform(new Point2D(e.getX(), e.getY()));
          double changeInX = p.getX() - ox;
          double changeInY = p.getY() - oy;
          var x1 = tx + changeInX;
          var y1 = ty + changeInY;
          if (x1 < texture.getX()) x1 = texture.getX();
          if (x1 > texture.getX() + texture.getFitWidth()) x1 = texture.getX() + texture.getFitWidth();
          if (y1 < texture.getY()) y1 = texture.getY();
          if (y1 > texture.getY() + texture.getFitHeight()) y1 = texture.getY() + texture.getFitHeight();
          texture.getRotation().setPivotX(x1);
          texture.getRotation().setPivotY(y1);
          update(texture, rect, arrow);
        });
      });

      arrow.getHeadB().setOnMousePressed(oe -> {
        selectTreeItem(hBox);
        var ox = oe.getX();
        arrow.getHeadB().setOnMouseDragged(e -> {
          double changeInX = e.getX() - ox;
          var angle = texture.getRotation().getAngle();
          if (changeInX > 0) texture.getRotation().setAngle(angle - 1 < 0 ? 361 - angle : angle - 1);
          if (changeInX < 0) texture.getRotation().setAngle((angle + 1) % 360);
          if (changeInX != 0) {
            update(texture, rect, arrow);
          }
        });
      });

      arrow.getMainLine().setOnMousePressed(oe -> {
        selectTreeItem(hBox);
        var ox = oe.getX();
        arrow.getMainLine().setOnMouseDragged(e -> {
          double changeInX = e.getX() - ox;
          var angle = texture.getRotation().getAngle();
          if (changeInX > 0) texture.getRotation().setAngle(angle - 1 < 0 ? 361 - angle : angle - 1);
          if (changeInX < 0) texture.getRotation().setAngle((angle + 1) % 360);
          if (changeInX != 0) {
            update(texture, rect, arrow);
          }
        });
      });
    });
//    texture.setOnMouseClicked(_ -> selectTreeItem(textureHBox, this.treeView));
//    texture.children().addListener((ListChangeListener<RotateTransit2DTexture>) _ -> selectTreeItem(textureHBox, this.treeView));
//    rect.setOnMouseReleased(e -> {//deselect the view component
//      if (e.getButton() == MouseButton.SECONDARY)
//        freezeEvent(textureHBox);
//    });
    hBox.setOnMouseReleased(e -> {//freeze event
      if(e.getButton() == MouseButton.SECONDARY)
        fireEvent(keyFrames.get(currentKeyFrame));
    });
//
//    treeItem.getChildren().add(textureItem);
//    selectTreeItem(textureHBox, this.treeView);
  }

  private void populateJointSelectionRectanglesExceptThis(RotateTransit2DTexture texture, List<Rectangle> rectangles) {
    for (var child : texture.children()) {
      populateJointSelectionRectangles(child, rectangles);
    }
  }

  private void populateJointSelectionRectangles(RotateTransit2DTexture texture, List<Rectangle> rectangles) {
    for (var child : texture.children()) {
      populateJointSelectionRectangles(child, rectangles);
    }
    rectangles.add(createJointSelectionRectangle(texture));
  }

  private void update(RotateTransit2DTexture texture, Node... nodes) {
    texture.update();
    for (var node : nodes) {
      node.getTransforms().clear();
      node.getTransforms().addAll(texture.getTransforms());
    }
  }

  public int getCurrentKeyFrame() {
    return currentKeyFrame;
  }

  public void setCurrentKeyFrame(int currentKeyFrame) {
    this.currentKeyFrame = currentKeyFrame;
  }
}
