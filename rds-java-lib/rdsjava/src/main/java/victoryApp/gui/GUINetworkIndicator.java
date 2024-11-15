package victoryApp.gui;

public class 
GUINetworkIndicator 
  {

  public Integer tag;
  public Integer x;
  public Integer y;
  public Float z;
  public Integer width;
  public Integer height;
  public String colorBad;
  public String colorMid;
  public String colorGood;
  public Integer borderSize;
  public String borderColor;
  public Boolean isCircle;

  public 
  GUINetworkIndicator(Integer tag, 
            Integer x, Integer y, Float z, 
            Integer width, Integer height, 
            String colorBad, String colorMid, String colorGood,
            Integer borderSize, String borderColor,
            Boolean isCircle) 
    {
    this.tag = tag;
    this.x = x;
    this.y = y;
    this.z = z;
    this.width = width;
    this.height = height;
    this.colorBad = colorBad;
    this.colorMid = colorMid;
    this.colorGood = colorGood;
    this.borderSize = borderSize;
    this.borderColor = borderColor;
    this.isCircle = isCircle;
    }
  }
