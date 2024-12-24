package com.whitewoodcity.control;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class NumberTextField extends TextField {

  public NumberTextField(int maxValue) {
    this(0, maxValue);
  }

  public NumberTextField(int minValue, int maxValue) {
    this.setTextFormatter(new TextFormatter<>(c -> {
      var newText = c.getControlNewText();
      if (newText.trim().isEmpty()) {
        setText("0");
        return null;
      } else if (!validate(newText)) {
        setText(c.getControlText());
        return null;
      } else if (Double.parseDouble(newText) > maxValue) {
        setText(maxValue + "");
        return null;
      } else if (Double.parseDouble(newText) < minValue) {
        setText(minValue + "");
        return null;
      } else {
        return c;
      }
    }
    ));
  }

  protected boolean validate(String s) {
    try {
      Double.parseDouble(s);
      return true;
    } catch (Throwable throwable) {
      return false;
    }
  }

  public double getDouble(){
    return Double.parseDouble(getText());
  }
}