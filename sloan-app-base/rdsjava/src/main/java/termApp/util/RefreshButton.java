package termApp.util;

import termApp.util.TermActionObject.OnActionListener;
import static termApp.util.Constants.*;
import static rds.RDSLog.*;

public class RefreshButton extends TermObject {
   private int x0, y0, font, width;
   private String btnText, barColor;
   private long lastReresh, refreshPeriod ;
   
   private OnActionListener action;
   private Rectangle progressBar ;
   
   private void setOrigin(int x0, int y0) {
      this.x0 = x0;
      this.y0 = y0;      
   }

   private void setFont(int font) {
      this.font = font;
   }
   
   private void setText( String buttonText ) {
      this.btnText = buttonText;
   }

   private void setBarColor( String barColor ) {
      this.barColor = barColor;
   }

   private void setWidth(int width) {
      this.width = width;
   }
   
   private void setRefreshPeriod( long period ) {
      this.refreshPeriod = period ;
   }
   
   private void setRefreshAction( OnActionListener action ) {
      this.action = action ;
   }
   
   private void init() {
      int x = x0;
      int y = y0;
      int w = width;
      int f = font;
      Button refresh = new Button(x,y,f,btnText,Align.LEFT,w,false);
      refresh.registerOnActionListener( new OnActionListener() {
         @Override
         public void onAction() {
            doRefresh();
         }
      } );
      refresh.show();
      
      y += font + 20;
      Rectangle border = new Rectangle(x,y,w,25,INVISIBLE,5,"black",Align.LEFT,true);
      progressBar = new Rectangle(border.clone());
      progressBar.setBorder("", 0);
      progressBar.setFill(barColor);
      progressBar.show();      
      
      lastReresh = System.currentTimeMillis() ;
   }
   
   public void tick() {
      long wait = System.currentTimeMillis() - lastReresh;
      progressBar.setWidth((int)(Math.min(width,width * wait/refreshPeriod)) );
      progressBar.refresh();
      if (wait < refreshPeriod) 
         return;
      
      doRefresh();
   }
   
   public void forceRefresh() {
      lastReresh = 0;
      tick();
   }
   
   private void doRefresh() {
      lastReresh = System.currentTimeMillis() ;
      action.onAction();
   }
   
   public static class RefreshButtonConstructor{ 
      private int x0, y0, font, width;
      private String btnText, barColor;
      private long refreshPeriod ;
      private OnActionListener action;
      
      public RefreshButtonConstructor(int x0, int y0) {
         setOrigin(x0,y0);
         setFont(45);
         setWidth(300);
         setText("Refresh");
         setBarColor(COLOR_BLUE);
         setRefreshPeriod(60000L);
         setRefreshAction(defaultAction());
      }
      
      public void setOrigin(int x0, int y0) {
         this.x0 = x0;
         this.y0 = y0;      
      }

      public void setFont(int font) {
         this.font = font;
      }
      
      public void setText( String buttonText ) {
         this.btnText = buttonText;
      }

      public void setBarColor( String barColor ) {
         this.barColor = barColor;
      }

      public void setWidth(int width) {
         this.width = width;
      }
      
      /**
       * set reresh rate
       * @param period (in milliseconds)
       */
      public void setRefreshPeriod( long milliseconds ) {
         this.refreshPeriod = milliseconds ;
      }
      
      public void setRefreshAction( OnActionListener action ) {
         this.action = action ;
      }
      
      public RefreshButton build() {
         RefreshButton refreshButton = new RefreshButton();
         
         refreshButton.setOrigin(x0,y0);
         refreshButton.setFont(font);
         refreshButton.setWidth(width);
         refreshButton.setText(btnText);
         refreshButton.setBarColor(barColor);
         refreshButton.setRefreshPeriod(refreshPeriod);
         refreshButton.setRefreshAction(action);
         
         refreshButton.init();
         return refreshButton;
      }

   }

   private static OnActionListener defaultAction() {
      return new OnActionListener() {
         
         @Override
         public void onAction() {
            trace("refresh");
         }
      };
      
   }
}
