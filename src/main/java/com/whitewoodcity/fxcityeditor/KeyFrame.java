package com.whitewoodcity.fxcityeditor;


import com.almasb.fxgl.dsl.FXGL;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.whitewoodcity.control.RotateTransit2DTexture;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.security.Key;

public class KeyFrame extends Rectangle implements GameAppDecorator{
  private final double width, height;

  public KeyFrame(double width, double height) {
    super(width, height);
    this.width = width;
    this.height = height;
  }

  private final BiMap<HBox, RotateTransit2DTexture> rotateTransit2DTextureBiMap = HashBiMap.create();
//  private Duration time;
  private final ObjectProperty<Duration> time = new SimpleObjectProperty<>();

//  public Duration getTime() {
//    return time;
//  }
//
//  public KeyFrame setTime(Duration time) {
//    this.time = time;
//    return this;
//  }

  public Duration getTime() {
    return time.get();
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

  public BiMap<HBox, RotateTransit2DTexture> getRotateTransit2DTextureBiMap() {
    return rotateTransit2DTextureBiMap;
  }

  public void copyFrom(KeyFrame keyFrame){
    var keySet = keyFrame.rotateTransit2DTextureBiMap.keySet();
    for(var hBox:keySet){
      var texture = keyFrame.rotateTransit2DTextureBiMap.get(hBox).clone();
      this.rotateTransit2DTextureBiMap.put(hBox, texture);

      texture.setOnMouseClicked(_ -> selectTreeItem(hBox));
      texture.children().addListener((ListChangeListener<RotateTransit2DTexture>) _ -> selectTreeItem(hBox));

      var gameApp = FXGL.<GameApp>getAppCast();

      gameApp.rectMap.put(this, createSelectionRectangle(texture));
      gameApp.arrowMap.put(this, createRotateArrow(texture));
    }

  }
}
