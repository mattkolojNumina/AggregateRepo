package victoryApp.gui;

public class 
GUIImageBase64 
extends GUIElement
  {
  public Integer width;
  public Integer height;
  public String data;
  public String filename;

  public 
  GUIImageBase64(Integer tag, 
                 Integer x, Integer y, Float z, 
                 Integer width, Integer height, 
                 String data, String filename) 
    {
    super(tag,x,y,z) ;
    this.width = width;
    this.height = height;
    this.data = data;
    this.filename = filename;
    }
  }
