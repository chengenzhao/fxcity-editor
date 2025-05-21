package com.whitewoodcity.fxcityeditor;

import com.almasb.fxgl.dsl.FXGL;
import com.whitewoodcity.fxgl.texture.Texture;
import com.whitewoodcity.fxgl.texture.TransitTexture;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Rotate;

public interface GameAppDecorator1 {
  default void showFrameData(){
    ButtonType okButtonType = ButtonType.OK;
    Dialog<ButtonType> dialog = new Dialog<>();

    var vbox = new VBox();
    var keyFrames = FXGL.<GameApp>getAppCast().keyFrames;
    var kf = keyFrames.get(FXGL.<GameApp>getAppCast().currentKeyFrame);
    var map = kf.getRotateTransit2DTextureBiMap();

    for (var item : FXGL.<GameApp>getAppCast().getAllComponentsLabelBoxes()) {
      var texture = map.get(item);
      var json = extractJsonFromTexture(texture);
      var textArea = new TextArea(json.toString());
      textArea.setWrapText(true);
      textArea.setEditable(false);
      textArea.setPrefHeight(100);
      vbox.getChildren().addAll(new Label(item.getLabelString()), textArea);
    }

    var scrollpane = new ScrollPane(vbox);
    dialog.getDialogPane().setContent(scrollpane);
    dialog.getDialogPane().getButtonTypes().add(okButtonType);
    dialog.getDialogPane().lookupButton(okButtonType);

    dialog.showAndWait();
  }

  default void showTransitData(){
    ButtonType okButtonType = ButtonType.OK;
    Dialog<ButtonType> dialog = new Dialog<>();

    var keyFrames = FXGL.<GameApp>getAppCast().keyFrames;

    var vbox = new VBox();

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
      vbox.getChildren().addAll(new Label(item.getLabelString()), textArea);
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
