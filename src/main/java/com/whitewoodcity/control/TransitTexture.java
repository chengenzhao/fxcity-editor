package com.whitewoodcity.control;

import com.whitewoodcity.fxgl.texture.Texture;
import javafx.scene.image.Image;
import javafx.scene.transform.Rotate;

public class TransitTexture extends Texture {

  private final Rotate rotation = new Rotate();

  public TransitTexture(Image image) {
    super(image);

    this.getTransforms().add(rotation);

    this.setFitWidth(image.getWidth());
    this.setFitHeight(image.getHeight());
  }

  public Rotate getRotation() {
    return rotation;
  }
}
