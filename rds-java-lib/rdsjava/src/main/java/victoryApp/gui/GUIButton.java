package victoryApp.gui;

public class 
GUIButton 
extends GUIElement 
  {
  public Integer width;
  public Integer height;
  public String backgroundColor;
  public String textColor;
  public String borderColor;
  public Integer borderSize;
  public String text;
  public Float textSize;

  public
  GUIButton(Integer tag,
            Integer x, Integer y, Float z,
            Integer width, Integer height,
            String backgroundColor,
            String textColor,
            String borderColor, Integer borderSize,
            String text, Float textSize)
    {
    super(tag,x,y,z) ;
    this.width = width ;
    this.height = height ;
    this.backgroundColor = backgroundColor ;
    this.textColor = textColor ;
    this.borderColor = borderColor ;
    this.borderSize = borderSize ;
    this.text = text ;
    this.textSize = textSize ;
    }

  public String getButtonText() 
    {
    return this.text;
    }

  public int getTag() 
    {
    return this.tag;
    }

  public GUIButton clone() 
    {
      return (new GUIButton(this.tag, this.x, this.y, this.z, 
            this.width, this.height, this.backgroundColor,
            this.textColor, this.borderColor, this.borderSize,
            this.text, this.textSize) 
      );
    }

  }
