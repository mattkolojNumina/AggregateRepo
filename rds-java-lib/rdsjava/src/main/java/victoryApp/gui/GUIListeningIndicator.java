package victoryApp.gui;

public class 
GUIListeningIndicator 
  {

  public Integer tag;
  public Integer x;
  public Integer y;
  public Float z;
  public Integer width;
  public Integer height;
  public String colorOn;
  public String colorOff;

  public 
  GUIListeningIndicator(Integer tag, 
            Integer x, Integer y, Float z, 
            Integer width, Integer height, 
            String colorOn, String colorOff) 
    {
    this.tag = tag;
    this.x = x;
    this.y = y;
    this.z = z;
    this.width = width;
    this.height = height;
    this.colorOn = colorOn;
    this.colorOff = colorOff;
    }
  }
