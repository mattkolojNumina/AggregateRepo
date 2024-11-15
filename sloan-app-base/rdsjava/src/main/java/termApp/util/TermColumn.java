package termApp.util;

/**
 * A TermOrderedGroup class that is vertical
 */
public class TermColumn extends TermOrderedGroup{
   
   /*
    * constructors
    */
   public TermColumn( TermBaseObject object, int spacing, int maxSize ) {
      super(object, spacing, maxSize, true);
   }
   
   public TermColumn(int x, int y, int spacing, int maxSize ) {
      super(x, y, spacing, maxSize, true);
   }
   
   public TermColumn( TermBaseObject object, int spacing ) {
      super( object, spacing, true );
   }   
   
   public TermColumn( TermBaseObject object ) {
      super( object, true );
   }

   public int rowCount() {
      return size();
   }
}
