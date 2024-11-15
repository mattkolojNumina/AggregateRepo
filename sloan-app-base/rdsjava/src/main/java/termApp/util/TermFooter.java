package termApp.util;

import static termApp.util.Constants.*;
import termApp.util.Constants.Align;
import termApp.util.TermActionObject.OnActionListener;

/*
 *  Footer class
 */
public class TermFooter extends TermObject {
   private static final boolean DEFAULT_DRAW_TIME = true;
   private static final boolean DEFAULT_DRAW_OP = true;

   private static TermFooter single_instance = null; 

   private boolean drawTime, drawOp, drawLogoff;
   private Button logoff;
   private TextField time, operator, termName;
   private Rectangle border;
   
   public TermFooter() {
      drawTime = drawOp = drawLogoff = false;
      logoff = null;
      time = null;
      operator = null;
      termName = null;
      border = null;
   }

   /*
   public static TermFooter getFooter() {
      if (single_instance == null)
         single_instance =  new TermFooter();
      
      return single_instance;
   }
   */
  
   public void hideBorder() {
      border.hide();
   }
   public boolean getDrawLogoff() {
      return this.drawLogoff;
   }
    public boolean getDrawOp() {
      return this.drawOp;
   }
   public boolean getDrawTime() {
      return this.drawTime;
   }
   public boolean getBorderOn() {
      return this.border.on();
   }
   public void init(boolean drawTime, boolean drawOp) {
      if (initialized())
         return;

      this.drawTime = drawTime ;
      this.drawOp = drawOp ;
      
      int width = SCREEN_WIDTH;
      int height = 90;
      int x0 = 0;
      int y0 = SCREEN_HEIGHT-height;
      int margin = 10;
      int font = 60;
      int x, y, w, h, f, b;
      
      x = x0 + margin;
      y = y0 + margin;
      f = font - margin;
      logoff = new Button(x,y,f,"Logout",Align.LEFT,false);
      logoff.registerOnActionListener(new OnActionListener() {
         @Override
         public void onAction() {
            screen.processLogout();         
         }
      });
      
      x = x0 + margin;
      operator = new TextField( x, y, f,"", Align.LEFT);
      
      if (drawLogoff) {
         operator.shift(300, 0);
      }
      setOperator(screen.getFooterOperatorId());
      
      x = width - margin;
      y = y0 + margin;
      f = 50;
      time = new TextField( x, y, f,"", Align.RIGHT);
      
      x = width - margin;
      y = SCREEN_HEIGHT - 25;
      f = 20;
      termName = new TextField( x, y, f,"", Align.RIGHT);      
      
      b = margin/2;
      x = x0 - b;
      y = y0;
      w = width + 2*b;
      h = height + 2*b;
      border = new Rectangle(x,y,w,h,"White",b,"Black",false); 
      
      on = true;
      refresh();
   }
   
   public void init() {
      init(DEFAULT_DRAW_TIME, DEFAULT_DRAW_OP);
   }
   
   public boolean initialized() {
      return border != null;
   }
   
   public void enableLogout( boolean loginRequired, boolean logoutAllowed) {
      if ( loginRequired && logoutAllowed ) 
         drawLogoff = true;
   }
   
   public void hide() {
      if (!initialized())
         return;
      logoff.hide();
      time.hide();
      operator.hide();
      border.hide();
   }
   
   public void show() {
      if (!initialized())
         return;
      if(!drawTime && !drawLogoff && drawOp) {
         hide();
         return;
      }
      
      border.show();
      if (drawLogoff)
         logoff.show();
      if (drawTime)
         time.show();
      if (drawOp)
         operator.show();
   }

   public boolean logoutPressed( int tag ) {
      return logoff.actionOccured(tag);
   }
   
   public void setOperator(String op) {
      if (operator == null || !drawOp)
         return;
      if (op.isEmpty())
         operator.updateText( "" );
      else
         operator.updateText( "Operator: %s", op);
   }
   
   public void setTermName(String termNameStr) {
      if (termNameStr == null )
         return;
      if (termNameStr.isEmpty())
         termName.updateText( "" );
      else
      	termName.updateText( termNameStr);   	
   }
   
   public void tick() {
      if (time == null || !drawTime)
         return;
      time.updateText(term.getDb().getString( "", 
            "SELECT DATE_FORMAT(NOW(), '%%m/%%d/%%y %%T')" ));
   }
}
