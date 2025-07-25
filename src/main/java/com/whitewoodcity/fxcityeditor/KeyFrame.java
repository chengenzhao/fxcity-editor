package com.whitewoodcity.fxcityeditor;


import com.almasb.fxgl.dsl.FXGL;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.whitewoodcity.control.LabelBox;
import com.whitewoodcity.control.RotateTransit2DTexture;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class KeyFrame extends Rectangle implements GameAppDecorator{
  private final double width, height;

  public KeyFrame(double width, double height) {
    super(width, height);
    this.width = width;
    this.height = height;
  }

  private final BiMap<LabelBox, RotateTransit2DTexture> rotateTransit2DTextureBiMap = HashBiMap.create();
  private final ObjectProperty<Duration> time = new SimpleObjectProperty<>();

  public Duration getTime() {
    return time.get();
  }

  public double getTimeInSeconds(){
    return getTime().toSeconds();
  }

  public ObjectProperty<Duration> timeProperty() {
    return time;
  }

  public KeyFrame setTime(Duration time) {
    this.time.set(time);
    return this;
  }

  public KeyFrame setColor(Paint fill){
    super.setFill(fill);
    return this;
  }

  public KeyFrame setCenterX(double x){
    super.setX(x - width/2);
    return this;
  }

  public KeyFrame setCenterY(double y){
    super.setY(y - height/2);
    return this;
  }

  public KeyFrame bindCenterX(ObservableValue<Number> x){
    super.xProperty().bind(x.map(Number::doubleValue).map(v -> v - width/2));
    return this;
  }

  public double getCenterX(){
    return getX() + width/2;
  }

  public KeyFrame bindCenterY(ObservableValue<Number> y){
    super.yProperty().bind(y.map(Number::doubleValue).map(v -> v - height/2));
    return this;
  }

  public void select(){
    this.setStroke(Color.web("#039ED3"));
  }
  public void deSelect(){
    this.setStroke(null);
  }

  public BiMap<LabelBox, RotateTransit2DTexture> getRotateTransit2DTextureBiMap() {
    return rotateTransit2DTextureBiMap;
  }

  public void copyFrom(KeyFrame keyFrame){
    var keySet = keyFrame.rotateTransit2DTextureBiMap.keySet();
    var gameApp = FXGL.<GameApp>getAppCast();
    for(var hBox:keySet){
      var texture = keyFrame.rotateTransit2DTextureBiMap.get(hBox).clone();
      this.rotateTransit2DTextureBiMap.put(hBox, texture);

      texture.setOnMouseClicked(_ -> selectTreeItem(hBox));
      texture.children().addListener((ListChangeListener<RotateTransit2DTexture>) _ -> selectTreeItem(hBox));

      gameApp.rectMaps.get(hBox).put(this, createSelectionRectangle(texture));
      gameApp.arrowMaps.get(hBox).put(this, createRotateArrow(texture));
    }

    for(var texture:keyFrame.rotateTransit2DTextureBiMap.values()){
      if(texture.parent()!=null){
        var child = rotateTransit2DTextureBiMap.get(keyFrame.rotateTransit2DTextureBiMap.inverse().get(texture));
        var parent = rotateTransit2DTextureBiMap.get(keyFrame.rotateTransit2DTextureBiMap.inverse().get(texture.parent()));
        child.setParent(parent);
      }
    }

    for(var texture:rotateTransit2DTextureBiMap.values())
      texture.update();
  }


}
