package com.whitewoodcity.control;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class NumberTextField extends TextField
{
  public NumberTextField() {
  }

  public NumberTextField(int maxValue) {
    this.setTextFormatter(new TextFormatter<>(c -> {
      var text = c.getControlNewText();
      if(text.trim().isEmpty()){
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