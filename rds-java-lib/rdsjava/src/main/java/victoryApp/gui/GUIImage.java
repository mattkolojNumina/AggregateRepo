package victoryApp.gui;

public class 
GUIImage 
extends GUIElement
  {
  public Integer width;
  public Integer height;
  public String url;

  public 
  GUIImage(Integer tag, 
           Integer x, Integer y, Float z, 
           Integer width, Integer height, String url) 
    {
    super(tag,x,y,z) ;
    this.width = width;
    this.height = height;
    this.url = url;
    }
  }
