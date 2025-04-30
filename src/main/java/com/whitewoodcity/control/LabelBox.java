package com.whitewoodcity.control;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class LabelBox extends HBox {
  public LabelBox() {
  }

  public LabelBox(double v) {
    super(v);
  }

  public LabelBox(Node... nodes) {
    super(nodes);
  }

  public LabelBox(double v, Node... nodes) {
    super(v, nodes);
  }

  public String getLabel(){
    return ((Label)this.getChildren().getFirst()).getText();
  }
}
