package com.whitewoodcity.control;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class NumberTextField extends TextField
{
  public NumberTextField(int maxValue) {
    this.setTextFormatter(new TextFormatter<>(c -> {
      var text = c.getControlNewText().trim();
      if(text.isEmpty()){
        c.setText("0");
        return c;
      }
      if(!text.matches("\\d*") || Long.parseLong(text) > maxValue)
        return null;
      else {
        return c;
      }
    }
    ));
  }
}