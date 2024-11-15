package termApp.util;

/**
 * A TermOrderedGroup class that is horizontal
 */
public class TermRow extends TermOrderedGroup{
   
   /*
    * constructors
    */
   public TermRow( TermBaseObject object, int spacing, int maxSize ) {
      super(object, spacing, maxSize, false);
   }
   
   public TermRow(int x, int y, int spacing, int maxSize ) {
      super(x, y, spacing, maxSize, false);
   }
   
   public TermRow( TermBaseObject object, int spacing ) {
      super( object, spacing, false );
   }   
   
   public TermRow( TermBaseObject object ) {
      super( object, false );
   }

   public int colCount() {
      return size();
   }
}
