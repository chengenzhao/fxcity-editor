package com.whitewoodcity.fxcityeditor;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.whitewoodcity.control.IntField;
import com.whitewoodcity.control.NumberField;
import com.whitewoodcity.control.RotateTransit2DTexture;
import com.whitewoodcity.control.arrows.Arrow;
import com.whitewoodcity.fxgl.texture.AnimatedTexture;
import com.whitewoodcity.fxgl.texture.AnimationChannel;
import com.whitewoodcity.fxgl.texture.Texture;
import com.whitewoodcity.javafx.binding.XBindings;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Screen;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;

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

  default void selectTreeItem(Node n) {
    var treeView = FXGL.<GameApp>getAppCast().treeView;
    var treeItem = getTreeItem(n, treeView.getRoot());
    var selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null && selectedItem.getValue() == n)
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

  default void removeTreeItem(Node n) {
    var treeView = FXGL.<GameApp>getAppCast().treeView;
    var treeItem = getTreeItem(n, treeView.getRoot());
    var nextItem = treeItem.nextSibling() == null ? treeItem.previousSibling() == null ?
      treeItem.getParent() : treeItem.previousSibling() : treeItem.nextSibling();
    selectTreeItem(nextItem.getValue());
    freezeEvent(n);
    treeItem.getParent().getChildren().remove(treeItem);
  }

  default HBox createDeletableLableBox(String name) {
    var textureLabel = new Label(name);
    var delTextureButton = new Button("×");
    var textureHBox = new HBox(20, textureLabel, delTextureButton);
    textureHBox.setAlignment(Pos.BASELINE_LEFT);
    return textureHBox;
  }

  default TreeItem<Node> createDeletableTreeItem(HBox textureHBox, Runnable runnable) {
    var textureItem = new TreeItem<Node>(textureHBox);

    ((Button) textureHBox.getChildren().get(1)).setOnAction(_ -> {
      removeTreeItem(textureHBox);
      runnable.run();
    });

    return textureItem;
  }

  default TreeItem<Node> createDeletableTreeItem(String name, Runnable runnable) {
    var box = createDeletableLableBox(name);
    return createDeletableTreeItem(box, runnable);
  }

  default Arrow createRotateArrow(RotateTransit2DTexture imageView) {
    var arrow = new Arrow(0, 0, 0, imageView.getFitHeight());
    arrow.x1Property().bind(imageView.getRotation().pivotXProperty());
    arrow.y1Property().bind(imageView.getRotation().pivotYProperty());
    arrow.y2Property().bind(XBindings.reduceDoubleValue(arrow.y1Property(), imageView.fitHeightProperty(), (y, h) -> y + Math.max(70, h)));
    arrow.x2Property().bind(arrow.x1Property());
    imageView.getTransforms().addListener((ListChangeListener<Transform>) _ -> {
      arrow.getTransforms().clear();
      arrow.getTransforms().addAll(imageView.getTransforms());
    });
    return arrow;
  }

  default Rectangle createJointSelectionRectangle(Texture texture) {
    var rect = createSelectionRectangle(texture);
    rect.getStrokeDashArray().addAll(5d);
    rect.setMouseTransparent(true);
    return rect;
  }

  default Rectangle createSelectionRectangle(Texture texture) {
    var rect = new Rectangle();
    rect.widthProperty().bind(texture.fitWidthProperty());
    rect.heightProperty().bind(texture.fitHeightProperty());
    rect.xProperty().bind(texture.xProperty());
    rect.yProperty().bind(texture.yProperty());
    rect.setFill(Color.TRANSPARENT);
    rect.setStroke(Color.web("#039ED3"));
    rect.getTransforms().addAll(texture.getTransforms());
    texture.getTransforms().addListener((ListChangeListener<Transform>) (_) -> {
      rect.getTransforms().clear();
      rect.getTransforms().addAll(texture.getTransforms());
    });
    return rect;
  }

  default void decorateBottomAndRightPane(Object object, Object... p) {
    var pane = FXGL.<GameApp>getAppCast().bottomPane;
    pane.getChildren().clear();
    switch (object) {
      case Image image -> {
        var view = new ImageView(image);
        var vbox = new VBox(20, new Label("Image Preview:"), view);
        vbox.setPadding(new Insets(20));
        pane.getChildren().add(vbox);

        decorateRightPane(image);
      }

      case Entity entity -> {
        var keyFrames = FXGL.<GameApp>getAppCast().keyFrames;
        keyFrames.sort(Comparator.comparingDouble(KeyFrame::getTimeInSeconds));

        var hbox = new HBox(20);
        hbox.setAlignment(Pos.TOP_RIGHT);
        hbox.setPadding(new Insets(20));
        var playButton = new Button("⏯");
        var stopButton = new Button("⏹");
        var addButton = new Button("+");
        hbox.layoutXProperty().bind(pane.widthProperty().subtract(hbox.widthProperty()));
        hbox.getChildren().addAll(playButton, stopButton, new Label("Total Time: "), FXGL.<GameApp>getAppCast().maxTime, addButton);

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
        anchor.setStroke(Color.LIGHTBLUE);
        anchor.endXProperty().bind(anchor.startXProperty());
        anchor.startXProperty().bind(line.startXProperty());
        anchor.startYProperty().bind(line.startYProperty().subtract(25));
        anchor.endYProperty().bind(anchor.startYProperty().add(50));

        pane.getChildren().addAll(hbox, anchor, line);

        for (int i = 0; i < keyFrames.size(); i++) {
          var kf = keyFrames.get(i);
          bindKeyFrameTag(kf, line, i > 0);
          var timeField = buildTimeFieldForKeyFrame(kf, i > 0);
          pane.getChildren().addAll(kf, timeField);

          if (i > 0) {
            var delButton = buildDelButtonForKeyFrame(kf, timeField);
            pane.getChildren().add(delButton);
          }
        }

        playButton.setOnAction(_ -> {
          decorateMiddlePane(keyFrames.getFirst());
          //collect data
          for (var item : keyFrames.getFirst().getRotateTransit2DTextureBiMap().keySet()) {
            ObjectMapper objectMapper = new ObjectMapper();
            var animationData = objectMapper.createArrayNode();

            for (var kf : keyFrames) {
              var json = objectMapper.createObjectNode();
              json.put("time", kf.getTimeInSeconds() * 1000);//time in millis
              var texture = kf.getRotateTransit2DTextureBiMap().get(item);
              json.put("x", texture.getX());
              json.put("y", texture.getY());
              var rotates = objectMapper.createArrayNode();
              for (var rotateRaw : texture.getTransforms()) {
                var rotate = (Rotate) rotateRaw;
                var rjson = objectMapper.createObjectNode();
                rjson.put("pivotX", rotate.getPivotX());
                rjson.put("pivotY", rotate.getPivotY());
                rjson.put("angle", rotate.getAngle());
                rotates.add(rjson);
              }
              json.set("rotates", rotates);
              animationData.add(json);
            }

            var json = objectMapper.createObjectNode();
            json.put("time", FXGL.<GameApp>getAppCast().maxTime.getDouble() * 1000);//time in millis
            var texture = keyFrames.getFirst().getRotateTransit2DTextureBiMap().get(item);
            json.put("x", texture.getX());
            json.put("y", texture.getY());
            var rotates = objectMapper.createArrayNode();
            for (var rotateRaw : texture.getTransforms()) {
              var rotate = (Rotate) rotateRaw;
              var rjson = objectMapper.createObjectNode();
              rjson.put("pivotX", rotate.getPivotX());
              rjson.put("pivotY", rotate.getPivotY());
              rjson.put("angle", rotate.getAngle());
              rotates.add(rjson);
            }
            json.set("rotates", rotates);
            animationData.add(json);

            //todo
            texture.constructTransition(animationData);
            texture.loopTransition();
          }


          entity.getViewComponent().getChildren().forEach(this::startAnimations);
        });

        stopButton.setOnAction(_ -> entity.getViewComponent().getChildren().forEach(this::stopAnimations));

        addButton.setOnAction(_ -> {
          var app = FXGL.<GameApp>getAppCast();
          app.getCurrentKeyFrame().deSelect();
          var maxTime = app.maxTime;
          var kf = generateKeyFrame(Duration.seconds(maxTime.getDouble()));

          kf.copyFrom(keyFrames.getLast());
          keyFrames.add(kf);

          app.setCurrentKeyFrame(kf);
          app.getCurrentKeyFrame().select();
          decorateMiddlePane(keyFrames.getLast());
          decorateBottomAndRightPane(entity);
        });

        decorateRightPane(entity);
      }
      case AnimatedTexture animatedTexture -> {
        var hbox = new HBox(20);
        hbox.setAlignment(Pos.TOP_RIGHT);
        hbox.setPadding(new Insets(20));
        var currentFrame = new TextField();
        currentFrame.setEditable(false);
        var currentTime = new TextField();
        currentTime.setEditable(false);
        var framesPerRow = new IntField(1, 50);
        framesPerRow.setPromptText("How many frames per row?");
        framesPerRow.setText(animatedTexture.getAnimationChannel().getSequence().size() + "");
        var playButton = new Button("⏯");
        var stopButton = new Button("⏹");
        hbox.layoutXProperty().bind(pane.widthProperty().subtract(hbox.widthProperty()));
        hbox.getChildren().addAll(currentFrame, currentTime, framesPerRow, playButton, stopButton);

        framesPerRow.textProperty().addListener((_, _, value) -> {
          var animatedChannel = animatedTexture.getAnimationChannel();
          var image = animatedChannel.getImage();
          animatedChannel = new AnimationChannel(image, Duration.seconds(1), Integer.parseInt(value));
          animatedTexture.updateAnimatedTexture(animatedChannel);
        });
        framesPerRow.setOnAction(_ -> playButton.fire());

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
        anchor.endYProperty().bind(anchor.startYProperty().add(60));

        currentFrame.textProperty().bind(animatedTexture.currentFrameProperty().map(f -> "Current Frame: " + f));
        currentTime.textProperty().bind(animatedTexture.timeProperty().map(Number::doubleValue).map(t -> " Duration: " + new DecimalFormat("0.000").format(t)));

        pane.getChildren().addAll(hbox, line, anchor);

        playButton.setOnAction(_ -> startAnimations(animatedTexture));

        stopButton.setOnAction(_ -> stopAnimations(animatedTexture));

        decorateRightPane(animatedTexture);
      }
      case RotateTransit2DTexture texture when p.length > 0 && p[0] instanceof BiMap biMap -> {
        var map = (BiMap<HBox, RotateTransit2DTexture>) biMap;
        var choiceBox = new ChoiceBox<HBox>();
        choiceBox.getItems().add(null);
        choiceBox.getItems().addAll(map.keySet());
        removeTextureFromItems(choiceBox.getItems(), texture, map);

        choiceBox.setValue(map.inverse().get(texture.parent()));

        choiceBox.setConverter(new StringConverter<>() {
          @Override
          public String toString(HBox hBox) {
            return hBox == null ? "" : ((Label) hBox.getChildren().getFirst()).getText();
          }

          @Override
          public HBox fromString(String string) {
            return choiceBox.getItems().stream()
              .filter(hbox -> ((Label) hbox.getChildren().getFirst()).getText().equals(string))
              .findFirst().orElse(null);
          }
        });

        choiceBox.setOnAction(_ -> {
          var childBox = map.inverse().get(texture);
          var parentBox = map.inverse().get(map.get(choiceBox.getValue()));
          for (var keyFrame : FXGL.<GameApp>getAppCast().keyFrames) {
            var m = keyFrame.getRotateTransit2DTextureBiMap();
            m.get(childBox).setParent(m.get(parentBox));
          }
        });

        var hbox = new HBox();
        hbox.getChildren().addAll(new Label("Parent: "), choiceBox);
        hbox.setPadding(new Insets(20));
        hbox.setSpacing(20);
        hbox.setAlignment(Pos.BASELINE_LEFT);
        pane.getChildren().add(hbox);

        decorateRightPane(texture);
      }
      default -> {
      }
    }
  }

  private void bindKeyFrameTag(KeyFrame kf, Line line, boolean draggable) {
    var maxTime = FXGL.<GameApp>getAppCast().maxTime;
    var ox = kf.getX();

    kf.bindCenterX(XBindings.reduce(kf.timeProperty(), maxTime.textProperty().map(Double::parseDouble).map(t -> Math.max(t, 0.0001)),
      (keyFrameTime, totalTime) -> Math.min(line.getStartX() + (line.getEndX() - line.getStartX()) * keyFrameTime.toSeconds() / totalTime, line.getEndX())));
    kf.bindCenterY(line.startYProperty());

    kf.setOnMousePressed(_ -> {
      FXGL.<GameApp>getAppCast().getCurrentKeyFrame().deSelect();
      kf.select();
      FXGL.<GameApp>getAppCast().setCurrentKeyFrame(kf);
      decorateMiddlePane(kf);
    });

    if (draggable) {
      kf.setOnMouseDragged(e -> {
        var cx = e.getX() - ox;
        var ex = ox + cx - line.getStartX();

        ex = Math.min(Math.max(0, ex), line.getEndX() - line.getStartX());

        kf.setTime(Duration.seconds(ex * maxTime.getDouble() / (line.getEndX() - line.getStartX())));
      });
    }
  }

  private Button buildDelButtonForKeyFrame(KeyFrame kf, TextField timeField) {
    var delButton = new Button("x");
    delButton.translateXProperty().bind(kf.xProperty());
    delButton.translateYProperty().bind(kf.yProperty().add(kf.heightProperty()).add(timeField.heightProperty()));
    var gameApp = FXGL.<GameApp>getAppCast();
    delButton.setOnAction(_ -> {
      gameApp.getCurrentKeyFrame().deSelect();
      gameApp.keyFrames.remove(kf);
      for (var map : gameApp.rectMaps.values()) {
        map.remove(kf);
      }
      for (var map : gameApp.arrowMaps.values()) {
        map.remove(kf);
      }
      gameApp.setCurrentKeyFrame(0);
      gameApp.getCurrentKeyFrame().select();
      decorateMiddlePane(gameApp.getCurrentKeyFrame());
      decorateBottomAndRightPane(gameApp.entity);
    });
    return delButton;
  }

  private TextField buildTimeFieldForKeyFrame(KeyFrame kf, boolean editable) {
    var maxTime = FXGL.<GameApp>getAppCast().maxTime;
    var timeField = new NumberField(0, (int) maxTime.getDouble() + 1);
    timeField.translateXProperty().bind(kf.xProperty());
    timeField.translateYProperty().bind(kf.yProperty().add(kf.heightProperty()));
    timeField.setPrefWidth(kf.getWidth() * 2);
    timeField.textProperty().bind(kf.timeProperty().map(t -> t.toSeconds() + ""));
    if (editable) {
      Runnable onFocusAction = () -> {
        timeField.textProperty().unbind();
        timeField.setEditable(true);
        timeField.setMaxValue(maxTime.getDouble());
      };
      Runnable lostFocusAction = () -> {
        timeField.textProperty().unbind();
        kf.setTime(Duration.seconds(timeField.getDouble()));
        timeField.textProperty().bind(kf.timeProperty().map(t -> t.toSeconds() + ""));
        timeField.setEditable(false);
      };
      timeField.setOnMouseClicked(_ -> onFocusAction.run());
      timeField.setOnKeyPressed(e -> {
        if (e.getCode() == KeyCode.ENTER) {
          lostFocusAction.run();
        }
      });
      timeField.focusedProperty().addListener((_, _, newValue) -> {
        if (newValue)
          onFocusAction.run();
        else
          lostFocusAction.run();
      });
    } else {
      timeField.setDisable(true);
    }
    return timeField;
  }

  default void decorateRightPane(Object object) {
    var rightPane = FXGL.<GameApp>getAppCast().rightPane;
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
        rightPane.add(new Label("X:"), 0, 0);
        rightPane.add(new Label("Y:"), 0, 1);
        var x = new NumberField(-WIDTH / 2, WIDTH / 2);
        var y = new NumberField(-HEIGHT / 2, HEIGHT / 2);
        rightPane.add(x, 1, 0);
        rightPane.add(y, 1, 1);

        Bindings.bindBidirectional(x.textProperty(), animatedTexture.xProperty(), new NumberStringConverter());
        Bindings.bindBidirectional(y.textProperty(), animatedTexture.yProperty(), new NumberStringConverter());

        x.setText(animatedTexture.getX() + "");
        y.setText(animatedTexture.getY() + "");

        x.setOnAction(_ -> animatedTexture.setX(x.getDouble()));
        y.setOnAction(_ -> animatedTexture.setY(y.getDouble()));
      }
      case RotateTransit2DTexture rotateTransit2DTexture -> {
        rightPane.add(new Label("X:"), 0, 0);
        rightPane.add(new Label("Y:"), 0, 1);
        var x = new NumberField(-WIDTH / 2, WIDTH / 2);
        var y = new NumberField(-HEIGHT / 2, HEIGHT / 2);
        rightPane.add(x, 1, 0);
        rightPane.add(y, 1, 1);

        Bindings.bindBidirectional(x.textProperty(), rotateTransit2DTexture.xProperty(), new NumberStringConverter());
        Bindings.bindBidirectional(y.textProperty(), rotateTransit2DTexture.yProperty(), new NumberStringConverter());

        final Separator sepHor = new Separator();
        sepHor.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepHor, 0, 2);
        GridPane.setColumnSpan(sepHor, 2);
        rightPane.getChildren().add(sepHor);

        for (int i = 0; i < rotateTransit2DTexture.getRotates().size(); i++) {
          var rotate = rotateTransit2DTexture.getRotates().get(i);
          rightPane.add(new Label("Pivot X:"), 0, 3 + i * 4);
          rightPane.add(new Label("Pivot Y:"), 0, 3 + i * 4 + 1);
          rightPane.add(new Label("Rotate:"), 0, 3 + i * 4 + 2);
          var px = new NumberField(-WIDTH, WIDTH);
          var py = new NumberField(-HEIGHT, HEIGHT);
          var r = new NumberField(0, 720);
          rightPane.add(px, 1, 3 + 4 * i);
          rightPane.add(py, 1, 3 + 4 * i + 1);
          rightPane.add(r, 1, 3 + 4 * i + 2);

          Bindings.bindBidirectional(px.textProperty(), rotate.pivotXProperty(), new NumberStringConverter());
          Bindings.bindBidirectional(py.textProperty(), rotate.pivotYProperty(), new NumberStringConverter());
          Bindings.bindBidirectional(r.textProperty(), rotate.angleProperty(), new NumberStringConverter());

          if (rotate == rotateTransit2DTexture.getRotation()) {
            r.textProperty().addListener((_, _, value) -> {
              rotate.setAngle(Double.parseDouble(value));
              rotateTransit2DTexture.update();
            });
          } else {
            r.setEditable(false);
          }

          px.setEditable(false);
          py.setEditable(false);

          final Separator sh = new Separator();
          sh.setValignment(VPos.CENTER);
          GridPane.setConstraints(sh, 0, 3 + 4 * i + 3);
          GridPane.setColumnSpan(sh, 2);
          rightPane.getChildren().add(sh);
        }

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

  default void decorateMiddlePane(KeyFrame keyFrame) {
    var entity = FXGL.<GameApp>getAppCast().entity;
    //don't use entity.getViewComponent().clearChildren();, this method will dispose node.disposing imageview will cause image -> null
    List.copyOf(entity.getViewComponent().getChildren())
      .forEach(e -> entity.getViewComponent().removeChild(e));
    var entityTree = FXGL.<GameApp>getAppCast().entityTree;
    for (var childItem : entityTree.getChildren()) {
      var hbox = (HBox) childItem.getValue();
      var texture = keyFrame.getRotateTransit2DTextureBiMap().get(hbox);
      entity.getViewComponent().addChild(texture);
    }
  }

  default KeyFrame generateKeyFrame(Duration duration) {
    return new KeyFrame(20, 50).setTime(duration).setColor(Color.ORANGE);//LIGHTSEAGREEN
  }

  private void removeTextureFromItems(ObservableList<HBox> items, RotateTransit2DTexture texture, BiMap<HBox, RotateTransit2DTexture> map) {
    for (var child : texture.children()) {
      removeTextureFromItems(items, child, map);
    }
    var item = map.inverse().get(texture);
    items.remove(item);
  }

  private void startAnimations(Node component) {
    switch (component) {
      case AnimatedTexture animatedTexture -> {
        if (!animatedTexture.isAnimating())
          animatedTexture.loop();
        else if (animatedTexture.isPaused())
          animatedTexture.resume();
        else
          animatedTexture.pause();
      }
      default -> {
      }
    }
  }

  private void stopAnimations(Node component) {
    switch (component) {
      case AnimatedTexture animatedTexture -> animatedTexture.stop();
      default -> {
      }
    }
  }

}
