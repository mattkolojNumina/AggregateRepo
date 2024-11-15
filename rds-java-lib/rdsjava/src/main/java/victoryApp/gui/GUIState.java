package victoryApp.gui;

import java.util.*;

import static victoryApp.gui.GUIConstants.*;
import static rds.RDSUtil.*;

public class 
GUIState 
  {

  public ArrayList<GUIRectangle> rectangles;
  public ArrayList<GUICircle> circles;
  public ArrayList<GUIButton> buttons;
  public ArrayList<GUIEntry> entries;
  public ArrayList<GUIEntryPW> passwords;
  public ArrayList<GUIText> texts;
  public ArrayList<GUISpinner> spinners;
  public ArrayList<GUIImageBase64> guiImageBase64List;
  public ArrayList<GUIImage> guiImageList;
  public Grammar grammar;
  public String[] phrases ;
  public Boolean scanEnabled ;
  public Boolean enableSettings ;
  public Boolean voiceEnabled ;
  public GUIButton scanButton;
  public GUINetworkIndicator guiNetworkIndicator;
  public GUIListeningIndicator guiListeningIndicator;
  public String backGroundColor;
  public boolean isPortraitMode;
  public Integer pcHeight;
  public Integer pcWidth;
  //public boolean isUpdate; //WGW 05-15-2022 'isUpdate' is no longer recognized by Android app
  //'transient' basically means this will be excluded from the json tree.
  //this is especially important for the footer, as the state tree would have a footer, 
  //which would have this state,  which would have the footer, which would have this state, 
  //which would have the footer... ad infinitum and cause a stack overflow.
  protected transient Footer footer;
  //making GUITable and associate values transient because the android doesn't really have a notion 
  //of a GUITable. The concept though may still be useful for the server-side program.
  protected transient GUITable table;
  protected transient int scrollOffset;
  //putwall stuff. again, making it transient for the same reason as GUITable
  protected transient GUIPutwall putwall;
  protected transient int putwallScrollOffset;
  //info box
  protected transient GUIInfoBox infoBox;

  public 
  GUIState() 
    {
    this.buttons = null ;
    this.entries = null ;
    this.passwords = null ;
    this.texts = null ;
    this.rectangles = null ;
    this.circles = null ;
    this.spinners = null ;
    this.guiImageBase64List = null ;
    this.guiImageList = null ;
    this.grammar = null ;
    this.phrases = null ;
    this.scanEnabled = null ;
    this.enableSettings = null ;
    this.scanButton = null ;
    this.guiNetworkIndicator = new GUINetworkIndicator(
      SSI_TAG,
      1780, 25, 0f,
      90, 90,
      RED, YELLOW, GREEN,
      0, BLACK, true
    );
    this.guiListeningIndicator = new GUIListeningIndicator(
      LISTENING_TAG,
      1690, 25, 0f,
      90, 90,
      RED, GREEN
    );
    this.footer = new Footer(this);
    this.table = null;
    this.backGroundColor = RED ;
    this.isPortraitMode = false ;
    this.pcHeight = SCREEN_HEIGHT ;
    this.pcWidth  = SCREEN_WIDTH ; 
    this.scrollOffset = 0;
    this.putwallScrollOffset = 0;
    }

  public void
  setBackGroundColor(String color) 
    {
    this.backGroundColor = color;
    }

  public GUINetworkIndicator
  getGUINetworkIndicator() 
    {
    return this.guiNetworkIndicator;
    }

  public Footer
  getFooter()
    {
    return this.footer;
    }

  public GUIInfoBox getGUIInfoBox() 
    {
    return this.infoBox;
    }

  public GUIInfoBox getInfoBox() 
    {
    return this.getGUIInfoBox();
    }

  public void
  setGUIInfoBox(GUIInfoBox infoBox) {
    //remove old info box if exists
    if(this.infoBox != null) {
      if(this.rectangles!=null) {
        for(GUIRectangle r: this.rectangles) {
          if (r.tag == LEFT_BOX_TAG) {this.rectangles.remove(r); break;}
        }
        for(GUIRectangle r: this.rectangles) {
          if (r.tag == RIGHT_BOX_TAG) {this.rectangles.remove(r); break;}
        }
      }
      for(int i = INFO_BOX_START_TAG; i < this.infoBox.tag; i++) {
        if(this.texts!=null) {
          for (GUIText t : this.texts) {
            if (t.tag == i) {this.texts.remove(t); break;}
          }
        }
      } 
    }

    this.infoBox = infoBox;
    
    if(this.rectangles == null) 
       this.rectangles = new ArrayList<GUIRectangle>();
    rectangles.add(infoBox.leftBox);
    rectangles.add(infoBox.rightBox);
    if(this.texts == null)
       this.texts = new ArrayList<GUIText>();
    for(GUIText t : infoBox.textBoxes) {
       texts.add(t);
    }
  }

  public void
  setInfoBox(GUIInfoBox infoBox) {
    this.setGUIInfoBox(infoBox);
  }

  public GUIPutwall getGUIPutwall() 
    {
    return this.putwall;
    }

  public void
  setGUIPutwall(GUIPutwall putwall) {
      //remove old putwall if it exists
      if (this.putwall != null) {
        for(int i = PUTWALL_START_TAG; i < this.putwall.tag; i++) {
          if(this.buttons!=null) {
            for(GUIButton b : this.buttons) {
              if (b.tag == i) {this.buttons.remove(b); break;}
            }
          }
        }
      }
      this.putwall = putwall;

      if (this.buttons==null)
          this.buttons = new ArrayList<GUIButton>();

      //if every part of the table can fit on the screen, great!
      //add the rows and entries, and we're done
      if (!putwall.scrollNeeded) {
        for(GUIPutwallBin bin : putwall.bins) {
          this.buttons.add(bin.button);
        }
      }
      else {
        //add left and right buttons for scrolling
        //plus a non-interactive scroll bar to show which section of putwall is being viewed
        this.buttons.add(new GUIButton(PUTWALL_SCROLL_LEFT_BUTTON,
          0, putwall.y-125, 0f,
          125,125,
          WHITE,BLACK,
          BLACK, 0,
          "<", 50f)
        );

        //the -50's are to avoid overlapping the settings toolbar
        this.buttons.add(new GUIButton(PUTWALL_SCROLL_RIGHT_BUTTON,
          (SCREEN_WIDTH - 125 - 50), putwall.y - 125, 0f,
          125,125,
          WHITE,BLACK,
          BLACK, 0,
          ">", 50f)
        );

        if (this.rectangles == null)
            this.rectangles = new ArrayList<GUIRectangle>();

        this.rectangles.add(new GUIRectangle(PUTWALL_SCROLL_BAR_BACKGROUND,      
          putwall.x + 125, putwall.y - 125, -1f,
          SCREEN_WIDTH - 250 - 50, 125,
          DARK_GRAY, 0, BLACK)
        ); 

        int scrollBarWidth = (int) ((float) (SCREEN_WIDTH - 250 - 50) * (float) putwall.scrollWindow/(float) putwall.getColumnCount());
        this.rectangles.add(new GUIRectangle(PUTWALL_SCROLL_BAR,      
          125, putwall.y - 125, 0f,
          scrollBarWidth, 
          125, LIGHT_GRAY, 1, BLACK)
        ); 
        redrawPutwall();

      }       
  }


  public GUITable
  getGUITable() 
    {
    return this.table;
    }

  public void
  setGUITable(GUITable table) 
    {

      if (table.rows==null) return;
 
      //remove old table if it exists
      if (this.table != null) {
        for(int i = TABLE_START_TAG; i < this.table.tag; i++) {
          if(this.rectangles!=null) {
            for(GUIRectangle r : this.rectangles) {
              if (r.tag == i) {this.rectangles.remove(r); break;}
            }
          }
          if(this.texts!=null) {
            for(GUIText t : this.texts) {
              if (t.tag == i) {this.texts.remove(t); break;}
            }
          }
        }
      }
      this.table = table;

      //init state lists if necessary 
      if (this.rectangles==null)
        this.rectangles = new ArrayList<GUIRectangle>();
      
      if (this.texts==null)
        this.texts = new ArrayList<GUIText>();

      //if every part of the table can fit on the screen, great!
      //add the rows and entries, and we're done
      if (!table.scrollNeeded) {
        for(GUIRow r : table.rows) {
          this.rectangles.add(r.row);
          for(Map.Entry<String,GUIText> entry : r.entries.entrySet()) {
            this.texts.add(entry.getValue());
          }
        }
      }
      else {
        //add up and down buttons for scrolling
        if (this.buttons==null)
          this.buttons = new ArrayList<GUIButton>();
        this.buttons.add(new GUIButton(SCROLL_UP_BUTTON,
          table.x+table.getWidth(), table.y, 0f,
          125,125,
          WHITE,BLACK,
          BLACK, 0,
          "^", 50f)
        );

        this.buttons.add(new GUIButton(SCROLL_DOWN_BUTTON,
          table.x+table.getWidth(), table.y + COLUMN_TITLE_BOX_HEIGHT + (table.scrollWindow*ROW_HEIGHT) - 125, 0f,
          125,125,
          WHITE,BLACK,
          BLACK, 0,
          "v", 50f)
        );
        redrawScrollTable();        
      }
    }

  public void redrawPutwall() {

    //whatever was previously being shown from this putwall... 
    //remove it so we can re-assign it to a new location when we call app.updateScreen
    if (this.putwall != null) {
      for(int i = PUTWALL_START_TAG; i < this.putwall.tag; i++) {
        if(this.buttons!=null) {
          for(GUIButton b : this.buttons) {
            if (b.tag == i) {this.buttons.remove(b); break;}
          }
        }
      }
    }

    //every bin not in the scroll window will be moved off-screen.
    //We have to explicitly move it to a spot off screen, or else app.updateScreen will understand
    //the omission of a bin sharing this tag as an intent to leave the bin untouched.
    //Not an ideal solution, but it does the job.
    alert("(bin.x + bin.width > putwallScrollOffset) && (bin.x < (putwallScrollOffset + putwall.scrollWindow))");
    for(int i = 0; i < putwall.bins.size(); i++) {
      GUIPutwallBin bin = putwall.bins.get(i);
      GUIButton nextButton = bin.button.clone();
      if (((bin.x + bin.width) > putwallScrollOffset) && (bin.x < (putwallScrollOffset + putwall.scrollWindow))) {
        trace("Bin %s:   %d > %d && %d < %d", bin.button.getButtonText(), (bin.x+bin.width), putwallScrollOffset, bin.x, (putwallScrollOffset+putwall.scrollWindow));
        nextButton.x -= (putwallScrollOffset * putwall.binWidth);
        this.buttons.add(nextButton);
      }
      else {
        alert("Bin %s:   %d <= %d and/or %d >= %d", bin.button.getButtonText(), (bin.x+bin.width), putwallScrollOffset, bin.x, (putwallScrollOffset+putwall.scrollWindow));
        nextButton.x = 9999;
        this.buttons.add(nextButton);
      }
    }
    
    //remove scrollBar rectangle, then redraw it
    GUIRectangle newScrollBar = null;
    GUIRectangle oldScrollBar = null;
    for(GUIRectangle r : this.rectangles) {
      if(r.tag == PUTWALL_SCROLL_BAR) {
        oldScrollBar = r;
        newScrollBar = r.clone();
        break;
      }
    }

    this.rectangles.remove(oldScrollBar);
    //scrollbar exists in an x-pos window between 125px and SCREEN_WIDTH-125 px, giving it a window of SCREEN_WIDTH-250 px it can cover.
    // but the left edge, i.e. x-pos, can only move between 125px and (SCREEN_WIDTH - 125 -scroll bar width) px,
    //meaning it can only move in some range that's a fraction of (SCREEN_WIDTH-125-125-scroll bar width) px
    newScrollBar.x = 125 + (int) ((float) (SCREEN_WIDTH - 250 - 50 - newScrollBar.width) * (float) putwallScrollOffset/(float) (putwall.getColumnCount()-putwall.scrollWindow));
    this.rectangles.add(newScrollBar);
  }

  public void scrollRightPutwall() {

    if ((putwall.getColumnCount() - putwall.scrollWindow) <= putwallScrollOffset) {
      putwallScrollOffset = putwall.getColumnCount() - putwall.scrollWindow;
      inform("scrollOffset: %d, columnCount: %d, scrollWindow: %d", putwallScrollOffset, putwall.getColumnCount(), putwall.scrollWindow);
      return;
    }
    putwallScrollOffset++;
    redrawPutwall();
    inform("scrollOffset: %d, columnCount: %d, scrollWindow: %d", putwallScrollOffset, putwall.getColumnCount(), putwall.scrollWindow);
  }

  public void scrollLeftPutwall() {
    if(putwallScrollOffset <= 0) {
      putwallScrollOffset = 0; 
      inform("scrollOffset: %d, columnCount: %d, scrollWindow: %d", putwallScrollOffset, putwall.getColumnCount(), putwall.scrollWindow);
      return;
    }
    inform("scrollOffset: %d", putwall.getColumnCount());
    putwallScrollOffset--;
    redrawPutwall();
    inform("scrollOffset: %d, columnCount: %d, scrollWindow: %d", putwallScrollOffset, putwall.getColumnCount(), putwall.scrollWindow);
  }

  public void redrawScrollTable() {

    //whatever was previously being shown from this table... 
    //remove it so we can re-assign it to a new location when we call app.updateScreen
    if (this.table != null) {
      for(int i = TABLE_START_TAG; i < this.table.tag; i++) {
        if(this.rectangles!=null) {
          for(GUIRectangle r : this.rectangles) {
            if (r.tag == i) {this.rectangles.remove(r); break;}
          }
        }
        if(this.texts!=null) {
          for(GUIText t : this.texts) {
            if (t.tag == i) {this.texts.remove(t); break;}
          }
        }
      }
    }

    //add title row
    this.rectangles.add(table.rows.get(0).row); 
    //counting the column width measurements also tells us how many entries there are
    //on each row
    for(Map.Entry<String,GUIText> entry : table.rows.get(0).entries.entrySet()) {
        this.texts.add(entry.getValue());
    }

    //every rectangle not in the scroll window (and not the column header rectangle) will be moved off-screen.
    //We have to explicitly move it to a spot off screen, or else app.updateScreen will understand
    //the omission of a rectangle sharing this tag as an intent to leave the rectangle untouched.
    //Not an ideal solution, but it does the job
    for(int i = 1; i < table.rows.size(); i++) {
      if ((i < (scrollOffset + 1)) || (i >= (table.scrollWindow + scrollOffset + 1))) {
        GUIRectangle nextRectangle = table.rows.get(i).row.clone();
        nextRectangle.x = 9999;
        this.rectangles.add(nextRectangle);
        for(Map.Entry<String,GUIText> entry: table.rows.get(i).entries.entrySet()) {
          GUIText nextText = entry.getValue().clone();
          nextText.x = 9999;
          this.texts.add(nextText);
        }
      }
      else {
        GUIRectangle nextRectangle = table.rows.get(i).row.clone();
        nextRectangle.y -= (scrollOffset * ROW_HEIGHT);
        this.rectangles.add(nextRectangle);
        for(Map.Entry<String,GUIText> entry: table.rows.get(i).entries.entrySet()) {
          GUIText nextText = entry.getValue().clone();
          nextText.y -= (scrollOffset * ROW_HEIGHT);
          this.texts.add(nextText);
        }     
      }
    }
  }

  public void scrollDownTable() {
    if ((table.rows.size() - 1 - table.scrollWindow) <= scrollOffset) {
      scrollOffset = table.rows.size() - 1 - table.scrollWindow;
      return;
    }
    scrollOffset++;
    redrawScrollTable();
  }

  public void scrollUpTable() {
    if(scrollOffset <= 0) {scrollOffset = 0; return;}
    scrollOffset--;
    redrawScrollTable();
  }

  public List<GUIButton> getButtons() 
    {
    return this.buttons;
    }
  }