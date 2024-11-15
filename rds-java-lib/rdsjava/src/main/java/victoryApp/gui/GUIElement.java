package victoryApp.gui;

public class 
GUIElement 
  {

  public Integer tag;
  public Integer x;
  public Integer y;
  //set z to -1 for background, 0 for middle ground, and 1 for foreground
  public Float z;

  public
  GUIElement(Integer tag,
             Integer x, Integer y, Float z)
    {
    this.tag = tag ;
    this.x = x ;
    this.y = y ;
    this.z = z ;
    }
  }
