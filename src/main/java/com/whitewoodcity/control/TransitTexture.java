package com.whitewoodcity.control;

import com.whitewoodcity.fxgl.texture.Texture;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;

public class TransitTexture extends Group {

  private final Rotate rotation = new Rotate();
  private final ImageView imageView;

  public TransitTexture(Image image) {

    this.getTransforms().add(rotation);

    imageView = new ImageView(image);
    imageView.setFitWidth(image.getWidth());
    imageView.setFitHeight(image.getHeight());
    this.getChildren().add(imageView);

    this.getTransforms().add(rotation);
  }

  public Rotate getRotation() {
    return rotation;
  }

  public ImageView getImageView(){
    return imageView;
  }
}
