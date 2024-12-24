package com.whitewoodcity.control;

public class IntTextField extends NumberTextField{
  public IntTextField(int maxValue) {
    super(maxValue);
  }

  public IntTextField(int minValue, int maxValue) {
    super(minValue, maxValue);
  }

  @Override
  protected boolean validate(String s) {
    try {
      Integer.parseInt(s);
      return true;
    } catch (Throwable throwable) {
      return false;
    }
  }

  public int getInt(){
    return Integer.parseInt(getText());
  }
}
