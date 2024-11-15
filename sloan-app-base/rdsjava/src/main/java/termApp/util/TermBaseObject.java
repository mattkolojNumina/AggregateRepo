package termApp.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import termApp.util.Constants.Align;
import static termApp.util.TermUtility.error;

public abstract class TermBaseObject
extends TermObject {
   protected final int tag;
   protected int x0, y0; 
   protected int width, height; 
   protected Align alignment;

   /**
    * Constructs an object and assigns it a tag, increments the masterTag.
    */
   public TermBaseObject() {
      this.tag = TermUtility.getTag();
      TermUtility.addToAll(this);
   }
   
   public TermBaseObject( int x, int y, int width, int height,  Align align, 
         boolean displayNow )  {
      this();
      this.x0 = x;
      this.y0 = y;
      this.width = width;
      this.height = height;
      this.alignment = align;
      this.on = displayNow;
   }
   
   /**
    * Creates and returns a copy of this object.
    * 
    * @return cloned instance of object
    * 
    * @see java.lang.Object#clone()
    */
   public TermBaseObject clone() {
      //clone object
      return null;
   }

   /**
    * Clear the object, i.e., clear parameters such that the visibility has changed, but parts of 
    * the object may still be visible on screen.
    * Similar to {@link #hide()}.
    */
   public void clear() {
      hide();
   }
   
   /**
    * get the horizontal origin. The {@link #alignment} of the object is based off of this field.
    * 
    * @return the x0
    */
   public int getX() {
      return x0;
   }

   /**
    * get the vertical origin (top)
    * 
    * @return the y0
    */
   public int getY() {
      return y0;
   }
   
   /**
    * get the horizontal length. 
    * 
    * @return the width
    */
   public int getWidth() {
      return width;
   }

   /**
    * get the vertical length.
    * 
    * @return the height
    */
   public int getHeight() {
      return height;
   }

   /**
    * Shift the object's position from its current origin by (x,y) amount
    * 
    * @param x horizontal shift
    * @param y vertical shift
    */
   public void shift( int x, int y ) {
      this.x0 += x;
      this.y0 += y;
      refresh();
   }
   
   /**
    * Move the object's origin to the (x,y) coordinates.
    * 
    * @param x new horizontal position
    * @param y new vertical position
    */
   public void move( int x, int y ) {
      this.x0 = x;
      this.y0 = y;
      refresh();
   }
   
   /**
    * Get the object's tag
    * 
    * @return the objects tag
    */
   public int getTag() {
      return tag;
   }
   
   /**
    * Set the alignment of the object.
    * Alignment values are {@code Align.CENTER}, {@code Align.RIGHT}, or {@code Align.LEFT}
    * Calls {@link #refresh()} after alignment is set.
    * 
    * @param alignment of object (type Align), not null
    * 
    * @see {@linkplain termApp.util.Constants.Align}
    */
   public void setAlignment(Align align) {
      if ( align == null )
         return;
      this.alignment = align;
      refresh();
   }
   
   /**
    * Set the alignment of the object from a string.
    * Tries to parse the argument to a type Align and calls {@link #setAlignment(Align)}.
    * If it fails, alignment is not set.
    * 
    * @param alignment of object (type String), not null
    * 
    * @see #setAlignment(Align)
    * @see {@linkplain termApp.util.Constants.Align}
    */
   public void setAlignment( String align ) {
      if ( align == null )
         return;
      try {
         setAlignment(Align.valueOf(align.toUpperCase()));
      } catch (IllegalArgumentException ex ) {
         error( "failed to extract Align from [%s]", align);
      }
   }
   
   /*
    * special accessor methods
    */

   /**
    * Tries to invoke the given method on the TermObject and pass it the integer parameter.
    * The method must be defined for the TermObject (or subclass) and take one int parameter.
    * The method return is ignored.
    * 
    * @param method method name to be invoked
    * @param i integer to pass to method
    */
   public void setInt(String method, int i ) {
      Method m = getMethod( method, i );
      if(m == null)
         return;
      
      try {
         m.invoke(this, i);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
         error("failed to invoke method [%s]", method );
      }
   }
   

   /**
    * Tries to invoke the given method on the TermObject and pass it the string parameter.
    * The method must be defined for the TermObject (or subclass) and take one String parameter.
    * The method return is ignored.
    * 
    * @param method method name to be invoked
    * @param text string to pass to method
    */
   public void setStr(String method, String text ) {
      Method m = getMethod( method, text );
      if(m == null)
         return;
      
      try {
         m.invoke(this, text);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
         error("failed to invoke method [%s]", method );
      }
   }

   public void setStr( String method, String format, Object...  args ) {
      try {
         setStr(method, String.format(format, args));
      } catch (java.util.IllegalFormatException e) {
         error( "failed to format %s", format);
      }
   }
   
   
   /**
    * Tries to invoke the given method on the TermObject and pass it the integer parameter.
    * The method must be defined for the TermObject (or subclass) and take one int parameter 
    * and have a boolean return type.
    * If these requirements are met, the method boolean return value is returned.
    * Otherwise, null is returned on failure.
    * 
    * @param method method name to be invoked
    * @param int integer to pass to method
    * @return the boolean value from the method call, or null on failure.
    */
   public Boolean getBoolean(String method, int i ) {
      Method m = getMethod( method, i );
      if(m == null)
         return null;
      
      Boolean r = null;
      try {
         r = (Boolean) m.invoke(this, i);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
         error("failed to invoke method [%s] on class %s with [%d]", method, this.getClass().getName(), i );
      } catch (ClassCastException e) {
         error("method %s does not return type [%s]", method, Boolean.class.getName() );
      }
      return r;
   }
   
   /**
    * Tries to invoke the given method on the TermObject and pass it the integer parameter.
    * The method must be defined for the TermObject (or subclass) and take one int parameter 
    * and have a boolean return type.
    * If these requirements are met, the method boolean return value is returned.
    * Otherwise, null is returned on failure.
    * 
    * @param method method name to be invoked
    * @param int integer to pass to method
    * @return the boolean value from the method call, or null on failure.
    */
   public Boolean getBoolean(String method, int i, String text ) {
      Method m = getMethod( method, i, text );
      if(m == null)
         return null;
      
      Boolean r = null;
      try {
         r = (Boolean) m.invoke(this, i, text);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
         error("failed to invoke method [%s] on class %s with [%d]", method, this.getClass().getName(), i );
      } catch (ClassCastException e) {
         error("method %s does not return type [%s]", method, Boolean.class.getName() );
      }
      return r;
   }
   
   /**
    * Tries to invoke the given method on the TermObject and pass it the string parameter.
    * The method must be defined for the TermObject (or subclass) and have a string return type.
    * If these requirements are met, the method string return value is returned.
    * Otherwise, null is returned on failure.
    * 
    * @param method method name to be invoked
    * @return the string value returned by the method, or null on failure.
    */
   public String getStr(String method ) {
      Method m = getMethod( method, null );
      if(m == null)
         return null;
      
      String text = null;
      try {
         text = (String) m.invoke(this, (Object[]) null);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
         error("failed to invoke method %s", method );
      } catch (ClassCastException e) {
         error("failed to get term object method [%s] on class %s", method, this.getClass().getName() );
      }      
      return text;
   }
  
   private Method getMethod( String method, Object o ) {
      try {
         if (o == null)
            return this.getClass().getMethod(method, (Class<?>[]) null);
         else
            return this.getClass().getMethod(method, o.getClass() );//new Class[] { o.getClass() });
      } catch (NoSuchMethodException | SecurityException e) {      
         error("failed to get term object method [%s] on class %s", method, this.getClass().getName() );
      }
      return null;
   }
   
   private Method getMethod( String method, int i ) {
      try {
         return this.getClass().getMethod(method, int.class );
      } catch (NoSuchMethodException | SecurityException e) {      
         error("failed to get term object method [%s] on class %s", method, this.getClass().getName() );
      }
      return null;
   }
   
   private Method getMethod( String method, int i, Object o ) {
      try {
         return this.getClass().getMethod(method, int.class, o.getClass() );
      } catch (NoSuchMethodException | SecurityException e) {      
         error("failed to get term object method [%s] on class %s", method, this.getClass().getName() );
      }
      return null;
   }
   

}


