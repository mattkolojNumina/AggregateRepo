/*
 * RDSTable.java
 * 
 * (c) 2007 Numina Group, Inc.
 */

package rds;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.sql.*;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.table.*;

import rds.RDSDatabase;


/**
 * A utility class that provides methods to create and configure a
 * {@code JTable} embedded in a {@code JScrollPane} for use in RDS applets.  
 * Typically, a database object is passed to the constructor to allow
 * population of the table with results from a query.
 */
public class RDSTable
      extends JTable {
   // class constants
   private static final Color DEFAULT_GRID_COLOR = Color.LIGHT_GRAY;

   /** The RDS Database associated with this table */
   private RDSDatabase db;

   /** The enclosing scroll pane of this table */
   private JScrollPane scrollPane;

   // maintain column status variables, indexed in the TableModel's
   // column order
   private Class<?>[] columnClasses;
   private int[]      columnAlignments;
   private boolean[]  columnEditableFlags;
   private String[]   columnToolTips;

   /** The size of blocks of rows designated as 'alternate' rows */
   private int alternateBlockSize;

   /*
    * --- constructors ---
    */

   /**
    * Constructs an RDS table with no associated database.
    * 
    * @param   columnNames  list of column identifiers
    */
   public RDSTable( String... columnNames ) {
      this( null, columnNames );
   }

   /**
    * Constructs an RDS table with a database and specified column names.
    * 
    * @param   db           the underlying database for the table
    * @param   columnNames  list of column identifiers
    */
   public RDSTable( RDSDatabase db, String... columnNames ) {
      this( columnNames, db );
   }

   /**
    * Constructs an RDS table with an associated database.
    * 
    * @param   columnNames  list of column identifiers
    * @param   db           database
    * @deprecated  as of July, 2007,
    *          replaced by {@code RDSTable( RDSDatabase, String... )}
    */
   @Deprecated
   public RDSTable( String[] columnNames, RDSDatabase db ) {
      super();
      this.db = db;

      // initialize column status variables
      this.columnClasses       = new Class<?>[ columnNames.length ];
      this.columnAlignments    = new int[      columnNames.length ];
      this.columnEditableFlags = new boolean[  columnNames.length ];
      this.columnToolTips      = new String[   columnNames.length ];
      for (int i = 0; i < columnNames.length; i++) {
         columnClasses[i]       = null;
         columnAlignments[i]    = RDSTableCellRenderer.NO_ALIGNMENT;
         columnEditableFlags[i] = false;
         columnToolTips[i]      = null;
      }

      this.alternateBlockSize = 1;

      // create and assign table model
      DefaultTableModel model = new DefaultTableModel( columnNames, 0 ) {
         @Override
         public boolean isCellEditable( int row, int column ) {
            return columnEditableFlags[ column ];
         }
         @Override
         public Class<?> getColumnClass( int columnIndex ) {
            return (columnClasses[ columnIndex ] == null) ? Object.class :
                  columnClasses[ columnIndex ];
         }
      };
      setModel( model );

      // other ui settings
      setGridColor( DEFAULT_GRID_COLOR );
   }


   /*
    * --- access methods ---
    */

   /**
    * Gets the table itself.  This method is provided for backward
    * compatibility as the previous version of {@code RDSTable}
    * did not extend {@code JTable}.
    * 
    * @return  the table object ({@code this})
    * @deprecated
    */
   public JTable getTable() {
      return this;
   }

   /**
    * Gets the scroll pane containing the table.  The enclosing scroll
    * pane is lazily created.
    * 
    * @return  the scroll pane
    */
   public JScrollPane getScrollPane() {
      if (scrollPane == null)
         scrollPane = new JScrollPane( this );

      return scrollPane;
   }

   /**
    * Sets the underlying database for this table.
    * 
    * @param   db  the {@code RDSDatabase} used for table-population queries
    */
   public void setDatabase( RDSDatabase db ) {
      this.db = db;
   }

   /**
    * Returns the cell value appearing in the view at {@code row}
    * in the named column.
    *
    * @param   row         the row whose value is to be queried
    * @param   columnName  the column whose value is to be queried
    * @return  the value of the specified cell
    */
   public Object getValueAt( int row, String columnName ) {
      return getModel().getValueAt(
            convertRowIndexToModel( row ),
            getColumn( columnName ).getModelIndex() );
   }

   /**
    * Sets the value of the cell appearing in the view at {@code row} in
    * the named column.
    * 
    * @param   value       the value to set
    * @param   row         the row, indexed in the current view
    * @param   columnName  the name of the column
    */
   public void setValueAt( Object value, int row, String columnName ) {
      getModel().setValueAt( value,
            convertRowIndexToModel( row ),
            getColumn( columnName ).getModelIndex() );
   }

   /**
    * Adds a row to the end of the table model.
    * 
    * @param   rowData  the data to add
    */
   public void addRow( Object... rowData ) {
      ((DefaultTableModel)getModel()).addRow( rowData );
   }

   /**
    * Removes a row from the table model.
    * 
    * @param   row  the index of the row to remove
    */
   public void removeRow( int row ) {
      ((DefaultTableModel)getModel()).removeRow( row );
   }

   /**
    * Removes all data from the table.
    */
   public void removeAllRows() {
      ((DefaultTableModel)getModel()).setRowCount( 0 );
   }

   /**
    * Sets the contents of a row in the table.  The row and the items in
    * the data array are both indexed according to the underlying table
    * model.
    * 
    * @param   row      the index of the row
    * @param   rowData  the data to add
    */
   public void setRowData( int row, Object... rowData ) {
      int numColumns = getModel().getColumnCount();
      for (int i = 0; i < numColumns && i < rowData.length; i++)
         getModel().setValueAt( rowData[i], row, i );
      for (int i = rowData.length; i < numColumns; i++)
         getModel().setValueAt( null, row, i );
   }

   /**
    * Sets the contents of the named column; data in other columns is not
    * affected.  The number of rows in the table will be equal to the length
    * of the data array.  Note that the items in the data array are
    * indexed according to the underlying table model.
    * 
    * @param   columnName  the name of the column
    * @param   columnData  an array of cell data
    */
   public void setColumnData( String columnName, Object... columnData ) {
      int index = getColumn( columnName ).getModelIndex();
      ((DefaultTableModel)getModel()).setRowCount( columnData.length );
      for (int i = 0; i < columnData.length; i++)
         getModel().setValueAt( columnData[i], i, index );
   }

   /**
    * Sets the class for the named column for rendering and sorting
    * purposes.
    * 
    * @param   columnName   the name of the column
    * @param   columnClass  the column class
    */
   public void setColumnClass( String columnName, Class<?> columnClass ) {
      try {
         int column = getColumnModel().getColumnIndex( columnName );
         setColumnClass( column, columnClass );
      } catch (IllegalArgumentException ex) {
         RDSUtil.alert(
               "unable to set column class: identifier [%s] not found",
               columnName );
         RDSUtil.alert( ex );
      }
   }

   /**
    * Sets the class for the specified column for rendering and sorting
    * purposes.  The column is indexed in the current view.
    * 
    * @param   column       the column index
    * @param   columnClass  the column class
    */
   public void setColumnClass( int column, Class<?> columnClass ) {
      columnClasses[ convertColumnIndexToModel( column ) ] = columnClass;
   }

   /**
    * Sets each unspecified column class to the class of the current value
    * in that column from the first row of the underlying table model.  If
    * the table is empty, this method performs no action.
    */
   public void updateColumnClasses() {
      if (getModel().getRowCount() == 0)
         return;

      for (int i = 0, n = getColumnCount(); i < n; i++)
         if (columnClasses[i] == null) {
            Object obj = getModel().getValueAt( 0, i );
            if (obj != null)
               columnClasses[i] = obj.getClass();
         }
   }

   /**
    * Gets the horizontal alignment assigned to the specified column,
    * specified in the view order.
    * 
    * @param  column  the column
    */
   public int getColumnAlignment( int column ) {
      return columnAlignments[ convertColumnIndexToModel( column ) ];
   }

   /**
    * Adjusts the text alignment of a named column.
    * 
    * @param   columnName           the name of the column
    * @param   horizontalAlignment  {@code SwingConstants.LEFT},
    *          {@code RIGHT}, or {@code CENTER}
    */
   public void setColumnAlignment( String columnName,
         int horizontalAlignment ) {
      try {
         int column = getColumnModel().getColumnIndex( columnName );
         setColumnAlignment( column, horizontalAlignment );
      } catch (IllegalArgumentException ex) {
         RDSUtil.alert(
               "unable to set column alignment: identifier [%s] not found",
               columnName );
         RDSUtil.alert( ex );
      }
   }

   /**
    * Adjusts the text alignment of a column, indexed in the view order.
    * 
    * @param   column               the column number (zero-indexed)
    * @param   horizontalAlignment  {@code SwingConstants.LEFT},
    *          {@code RIGHT}, or {@code CENTER}
    */
   public void setColumnAlignment( int column, int horizontalAlignment ) {
      columnAlignments[ convertColumnIndexToModel( column ) ] =
         horizontalAlignment;
   }

   /**
    * Sets the minimum, maximum, and preferred widths for a named table
    * column.  A width value less than zero is ignored; use such values
    * to indicate that the setting should be left at its current value.
    * 
    * @param   columnName      the name of the column
    * @param   minWidth        the minimum column width
    * @param   preferredWidth  the preferred column width
    * @param   maxWidth        the maximum column width
    */
   public void setColumnWidths( String columnName, int minWidth,
         int preferredWidth, int maxWidth) {
      TableColumn col = getColumn( columnName );
      if (minWidth >= 0)
         col.setMinWidth( minWidth );
      if (preferredWidth >= 0)
         col.setPreferredWidth( preferredWidth );
      if (maxWidth >= 0)
         col.setMaxWidth( maxWidth );
   }

   /**
    * Sets the minimum, maximum, and preferred widths for a table column,
    * indexed in the view.  A width value less than zero is ignored; use
    * such values to indicate that the setting should be left at its current
    * value.
    * 
    * @param   column          the column index
    * @param   minWidth        the minimum column width
    * @param   preferredWidth  the preferred column width
    * @param   maxWidth        the maximum column width
    */
   public void setColumnWidths( int column, int minWidth,
         int preferredWidth, int maxWidth) {
      setColumnWidths( getColumnName( column ), minWidth,
            preferredWidth, maxWidth );
   }

   /**
    * Sets whether or not the named column is editable.
    * 
    * @param   columnName  the name of the column
    * @param   editable    {@code true} if the column should be editable,
    *          {@code false} otherwise
    */
   public void setColumnEditable( String columnName, boolean editable ) {
      try {
         int column = getColumnModel().getColumnIndex( columnName );
         setColumnEditable( column, editable );
      } catch (IllegalArgumentException ex) {
         RDSUtil.alert(
               "unable to set column editable: identifier [%s] not found",
               columnName );
         RDSUtil.alert( ex );
      }
   }

   /**
    * Sets whether or not the indicated column is editable.  The column
    * index is specified with respect to the view order.
    * 
    * @param   column    the column index
    * @param   editable  {@code true} if the column should be editable,
    *          {@code false} otherwise
    */
   public void setColumnEditable( int column, boolean editable ) {
      columnEditableFlags[ convertColumnIndexToModel( column ) ] = editable;
   }

   /**
    * Sets whether or not the table is editable.
    * 
    * @param   editable  {@code true} if the table should be editable,
    *          {@code false} otherwise
    */
   public void setEditable( boolean editable ) {
      for (int i = 0, n = getColumnCount(); i < n; i++)
         columnEditableFlags[ i ] = editable;
   }
 
   /**
    * Sets all of the column tool tips.  The number of tool tips specified
    * must be equal to the number of columns in the table.
    * 
    * @param   toolTips  the tool tips
    */
   public void setColumnToolTips( String... toolTips ) {
      if (toolTips.length != getColumnCount()) {
         RDSUtil.alert( "incorrect number of column tool tips specified" );
         return;
      }

      columnToolTips = toolTips;
   }

   /**
    * Sets the tooltip text for a named column header.
    * 
    * @param   columnName   the name of the column
    * @param   toolTipText  the tooltip text
    */
   public void setColumnHeaderToolTip( String columnName,
         String toolTipText ) {
      try {
         int column = getColumnModel().getColumnIndex( columnName );
         setColumnHeaderToolTip( column, toolTipText );
      } catch (IllegalArgumentException ex) {
         RDSUtil.alert(
               "unable to set header tooltip: identifier [%s] not found",
               columnName);
         RDSUtil.alert( ex );
      }
   }

   /**
    * Sets the tooltip text for a column header.  The column index is
    * specified with respect to the view order.
    * 
    * @param   column       the column index
    * @param   toolTipText  the tooltip text
    */
   public void setColumnHeaderToolTip( int column, String toolTipText ) {
      columnToolTips[ convertColumnIndexToModel( column ) ] = toolTipText;
   }

   /**
    * Sets the size of blocks of rows designated as 'alternate' rows,
    * typically for shading purposes by the table cell renderers.  Preferable
    * values for this parameter are 1 (for standard tables) or 3 (for
    * especially dense or otherwise difficult-to-scan tables).  A value
    * less than or equal to zero will cause no row to be considered alternate,
    * thus disabling the default mechanism for table shading.
    * 
    * @param   alternateBlockSize  the new value for the block size
    */
   public void setAlternateBlockSize( int alternateBlockSize ) {
      this.alternateBlockSize = alternateBlockSize;
   }

   /**
    * Determines whether the specified row should be designated an
    * 'alternate' row.  This method is typically used by a cell renderer
    * to shade rows for improved readability.
    * 
    * @param   row  the row number
    * @return  {@code true} if the row is considered an alternate row,
    *          {@code false} otherwise
    */
   public boolean isAlternateRow( int row ) {
      if (alternateBlockSize > 0 && row / alternateBlockSize % 2 == 1)
         return true;
      return false;
   }

   /**
    * Fills the table with data obtained via the provided sql query.  The
    * number of rows in the table will be equal to the number of rows
    * returned by the query.
    * 
    * @param   query  the SQL query used to generate table data
    * @throws  {@code SQLException}  if a database error occurs
    */
   public void populateTable( String query )
         throws SQLException {
      if (db == null || query == null || query.isEmpty())
         return;

      int numColumns = getColumnCount();
      int row = 0;

      Statement stmt = null;
      try {
         stmt = db.connect().createStatement();
         ResultSet res = db.executeTimedQuery( stmt, query );
   
         while (res.next()) {
            Object[] data = new Object[ numColumns ];
            for (int i = 0; i < numColumns; i++)
               data[i] = res.getObject( i + 1 );
   
            if (row < getRowCount())
               setRowData( row, data );
            else
               addRow( data );
            row++;
         }
      } catch (SQLException ex) {
         throw ex;
      } finally {
         RDSDatabase.closeQuietly( stmt );
      }

      ((DefaultTableModel)getModel()).setRowCount( row );
      updateColumnClasses();
   }

   /**
    * Invokes the table print method with the specified header and footer
    * text.
    * 
    * @param   header  header string
    * @param   footer  footer string
    * @return  {@code true}, unless printing is cancelled by the user
    * @throws  java.awt.print.PrinterException  if an error in the print
    *          system causes the job to be aborted
    */
   public boolean print( String header, String footer )
         throws PrinterException {
      MessageFormat headerFormat = (header == null) ? null :
            new MessageFormat( header );
      MessageFormat footerFormat = (footer == null) ? null :
            new MessageFormat( footer );
      return print( PrintMode.FIT_WIDTH, headerFormat, footerFormat );
   }


   /*
    * --- JTable method overrides ---
    */

   /**
    * Returns the default table header object, which is a
    * {@code JTableHeader}.  This method overrides the standard
    * {@code JTable} method to provide for column-header tooltips that
    * are specified in a local array.
    *
    * @return  the default table header object
    */
   @Override
   protected JTableHeader createDefaultTableHeader() {
      return new JTableHeader( columnModel ) {
         @Override
         public String getToolTipText( MouseEvent evt ) {
            String tip = null;

            int index = columnModel.getColumnIndexAtX(
                  (int)evt.getPoint().getX() );
            if (index >= 0)
               tip = columnToolTips[
                     columnModel.getColumn( index ).getModelIndex() ];

            if (tip == null)
               tip = super.getToolTipText( evt );

            return tip;
         }
      };
   }


   /**
    * Creates default cell renderers for common classes.  This method
    * overrides all of the default renderers specified in the
    * {@code createDefaultRenderers} method of {@code JTable}.
    */
   @Override
   protected void createDefaultRenderers() {
      super.createDefaultRenderers();

      // strings and other objects
      setDefaultRenderer( Object.class,
            new RDSTableCellRenderer() );

      // numbers
      setDefaultRenderer( Number.class,
            new RDSTableCellRenderer.NumberRenderer() );
      setDefaultRenderer( Float.class,
            new RDSTableCellRenderer.DoubleRenderer() );
      setDefaultRenderer( Double.class,
            new RDSTableCellRenderer.DoubleRenderer() );

      // booleans
      setDefaultRenderer( Boolean.class,
            new RDSTableCellRenderer.BooleanRenderer() );

      // date/time formats
      setDefaultRenderer( java.util.Date.class,
            new RDSTableCellRenderer() );
      setDefaultRenderer( java.sql.Timestamp.class,
            new RDSTableCellRenderer.DateTimeRenderer(
                  "yyyy-MM-dd HH:mm:ss" ) );

      // icons
      setDefaultRenderer( Icon.class,
            new RDSTableCellRenderer.IconRenderer() );
      setDefaultRenderer( ImageIcon.class,
            new RDSTableCellRenderer.IconRenderer() );
   }

}  // end RDSTable class