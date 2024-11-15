package victoryApp.gui;

import java.util.*;

import static victoryApp.gui.GUIConstants.*;
import static rds.RDSUtil.*;


//NOTE: This table works best when using only a few columns
//where the data is not very long (ballpark 64 chars at most)
public class GUITable {

  public List<GUIRow> rows;
  public Map<String,Integer> columnWidths;
  public List<Map<String,String>> data;
  public int totalWidth;
  public int x;
  public int y;
  public int tag;

  public String colorOne;
  public String colorTwo;

  public boolean scrollNeeded;
  public int scrollWindow;

  public GUITable(int x, int y) {

      this.tag = TABLE_START_TAG;
      this.x = x;
      this.y = y;
      this.totalWidth = 0;
      this.scrollNeeded = false;
      this.scrollWindow = 0;      
  }

  public GUITable(int x, int y, 
                  List<Map<String,String>> data) {
      this(x,y,DARK_GRAY,LIGHT_GRAY, data);
  }

  public GUITable(int x, int y, String colorOne, String colorTwo,
                  List<Map<String,String>> data) {
      this(x,y);
      this.data = data;
      this.colorOne = colorOne;
      this.colorTwo = colorTwo;
      if ((data != null) && (!data.isEmpty())) {
        //figure out how much space each column will need by
        //finding the longest string for each column
        //take all the keys and place them over our rectangle
        columnWidths = getColumnWidths(data);
        drawTable(); 
      }
      else {alert("null data provided");}
  }

  public void setColumnWidth(String columnName, int width) {
    if(columnWidths==null) {

    };
    if(columnWidths.get(columnName)!=null) {
      columnWidths.replace(columnName, width);
      this.totalWidth=0;
      for (Map.Entry<String,Integer> entry : columnWidths.entrySet()) {
        this.totalWidth += columnWidths.get(entry.getKey());
      }
      drawTable();
    }
  }

  //determine how long the longest string in each column is.
  //this way, we can assure that we give each column enough space
  //and reduce risk of text runover.
  //Mileage may vary if you have very long strings.
  public Map<String,Integer> getColumnWidths(List<Map<String,String>> data) {
    Map<String,Integer> result = new LinkedHashMap<String,Integer>();
    for(String s : data.get(0).keySet()) {
      //at minimum, each column needs to be wide enough to hold the field, plus a bit of extra padding
      int width = (int) (((float) s.length()) * PIXELS_PER_CHARACTER_SIZE * DEFAULT_COLUMN_TITLE_TEXT_SIZE +100);
      result.put(s, width);
//      trace("start value: '%s' column is %d pixels", s, width);
    }
    for(Map<String,String> m : data) {
      for (Map.Entry<String,String> entry : m.entrySet()) {
        int width = (int) (((float) entry.getValue().length()) * PIXELS_PER_CHARACTER_SIZE * DEFAULT_TABLE_ENTRY_TEXT_SIZE);
        if (width > result.get(entry.getKey())) {
          result.replace(entry.getKey(), width);
//          trace("'%s' column is now %d pixels wide", entry.getKey(), width);
        }
      }
    }
    this.totalWidth = 0;
    for (Map.Entry<String,Integer> entry : result.entrySet()) {
      result.replace(entry.getKey(), entry.getValue() + 50);
      this.totalWidth += result.get(entry.getKey());
    }
//    trace("total width is %d", totalWidth);
    return result;
  }

  private void drawTable() {
    int x_pos=x;
    int y_pos=y;

    rows = new ArrayList<GUIRow>();

    //assuming all columns will be uniform length
    int left_margin = 25;
    int top_margin = 20;
    String rectangle_color = colorOne;
    if(columnWidths==null) {
      inform("no column widths provided; using defaults");
      columnWidths = getColumnWidths(data);
    }    
    //Rectangle to contain table column names
    GUIRectangle columnNameRectangle = 
        new GUIRectangle(tag++,x_pos,y_pos, -1f,
                         totalWidth,COLUMN_TITLE_BOX_HEIGHT,
                         rectangle_color,1,BLACK);
    Map<String,GUIText> rowEntries = new LinkedHashMap<String,GUIText>();
    for(String s : data.get(0).keySet()) {
       rowEntries.put(s, new GUIText(tag++,
              x_pos+left_margin, y_pos+top_margin, 0f,
              DEFAULT_COLUMN_TITLE_TEXT_SIZE, BLACK, camelCasePrettyPrint(s),
              true, false)
       );
       x_pos += columnWidths.get(s);
    }
    rows.add(new GUIRow(columnNameRectangle, rowEntries, columnWidths));
    //first entry will be placed just under the rectangle with column names
    y_pos += COLUMN_TITLE_BOX_HEIGHT;
    x_pos = x;
    //iterate through each row of data
    for(Map<String,String> m : data) {
      //each row background alternates in color
      rectangle_color = (rectangle_color.equals(colorOne) ? colorTwo : colorOne);
      //we just discovered the row is going to dip into the footer, we'll need a scroll window.
      //we'll store the number of rows we coult fit until now as the scroll window
      if(((y_pos + ROW_HEIGHT) > (SCREEN_HEIGHT - 200)) && (!scrollNeeded)) {
        scrollNeeded = true; 
      }
      else if (!scrollNeeded) {
        scrollWindow++;
      }
      GUIRectangle rowBackground = new GUIRectangle(
                  tag++, x_pos,y_pos,-1f,
                  totalWidth,ROW_HEIGHT,rectangle_color,
                  1,BLACK);
      //now check each entry in each row; add it to the appropriate position
      rowEntries = new LinkedHashMap<String,GUIText>();
      for(Map.Entry<String,String> entry : m.entrySet()) {
        String entryText = entry.getValue();
        //do another check to make sure data won't spill over into another column
        int textWidth = ((int) (((double) (entry.getValue().length() * DEFAULT_TABLE_ENTRY_TEXT_SIZE)) * PIXELS_PER_CHARACTER_SIZE));
        //if it will, we'll fit as much as we can of the original text, then add '...' to the end
        if(textWidth > columnWidths.get(entry.getKey())) {
          int maxChars = ((int) (((double) columnWidths.get(entry.getKey())) / (PIXELS_PER_CHARACTER_SIZE * DEFAULT_TABLE_ENTRY_TEXT_SIZE)));
          //if the column is so small that we can't even fit 3 characters, fit some number of dots instead
          if (maxChars <= 3) {
            entryText = "";
            for(int i = 0; i < maxChars; i++) entryText += ".";
          }
          else {entryText = entry.getValue().substring(0,maxChars-3) + "...";}
        }
        rowEntries.put(entry.getKey(), new GUIText(tag++,
               x_pos+left_margin,y_pos+top_margin, 0f,
               DEFAULT_TABLE_ENTRY_TEXT_SIZE, BLACK, entryText,
               false, false)
        );
        x_pos += columnWidths.get(entry.getKey());
      }
      rows.add(new GUIRow(rowBackground, rowEntries, columnWidths));
      y_pos += ROW_HEIGHT;
      x_pos = x;
    }

  }

  //whatever 'tag' is, that should equal the highest number tag of any of the elements that
  //make up this table. Calling it "maxTag" I think is helpful to clarify that,
  //as long as you use a tag number larger than this value for other screen elements,
  //you don't have to worry about any sort of overlap
  public int getMaxTag() {
    return this.tag;
  }
 
  public int getWidth() {
    return this.totalWidth;
  }

  public int getHeight() {
    return (COLUMN_TITLE_BOX_HEIGHT + (data.size() * ROW_HEIGHT));
  }

  public List<GUIRow> getRows() {
    return this.rows;
  }
}
