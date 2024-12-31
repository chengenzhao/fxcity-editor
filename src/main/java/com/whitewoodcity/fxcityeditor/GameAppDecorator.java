package com.whitewoodcity.fxcityeditor;

import com.almasb.fxgl.entity.Entity;
import com.whitewoodcity.control.NumberField;
import com.whitewoodcity.fxgl.texture.AnimatedTexture;
import com.whitewoodcity.javafx.binding.XBindings;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Rotate;
import javafx.stage.Screen;
import javafx.util.converter.NumberStringConverter;

public interface GameAppDecorator {

  int HEIGHT = 1000;
  int WIDTH = (int) (Screen.getPrimary().getBounds().getWidth() / Screen.getPrimary().getBounds().getHeight() * 1000);

  default void fireEvent(Node n) {
    n.fireEvent(new MouseEvent(MouseEvent.MOUSE_PRESSED,
      n.getLayoutX(), n.getLayoutY(), n.getLayoutX(), n.getLayoutY(), MouseButton.PRIMARY, 1,
      true, true, true, true, true, true, true,
      true, true, true, null));
  }

  default void freezeEvent(Node n) {
    n.fireEvent(new MouseEvent(MouseEvent.MOUSE_RELEASED,
      n.getLayoutX(), n.getLayoutY(), n.getLayoutX(), n.getLayoutY(), MouseButton.SECONDARY, 1,
      true, true, true, true, true, true, true,
      true, true, true, null));
  }

  default void selectTreeItem(Node n, TreeView<Node> treeView) {
    var treeItem = getTreeItem(n, treeView.getRoot());
    var selectedItem =treeView.getSelectionModel().getSelectedItem();
    if(selectedItem!=null && selectedItem.getValue() == n)
      fireEvent(n);
    else
      treeView.getSelectionModel().select(treeItem);
  }

  default TreeItem<Node> getTreeItem(Node n, TreeItem<Node> treeItem) {
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

  default void removeTreeItem(Node n, TreeView<Node> treeView) {
    var treeItem = getTreeItem(n, treeView.getRoot());
    var nextItem = treeItem.nextSibling() == null ? treeItem.previousSibling() == null ?
      treeItem.getParent() : treeItem.previousSibling() : treeItem.nextSibling();
    selectTreeItem(nextItem.getValue(), treeView);
    freezeEvent(n);
    treeItem.getParent().getChildren().remove(treeItem);
  }

  default void decorateRightPane(Object object, GridPane rightPane) {
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

        Bindings.bindBidirectional(x.textProperty(), e.xProperty(), new NumberStringConverter());
        Bindings.bindBidirectional(y.textProperty(), e.yProperty(), new NumberStringConverter());

        x.setText((int) e.getX() + "");
        y.setText((int) e.getY() + "");

        x.setOnAction(_ -> e.setX(x.getDouble()));
        y.setOnAction(_ -> e.setY(y.getDouble()));
      }
      case AnimatedTexture animatedTexture -> {
        rightPane.add(new Label("Translate X:"), 0, 0);
        rightPane.add(new Label("Translate Y:"), 0, 1);
        var x = new NumberField(-WIDTH / 2, WIDTH / 2);
        var y = new NumberField(-HEIGHT / 2, HEIGHT / 2);
        rightPane.add(x, 1, 0);
        rightPane.add(y, 1, 1);

        Bindings.bindBidirectional(x.textProperty(), animatedTexture.translateXProperty(), new NumberStringConverter());
        Bindings.bindBidirectional(y.textProperty(), animatedTexture.translateYProperty(), new NumberStringConverter());

        x.setText(animatedTexture.getTranslateX() + "");
        y.setText(animatedTexture.getTranslateY() + "");

        x.setOnAction(_ -> animatedTexture.setTranslateX(x.getDouble()));
        y.setOnAction(_ -> animatedTexture.setTranslateY(y.getDouble()));

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

  default void decorateBottomAndRightPane(Object object, Pane pane, GridPane rightPane) {
    pane.getChildren().clear();
    switch (object) {
      case Image image -> {
        var view = new ImageView(image);
        var vbox = new VBox(20, new Label("Image Preview:"), view);
        vbox.setPadding(new Insets(20));
        pane.getChildren().add(vbox);

        decorateRightPane(image, rightPane);
      }

      case Entity entity ->{
        var hbox = new HBox(20);
        hbox.setAlignment(Pos.TOP_RIGHT);
        hbox.setPadding(new Insets(20));
        var playButton = new Button("⏯");
        var stopButton = new Button("⏹");
        hbox.layoutXProperty().bind(pane.widthProperty().subtract(hbox.widthProperty()));
        hbox.getChildren().addAll(playButton, stopButton);

        var line = new Line();
        line.setStroke(Color.DARKCYAN);
        line.setStrokeWidth(10);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStartX((pane.getWidth() - 1200) / 2);
        line.startYProperty().bind(hbox.layoutYProperty().add(hbox.heightProperty().multiply(2)));
        line.setEndX(line.getStartX() + 1200);
        line.endYProperty().bind(line.startYProperty());

        var anchor = new Line();
        anchor.setStrokeLineCap(StrokeLineCap.ROUND);
        anchor.setStrokeWidth(20);
        anchor.setStroke(Color.RED);
        anchor.endXProperty().bind(anchor.startXProperty());
        anchor.startXProperty().bind(line.startXProperty());
        anchor.startYProperty().bind(line.startYProperty().subtract(25));
        anchor.endYProperty().bind(anchor.startYProperty().add(50));

        pane.getChildren().addAll(hbox, line, anchor);

        playButton.setOnAction(_ -> entity.getViewComponent().getChildren().forEach(this::startAnimations));

        stopButton.setOnAction(_ -> entity.getViewComponent().getChildren().forEach(this::stopAnimations));

        decorateRightPane(entity, rightPane);
      }

      case AnimatedTexture animatedTexture -> {
        var hbox = new HBox(20);
        hbox.setAlignment(Pos.TOP_RIGHT);
        hbox.setPadding(new Insets(20));
        var playButton = new Button("⏯");
        var stopButton = new Button("⏹");
        hbox.layoutXProperty().bind(pane.widthProperty().subtract(hbox.widthProperty()));
        hbox.getChildren().addAll(playButton, stopButton);

        var line = new Line();
        line.setStroke(Color.DARKCYAN);
        line.setStrokeWidth(10);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStartX((pane.getWidth() - 1200) / 2);
        line.startYProperty().bind(hbox.layoutYProperty().add(hbox.heightProperty().multiply(2)));
        line.setEndX(line.getStartX() + 1200);
        line.endYProperty().bind(line.startYProperty());

        var anchor = new Line();
        anchor.setStrokeLineCap(StrokeLineCap.ROUND);
        anchor.setStrokeWidth(20);
        anchor.setStroke(Color.RED);
        anchor.endXProperty().bind(anchor.startXProperty());
        anchor.startXProperty().bind(
          XBindings.reduce(
            line.startXProperty().map(Number::doubleValue),
            line.endXProperty().map(Number::doubleValue),
            animatedTexture.timeProperty().map(Number::doubleValue),
            (s, e, t) -> s + (e - s) / animatedTexture.getAnimationChannel().getChannelDuration().toSeconds() * t));
        anchor.startYProperty().bind(line.startYProperty().subtract(25));
        anchor.endYProperty().bind(anchor.startYProperty().add(50));

        pane.getChildren().addAll(hbox, line, anchor);

        playButton.setOnAction(_ -> startAnimations(animatedTexture));

        stopButton.setOnAction(_ -> stopAnimations(animatedTexture));

        decorateRightPane(animatedTexture, rightPane);
      }
      default -> {
      }
    }
  }

  private void startAnimations(Node component){
    switch (component){
      case AnimatedTexture animatedTexture ->{
        if (!animatedTexture.isAnimating())
          animatedTexture.loop();
        else if (animatedTexture.isPaused())
          animatedTexture.resume();
        else
          animatedTexture.pause();
      }
      default -> {}
    }
  }

  private void stopAnimations(Node component){
    switch (component){
      case AnimatedTexture animatedTexture -> animatedTexture.stop();
      default -> {}
    }
  }
}
