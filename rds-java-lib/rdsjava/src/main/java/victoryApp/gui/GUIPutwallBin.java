package victoryApp.gui;

import java.util.*;

//Most of the putwall info and GUIElements will be created and stored
//at the bin level. GUIPutwall exists mostly to help with drawing
//and scrolling through bins correctly.
//
//To make this work, we basically assume GUIPutwallBins occupy
//squares on a grid, with the origin at the top left corner.
//The table you query to create the GUIPutwallBins will have
//to include some values x, y, width, height, indication position
//and dims along the grid.
public class GUIPutwallBin {

  public GUIButton button;
  public Map<String,String> data;
  public int x;
  public int y;
  public int width;
  public int height;
 
  public GUIPutwallBin(GUIButton button, Map<String,String> data) {
    this.button = button;
    this.data = data;
    try {
      this.x = Integer.parseInt(data.get("x"));
      this.y = Integer.parseInt(data.get("y"));
      this.width = Integer.parseInt(data.get("width"));
      this.height = Integer.parseInt(data.get("height"));
    } catch (Exception ex) {
      this.x = 0;
      this.y = 0;
      this.width = 0;
      this.height = 0;
    }
  }

  public Map<String,String> getData() {
    return this.data;
  }

  public int getButtonTag() {
    return this.button.tag;
  }
}
