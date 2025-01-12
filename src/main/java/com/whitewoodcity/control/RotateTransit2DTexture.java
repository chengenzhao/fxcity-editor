package com.whitewoodcity.control;

import com.whitewoodcity.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

public class RotateTransit2DTexture extends Texture {

  private final List<Rotate> rotates = new ArrayList<>();
  private final Rotate rotate;

  private RotateTransit2DTexture parent;
  private List<RotateTransit2DTexture> children = new ArrayList<>();

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
    List<Rotate> rotations = this.getTransforms().stream().map(Rotate.class::cast).toList();
    assert rotations.size() == rotates.size();
//    if(rotations.size() != rotates.size()){
////      this.getTransforms().clear();
////      this.rotates.forEach(e -> this.getTransforms().add(e.clone()));
////      update();
////      return;
//      throw new RuntimeException("error");
//    }

//    System.out.println(this.getTransforms().size());
    for(var child:children) {
      child.update();
    }

    for(int i=0;i<rotates.size();i++){
      var rotate = rotates.get(i);
      var point = new Point2D(rotate.getPivotX(),rotate.getPivotY());
      //find the current position of transformed coordinates
      for(int j=0;j<i;j++){
//        System.out.print(point+" -> ");
        try {
          point = rotates.get(j).inverseTransform(point);//critical action, current position of transformed coordinates is inverse transformed position
        } catch (NonInvertibleTransformException e) {
          throw new RuntimeException(e);
        }
//        System.out.println(point);
      }
      var r = (Rotate)this.getTransforms().get(i);
      r.setPivotX(point.getX());
      r.setPivotY(point.getY());
      r.setAngle(rotate.getAngle());
      this.getTransforms().set(i, r);
    }

  }

  public void addRotate(Rotate rotate){
    for(var child:children)
      child.addRotate(rotate);
    this.rotates.add(rotate);
    updateTransforms();
  }

  public void removeRotate(Rotate rotate){
    for(var child:children)
      child.removeRotate(rotate);
    this.rotates.remove(rotate);
    updateTransforms();
  }

  private void updateTransforms(){
    this.getTransforms().clear();
    for(var r:rotates)
      this.getTransforms().add(r.clone());
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

  public Point2D inverseTransform(Point2D point){
    for(int i=getTransforms().size()-1;i>=0;i--){
      var t = getTransforms().get(i);
      try {
        point = t.inverseTransform(point);
      } catch (NonInvertibleTransformException e) {
        throw new RuntimeException(e);
      }
    }
    return point;
  }

  public void setParent(RotateTransit2DTexture parent) {
    if(this.parent != null) {
      this.parent.children().remove(this);
      removeRotate(this.parent.getRotation());
    }
    this.parent = parent;
    if(parent!=null) {
      this.parent.children().add(this);
      addRotate(parent.getRotation());
    }
  }

  public RotateTransit2DTexture parent(){
    return this.parent;
  }

  public List<RotateTransit2DTexture> children() {
    return children;
  }
}
