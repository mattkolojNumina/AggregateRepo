package termApp.util;

import static termApp.util.Constants.*;

/**
 * A TermGroup class with more structure:
 * <li>vertically evenly spaced elements</li>
 * <li>maximal number of elements in group</li>
 *
 */
public class TermOrderedGroup extends TermGroup {
   protected static final int DEFAULT_SPACING = 80;
   protected static final int DEFAULT_MAX_SIZE = 100;
   
   protected int spacing = DEFAULT_SPACING;
   protected int maxSize = DEFAULT_MAX_SIZE;
   protected boolean isVertical;
   
   /*
    * constructors
    */
   public TermOrderedGroup( TermBaseObject object, int spacing, int maxSize, boolean isVertical ) {
      super(object);
      this.spacing = spacing;
      this.maxSize = maxSize;
      this.isVertical = isVertical;
   }
   
   public TermOrderedGroup(int x, int y, int spacing, int maxSize, boolean isVertical ) {
      super(x, y);
      this.spacing = spacing;
      this.maxSize = maxSize;
      this.isVertical = isVertical;
   }
   
   public TermOrderedGroup( TermBaseObject object, int spacing, boolean isVertical ) {
      this( object, spacing, DEFAULT_MAX_SIZE, isVertical );
   }   
   
   public TermOrderedGroup( TermBaseObject object, boolean isVertical ) {
      this( object, DEFAULT_SPACING, DEFAULT_MAX_SIZE, isVertical );
   }

   
   @Override
   public TermOrderedGroup clone() {
      return clone(null);
   }
   
   public TermOrderedGroup clone(Align align) {
      TermOrderedGroup clone = new TermOrderedGroup(x0, y0, spacing, maxSize,isVertical);
      for ( TermBaseObject o : elements ) {
         o = o.clone();
         if (align != null)
            o.setStr("setAlignment", align.toString() );
         clone.add(o);
      }
      return clone;
   }
   
   /*
    * display methods
    */
   
   protected void setMaxSize(int max) {
      //TODO remove elements if necessary
      this.maxSize = max;
   }
   
   protected void setSpacing(int space) {
      //TODO apply to elements
      this.spacing = space;
      redraw();
   }
   
   private void setVertical(boolean isVertical) {
      this.isVertical = isVertical;
   }
   
   protected void redraw() {
      int i=0;      
      for ( TermBaseObject object : elements ) {
         int space = i++ *spacing;
         int x = isVertical ? x0          : x0 + space;
         int y = isVertical ? y0 + space  : y0;
         object.move(x, y);
      } 
   }

   /*//TODO verify inheritance behaves correctly
   @Override
   public void move(int x, int y) {
      this.x0 = x;
      this.y0 = y;  
      for (int i = 0; i < size(); i++)
         getElement(i).move(x0,y0 + i*rowMargin);
   }
   */

   /**
    * 
    */
   @Override
   public void put( TermBaseObject object, boolean showNow ) {
      add(object,showNow);
   }

   /**
    * 
    * @see termApp.util.TermGroup#add(termApp.util.TermBaseObject, boolean)
    */
   @Override
   public void add( TermBaseObject object, boolean showNow ) {
      if (size() >= maxSize && maxSize > 0) {
         object.hide();
         return;
      }

      int space = elements.size()*spacing;
      int x = isVertical ? x0          : x0 + space;
      int y = isVertical ? y0 + space  : y0;
      
      object.move(x, y);
      elements.add( object );
      if (showNow)
         show();
   }
   
   /**
    * clones first element count times
    * @param count
    */
   public void increase( int count ) {
      if ( count < 0 || size() <= 0)
         return;
      for (int i = 0; i < count; i++)
         add(getElement(0).clone(), false);
   }


   /*//TODO uncomment to allow appended elements
   public void setList( String method, List<String> list ) {
      if (list == null || list.isEmpty() )
         return;
      
      int i = 0;
      for ( ; i < size() ; i++ ) {
         try {
            setElement(i,method,list.get(i));
         } catch (IndexOutOfBoundsException  ex) {
            setElement(i,method,"");
         }
      }
      for ( ; i < list.size() ; i++ ) {
         increase( 1 );
         setElement(i,method,list.get(i++));
      }
      
      show();
   }
   
   public void setMap( String method, Map<String,String> map, List<String> keys ) {
      if (map == null || keys == null || keys.isEmpty() )
         return;
      
      int i = 0;
      for ( ; i < size() ; i++ ) {
         try {
            setElement(i,method,map.get(keys.get(i)));
         } catch (IndexOutOfBoundsException  ex) {
            setElement(i,method,"");
         }
      }    
      for ( ; i < keys.size() ; i++ ) {
         increase( 1 );
         setElement(i,method,getMapStr(map,keys.get(i) ));
      }   
      
      show();
   }
   */

   public void rotate(int vSpace, int hSpace) {
      if (isVertical) 
         toRow(hSpace);
      else
         toColumn(vSpace);
   }
   
   public void toColumn(int spacing) {
      if (isVertical) 
         return;
      setSpacing(spacing);
      setVertical(true);
      redraw();
   }
   
   public void toRow(int spacing) {
      if (!isVertical) 
         return;
      setSpacing(spacing);
      setVertical(false);
      redraw();
   }

}
