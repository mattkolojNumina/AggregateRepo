package termApp.util;

import termApp.util.Constants.Align;
import termApp.util.TermActionObject.OnActionListener;

public class NumPad extends TermObject {

   private TermGroup numPad;
   private int x0, y0, font, gap, charLimit;
   private String value;
   private NumPadAction btnAction, enterBtnAction;
   
   private NumPad() {
      btnAction = defaultAction();
      enterBtnAction = defaultAction();
   }
   
   @Override
   public void show() {
      super.show();
      numPad.show();
   }

   @Override
   public void hide() {
      super.hide();
      numPad.hide();
   }
   
   public void setGlobalBtnAction(NumPadAction action) {
      btnAction = action;
   }
   
   public void setEnterBtnAction(NumPadAction action) {
      enterBtnAction = action;
   }
   
   private void init() {
      numPad = new TermGroup(x0,y0);
      value = "";
      initButtons();
   }

   private void initButtons() {
      
      makeNumPadButton("C",0,3,clearAction());
      makeNumPadButton( 0 ,1,3,appendAction(0));
      makeNumPadButton("E",2,3,enterAction());
      
      makeNumPadButton( 1 ,0,2,appendAction(1));
      makeNumPadButton( 2 ,1,2,appendAction(2));
      makeNumPadButton( 3 ,2,2,appendAction(3));

      makeNumPadButton( 4 ,0,1,appendAction(4));
      makeNumPadButton( 5 ,1,1,appendAction(5));
      makeNumPadButton( 6 ,2,1,appendAction(6));

      makeNumPadButton( 7 ,0,0,appendAction(7));
      makeNumPadButton( 8 ,1,0,appendAction(8));
      makeNumPadButton( 9 ,2,0,appendAction(9));

   }
   
   private void makeNumPadButton( String display, int i, int j, OnActionListener action ) {
      int btnW = calcBtnWidth(font);
      int btnH = calcBtnHeight(font);
      int x = i*(btnW + gap);
      int y = j*(btnH + gap);
      Button btn = new Button(x,y,font,display, Align.LEFT, btnW, false);
      btn.registerOnActionListener(action);
      numPad.put(btn);
   }

   private void makeNumPadButton( int num, int i, int j, OnActionListener action ) {
      makeNumPadButton("" + num, i, j, action);
   }

   public void forceClear() {
      clearAction().onAction();
   }
   
   private void clearValue() {
      value = "";
   }
   
   private OnActionListener clearAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            clearValue();
            btnAction.action(value);
         }
      };
   }
   
   private void appendValue(int num) {
      if (value == null)
         value = "";
      value = value + num;
   }
   
   private OnActionListener appendAction(int num) {
      return new OnActionListener() {
         @Override
         public void onAction() {
            appendValue(num);
            if (value.length() >= charLimit) {
               enterBtnAction.action(value);
               clearValue();
            }
            btnAction.action(value);
         }
      };
   }   
   
   private void enterValue() {
   }
   
   private OnActionListener enterAction() {
      return new OnActionListener() {
         @Override
         public void onAction() {
            enterValue();
            enterBtnAction.action(value);
            clearValue();
            btnAction.action(value);
         }
      };
   }

   private void setOrigin(int x0, int y0) {
      this.x0 = x0;
      this.y0 = y0;      
   }

   /*
   private void setDimensions(int width, int height) {
      this.width = width;
      this.height = height;
   }
   */
   
   private void setFont(int font) {
      this.font = font;
   }
   
   private void setCharLimit(int limit) {
      this.charLimit = limit;
   }
   
   private void setGap(int gap) {
      this.gap = gap;
   }
   
   private static int calcBtnWidth( int font ) {
      int minWidth = (int) ((2.12 * font / 3 + 100/3) * 1.05) ;
      int stdWidth = font + 20;
      return Math.max(stdWidth, minWidth);
   }
   
   private static int calcBtnHeight( int font ) {
      return font + 20;
   }
   
   public static class NumPadConstructor{ 
      private int x0, y0, font, gap, charLimit;
      
      public NumPadConstructor(int x0, int y0) {
         setOrigin(x0,y0);
         setFont(65);
         setGap(15);
         setCharLimit(10);
         //setDimensions(600,800);
      }
      
      public void setOrigin(int x0, int y0) {
         this.x0 = x0;
         this.y0 = y0;      
      }

      public void setFont(int font) {
         this.font = font;
      }

      public void setCharLimit(int limit) {
         this.charLimit = limit;
      }
      
      /*
      public void setDimensions(int width, int height) {
         this.width = width;
         this.height = height;
      }
      */
      
      public void setGap(int gap) {
         this.gap = gap;
      }
      
      public NumPad build() {
         NumPad numPad = new NumPad();
         numPad.setOrigin(x0,y0);
         numPad.setFont(font);
         numPad.setGap(gap);
         numPad.setCharLimit(charLimit);
         //numPad.setDimensions(width,height);
         numPad.init();
         return numPad;
      }

   }

   private NumPadAction defaultAction() {
      return new NumPadAction() {
         
         @Override
         public void action(String value) {
            System.out.printf("num pad value: %s\n", value);
         }
      };
      
   }
   
   public interface NumPadAction { 
      void action(String value); 
   }
   
}
