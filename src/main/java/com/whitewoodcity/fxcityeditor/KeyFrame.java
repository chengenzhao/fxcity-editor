package com.whitewoodcity.fxcityeditor;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.whitewoodcity.control.RotateTransit2DTexture;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class KeyFrame extends Rectangle {
  private final double width, height;

  public KeyFrame(double width, double height) {
    super(width, height);
    this.width = width;
    this.height = height;
  }

  private final BiMap<HBox, RotateTransit2DTexture> rotateTransit2DTextureBiMap = HashBiMap.create();
  private Duration time;

  public Duration getTime() {
    return time;
  }

  public KeyFrame setTime(Duration time) {
    this.time = time;
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
}
