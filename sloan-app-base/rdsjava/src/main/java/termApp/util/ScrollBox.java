package termApp.util;

import java.util.*;

import termApp.util.Constants.*;
import termApp.util.TermActionObject.OnActionListener;

public class ScrollBox {
   private List<Map<String,String>> displayList;
   private List<RowOfText> rowElements;
   private int startIndex;

   private Button up, dn;
   private List<Integer> columnPositions,columnWidths;
   private List<Align> columnAlignments;
   private List<String> colNames;
   private List<String> colKeys;

   private int indexJump;
   private int x0, y0, topMargin, bottomMargin, leftMargin, rightMargin, width, rows, columns,headerFont,font,rowGap;
   private int btnMargin, btnWidth, btnFont;
   private Rectangle bkgd;
   private TermGroup elements;

   private ScrollBox() {
      displayList = new ArrayList<Map<String,String>>();
      rowElements = new ArrayList<RowOfText>();
      elements = new TermGroup(0, 0);
   }

   private void setOrigin(int x0, int y0) {
      this.x0 = x0;
      this.y0 = y0;      
   }

   private void setFont(int headerFont, int bodyFont) {
      this.headerFont = headerFont;
      this.font = bodyFont;
   }

   private void setMargins(int top, int bottom, int left,  int right) {
      this.topMargin = top;
      this.leftMargin = left;
      this.rightMargin = right;   
      this.bottomMargin = bottom + font/4;
   }

   private void setRows(int rows, int rowGap) {
      this.rows = rows;
      this.rowGap = rowGap;         
   }

   private void setWidth(int width) {
      this.width = width;
   }

   private void setButton(int font, int width, int margin) {
      this.btnFont = font;
      this.btnWidth = width;
      this.btnMargin = margin;
   }

   private void setIndexJump(int jump) {
      this.indexJump = jump;
   }

   private void setColumns(int cols, List<Integer> colPos, List<Integer> colWidth, 
         List<Align> colAlign, List<String> colName, List<String> colKeys ) {
      this.columnPositions    = colPos;
      this.columnWidths       = colWidth;
      this.columnAlignments   = colAlign;
      this.colNames           = colName;
      this.colKeys            = colKeys;
      this.columns = cols;
   }

   private void addColHeaders() {
      Iterator<Integer> posIt = columnPositions.iterator();
      Iterator<Align> alignIt = columnAlignments.iterator();
      Iterator<String> headerIt = colNames.iterator();
      int x = x0 + leftMargin;
      int y = y0 + topMargin - rowGap - headerFont;
      int f = headerFont;
      while (posIt.hasNext()) {
         x = x0 + leftMargin + posIt.next();
         Align align = alignIt.hasNext() ? alignIt.next() : Align.CENTER; 
         String header = headerIt.hasNext() ? headerIt.next() : ""; 
         elements.add(new TextField(x,y,f,"black",header,align,false)) ;
      }
   }

   private void init() {
      initRows();
      initButtons();
   }

   private void initRows() {
      int x = x0 + leftMargin;
      int y = y0 + topMargin;
      int f = font;      
      int s = font + rowGap;
      int w = width - leftMargin - rightMargin - btnWidth - btnMargin*2;
      for (int i = 0; i < rows ; i++ ) {
         y = y0 + topMargin + (s)*i;
         rowElements.add(new RowOfText(x,y,f,rowGap,w,
               columnPositions,columnWidths,columnAlignments));
      }
   }

   private void initButtons() {
      int m = btnMargin;
      int fudge = (font-btnFont)/2;
      int x = x0 + width - m;
      int y = y0 + topMargin + fudge;
      int f = btnFont;
      int s = rowGap+font;
      int w = btnWidth > 0 ? btnWidth : -1;
      up = new Button(x,y,f,"-",Align.RIGHT,w,!atMin());
      y += (s) * (rows-1);
      dn = new Button(x,y,f,"+",Align.RIGHT,w,!atMax());

      up.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            downIndex();
            updateButtons();
            updateRowDisplays();
         }
      });

      dn.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            upIndex();
            updateButtons();
            updateRowDisplays();
         }
      });  
   }

   private void initRowBackground() {
      for (RowOfText row : rowElements) {
         row.initBackground();      
      }
   }
   
   private void initBackground() {
      int x = x0;
      int y = y0;
      int s = font+rowGap;
      int w = width ;
      int h = topMargin + s*rows + bottomMargin;
      bkgd = new Rectangle(x, y, w, h, Constants.INVISIBLE, true);
      elements.add(bkgd);
   }

   private void updateRowDisplays() {
      if (startIndex>maxShift())
         startIndex = maxShift();
      Iterator<Map<String, String>> it = displayList.iterator();

      int i = 0;
      while (it.hasNext() && i++ < startIndex)
         it.next();

      for (RowOfText row : rowElements) {
         row.updateDisplay(it,colKeys);      
         row.updateExtra();
         row.updateBackground();
      }
   }

   private int minShift() {
      return 0;
   }

   private int maxShift() {
      return Math.max(0, displayList.size() - rowElements.size());
   }

   private void downIndex() {
      if (startIndex>minShift())
         startIndex -= indexJump;
      startIndex = Math.max(0, startIndex);
   }

   private boolean atMin() {
      return startIndex <= minShift();
   }   

   private boolean atMax() {
      return startIndex >= maxShift();
   }

   private void upIndex() {
      if (startIndex<maxShift())
         startIndex += indexJump;   
   }

   private void updateButtons() {
      if (atMin()) 
         up.hide();
      else         
         up.show();

      if (atMax()) 
         dn.hide();
      else         
         dn.show();   
   }

   private void addButtonColumn(int pos, String displayKey, Button btn, ButtonAction action) {
      for (RowOfText row : rowElements) {
         row.addButton(pos,displayKey,(Button)btn.clone(),action);      
      }
   }

   public void setDisplayList(List<Map<String, String>> displayList) {
      if (displayList == null)
         displayList = new ArrayList<Map<String,String>>();
   
      this.displayList = displayList;
   }

   public void show() {
      update();
      elements.show();
   }

   public void hide() {
      up.hide();
      dn.hide();
      elements.hide();
      for (RowOfText row : rowElements) 
         row.hide();      
   }

   public void update() {
      updateRowDisplays() ;
      updateButtons();
   }

   public void resetScroll() {
      startIndex = 0;
      updateButtons();
      updateRowDisplays();
   }
   
   public void updateDisplayList(List<Map<String, String>> displayList) {
      setDisplayList(displayList);
      update();
   }

   public void updateBackground(String fill, String border, int thickness) {
      if(bkgd == null)
         initBackground();
      bkgd.updateFill(fill);
      bkgd.updateBorder(border,thickness);
      bkgd.show();
   }

   public static class ScrollBoxConstructor{
      private List<Map<String,String>> displayList;

      private List<Integer> columnPositions,columnWidths;
      private List<Align> columnAlignments;
      private List<String> colNames;
      private List<String> colKeys;
      private List<Map<String,Object>> buttons;

      private int indexJump;
      private int x0, y0, topMargin, bottomMargin, leftMargin, rightMargin, width, rows, columns,headerFont,font,rowGap;
      private int btnMargin, btnWidth, btnFont;
      private boolean drawHeaders;

      public ScrollBoxConstructor(int x0, int y0) {
         setOrigin(x0,y0);
         setRows(5,5);
         setFont(80,70);
         setMargins(0,0,0,0);
         setButton(50, 0, 5);
         setWidth(500);
         setIndexJump(1);
         displayList = new ArrayList<Map<String,String>>();

         columns = 0;
         columnPositions   = new ArrayList<Integer>();
         columnWidths      = new ArrayList<Integer>();
         columnAlignments  = new ArrayList<Align>();
         colNames          = new ArrayList<String>();
         colKeys           = new ArrayList<String>();
         buttons           = new ArrayList<Map<String,Object>>();
      }

      public void setOrigin(int x0, int y0) {
         this.x0 = x0;
         this.y0 = y0;      
      }

      public void setFont(int headerFont, int bodyFont) {
         this.headerFont = headerFont;
         this.font = bodyFont;
      }

      public void setMargins(int top, int bottom, int left,  int right) {
         this.topMargin = top;
         this.leftMargin = left;
         this.rightMargin = right;   
         this.bottomMargin = bottom;
      }

      public void setRows(int rows, int rowGap) {
         this.rows = rows;
         this.rowGap = rowGap;         
      }

      public void setWidth(int width) {
         this.width = width;
      }

      public void setButton(int font, int width, int margin) {
         this.btnFont = font;
         this.btnWidth = width;
         this.btnMargin = margin;
      }

      public int leftOfButton() {
         return width-(leftMargin+btnMargin*2+btnWidth+rightMargin);
      }
      
      public void setIndexJump(int jump) {
         this.indexJump = jump;
      }

      public void addColumn(int pos, int width, Align align, String header, String key) {
         columnPositions.add(pos);
         columnWidths.add(width);
         columnAlignments.add(align);
         colNames.add(header);
         colKeys.add(key);
         columns++;
      }
      
      public void clearColumns() {
         columns = 0;
         columnPositions   = new ArrayList<Integer>();
         columnWidths      = new ArrayList<Integer>();
         columnAlignments  = new ArrayList<Align>();
         colNames          = new ArrayList<String>();
         colKeys           = new ArrayList<String>();
      }
      
      public void addButtonColumn(int pos, String displayKey, Button btn, ButtonAction action) {
         Map<String,Object> buttonMap = new  HashMap<String,Object>();
         buttonMap.put("position",pos);
         buttonMap.put("displayKey",displayKey);
         buttonMap.put("button",btn);
         buttonMap.put("buttonAction",action);
         buttons.add(buttonMap);
      }
      
      public void clearButtons() {
         buttons.clear();
      }
      
      public void drawHeaders(boolean draw) {
         this.drawHeaders = draw;
      }
      
      public void setDisplayList(List<Map<String, String>> displayList) {
         if (displayList == null)
            displayList = new ArrayList<Map<String,String>>();
      
         this.displayList = displayList;
      }

      public ScrollBox build() {
         ScrollBox box = new ScrollBox();
         box.setOrigin(x0, y0);
         box.setFont(headerFont, font);
         box.setMargins(topMargin, bottomMargin, leftMargin, rightMargin);
         box.setRows(rows, rowGap);
         box.setWidth(width);
         box.setButton(btnFont, btnWidth, btnMargin);
         box.setIndexJump(indexJump);
         box.setColumns(columns, columnPositions, columnWidths, columnAlignments, colNames, colKeys);
         if (drawHeaders)
            box.addColHeaders();
         box.setDisplayList(displayList);
         box.init();
         for( Map<String, Object> btnMap : buttons) {
            int pos = (int)btnMap.get("position");
            String displayKey = (String)btnMap.get("displayKey");
            Button btn = (Button)btnMap.get("button");
            ButtonAction action = (ButtonAction)btnMap.get("buttonAction");
            box.addButtonColumn(pos, displayKey, btn, action);
         }
         box.initRowBackground();
         box.initBackground();
         return box;
      }
   }
   
   
   private class RowOfText {
      private TermGroup group, extra;
      private Map<String,String> map;
      private List<String> extraKeys;
      private List<Integer> cellWidth;
      private List<Integer> cellPos;
      private List<Align> cellAlign;
      private Rectangle highlight;

      private int x0, y0, font, width, height, margin;

      public RowOfText(int x, int y, int font, 
            int rowGap, int w, List<Integer> colPos, List<Integer> colWidths, List<Align> aligns) {
         this.x0 = x;
         this.y0 = y;
         this.margin = rowGap / 2;
         this.width = w + 2*margin;
         this.height = font + rowGap;
         this.font = font;
         this.cellPos = colPos;
         this.cellWidth = colWidths;
         this.cellAlign = aligns;
         if (cellPos == null)
            cellPos = new ArrayList<Integer>();
         if (cellWidth == null)
            cellWidth = new ArrayList<Integer>();
         if (cellAlign == null)
            cellAlign = new ArrayList<Align>();

         initTermObjects();
         extra = new TermGroup(x0,y0);
         extraKeys = new ArrayList<String>();

      }

      public void initBackground() {
         highlight = new Rectangle(x0, y0, width, height, Constants.INVISIBLE,false);
         highlight.shift(-margin, 0);
      }

      public void show() {
         group.show();
         extra.show();
      }

      public void hide() {
         map = null;
         group.hide();
         extra.hide();
      }

      public void addButton(int pos, String displayKey, Button passBtn, final ButtonAction action) {
         final Button btn = passBtn;
         btn.hide();
         int x = x0 + pos + btn.x0;
         int y = y0 + margin + btn.y0;
         btn.move(x,y);
         btn.registerOnActionListener(new OnActionListener() {
            @Override
            public void onAction() {
               action.onAction(btn,map);
            }
         });
         extra.add(btn);
         extraKeys.add(displayKey);
      }

      private void updateExtra() {
         Iterator<TermBaseObject> extraIt = extra.getList().iterator();
         Iterator<String> keysIt = extraKeys.iterator();      
         while (extraIt.hasNext()) {
            TermBaseObject obj = extraIt.next();
            String showKey = "false";
            if (map != null)
               showKey = keysIt.hasNext() ? map.get(keysIt.next()) : "";
            if (showKey == null)
               showKey = "";
            boolean hide = "false".equals(showKey) || "0".equals(showKey);
            if (hide)
               obj.hide();
            else if (!obj.on())
               obj.show();
         }
      }

      private void updateBackground() {
         String color = map == null ? null : map.get("background");
         if (color == null || color.isEmpty()) {
            highlight.hide();
            return;
         }
         if (color.equals("red"))
            color = Constants.COLOR_RED;
         else if (color.equals("green"))
            color = Constants.COLOR_GREEN;
         else if (color.equals("yellow"))
            color = Constants.COLOR_YELLOW;
         else if (color.equals("blue"))
            color = Constants.COLOR_BLUE;
         else if (color.equals("purple"))
            color = Constants.COLOR_PURPLE;
         else if (color.equals("pink"))
            color = Constants.COLOR_PINK;
         else if (color.equals("orange"))
            color = Constants.COLOR_ORANGE;
         else if (color.matches("\\$[0-9a-fA-F]{8}")) 
           ; // valid color, do nothing
         else
            color = Constants.INVISIBLE;
         highlight.updateFill(color);
         highlight.show();
      }
      
      public void updateDisplay(Iterator<Map<String, String>> it, List<String> colKeys) {
         if (!it.hasNext()) {
            hide();
            return;
         }

         while (it.hasNext()) {
            Map<String,String> display = it.next();
            boolean hide = "true".equals(display.get("rowHide"));
            if (hide)
               continue;
            map = display;
            setDisplay(colKeys);
            return;
         }
         hide();
         return;
      }


      private void setDisplay(List<String> colKeys ) {
         Iterator<TermBaseObject> groupIt = group.getList().iterator();
         Iterator<String> keysIt = colKeys.iterator();
         Iterator<Integer> widthIt = cellWidth.iterator();

         while (groupIt.hasNext()) {
            TextField label = (TextField)groupIt.next();
            String text = keysIt.hasNext() ? map.get(keysIt.next()) : "";
            if (text == null)
               text = "";
            int width = widthIt.hasNext() ? widthIt.next() : 0;
            TextField.updateLabel(label,text,font,width);
         }

      }

      private void initTermObjects() {
         int f = font;
         group = new TermGroup(x0,y0);

         Iterator<Integer> posIt = cellPos.iterator();
         Iterator<Align> alignIt = cellAlign.iterator();
         while (posIt.hasNext()) {
            int x = x0 + posIt.next();
            int y = y0;
            Align align = alignIt.hasNext() ? alignIt.next() : Align.CENTER; 
            TextField label = new TextField(x,y,f,"",align) ;
            group.add(label);
         }
      }

   }

   public interface ButtonAction{ 
      void onAction(Button btn, Map<String,String> map); 
   } 
}
