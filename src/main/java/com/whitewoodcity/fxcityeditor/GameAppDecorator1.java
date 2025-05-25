package com.whitewoodcity.fxcityeditor;

import com.almasb.fxgl.dsl.FXGL;
import com.whitewoodcity.fxgl.texture.Texture;
import com.whitewoodcity.fxgl.texture.TransitTexture;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Rotate;

public interface GameAppDecorator1 {
  default void showFrameData() {
    ButtonType okButtonType = ButtonType.OK;
    Dialog<ButtonType> dialog = new Dialog<>();

    var vbox = new VBox();
    var keyFrames = FXGL.<GameApp>getAppCast().keyFrames;
    var kf = keyFrames.get(FXGL.<GameApp>getAppCast().currentKeyFrame);
    var map = kf.getRotateTransit2DTextureBiMap();
    vbox.setSpacing(5);

    for (var item : FXGL.<GameApp>getAppCast().getAllComponentsLabelBoxes()) {
      var texture = map.get(item);
      var json = extractJsonFromTexture(texture);
      var textArea = new TextArea(json.toString());
      textArea.setWrapText(true);
      textArea.setEditable(false);
      textArea.setPrefHeight(100);
      var rotateNum = new TextField("" + json.getJsonArray(TransitTexture.JsonKeys.ROTATES.key()).size());
      rotateNum.setEditable(false);
      rotateNum.setPrefWidth(50);
      var hbox = new HBox(new Label("# of rotates in transforms:"), rotateNum);
      hbox.setSpacing(20);
      var s = new Separator();
      s.setPrefWidth(500);
      s.setOrientation(Orientation.HORIZONTAL);
      if(!vbox.getChildren().isEmpty())
        vbox.getChildren().add(s);
      vbox.getChildren().addAll( new Label(item.getLabelString()), hbox, textArea);
    }

    var scrollpane = new ScrollPane(vbox);
    dialog.getDialogPane().setContent(scrollpane);
    dialog.getDialogPane().getButtonTypes().add(okButtonType);
    dialog.getDialogPane().lookupButton(okButtonType);

    dialog.showAndWait();
  }

  default void showTransitData() {
    ButtonType okButtonType = ButtonType.OK;
    Dialog<ButtonType> dialog = new Dialog<>();

    var keyFrames = FXGL.<GameApp>getAppCast().keyFrames;

    var vbox = new VBox();
    vbox.setSpacing(5);

    for (var item : FXGL.<GameApp>getAppCast().getAllComponentsLabelBoxes()) {
      var animationData = new JsonArray();

      var jsons = keyFrames.stream().map(kf -> extractJsonFromTexture(kf.getTimeInSeconds() * 1000, kf.getRotateTransit2DTextureBiMap().get(item))).toList();
      animationData.addAll(new JsonArray(jsons));

      var texture = keyFrames.getFirst().getRotateTransit2DTextureBiMap().get(item);
      var jsonNode = extractJsonFromTexture(FXGL.<GameApp>getAppCast().maxTime.getDouble() * 1000, texture);
      animationData.add(jsonNode);

      var textArea = new TextArea(animationData.toString());
      textArea.setWrapText(true);
      textArea.setPrefHeight(100);
      textArea.setEditable(false);

      var rotateNum = new TextField("" + jsons.getFirst().getJsonArray(TransitTexture.JsonKeys.ROTATES.key()).size());
      rotateNum.setEditable(false);
      rotateNum.setPrefWidth(50);
      var hbox = new HBox(new Label("# of rotates in transforms:"), rotateNum);
      hbox.setSpacing(20);

      var s = new Separator();
      s.setPrefWidth(500);
      s.setOrientation(Orientation.HORIZONTAL);
      if(!vbox.getChildren().isEmpty())
        vbox.getChildren().add(s);

      vbox.getChildren().addAll(new Label(item.getLabelString()), hbox, textArea);
    }

    var scrollpane = new ScrollPane(vbox);
    dialog.getDialogPane().setContent(scrollpane);
    dialog.getDialogPane().getButtonTypes().add(okButtonType);
    dialog.getDialogPane().lookupButton(okButtonType);

    dialog.showAndWait();
  }

  default JsonObject extractJsonFromTexture(double timeInMillis, Texture texture) {
    var json = extractJsonFromTexture(texture);
    json.put(TransitTexture.JsonKeys.TIME.key(), timeInMillis);//time in millis
    return json;
  }

  default JsonObject extractJsonFromTexture(Texture texture) {
    var json = new JsonObject();

    json.put(TransitTexture.JsonKeys.X.key(), texture.getX());
    json.put(TransitTexture.JsonKeys.Y.key(), texture.getY());
    var rotates = new JsonArray();
    for (var rotateRaw : texture.getTransforms()) {
      var rotate = (Rotate) rotateRaw;
      var rjson = new JsonObject();
      rjson.put(TransitTexture.JsonKeys.PIVOT_X.key(), rotate.getPivotX());
      rjson.put(TransitTexture.JsonKeys.PIVOT_Y.key(), rotate.getPivotY());
      rjson.put(TransitTexture.JsonKeys.ANGLE.key(), rotate.getAngle());
      rotates.add(rjson);
    }
    json.put(TransitTexture.JsonKeys.ROTATES.key(), rotates);
    return json;
  }
}
