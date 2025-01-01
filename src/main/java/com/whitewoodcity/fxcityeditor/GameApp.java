package com.whitewoodcity.fxcityeditor;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.whitewoodcity.control.TransitTexture;
import com.whitewoodcity.fxgl.texture.AnimatedTexture;
import com.whitewoodcity.fxgl.texture.AnimationChannel;
import com.whitewoodcity.javafx.binding.XBindings;
import com.whitewoodcity.model.View;
import javafx.geometry.Insets;
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
        var image = new Image(view.image().toURI().toString());//file -> image
        var texture = switch (view.textureType()){
          case TRANSIT -> new TransitTexture(image);
          default -> new AnimatedTexture(new AnimationChannel(image,1,Duration.seconds(1)));
        };

        entity.getViewComponent().addChild(texture);

        var rect = new Rectangle();
        rect.widthProperty().bind(texture.fitWidthProperty());
        rect.heightProperty().bind(texture.fitHeightProperty());
        rect.xProperty().bindBidirectional(texture.xProperty());
        rect.yProperty().bindBidirectional(texture.yProperty());
        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(Color.web("#039ED3"));

        var textureItem = new TreeItem<Node>();
        var textureName = view.image().getName();
        textureName = textureName.substring(0, textureName.indexOf("."));
        var textureLabel = new Label(textureName);
        var delTextureButton = new Button("×");
        var textureHBox = new HBox(20, textureLabel, delTextureButton);
        textureHBox.setAlignment(Pos.BASELINE_LEFT);
        textureItem.setValue(textureHBox);

        Circle temp = new Circle(6);
        temp.setFill(Color.GREEN);
        Rotate r = new Rotate();

        Circle pivot = new Circle(7);
        if(view.textureType() == View.TextureType.TRANSIT){
          pivot.setFill(Color.web("#039ED3"));
          var transitTexture = (TransitTexture)texture;
          var rotation = transitTexture.getRotation();

          rect.getTransforms().addAll(rotation,r);
          transitTexture.getTransforms().add(r);
          rotation.pivotXProperty().bind(pivot.centerXProperty());
          rotation.pivotYProperty().bind(pivot.centerYProperty());

          r.pivotXProperty().bind(XBindings.reduceDoubleValue(temp.centerXProperty(),temp.centerYProperty(), rotation.pivotXProperty(),rotation.pivotYProperty(), rotation.angleProperty(),
            (x,y,x0,y0,a) -> Math.cos(a)*(x-x0) + Math.sin(a)*(y-y0)));//needs to apply new position of new coordinate
          r.pivotYProperty().bind(XBindings.reduceDoubleValue(temp.centerXProperty(),temp.centerYProperty(), rotation.pivotXProperty(),rotation.pivotYProperty(), rotation.angleProperty(),
            (x,y,x0,y0,a) -> -Math.sin(a)*(x-x0) + Math.cos(a)*(y-y0)));//needs to apply new position of new coordinate

          temp.setOnMousePressed(originalE -> {
            if(originalE.getButton()==MouseButton.SECONDARY){
              r.setAngle(r.getAngle()+5);
            }else{
              var ox = originalE.getSceneX();
              var oy = originalE.getSceneY();
              var tx = temp.getCenterX();
              var ty = temp.getCenterY();
              temp.setOnMouseDragged(e -> {
                double changeInX = e.getSceneX() - ox;
                double changeInY = e.getSceneY() - oy;
                temp.setCenterX(tx + changeInX);
                temp.setCenterY(ty + changeInY);
              });
            }
          });

          pivot.setOnMousePressed(originalE -> {
            if(originalE.getButton()==MouseButton.SECONDARY){
              rotation.setAngle(rotation.getAngle()+5);
              System.out.println(rotation.getPivotX());
            }else{
              selectTreeItem(textureHBox, treeview);
              var ox = originalE.getX();
              var oy = originalE.getY();
              var tx = pivot.getCenterX();
              var ty = pivot.getCenterY();
              pivot.setOnMouseDragged(e -> {
                double changeInX = e.getX() - ox;
                double changeInY = e.getY() - oy;
                pivot.setCenterX(tx + changeInX);
                pivot.setCenterY(ty + changeInY);
              });
            }
          });
        }

        textureHBox.setOnMousePressed(_ -> {
          decorateBottomAndRightPane(texture, bottomPane, rightPane);
          entity.getViewComponent().removeDevChild(rect);
          entity.getViewComponent().addDevChild(rect);
          if(view.textureType() == View.TextureType.TRANSIT){
            entity.getViewComponent().removeDevChild(pivot);
            entity.getViewComponent().addDevChild(pivot);

            entity.getViewComponent().removeDevChild(temp);
            entity.getViewComponent().addDevChild(temp);
          }

          rect.setOnMousePressed(originalE -> {
            selectTreeItem(textureHBox, treeview);
            var ox = originalE.getX();
            var oy = originalE.getY();
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
            if(view.textureType() == View.TextureType.TRANSIT) entity.getViewComponent().removeDevChild(pivot);
          }
        });

        delTextureButton.setOnAction(_ -> {
          removeTreeItem(textureHBox,treeview);
          entity.getViewComponent().removeChild(texture);
        });

        entityTree.getChildren().add(textureItem);
        selectTreeItem(textureHBox, treeview);
      })
    );
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
}
