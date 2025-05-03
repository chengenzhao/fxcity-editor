package com.whitewoodcity.model;

import java.io.File;

public record View(File image, TextureType textureType) {

  public enum TextureType{
    //ANIMATED,
    TRANSIT;

    @Override
    public String toString() {
      var s = name().replace("_"," ").toLowerCase();
      return s.substring(0,1).toUpperCase() + s.substring(1) + " Texture";
    }
  }
}
