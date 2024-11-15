package victoryApp.gui;

public class 
GUIResponse 
  {
  public int    tag    = 0 ;
  public String source = null ;
  public String text   = null ;

  public 
  GUIResponse(int tag, String source, String text) 
    { 
    this.tag    = tag ;
    this.source = source ;
    this.text   = text ;
    }

  public String getSource() 
    {
    return this.source;
    }

  public int getTag()
    {
    return this.tag;
    }

  public String getText()
    {
    return this.text;
    }
  }

