package com.whitewoodcity.fxcityeditor;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.whitewoodcity.control.NumberTextField;
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
    menu.getItems().add(exit);

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

    var treeviewroot = new TreeItem<Node>();

    var hbox = new HBox(10);
    var resourcesTree = new TreeItem<Node>(hbox);
    var addImageButton = new Button("+");
    hbox.setAlignment(Pos.BASELINE_LEFT);

    hbox.getChildren().addAll(new Label("Resources"), addImageButton);

    var entityTree = new TreeItem<Node>(new Label("Entity0"));

    addImageButton.setOnAction(_ -> {
      FileChooser fileChooser = new FileChooser();

      //Set extension filter
      fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image files", "*.PNG", "*.JPG"));

      //Show open file dialog
      File file = fileChooser.showOpenDialog(null);

      if (file != null) {
        var image = new Image(file.toURI().toString());
        var label = new Label(file.getName());
        label.setOnMousePressed(_ -> {
          decorateRightPane(image, rightPane);
          decorateBottomPane(image, bottomPane);
        });
        var treeItem = new TreeItem<Node>(label);
        resourcesTree.getChildren().add(treeItem);
        treeview.getSelectionModel().select(treeItem);
        fireEvent(label);
      }
    });

    treeviewroot.getChildren().addAll(resourcesTree, entityTree);

    treeview.setRoot(treeviewroot);
    treeview.setShowRoot(false);

    treeview.setOnKeyPressed(e -> {
      TreeItem<Node> selected = treeview.getSelectionModel().getSelectedItem();
      if (selected != null && e.getCode() == KeyCode.ENTER) {
        fireEvent(selected.getValue());
      }
    });

    rightPane.add(new Label("X:"), 0, 0);
    rightPane.add(new Label("Y:"), 0, 1);
    var x = new NumberTextField(WIDTH);
    var y = new NumberTextField(HEIGHT);
    rightPane.add(x, 1, 0);
    rightPane.add(y, 1, 1);

    x.promptTextProperty().bind(entity.xProperty().asString());
    y.promptTextProperty().bind(entity.yProperty().asString());

    x.setText((int) entity.getX() + "");
    y.setText((int) entity.getY() + "");

    x.setOnAction(_ -> entity.setX(Integer.parseInt(x.getText())));
    y.setOnAction(_ -> entity.setY(Integer.parseInt(y.getText())));

    exit.setOnAction(_ -> System.exit(0));

    FXGL.getGameScene().addUINodes(menubar, treeview, rightPane,bottomPane);
  }

  private void fireEvent(Node n){
    n.fireEvent(new MouseEvent(MouseEvent.MOUSE_PRESSED,
      n.getLayoutX(), n.getLayoutY(), n.getLayoutX(), n.getLayoutY(), MouseButton.PRIMARY, 1,
      true, true, true, true, true, true, true,
      true, true, true, null));
  }

  private void decorateRightPane(Object object, GridPane rightPane){
    rightPane.getChildren().clear();
    switch (object){
      case Image image -> {
        rightPane.add(new Label("Image Width:"), 0, 0);
        rightPane.add(new Label("Image Height:"), 0, 1);
        var width = new Label(image.getWidth()+"");
        var height = new Label(image.getHeight()+"");
        rightPane.add(width, 1, 0);
        rightPane.add(height, 1, 1);
      }
      default -> {
        var gamescene = FXGL.getGameScene();
        rightPane.add(new Label("GameScene Width:"), 0, 0);
        rightPane.add(new Label("GameScene Height:"), 0, 1);
        var width = new Label(gamescene.getWidth()+"");
        var height = new Label(gamescene.getHeight()+"");
        rightPane.add(width, 1, 0);
        rightPane.add(height, 1, 1);
      }
    }
  }

  private void decorateBottomPane(Object object, Pane pane){
    pane.getChildren().clear();
    switch (object){
      case Image image ->{
        var view = new ImageView(image);
        var vbox = new VBox(20,new Label("Image Preview:"),view);
        vbox.setPadding(new Insets(20));
        pane.getChildren().add(vbox);
      }
      default -> {}
    }
  }
}
