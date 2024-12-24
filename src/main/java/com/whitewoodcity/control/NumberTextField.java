package com.whitewoodcity.control;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class NumberTextField extends TextField {
  public NumberTextField() {
  }

  public NumberTextField(int maxValue) {
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
      } else if (Double.parseDouble(newText) < 0) {
        setText("0");
        return null;
      } else {
        return c;
      }
    }
    ));
  }

  private boolean validate(String s) {
    try {
      Double.parseDouble(s);
      return true;
    } catch (Throwable throwable) {
      return false;
    }
  }
}