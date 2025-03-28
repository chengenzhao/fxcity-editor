package com.whitewoodcity.control;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.whitewoodcity.fxgl.texture.Texture;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class RotateTransit2DTexture extends Texture {

  private final ObservableList<Rotate> rotates = FXCollections.observableArrayList();
  private final Rotate rotate;

  private RotateTransit2DTexture parent;
  private final ObservableList<RotateTransit2DTexture> children = FXCollections.observableArrayList();

  private Transition transition;

  public RotateTransit2DTexture(Image image) {
    this(image, new Rotate(0));
  }

  public RotateTransit2DTexture(Image image, Rotate rotate) {
    super(image);

    setFitWidth(image.getWidth());
    setFitHeight(image.getHeight());

    this.rotate = rotate;

    this.addRotate(rotate);
  }

  public List<Rotate> getRotates() {
    return rotates;
  }

  public void update() {
    List<Rotate> rotations = this.getTransforms().stream().map(Rotate.class::cast).toList();
    assert rotations.size() == rotates.size();

    for (var child : children) {
      child.update();
    }

    for (int i = 0; i < rotates.size(); i++) {
      var rotate = rotates.get(i);
      var point = new Point2D(rotate.getPivotX(), rotate.getPivotY());
      //find the current position of transformed coordinates
      for (int j = i - 1; j >= 0; j--) {
        try {
          point = rotates.get(j).inverseTransform(point);//critical action, current position of transformed coordinates is inverse transformed position
        } catch (NonInvertibleTransformException e) {
          throw new RuntimeException(e);
        }
      }
      var r = (Rotate) this.getTransforms().get(i);
      r.setPivotX(point.getX());
      r.setPivotY(point.getY());
      r.setAngle(rotate.getAngle());
      this.getTransforms().set(i, r);
    }

  }

  public void addRotate(Rotate rotate) {
    for (var child : children)
      child.addRotate(rotate);
    this.rotates.add(rotate);
    updateTransforms();
  }

  public void removeRotate(Rotate rotate) {
    for (var child : children)
      child.removeRotate(rotate);
    this.rotates.remove(rotate);
    updateTransforms();
  }

  private void updateTransforms() {
    this.getTransforms().clear();
    for (var r : rotates)
      this.getTransforms().add(r.clone());
  }

  public void addRotates(Rotate... rs) {
    for (var r : rs) addRotate(r);
  }

  public Rotate getRotation() {
    return rotate;
  }

  public Point2D transform(Point2D point) {
    for (var t : this.getTransforms()) {
      point = t.transform(point);
    }
    return point;
  }

  public Point2D inverseTransform(Point2D point) {
    for (int i = getTransforms().size() - 1; i >= 0; i--) {
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
    //remove parent
    if (this.parent != null) {
      this.parent.children().remove(this);
      removeAncestorsRotations(this, this.parent.getRotation());
    }

    this.parent = parent;
    if (parent != null) {
      this.parent.children().add(this);
      var rs = this.parent.getRotates().toArray(new Rotate[0]);
      addAncestorsRotations(this, rs);
    }
    updateTransforms();
  }

  private void removeAncestorsRotations(RotateTransit2DTexture texture, Rotate rotate) {
    for (var child : texture.children)
      removeAncestorsRotations(child, rotate);
    var i = texture.rotates.indexOf(rotate);
    texture.rotates.subList(i, texture.rotates.size()).clear();
    texture.updateTransforms();
  }

  private void removeAncestorsRotations(RotateTransit2DTexture texture) {
    removeRotate(texture.getRotation());
    if (texture.parent != null)
      removeAncestorsRotations(texture.parent);
  }

  private void addAncestorsRotations(RotateTransit2DTexture texture, Rotate... rotates) {
    for (var child : texture.children)
      addAncestorsRotations(child, rotates);
    texture.rotates.addAll(rotates);
    texture.updateTransforms();
  }

  public RotateTransit2DTexture parent() {
    return this.parent;
  }

  public ObservableList<RotateTransit2DTexture> children() {
    return children;
  }

  @Override
  public RotateTransit2DTexture clone() {
    var texture = new RotateTransit2DTexture(getImage(), rotate.clone());
    texture.setTranslateX(this.getTranslateX());
    texture.setTranslateY(this.getTranslateY());
    texture.setTranslateZ(this.getTranslateZ());
    texture.setX(this.getX());
    texture.setY(this.getY());
    return texture;
  }

  public void constructTransition(ArrayNode jsonArray) {
    var list = new ArrayList<TransitionData>();
    for (int i = 0; i < jsonArray.size() - 1; i++) {
      list.add(new TransitionData((ObjectNode) jsonArray.get(i), (ObjectNode) jsonArray.get(i + 1)));
    }
    var tran = new SequentialTransition(this);
    for (var data : list) {
      tran.getChildren().add(new CusteomTransition(this,data.start(), data.end()));
    }
    this.transition = tran;
  }

  public void startTransition() {
    transition.setCycleCount(1);
    transition.play();
  }

  public void loopTransition() {
    transition.setCycleCount(Timeline.INDEFINITE);
    transition.play();
  }

  public void stopTransition() {
    transition.stop();
  }
}

record TransitionData(ObjectNode start, ObjectNode end) { }

class CusteomTransition extends Transition{

  private final RotateTransit2DTexture cachedNode;
  private final ObjectNode start;
  private final ObjectNode end;

  public CusteomTransition(RotateTransit2DTexture cachedNode, ObjectNode start, ObjectNode end) {
    this.cachedNode = cachedNode;
    this.start = start;
    this.end = end;
    setCycleDuration(Duration.millis(end.get("time").asDouble() - start.get("time").asDouble()));
  }

  @Override
  protected void interpolate(double frac) {
    cachedNode.setX((end.get("x").asDouble() - start.get("x").asDouble())*frac + start.get("x").asDouble());
    cachedNode.setY((end.get("y").asDouble() - start.get("y").asDouble())*frac + start.get("y").asDouble());
  }
}