package termApp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import termApp.util.Constants.*;

/**
 * A base class for managing a list of TermObjects 
 */
public class TermGroup {
   protected int x0, y0;
   protected List<TermBaseObject> elements;
   
   /*
    * constructors
    */
   /**
    * Constructs an empty group with an origin at x, y coordinates 
    * 
    * @param x horizontal position
    * @param y vertical position
    */
   public TermGroup(int x, int y) {
      this.x0 = x;
      this.y0 = y;
      this.elements = new ArrayList<TermBaseObject>();
   }
   
   /**
    * Constructs a group with the same origin as the object and adds it to the group.
    * 
    * @param object first object in the group
    */
   public TermGroup( TermBaseObject object ) {
      this(object.x0,object.y0); 
      add(object,false);
   }
   
   public TermGroup( TermGroup group ) {
      this(group.x0,group.y0);
      for ( TermBaseObject o : group.elements ) 
         add(o);
   }
   
   /**
    * Creates and returns a copy of this TermGroup.
    * 
    * @return cloned instance of object
    * 
    * @see java.lang.Object#clone()
    */
   public TermGroup clone() {
      TermGroup clone = new TermGroup(this.x0,this.y0);
      for ( TermBaseObject o : elements ) 
         clone.add(o.clone());
      return clone;
   }
   
   /*
    * display methods
    */
   
   /**
    * Shifts the TermGroup and all elements within it relative to the TermGroup origin 
    * by the specified coordinates.
    * 
    * @param x horizontal shift
    * @param y vertical shift
    */
   public void shift(int x, int y) {
      this.x0 += x;
      this.y0 += y;
      for ( TermBaseObject o : elements ) 
         o.shift(x,y);
   }
   
   /**
    * Moves the TermGroup and all elements within it to the specified coordinates,
    * Each element is moved relative to the {@link #x0}, {@link #y0} origin.
    * 
    * @param x horizontal position
    * @param y vertical position
    */
   public void move(int x, int y) {
      shift(x-x0,y-y0);
   }
  
   
   /**
    * Aligns each element within the TermGroup to the specified alignment.
    * 
    * @param align alignment to set
    */
   public void setAlignment( Align align ) {
      for ( TermBaseObject o : elements ) 
         o.setAlignment(align);
   }
   
   /**
    * Calls refresh on each element in TermGroup
    * 
    * @see termApp.util.TermBaseObject#refresh()
    */
   public void refresh() {
      for ( TermBaseObject o : elements ) 
         o.refresh();
   }
   
   /**
    * Calls clear on each element in TermGroup
    * 
    * @see termApp.util.TermBaseObject#clear()
    */
   public void clear() {
      for ( TermBaseObject o : elements ) 
         o.clear();
   }
   
   /**
    * Calls hide on each element in TermGroup
    * 
    * @see termApp.util.TermBaseObject#hide()
    */
   public void hide() {
      for ( TermBaseObject o : elements ) 
         o.hide();
   }
   
   /**
    * Calls show on each element in TermGroup
    * 
    * @see termApp.util.TermBaseObject#show()
    */
   public void show() {
      for ( TermBaseObject o : elements ) 
         o.show();
   }
   
   /**
    * Calls {@link #add(TermBaseObject, boolean)} on an element after shifting it 
    * relative to the {@link #x0}, {@link #y0} origin by its own origin
    * 
    * @param object element to be added
    * @param showNow call {@link #show()} after added
    * 
    * @see #add(TermBaseObject, boolean)
    */
   public void put( TermBaseObject object, boolean showNow ) {
      object.shift(x0, y0);
      add(object,showNow);
   }
   
   public void put( TermBaseObject object ) {
      put(object, false);
   }
   
   public void put( List<TermBaseObject> list ) {
      if ( list == null || list.isEmpty() )
         return;
      for (TermBaseObject o : list)
         put(o);
   }

   public void put( TermBaseObject... array ) {
      try {
         put( Arrays.asList(array));
      } catch (NullPointerException ex) { }
   }
   
   /**
    * Adds an element to the TermGroup
    * 
    * @param object element to be added
    * @param showNow call {@link #show()} after added
    */
   public void add( TermBaseObject object, boolean showNow ) {
      elements.add( object ); 
      if (showNow)
         show();
   }
   
   public void add( TermBaseObject object ) {
      add(object, false);
   }
   
   public void add( List<TermBaseObject> list ) {
      if ( list == null || list.isEmpty() )
         return;
      for (TermBaseObject o : list)
         add(o);
   }

   public void add( TermBaseObject... array ) {
      try {
         add( Arrays.asList(array));
      } catch (NullPointerException ex) { }
   }
   
   /**
    * Returns the element at the specified position in this TermGroup.
    * 
    * @param index index of the element to return
    * @return the element at the specified position in this TermGroup,
    *         null if index is out of bounds
    */
   public TermBaseObject getElement( int index ) {
      try {
         return elements.get(index);
      } catch (IndexOutOfBoundsException  ex) {
         return null;      
      }
   }

   /**
    * Returns a subset of the term group from the indices given
    * 
    * @param fromIndex  low endpoint (inclusive) of the TermGroup
    * @param toIndex  high endpoint (exclusive) of the TermGroup
    * @return
    */
   public TermGroup subGroub( int fromIndex, int toIndex) {
      try {
         List<TermBaseObject> list = elements.subList(fromIndex, toIndex);
         TermGroup group = new TermGroup(list.get(0).getX(),list.get(0).getY());
         for ( TermBaseObject o : list ) 
            group.add(o);
         return group;
      } catch (NullPointerException | IndexOutOfBoundsException | IllegalArgumentException ex) {
         return null;      
      }
   }
   
   public TermGroup subGroub( int fromIndex ) {
      return subGroub(fromIndex, elements.size());
   }
   
   /**
    * Tries to invoke the given method on the element at the specified position in this TermGroup and pass it
    * the formated string.
    * Will fail if index is out of bounds or method not defined on TermObject subclass.
    * 
    * @param index index of the element to invoke
    * @param method method name to be invoked
    * @param format format string, not null
    * @param args arguments referenced by the format specifiers in the format string, optional
    * 
    * @see termApp.util.TermBaseObject#setStr(String, String)
    */
   public void setElement( int index, String method, String format, Object...  args ) {
      try {
         getElement(index).setStr(method, format, args);
      } catch (NullPointerException  ex) { 
         TermUtility.error( "index [%d] out of bounds", index);
      }
   }

   /**
    * Tries to invoke the given method on each element in the TermGroup and pass it the respective string item 
    * in the list.
    * If <tt>list.size() &lt; {@link #size()}</tt> and empty string is called on the remaining elements.
    * If <tt>list.size() &gt; {@link #size()}</tt> the remaining items in the list are ignored.
    * If additions are successful, {@link #refresh()} is called.
    * 
    * @param method method name to be invoked
    * @param list list of string parameters for method, not null
    * 
    * @see TermGroup#setElement(int, String, String, Object...);
    */
   public void setList( String method, List<String> list ) {
      if (list == null )
         return;
      
      int i = 0;
      for ( ; i < size() ; i++ ) {
         try {
            setElement(i,method,list.get(i));
         } catch (IndexOutOfBoundsException  ex) {
            setElement(i,method,"");
         }
      }
      // ignore remaining items in list
      
      refresh();
   }
   
   public void setList( String method, String... array ) {
      try {
         setList(method, Arrays.asList(array));
      } catch (NullPointerException ex) { }
   }
   
   /**
    * Tries to invoke the given method on each element in the TermGroup and pass it the string value from 
    * the map taken from respective key in the list.
    * If <tt>list.size() &lt; {@link #size()}</tt> and empty string is called on the remaining elements.
    * If <tt>list.size() &gt; {@link #size()}</tt> the remaining items in the list are ignored.
    * If additions are successful, {@link #refresh()} is called.
    * 
    * @param method method name to be invoked
    * @param map map of string key, value pairs, not null
    * @param keys list of keys used to get string parameters for method, not null
    *     
    * @see TermGroup#setElement(int, String, String, Object...);
    */
   public void setMap( String method, Map<String,String> map, List<String> keys ) {
      if (map == null || keys == null  )
         return;
      
      int i = 0;
      for ( ; i < size() ; i++ ) {
         try {
            setElement(i,method,map.get(keys.get(i)));
         } catch (IndexOutOfBoundsException  ex) {
            setElement(i,method,"");
         }
      }    
      // ignore remaining keys in list
      
      refresh();
   }
   
   public void setMap( String method,  Map<String,String> map, String... array ) {
      try {
         setMap(method, map, Arrays.asList(array));
      } catch (NullPointerException ex) { }
   }
   
   /**
    * Returns the number of elements in this TermGroup.
    * 
    * @return the number of elements in this TermGroup
    */
   public int size() {
      return elements.size();
   }

   /**
    * Returns the list of elements in this TermGroup.
    * 
    * @return the list of elements in this TermGroup
    */ 
   public List<TermBaseObject> getList() {
      return elements;
   }
   
   /*
    * special accessor methods
    */
   
   /**
    * Tries to invoke the given method on each element in the TermGroup and pass it the given integer value.
    * 
    * @param method method name to be invoked
    * @param i integer value passed to method
    * 
    * @see termApp.util.TermBaseObject#setInt(String, int)
    */
   public void setInt(String method, int i) {
      for ( TermBaseObject o : elements ) 
         o.setInt(method,i);   
   }
   
   /**
    * Tries to invoke the given method on each element in the TermGroup and pass it the given string value.
    *     
    * @param method method name to be invoked
    * @param text string value passed to method
    * 
    * @see termApp.util.TermBaseObject#setStr(String, String)
    */
   public void setStr(String method, String text) {
      for ( TermBaseObject o : elements ) 
         o.setStr(method,text);   
   }
   
   /**
    * Returns a list of strings generated by invoking the given method on each element in the TermGroup.
    * If invocation fails, null is added in place of a string value.
    *     
    * @param method method name to be invoked
    * @return list of strings 
    * 
    * @see termApp.util.TermBaseObject#getStr(String, String)
    */
   public List<String> getStr(String method) {
      List<String> list = new ArrayList<String>();
      for ( TermBaseObject o : elements ) 
         list.add(o.getStr(method));   
      return list;
   }

   /**
    * Returns a list of booleans generated by invoking the given method on each element in the TermGroup 
    * passing it the integer value.
    * If invocation fails, null is added in place of a Boolean value.   
    * <p>
    * <b>Note:</b> useful for calling {@linkplain termApp.util.Button#pressed(int)}
    * 
    * @param method method name to be invoked
    * @param i integer value to pass to method
    * @return list of booleans returned by method
    * 
    * @see termApp.util.TermBaseObject#getBoolean(String, int)
    */
   public List<Boolean> getBoolean(String method, int i) {
      List<Boolean> list = new ArrayList<Boolean>();
      for ( TermBaseObject o : elements ) 
         list.add(o.getBoolean(method,i));   
      return list;
   }

   public List<Boolean> getBoolean(String method, int i, String text) {
      List<Boolean> list = new ArrayList<Boolean>();
      for ( TermBaseObject o : elements ) 
         list.add(o.getBoolean(method,i, text));   
      return list;
   }
   
   protected String getMapStr(Map<String, String> m, String name) {
      if (m == null)
         return "";
      String val = m.get( name );
      return (val == null) ? "" : val;
   }

   public void clearList() {
      elements.clear();
      
   }
   
}