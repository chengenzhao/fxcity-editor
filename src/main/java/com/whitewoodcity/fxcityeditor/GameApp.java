package com.whitewoodcity.fxcityeditor;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.whitewoodcity.control.RotateTransit2DTexture;
import com.whitewoodcity.control.arrows.Arrow;
import com.whitewoodcity.control.arrows.CrossArrows;
import com.whitewoodcity.fxgl.texture.AnimatedTexture;
import com.whitewoodcity.fxgl.texture.AnimationChannel;
import com.whitewoodcity.model.View;
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
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;

public class GameApp extends GameApplication implements GameAppDecorator {

  Entity entity;
  final BiMap<Label, File> fileBiMap = HashBiMap.create();
  final BiMap<Label, View> textureBiMap = HashBiMap.create();

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
        var image = new Image(view.image().toURI().toString());

        var name = view.image().getName();
        name = name.substring(0, name.indexOf("."));

        switch (view.textureType()){
          case TRANSIT -> {
            var texture = new RotateTransit2DTexture(image);
            addTransitTexture(entityTree, name, texture, treeview, bottomPane, rightPane);
          }
          case ANIMATED -> {
            var texture = new AnimatedTexture(new AnimationChannel(image,1,Duration.seconds(1)));
            addAnimatedTexture(entityTree, name, texture, treeview, bottomPane, rightPane);
          }
          default -> {}
        }
      }
    ));
    entityHBox.setAlignment(Pos.BASELINE_LEFT);
    entityHBox.getChildren().addAll(new Label("Entity0"), addViewComponentButton);
    entityHBox.setOnMousePressed(_ -> decorateBottomAndRightPane(entity, bottomPane, rightPane));

    addImageButton.setOnAction(_ -> addImage(treeview,bottomPane,rightPane));

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

  private void addImage(TreeView<Node> treeView, Pane bottomPane, GridPane rightPane){
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files", "*.PNG", "*.JPG"));
    File file = fileChooser.showOpenDialog(null);

    if (file != null) {
      var label = new Label(file.getName());
      try {
        fileBiMap.put(label, file);
      } catch (Exception e) {
        var node = fileBiMap.inverse().get(file);
        selectTreeItem(node, treeView);
        return;
      }
      var image = new Image(file.toURI().toString());
      label.setOnMousePressed(_ -> decorateBottomAndRightPane(image, bottomPane, rightPane));
      var treeItem = new TreeItem<Node>(label);
      treeView.getTreeItem(0).getChildren().add(treeItem);//row 0 is resources tree item
      selectTreeItem(label, treeView);
    }
  }

  private void addAnimatedTexture(TreeItem<Node> entityTree, String name, AnimatedTexture texture, TreeView<Node> treeview, Pane bottomPane, GridPane rightPane){

    entity.getViewComponent().addChild(texture);

    var rect = new Rectangle();
    rect.widthProperty().bind(texture.fitWidthProperty());
    rect.heightProperty().bind(texture.fitHeightProperty());
    rect.xProperty().bindBidirectional(texture.xProperty());
    rect.yProperty().bindBidirectional(texture.yProperty());
    rect.setFill(Color.TRANSPARENT);
    rect.setStroke(Color.web("#039ED3"));

    var textureItem = new TreeItem<Node>();
    var textureLabel = new Label(name);
    var delTextureButton = new Button("×");
    var textureHBox = new HBox(20, textureLabel, delTextureButton);
    textureHBox.setAlignment(Pos.BASELINE_LEFT);
    textureItem.setValue(textureHBox);

    textureHBox.setOnMousePressed(_ -> {
      decorateBottomAndRightPane(texture, bottomPane, rightPane);
      entity.getViewComponent().removeDevChild(rect);
      entity.getViewComponent().addDevChild(rect);

      rect.setOnMousePressed(oe -> {
        selectTreeItem(textureHBox, treeview);
        var ox = oe.getX();
        var oy = oe.getY();
        var tx = rect.getX();
        var ty = rect.getY();
        rect.setOnMouseDragged(e -> {
          double changeInX = e.getX() - ox;
          double changeInY = e.getY() - oy;
          rect.setX(tx + changeInX);
          rect.setY(ty + changeInY);
        });
      });
    });
    texture.setOnMouseClicked(_ -> selectTreeItem(textureHBox, treeview));
    rect.setOnMouseReleased(e -> {//deselect the view component
      if (e.getButton() == MouseButton.SECONDARY)
        freezeEvent(textureHBox);
    });
    textureHBox.setOnMouseReleased(e -> {//freeze event
      if (e.getButton() == MouseButton.SECONDARY) {
        entity.getViewComponent().removeDevChild(rect);
      }
    });

    delTextureButton.setOnAction(_ -> {
      removeTreeItem(textureHBox,treeview);
      entity.getViewComponent().removeChild(texture);
    });

    entityTree.getChildren().add(textureItem);
    selectTreeItem(textureHBox, treeview);
  }

  private void addTransitTexture(TreeItem<Node> treeItem, String name, RotateTransit2DTexture texture, TreeView<Node> treeview, Pane bottomPane, GridPane rightPane){
    entity.getViewComponent().addChild(texture);

    var rect = new Rectangle();
    rect.widthProperty().bind(texture.fitWidthProperty());
    rect.heightProperty().bind(texture.fitHeightProperty());
    rect.xProperty().bind(texture.xProperty());
    rect.yProperty().bind(texture.yProperty());
    rect.setFill(Color.TRANSPARENT);
    rect.setStroke(Color.web("#039ED3"));

    var arrow = new Arrow(0,0,0,rect.getHeight());
    arrow.x1Property().bind(texture.getRotation().pivotXProperty());
    arrow.y1Property().bind(texture.getRotation().pivotYProperty());
    arrow.y2Property().bind(arrow.y1Property().add(rect.heightProperty()));
    arrow.x2Property().bind(arrow.x1Property());

    var textureItem = new TreeItem<Node>();
    var textureLabel = new Label(name);
    var delTextureButton = new Button("×");
    var textureHBox = new HBox(20, textureLabel, delTextureButton);
    textureHBox.setAlignment(Pos.BASELINE_LEFT);
    textureItem.setValue(textureHBox);

    textureHBox.setOnMousePressed(_ -> {
      decorateBottomAndRightPane(texture, bottomPane, rightPane);
      entity.getViewComponent().removeDevChild(rect);
      entity.getViewComponent().addDevChild(rect);
      entity.getViewComponent().removeDevChild(arrow);
      entity.getViewComponent().addDevChild(arrow);

      rect.setOnMousePressed(oe -> {
        selectTreeItem(textureHBox, treeview);
        var op = texture.transform(new Point2D(oe.getX(), oe.getY()));
        var ox = op.getX();
        var oy = op.getY();
        var rx = rect.getX();
        var ry = rect.getY();
        var ax = arrow.getX1();
        var ay = arrow.getY1();
        rect.setOnMouseDragged(e -> {
          var p = texture.transform(new Point2D(e.getX(), e.getY()));
          double changeInX = p.getX() - ox;
          double changeInY = p.getY() - oy;
          texture.setX(rx + changeInX);
          texture.setY(ry + changeInY);
          texture.getRotation().setPivotX(ax + changeInX);
          texture.getRotation().setPivotY(ay + changeInY);
          texture.update();
        });
      });

      arrow.getOrigin().setOnMousePressed(oe -> {
        selectTreeItem(textureHBox, treeview);
        var op = texture.transform(new Point2D(oe.getX(), oe.getY()));
        var ox = op.getX();
        var oy = op.getY();
        var tx = arrow.getX1();
        var ty = arrow.getY1();
        arrow.getOrigin().setOnMouseDragged(e -> {
          var p = texture.transform(new Point2D(e.getX(), e.getY()));
          double changeInX = p.getX() - ox;
          double changeInY = p.getY() - oy;
          var x1 = tx + changeInX;
          var y1 = ty + changeInY;
          if(x1 < texture.getX()) x1 = texture.getX();
          if(x1 > texture.getX()+texture.getFitWidth()) x1 = texture.getX()+texture.getFitWidth();
          if(y1 < texture.getY()) y1 = texture.getY();
          if(y1 > texture.getY()+texture.getFitHeight()) y1 = texture.getY()+texture.getFitHeight();
          texture.getRotation().setPivotX(x1);
          texture.getRotation().setPivotY(y1);
          texture.update();
        });
      });

      //todo 这里有点问题，点了之后会跳
      arrow.getHeadB().setOnMousePressed(oe -> {
        selectTreeItem(textureHBox, treeview);
        var ox = oe.getX();
        arrow.getHeadB().setOnMouseDragged(e -> {
          double changeInX = e.getX() - ox;
          var angle = (int)(texture.getRotation().getAngle());
          if(changeInX > 0) texture.getRotation().setAngle(angle - 1 < 0 ? 361 - angle : angle -1);
          if(changeInX < 0) texture.getRotation().setAngle((angle + 1)%360);
          if(changeInX!=0){
            texture.update();//make transforms work
            arrow.getTransforms().clear();
            arrow.getTransforms().addAll(texture.getTransforms());
            rect.getTransforms().clear();
            rect.getTransforms().addAll(texture.getTransforms());
          }
        });
      });
    });
    texture.setOnMouseClicked(_ -> selectTreeItem(textureHBox, treeview));
    rect.setOnMouseReleased(e -> {//deselect the view component
      if (e.getButton() == MouseButton.SECONDARY)
        freezeEvent(textureHBox);
    });
    textureHBox.setOnMouseReleased(e -> {//freeze event
      if (e.getButton() == MouseButton.SECONDARY) {
        entity.getViewComponent().removeDevChild(rect);
        entity.getViewComponent().removeDevChild(arrow);
      }
    });

    delTextureButton.setOnAction(_ -> {
      removeTreeItem(textureHBox,treeview);
      entity.getViewComponent().removeChild(texture);
    });

    treeItem.getChildren().add(textureItem);
    selectTreeItem(textureHBox, treeview);

//    var rotate = new Rotate();
//    rotate.pivotXProperty().bind(arrow.x1Property());
//    rotate.pivotYProperty().bind(arrow.y1Property());
//    rotate.angleProperty().bindBidirectional(texture.getRotation().angleProperty());
//    arrow.getTransforms().add(rotate);
//
//    var textureItem = new TreeItem<Node>();
//    var textureLabel = new Label(name);
//    var addTextureButton = new Button("+");
//    var delTextureButton = new Button("×");
//    var textureHBox = new HBox(10, textureLabel, addTextureButton, delTextureButton);
//    textureHBox.setAlignment(Pos.BASELINE_LEFT);
//    textureItem.setValue(textureHBox);
//
//    delTextureButton.setOnAction(_ -> {
//      removeTreeItem(textureHBox,treeview);
//      entity.getViewComponent().removeChild(texture);
//    });
//
//    textureHBox.setOnMousePressed(_ -> {
//      decorateBottomAndRightPane(texture, bottomPane, rightPane);
//      if(texture.getParent() instanceof RotateTransit2DTexture parent){
//        parent.getChildren().remove(rect);
//        parent.getChildren().add(rect);
//      }else{
//        entity.getViewComponent().removeDevChild(rect);
//        entity.getViewComponent().addDevChild(rect);
//        entity.getViewComponent().removeDevChild(arrow);
//        entity.getViewComponent().addDevChild(arrow);
//      }
//
//      rect.setOnMousePressed(oe -> {
//        selectTreeItem(textureHBox, treeview);
//        var ox = oe.getX();
//        var oy = oe.getY();
//        var tx = rect.getX();
//        var ty = rect.getY();
//        rect.setOnMouseDragged(e -> {
//          double changeInX = e.getX() - ox;
//          double changeInY = e.getY() - oy;
//          rect.setX(tx + changeInX);
//          rect.setY(ty + changeInY);
//        });
//      });
//
//      arrow.getOrigin().setOnMousePressed(oe -> {
//        selectTreeItem(textureHBox, treeview);
//        Rotate r = (Rotate) arrow.getTransforms().getFirst();
//        var op = r.transform(oe.getX(), oe.getY());
//        var ox = op.getX();
//        var oy = op.getY();
//        var tx = arrow.getX1();
//        var ty = arrow.getY1();
//        arrow.getOrigin().setOnMouseDragged(e -> {
//          var p = r.transform(e.getX(), e.getY());
//          double changeInX = p.getX() - ox;
//          double changeInY = p.getY() - oy;
//          var x1 = tx + changeInX;
//          var y1 = ty + changeInY;
//          var image = texture.getImageView();
//          if(x1 < image.getX()) x1 = image.getX();
//          if(x1 > image.getX()+image.getFitWidth()) x1 = image.getX()+image.getFitWidth();
//          if(y1 < image.getY()) y1 = image.getY();
//          if(y1 > image.getY()+image.getFitHeight()) y1 = image.getY()+image.getFitHeight();
//          arrow.setX1(x1);
//          arrow.setY1(y1);
//        });
//      });
//
//      arrow.getHeadB().setOnMousePressed(oe -> {
//        selectTreeItem(textureHBox, treeview);
//        var ox = oe.getX();
//        arrow.getHeadB().setOnMouseDragged(e -> {
//          double changeInX = e.getX() - ox;
//          var angle = rotate.getAngle();
//          if(changeInX > 0) rotate.setAngle(angle - 1);
//          if(changeInX < 0) rotate.setAngle(angle + 1);
//        });
//      });
//
//      arrow.getMainLine().setOnMousePressed(oe -> {
//        selectTreeItem(textureHBox, treeview);
//        var ox = oe.getX();
//        arrow.getMainLine().setOnMouseDragged(e -> {
//          double changeInX = e.getX() - ox;
//          var angle = rotate.getAngle();
//          if(changeInX > 0) rotate.setAngle(angle - 1);
//          if(changeInX < 0) rotate.setAngle(angle + 1);
//        });
//      });
//    });
//
//    treeItem.getChildren().add(textureItem);
//    selectTreeItem(textureHBox, treeview);
  }
}
