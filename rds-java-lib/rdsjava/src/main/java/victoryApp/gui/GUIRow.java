package victoryApp.gui;

import java.util.*;

//NOTE: This table works best when using only a few columns
//where the data is not very long (ballpark 64 chars at most)
public class GUIRow {

  public GUIRectangle row;
  public Map<String,GUIText> entries; 
  public Map<String,Integer> columnWidths;

  public GUIRow(GUIRectangle row, Map<String,GUIText> entries, Map<String,Integer> columnWidths) {
      this.row = row;
      this.entries = entries;
      this.columnWidths = columnWidths;
  }

  public void setColor(String color) {
    this.row.color = color;
  }

  public void setTextColor(String color) {
    for(Map.Entry<String,GUIText> entry : entries.entrySet()) {
      entry.getValue().textColor = color;
    }
  }

  public String get(String key) {
    for (Map.Entry<String,GUIText> entry : entries.entrySet()) {
      if(entry.getKey().equals(key)) {
        return entry.getValue().text; 
      }
    }
    return "";
  }
}
