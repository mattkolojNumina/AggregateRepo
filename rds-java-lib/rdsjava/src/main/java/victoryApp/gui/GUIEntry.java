package victoryApp.gui;

public class 
GUIEntry 
extends GUIElement
  {
  public Integer width;
  public Integer height;
  public Float textSize;
  public String defaultValue;
  public String hint;
  public String textColor;
  public String backgroundColor;
  public String accentColor;
  public Boolean useNumericKeypad;

  public
  GUIEntry(Integer tag,
           Integer x, Integer y, Float z,
           Integer width, Integer height,
           Float textSize,
           String defaultValue, String hint,
           String textColor, String backgroundColor, String accentColor,
           Boolean useNumericKeypad)
    {
    super(tag,x,y,z) ;
    this.width = width ;
    this.height = height ;
    this.textSize = textSize ;
    this.defaultValue = defaultValue ;
    this.hint = hint ;
    this.textColor = textColor ;
    this.backgroundColor = backgroundColor ;
    this.accentColor = accentColor ;
    this.useNumericKeypad = useNumericKeypad ;
    }
  }
