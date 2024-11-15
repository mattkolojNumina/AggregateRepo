package victoryApp.gui;

import java.util.*;

import static victoryApp.gui.GUIConstants.*;

public class GUIInfoBox {

  public GUIRectangle  leftBox;
  public GUIRectangle rightBox;
  public Map<String,String> info;

  public float infoBoxTextSize;
  public int tag;
  public ArrayList<GUIText> textBoxes;

  public static final int ROW_PADDING = 10;

  public GUIInfoBox(Map<String,String> info) {
      this.info = info;
      tag = INFO_BOX_START_TAG;
      leftBox = 
      new GUIRectangle(LEFT_BOX_TAG, (SCREEN_WIDTH/2-INFO_BOX_WIDTH) + 50, INFO_BOX_Y - 150, -1f,
                       INFO_BOX_WIDTH - 50,INFO_BOX_HEIGHT,LIGHT_GRAY,
                       1, LIGHT_GRAY);
      rightBox = 
      new GUIRectangle(RIGHT_BOX_TAG, (SCREEN_WIDTH/2) - 200, INFO_BOX_Y - 150, -1f,
                       INFO_BOX_WIDTH - 125,INFO_BOX_HEIGHT,LIGHT_GRAY,
                       1, LIGHT_GRAY);

      int entriesPerBox = info.size()/2;
      if (info.size()%2 == 1) entriesPerBox++;

      infoBoxTextSize = 2.5f * (float) INFO_BOX_HEIGHT/((float) entriesPerBox);
      if (entriesPerBox==2) infoBoxTextSize -= 40; //WGW text gets tough and difficult to read if text is set to occupy half the info box height

      //Create iterator for each text box
      Iterator<Map.Entry<String, String>> iterator = info.entrySet().iterator();
      textBoxes = new ArrayList<GUIText>();
      GUIText textBox = null;

      //Determine the longest length key string, then add padding for the values to be consistent and match for any length key.
      List<String> strings = new ArrayList<>(info.keySet()); 

      //Find longest string for left half
      String max = strings.subList(0, entriesPerBox).stream().max(Comparator.comparingInt(String::length)).get();
      int valueLength = (int) (getWidth(max) * infoBoxTextSize * .45);

      //Left box
      for(int i=0; i < entriesPerBox && iterator.hasNext(); i++) {
        Map.Entry<String,String> entry = iterator.next(); 
        textBox = new GUIText(tag++, GRID_TXT_X, (i*ROW_PADDING) + leftBox.y + (int) ((float) i * infoBoxTextSize * PIXELS_PER_CHARACTER_SIZE_HEIGHT),
                              0f, infoBoxTextSize, BLACK, camelCasePrettyPrint(entry.getKey())+(!entry.getKey().isEmpty()?" ":""),
                              DEFAULT_IS_BOLD, DEFAULT_IS_ITALIC);
        textBoxes.add(textBox);
        textBox = new GUIText(tag++, GRID_TXT_X+valueLength+20, (i*ROW_PADDING) + leftBox.y + (int) ((float) i * infoBoxTextSize * PIXELS_PER_CHARACTER_SIZE_HEIGHT),
                              0f, infoBoxTextSize, BLACK, entry.getValue(),
                              DEFAULT_IS_BOLD, DEFAULT_IS_ITALIC);
        textBoxes.add(textBox);     
      }

      //Find longest string for right half
      max = strings.subList(entriesPerBox, info.size()).stream().max(Comparator.comparingInt(String::length)).get();
      valueLength = (int) (getWidth(max) * infoBoxTextSize * .45);

      //Right box
      for(int i=entriesPerBox; i < info.size() && iterator.hasNext(); i++) {
        Map.Entry<String,String> entry = iterator.next(); 
        textBox = new GUIText(tag++, GRID_TXT_X2, ((i-entriesPerBox)*ROW_PADDING) + rightBox.y + (int) ((float) (i-entriesPerBox) * infoBoxTextSize * PIXELS_PER_CHARACTER_SIZE_HEIGHT),
                              0f, infoBoxTextSize, BLACK, camelCasePrettyPrint(entry.getKey())+(!entry.getKey().isEmpty()?" ":""),
                              DEFAULT_IS_BOLD, DEFAULT_IS_ITALIC);
        textBoxes.add(textBox);
        textBox = new GUIText(tag++, GRID_TXT_X2+valueLength+20, 
                              ((i-entriesPerBox)*ROW_PADDING) + rightBox.y + (int) ((float) (i-entriesPerBox) * infoBoxTextSize * PIXELS_PER_CHARACTER_SIZE_HEIGHT),
                              0f, infoBoxTextSize, BLACK, entry.getValue(),
                              DEFAULT_IS_BOLD, DEFAULT_IS_ITALIC);
        textBoxes.add(textBox);     
      }
  }

}
