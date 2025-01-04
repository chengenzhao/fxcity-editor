package com.whitewoodcity.control;

import com.whitewoodcity.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

public class RotateTransit2DTexture extends Texture {

  private final List<Rotate> rotates = new ArrayList<>();
  private final Rotate rotate;

  public RotateTransit2DTexture(Image image) {
    super(image);

    setFitWidth(image.getWidth());
    setFitHeight(image.getHeight());

    rotate = new Rotate(0, this.getX(), this.getY());

    this.addRotate(rotate);
  }

  public List<Rotate> getRotates() {
    return rotates;
  }

  public void update(){
    List<Rotate> rotations = this.getTransforms().stream().filter(e -> e instanceof Rotate).map(Rotate.class::cast).toList();
    if(rotations.size() != rotates.size()){
      throw new RuntimeException("rotate number is incorrect"){};
    }

    for(int i=0;i<rotates.size();i++){
      var rotate = rotates.get(i);
      var point = new Point2D(rotate.getPivotX(),rotate.getPivotY());
      //find the current position of transformed coordinates
      for(int j=0;j<i;j++){
        point = rotates.get(j).transform(point);
      }
      var r = rotations.get(i);
      r.setPivotX(point.getX());
      r.setPivotY(point.getY());
      r.setAngle(rotate.getAngle());
    }
  }

  public void addRotate(Rotate rotate){
    this.rotates.add(rotate);
    this.getTransforms().add(rotate.clone());
  }

  public void addRotates(Rotate... rs){
    for(var r : rs) addRotate(r);
  }

  public Rotate getRotation() {
    return rotate;
  }

  public Point2D transform(Point2D point){
    for(var t: this.getTransforms()){
      point = t.transform(point);
    }
    return point;
  }
}
