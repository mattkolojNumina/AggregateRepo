package termApp.util;

import termApp.util.Constants.Align;

public abstract class TermActionObject extends TermBaseObject {
   protected OnActionListener listener;
   protected OnTextActionListener textListener;

   /*
    * constructors
    */
   
   public TermActionObject() {
      super();
      this.listener = null;
      this.textListener = null;
   }


   public TermActionObject( int x, int y, int width, int height,  Align align, 
         boolean displayNow )  {
      super(x,y,width,height,align,displayNow);
      this.listener = null;
      this.textListener = null;
   }
   
   /*
    * action listener
    */
   
   public interface OnActionListener { 
      void onAction(); 
   } 
   
   public interface OnTextActionListener { 
      void onAction( String text ); 
   } 
   
   public void registerOnActionListener(OnActionListener listener) { 
       this.listener = listener; 
   } 
   
   public void registerOnTextActionListener(OnTextActionListener listener) { 
      this.textListener = listener; 
  } 

   public boolean doAction( int tag, String text ) {
      if (this.textListener != null && actionOccured(tag) ) { 
         textListener.onAction(text); 
         return true;
      } 
      return false;
   }

   public boolean doAction( int tag ) {
      if (this.listener != null && actionOccured(tag) ) { 
         listener.onAction(); 
         return true;
      } 
      return false;
   }

   public boolean actionOccured( int checkTag ) {
      if (on)
         return tag == checkTag;
      return false;
   }
   
   public void setFocus() {
      term.setFocus(tag);
   }
}
