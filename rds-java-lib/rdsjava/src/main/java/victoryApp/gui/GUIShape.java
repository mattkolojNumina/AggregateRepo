package victoryApp.gui;

public class 
GUIShape 
  {
  public Integer tag;
  public Integer x;
  public Integer y;
  public Float z;
  public Integer width;
  public Integer height;
  public String color;
  public Integer borderSize;
  public String borderColor;

  public 
  GUIShape(Integer tag, 
           Integer x, Integer y, Float z, 
           Integer width, Integer height, 
           String color, Integer borderSize, String borderColor) 
    {
    this.tag = tag;
    this.x = x;
    this.y = y;
    this.z = z;
    this.width = width;
    this.height = height;
    this.color = color;
    this.borderSize = borderSize;
    this.borderColor = borderColor;
    }

  public void
  setColor(String color) 
    {
    this.color=color;
    }
  }
