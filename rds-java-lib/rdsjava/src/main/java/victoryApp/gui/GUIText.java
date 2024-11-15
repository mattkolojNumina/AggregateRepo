package victoryApp.gui;

public class 
GUIText 
extends GUIElement
  {
  public Float textSize;
  public String textColor;
  public String text;
  public Boolean bold;
  public  Boolean italic;

  public 
  GUIText(Integer tag, 
          Integer x, Integer y, Float z, 
          Float textSize, String textColor, String text, 
          Boolean bold, Boolean italic) 
    {
    super(tag,x,y,z) ;
    this.textSize = textSize;
    this.textColor = textColor;
    this.text = text;
    this.bold = bold;
    this.italic = italic;
    }

  public GUIText clone() 
    {
      return (new GUIText(this.tag,
                         this.x, this.y, this.z,
                         this.textSize, this.textColor, this.text,
                         this.bold, this.italic));
    }
  
  public String getText() {
    return this.text;
  }
  }
