package victoryApp.gui;

public class 
GUIRectangle 
extends GUIShape
  {
  public 
  GUIRectangle(Integer tag, 
               Integer x, Integer y, Float z, 
               Integer width, Integer height, 
               String color, Integer borderSize, String borderColor) 
    {
    super(tag,x,y,z,
          width,height,
          color,borderSize,borderColor) ;
    }
  
  public GUIRectangle clone() 
    {
      return (new GUIRectangle(this.tag, this.x, this.y, this.z, 
            this.width, this.height, 
            this.color, this.borderSize, this.borderColor));
    }
  }
