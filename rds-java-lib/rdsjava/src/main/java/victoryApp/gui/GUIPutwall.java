package victoryApp.gui;

import java.util.*;

import static victoryApp.gui.GUIConstants.*;


//Most of the putwall info and GUIElements will be created and stored
//at the bin level. GUIPutwall exists mostly to help with drawing
//and scrolling through bins correctly.
//
//To make this work, we basically assume GUIPutwallBins occupy
//squares on a grid, with the origin at the top left corner.
//The table you query to create the GUIPutwallBins will have
//to include some values x, y, width, height, indication position
//and dims along the grid.
public class GUIPutwall {

  public List<GUIPutwallBin> bins;
  public int x;
  public int y;

  public int width;
  public int height;

  public boolean scrollNeeded;
  public int scrollWindow;

  public int tag;

  public int binWidth;
  public int binHeight;

  public static final int DEFAULT_PUTWALL_ORIGIN_X = 0;
  public static final int DEFAULT_PUTWALL_ORIGIN_Y = 250;

  public GUIPutwall(List<Map<String,String>> data) {
      this.tag = PUTWALL_START_TAG;
      this.width = 0;
      this.height = 0;
      this.binWidth=0;
      this.binHeight=0;
      this.scrollNeeded = false;
      this.scrollWindow = 0;     
      addBins(data); 
      findDimensions();
  }

  public void findDimensions() {
    this.x = DEFAULT_PUTWALL_ORIGIN_X;
    this.y = DEFAULT_PUTWALL_ORIGIN_Y;
    if((bins==null) || (bins.isEmpty())) {
      this.width = 0;
      this.height = 0;
      scrollNeeded = false;
      scrollWindow = 0;
      return;
    }
    this.width = getColumnCount() * binHeight;
    this.height = getRowCount() * binHeight; 
    if ((this.x + this.width) > SCREEN_WIDTH) {
      scrollNeeded = true;
      scrollWindow = SCREEN_WIDTH/this.binWidth;
    }
  }

  //whatever 'tag' is, that should equal the highest number tag of any of the elements that
  //make up this table. Calling it "maxTag" I think is helpful to clarify that,
  //as long as you use a tag number larger than this value for other screen elements,
  //you don't have to worry about any sort of overlap
  public int getMaxTag() {
    int maxTag = PUTWALL_START_TAG;
    for(GUIPutwallBin bin : this.bins) {
      maxTag = (bin.button.tag > maxTag ? bin.button.tag : maxTag);
    }
    return maxTag;
  }
 
  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }

  public int getRowCount() {
    int rowCount = 0;
    for (GUIPutwallBin bin : this.bins) {
      rowCount = ((bin.y + bin.height) > rowCount ? (bin.y + bin.height) : rowCount);
    }
    return rowCount;
  }

  public int getColumnCount() {
    int columnCount = 0;
    for (GUIPutwallBin bin : this.bins) {
      columnCount = ((bin.x + bin.width) > columnCount ? (bin.x + bin.width) : columnCount);
    }
    return columnCount;
  }

  public List<GUIPutwallBin> getBins() {
    return this.bins;
  }

  public GUIPutwallBin getBin(String binID) { 
    if((binID==null) || (binID.isEmpty())) return null;

    for (GUIPutwallBin bin : this.bins) {
      if(binID.equals(bin.button.getButtonText())) {
        return bin;
      }
    }

    return null;
  }
  public GUIPutwallBin getBin(int tag) {
    if(tag < PUTWALL_START_TAG || tag > getMaxTag()) return null;
    for(GUIPutwallBin bin : this.bins) {
      if(tag == bin.button.tag) return bin;
    }
    return null;
  }

  public void highlightBin (String binID) {

    if((binID==null) || (binID.isEmpty())) return;

    for (GUIPutwallBin bin : this.bins) {
      if(binID.equals(bin.button.getButtonText())) {
        bin.button.borderColor = YELLOW;
        bin.button.borderSize = 5;
      }
    }
  }

  public void setBinColor (String binID, String color) {

    if((binID==null) || (binID.isEmpty())) return;

    for (GUIPutwallBin bin : this.bins) {
      if(binID.equals(bin.button.getButtonText())) {
        bin.button.backgroundColor = color;
      }
    }

  }


  public void addBins(List<Map<String,String>> data) {
    GUIButton binButton = null;
    if(this.bins == null) this.bins = new ArrayList<GUIPutwallBin>();
    try {
      int putwallHeight = 0;
      int putwallWidth = 0;
      for (Map<String,String> m : data) {
        int y_bin = Integer.parseInt(m.get("y")) + Integer.parseInt(m.get("height"));
        int x_bin = Integer.parseInt(m.get("x")) + Integer.parseInt(m.get("width"));
        if (y_bin > putwallHeight) putwallHeight = y_bin;
        if (x_bin > putwallWidth) putwallWidth = x_bin;
      }
      //scaler = how many stacked bins can we fit on the screen normally versus how many we're trying to show
      float scaler = (float) ((FOOTER_Y - DEFAULT_PUTWALL_ORIGIN_Y)/(float) BIN_UNIT_HEIGHT) /(float) putwallHeight;
      this.binHeight = (putwallHeight >= 3 ? (int) (scaler * (float) BIN_UNIT_HEIGHT) : BIN_UNIT_HEIGHT);
      this.binWidth = (putwallHeight >= 3 ? (int) (1.1 * scaler * (float) BIN_UNIT_WIDTH) : BIN_UNIT_WIDTH);  //WGW 06-08-22 added the 1.1
      //center-align the putwall
      if ((putwallWidth * this.binWidth) > SCREEN_WIDTH) this.x = 0;
      else this.x = (SCREEN_WIDTH - (putwallWidth * binWidth))/2;
      //we're always going to use the default putwall origin y
      this.y = DEFAULT_PUTWALL_ORIGIN_Y;
      for (Map<String,String> m : data) {
        binButton = new GUIButton(tag++,
            this.x + (binWidth * Integer.parseInt(m.get("x"))), 
            this.y + (binHeight * Integer.parseInt(m.get("y"))),
            0f,
            binWidth * Integer.parseInt(m.get("width")), 
            binHeight * Integer.parseInt(m.get("height")),
            ((m.get("color") != null) && (!m.get("color").isEmpty()) ? m.get("color") : LIGHT_GRAY),
            BLACK,
            DARK_GRAY, 
            3,
            (m.get("binID")==null ? "" : m.get("binID")),
            0.75f * (this.binWidth == BIN_UNIT_WIDTH ? BIN_BUTTON_TEXT_SIZE : (BIN_BUTTON_TEXT_SIZE * scaler))); //WGW 06-09-22 added the .75f
        bins.add(new GUIPutwallBin(binButton, m));
      }
    } catch (Exception ex) {
      this.bins = null;
    }
  }
}
