/*
 * RDSTableCellRenderer.java
 */

package rds;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.*;
import java.util.EventObject;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;


/**
 * An extension of the default table renderer that provides a table view
 * suited for most RDS applications.
 */
public class RDSTableCellRenderer
      extends DefaultTableCellRenderer {
   public static final int NO_ALIGNMENT = -1;
   protected static final Border DEFAULT_BORDER = new EmptyBorder( 0, 5, 0, 5 );

   protected static int alternateRowDarkness = 15;

   protected int defaultAlignment = LEADING;
   protected Font rendererFont = null;
   protected Border rendererBorder = DEFAULT_BORDER;

   /**
    * Constructs a cell renderer.
    */
   public RDSTableCellRenderer() {
      super();
   }

   /**
    * Specifies the font for this renderer; if {@code null}, the
    * table font is used.
    */
   public void setRendererFont( Font rendererFont ) {
      super.setFont( rendererFont );
      this.rendererFont = rendererFont;
   }

   /**
    * Specifies the border for this renderer.
    */
   public void setRendererBorder( Border rendererBorder ) {
      super.setBorder( rendererBorder );
      this.rendererBorder = rendererBorder;
   }

   /**
    * Specifies the default alignment for this renderer.  This will be used
    * for columns for which the alignment has not been explicitly set.
    */
   public void setDefaultAlignment( int defaultAlignment ) {
      this.defaultAlignment = defaultAlignment;
   }

   /**
    * Sets the value that determines the amount by which alternate rows
    * in the table are darker than the table's background color.
    * 
    * @param   newAlternateRowDarkness  the darkness value
    */
   public static void setAlternateRowDarkness( int newAlternateRowDarkness ) {
      alternateRowDarkness = newAlternateRowDarkness;
   }

   /**
    * Gets the color of an alternate row, given the normal color.
    * 
    * @param   color  the original color
    * @return  the alternate color
    */
   public static Color getAlternateColor( Color color ) {
      int r = Math.max( color.getRed()   - alternateRowDarkness, 0 );
      int g = Math.max( color.getGreen() - alternateRowDarkness, 0 );
      int b = Math.max( color.getBlue()  - alternateRowDarkness, 0 );
      return new Color( r, g, b );
   }

   /**
    * Renders the value of the cell.
    */
   @Override
   public Component getTableCellRendererComponent( JTable table,
         Object value, boolean isSelected, boolean hasFocus, int row,
         int column ) {

      super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column );

      // set alternating row colors
      if (!isSelected) {
         setForeground( table.getForeground() );
         Color c = table.getBackground();
         if (table instanceof RDSTable &&
               ((RDSTable)table).isAlternateRow( row ))
            c = getAlternateColor( c );
         setBackground( c );
      }

      // set horizontal alignment
      int alignment = defaultAlignment;
      if (table instanceof RDSTable) {
         int newAlignment = ((RDSTable)table).getColumnAlignment( column );
         if (newAlignment != NO_ALIGNMENT)
            alignment = newAlignment;
      }
      super.setHorizontalAlignment( alignment );

      // set font and border
      if (rendererFont != null)
         super.setFont( rendererFont );
      if (rendererBorder != null)
         super.setBorder( rendererBorder );

      return this;
   }

   /**
    * A renderer for <code>Number</code>s.
    */
   public static class NumberRenderer
         extends RDSTableCellRenderer {
      public NumberRenderer() {
         super();
         defaultAlignment = RIGHT;
      }
   }  // end NumberRenderer member class

   /**
    * A renderer for <code>Double</code>s and <code>Float</code>s.
    */
   public static class DoubleRenderer
         extends NumberRenderer {
      DecimalFormat formatter;

      public DoubleRenderer() {
         super();
         formatter = new DecimalFormat();
      }

      public DoubleRenderer( String pattern ) {
         this();
         setFormat( pattern );
      }

      public void setFormat( String pattern ) {
         formatter.applyPattern( pattern );
      }

      @Override
      public void setValue( Object value ) {
         setText( (value == null) ? "" : formatter.format( value ) );
      }
   }  // end DoubleRenderer member class

   /**
    * A renderer for {@code Boolean}s and other boolean-like
    * two-choice cells ("yes"/"no", "on"/"off", etc.).
    */
   public static class BooleanRenderer
         extends JCheckBox
         implements TableCellRenderer {
      private String trueString;

      public BooleanRenderer() {
         this( null );
      }

      public BooleanRenderer( String trueString ) {
         super();
         this.trueString = trueString;
         setBorderPaintedFlat( true );
         setHorizontalAlignment( CENTER );
      }

      public void setTrueString( String trueString ) {
         this.trueString = trueString;
      }

      @Override
      public Component getTableCellRendererComponent( JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column ) {
         if (isSelected) {
            setForeground( table.getSelectionForeground() );
            setBackground( table.getSelectionBackground() );
         } else {
            setForeground( table.getForeground() );
            Color c = table.getBackground();
            if (table instanceof RDSTable &&
                  ((RDSTable)table).isAlternateRow( row ))
               c = getAlternateColor( c );
            setBackground( c );
         }

         setValue( value );

         return this;
      }

      protected void setValue( Object value ) {
         boolean isTrue = false;
         if (value == null)
            isTrue = false;
         else if (value instanceof Boolean)
            isTrue = ((Boolean)value).booleanValue();
         else if (trueString != null)
            isTrue = value.toString().equals( trueString );
         setSelected( isTrue );
      }

      // override for performance (see DefaultTableCellRenderer)

      public boolean isOpaque() {
         Color back = getBackground();
         Component p = getParent();
         if (p != null)
            p = p.getParent();

         // p should now be the JTable. 
         boolean colorMatch = (back != null) && (p != null) &&
            back.equals( p.getBackground() ) && p.isOpaque();
         return !colorMatch;  // note difference from DefaultTableCellRenderer
      }
      public void invalidate() {}
      public void validate() {}
      public void revalidate() {}
      public void repaint( long tm, int x, int y, int width, int height ) {}
      public void repaint( Rectangle r ) {}
      public void repaint() {}

   }  // end BooleanRenderer member class

   /**
    * A renderer for date/time cells.
    */
   public static class DateTimeRenderer
         extends RDSTableCellRenderer {
      protected SimpleDateFormat formatter;

      public DateTimeRenderer() {
         this( null );
      }

      public DateTimeRenderer( String pattern ) {
         super();
         defaultAlignment = CENTER;

         if (pattern != null)
            formatter = new SimpleDateFormat( pattern );
         else
            formatter = new SimpleDateFormat();
      }

      public void setFormat( String pattern ) {
         formatter.applyPattern( pattern );
      }

      @Override
      public void setValue( Object value ) {
         setText( (value == null) ? "" : formatter.format( value ) );
      }
   }  // end DateRenderer member class

   /**
    * A renderer for icons.
    */
   public static class IconRenderer
         extends RDSTableCellRenderer {
      HashMap<String, Icon> iconMap;

      public IconRenderer() {
         super();
         defaultAlignment = CENTER;
      }

      public void mapIcon( String key, Icon value ) {
         if (iconMap == null)
            iconMap = new HashMap< String, Icon >();
         iconMap.put( key, value );
      }

      @Override
      public void setValue( Object value ) {
         Icon iconVal = null;

         if (value != null && value instanceof Icon)
            iconVal = (Icon)value;
         else if (iconMap != null) {
            if (value != null)
               iconVal = iconMap.get( value.toString() );
            if (iconVal == null)
               iconVal = iconMap.get( "default" );
         }

         setIcon( iconVal );
      }
   }  // end IconRenderer member class

   /**
    * A renderer for buttons; use with {@code ButtonEditor} to
    * provide clickable buttons with (optional) actions on click.
    */
   public static class ButtonRenderer
         extends JButton
         implements TableCellRenderer {

      public ButtonRenderer() {
         super();
         setHorizontalAlignment( CENTER );
         setFocusable( false );
      }

      @Override
      public Component getTableCellRendererComponent( JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column ) {
         if (isSelected) {
            setForeground( table.getSelectionForeground() );
            setBackground( table.getSelectionBackground() );
         } else {
            setForeground( table.getForeground() );
            Color c = table.getBackground();
            if (table instanceof RDSTable &&
                  ((RDSTable)table).isAlternateRow( row ))
               c = getAlternateColor( c );
            setBackground( c );
         }

         setValue( value );

         return this;
      }

      protected void setValue( Object value ) {
         setText( (value == null) ? "" : value.toString() );
      }

      // override for performance (see DefaultTableCellRenderer)

      public boolean isOpaque() {
         Color back = getBackground();
         Component p = getParent();
         if (p != null)
            p = p.getParent();

         // p should now be the JTable. 
         boolean colorMatch = (back != null) && (p != null) &&
            back.equals( p.getBackground() ) && p.isOpaque();
         return !colorMatch;  // note difference from DefaultTableCellRenderer
      }
      public void invalidate() {}
      public void validate() {}
      public void revalidate() {}
      public void repaint( long tm, int x, int y, int width, int height ) {}
      public void repaint( Rectangle r ) {}
      public void repaint() {}

   }  // end ButtonRenderer member class

   /** A cell editor for columns rendered using a {@code ButtonRenderer}. */
   public static class ButtonEditor
         extends AbstractCellEditor
         implements TableCellEditor {
      protected JButton button;

      public ButtonEditor() {
         button = new JButton();
         button.setFocusable( false );

         MouseAdapter adapter = new MouseAdapter() {
            public void mouseReleased( MouseEvent evt ) {
               cancelCellEditing();
            }
            public void mouseExited( MouseEvent evt ) {
               cancelCellEditing();
            }
         };
         button.addMouseListener( adapter );
         button.addMouseMotionListener( adapter );
      }

      @Override
      public Component getTableCellEditorComponent( JTable table,
            Object value, boolean isSelected, int row, int column ) {
         if (isSelected) {
            button.setForeground( table.getSelectionForeground() );
            button.setBackground( table.getSelectionBackground() );
         } else {
            button.setForeground( table.getForeground() );
            Color c = table.getBackground();
            if (table instanceof RDSTable &&
                  ((RDSTable)table).isAlternateRow( row ))
               c = getAlternateColor( c );
            button.setBackground( c );
         }

         setValue( value );

         return button;
      }

      protected void setValue( Object value ) {
         button.setText( (value == null) ? "" : value.toString() );
      }

      @Override
      public boolean isCellEditable( EventObject evt ) {
         return (evt instanceof MouseEvent &&
               ((MouseEvent)evt).getClickCount() >= 1);
      };

      @Override
      public Object getCellEditorValue() {
         return button.getText();
      }
   }  // end ButtonEditor member class


}  // end RDSTableCellRenderer class
