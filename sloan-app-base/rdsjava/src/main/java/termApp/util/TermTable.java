package termApp.util;

import java.util.Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TermTable {
   protected int x0, y0,rowMargin,rowMax;
   private List<TermOrderedGroup> columns;
   
   
   /*
    * constructors
    */
   public TermTable( TermOrderedGroup object ) {
      this.columns = new ArrayList<TermOrderedGroup>();
      this.x0 = object.x0;
      this.y0 = object.y0;
      this.rowMargin = object.spacing;
      this.rowMax = object.maxSize;
      addColumn(0, object);
   }

   public TermTable(int x, int y, int spacing, int maxSize) {
      this.x0 = x;
      this.y0 = y;
      this.rowMargin = spacing;
      this.rowMax = maxSize;
      this.columns = new ArrayList<TermOrderedGroup>();
   }
   
/*   public boolean addHeader( List<String> headers ) {
      if (colCount() <= 0 || colCount() != headers.size())
         return false;
      
      int i = 0;
      for (TermColumn c : columns) { 
         c.updateElement(0, headers.get(i++));
      }
      return true;
   }*/
   
   public void refresh() {
      for ( TermOrderedGroup c : columns ) 
         c.refresh();
   }
   
   public void clear() {
      for ( TermOrderedGroup c : columns ) 
         c.clear();
   }
   
   public void hide() {
      for ( TermOrderedGroup c : columns ) 
         c.hide();
   }
   
   public void show() {
      for ( TermOrderedGroup c : columns ) 
         c.show();
   }
   
   public void shift(int x, int y) {
      for ( TermOrderedGroup c : columns ) 
         c.shift(x,y);
   }
   
   public void addColumn( int relPos, TermOrderedGroup column, boolean showNow ) {
      //TODO enforce same max size, orientation, and spacing
      column.move(x0+relPos, y0);
      column.setMaxSize(rowMax);
      //column.setMaxSize(rowMax);
      column.setSpacing(rowMargin);
      columns.add(column);
      if (showNow)
         show();
   }
   
   public void addColumn( int relPos, TermOrderedGroup column ) {
      addColumn(relPos, column, false);
   }
   
   public void rotate(int vSpace, int hSpace) {
      int i=0;
      for ( TermOrderedGroup c : columns ) {
         c.rotate(vSpace, hSpace);
         c.move(x0, y0 + i++ * vSpace);
      }
   }
   
   public int colCount() {
      return columns.size();
   }
   
   public int rowCount() {
      try {
         return getColumn(0).size();
      } catch (Exception ex) {
         return 0;
      }
   }
   
   public TermOrderedGroup getColumn( int j) {
      try {
         return columns.get(j);
      } catch (IndexOutOfBoundsException  ex) {
         return null;      
      }
   }
   
   public TermGroup getRow( int i) {
      try {
         TermGroup row = new TermGroup(getElement(i,0).getX(),getElement(i,0).getY());
         for (int j = 0; j<colCount(); j++) {
            row.add(getElement(i, j));
         }
         return row;
      } catch (NullPointerException | IndexOutOfBoundsException | IllegalArgumentException ex) {
         return null;      
      }
   }
   
   protected List<TermBaseObject> getColObj( int j) {
      try {
         return getColumn(j).getList();
      } catch (NullPointerException  ex) {
         return null;      
      }
   }
   
   protected List<TermBaseObject> getRowObj( int i) {
      try {
         return getRow(i).getList();
      } catch (NullPointerException  ex) {
         return null;      
      }
   }
   
   protected TermBaseObject getElement( int i, int j ) {
      try {
         return getColumn(j).getElement(i);
      } catch (Exception  ex) {
         return null;      
      }
   }

   public void setElement( int i, int j, String method, 
         String format, Object...  args ) {
      try {
         getElement(i,j).setStr(method, String.format(format, args));
      } catch (java.util.IllegalFormatException e) {
         TermUtility.error( "failed to format %s", format);
      } catch (NullPointerException  ex) { 
         TermUtility.error( "index [%d] out of bounds", i);
      }
   }
/*
   public void setColList( int j, int startAt, String method, List<String> list ) {
      if (list == null || list.isEmpty() )
         return;
      
      if (getColumn(j)== null )
         return;
      
      getColumn(j).setList(startAt, method, list);
   }
   
   public void setColList( int j, String method, List<String> list ) {
      if (list == null || list.isEmpty() )
         return;
      
      if (getColumn(j)== null )
         return;
      
      getColumn(j).setList(method, list);
   }
   
   public void setColList( int j, String method, String... list ) {
      try {
         setColList(j, method, Arrays.asList(list));
      } catch (NullPointerException ex) { }
   }
   
   public void setColMap( int j, int startAt, String method, Map<String,String> map,
         List<String> keys ) {
      if (map == null || keys == null )
         return;
      
      if (getColumn(j)== null )
         return;
      
      getColumn(j).setMap(startAt, method, map, keys);
   }
   
   public void setColMap( int j, String method, Map<String,String> map,
         List<String> keys ) {
      if (map == null || keys == null )
         return;
      
      if (getColumn(j)== null )
         return;
      
      getColumn(j).setMap(method, map, keys);
   }
   
   public void setColMap( int j, String method, Map<String,String> map,
         String... keys ) {
      try {
         setColMap(j, method, map, Arrays.asList(keys));
      } catch (NullPointerException ex) { }
   }
   */
   /*
   public void setRowList( int i, String method, List<String> list ) {
      if (list == null || list.isEmpty() )
         return;
      
      if (getRowObj(i)== null )
         return;
      
      int j = 0;
      while (j < colCount() ) {
         try {
            setElement(i,j,method,list.get(j++));
         } catch (IndexOutOfBoundsException  ex) {  
            setElement(i,j,method,"");
         }
      }
      // ignore remaining items in list
      
      refresh();
   }
   
   public void setRowList( int j, String method, String... list ) {
      try {
         setRowList(j, method, Arrays.asList(list));
      } catch (NullPointerException ex) { }
   }
   
   public void setRowMap( int i, String method, Map<String,String> map, List<String> keys ) {
      if (map == null || keys == null )
         return;
      
      if (getRowObj(i)== null )
         return;
      
      int j = 0;
      while (j < colCount() ) {
         try {
            setElement(i,j,method,map.get(keys.get(i)));
         } catch (IndexOutOfBoundsException  ex) {
            setElement(i,j,method,"");
         }
      }
      // ignore remaining keys in list
      
      refresh();
   }
   
   public void setRowMap( int i, String method, Map<String,String> map, 
         String... keys ) {
      try {
         setRowMap(i, method, map, Arrays.asList(keys));
      } catch (NullPointerException ex) { }
   }
   */
   public void setPair(int i, int j, String method, 
         String name, String value) {
      setElement( i,   j, method, name );
      setElement( i, j+1, method, value );
   }
   
   public void setPair(int i, int j, String method,
         Map<String, String> m, String key, String name) {
      if ( m == null || key == null )
         return;
      setPair(i, j, method, name, getMapStr(m, key) );
   }
   
   public void setPair( int i, int j, String method,
         Map<String,String> m, String key ) {
      setPair(i, j, method, m, key, key);
   }
   
   public void setListMapCol( String method, List<Map<String,String>> list, 
         int fromTableRow, int fromListIndex, String... colKeys) {
      setListMapCol(method, list, fromTableRow, fromListIndex, Arrays.asList(colKeys));
   }
   
   public void setListMapCol( String method, List<Map<String,String>> list, 
         int fromTableRow, int fromListIndex, List<String> colKeys) {
      //TODO verify
      if( fromTableRow<0 || fromTableRow >=rowCount() )
         return;
      
      List<Map<String,String>> setList = null;
      int setRows = rowCount() - fromTableRow;
      try {
         setList = list.subList(fromListIndex, list.size());
         setList = setList.subList(0, Math.min(setList.size(), setRows ));
      } catch (IllegalArgumentException | IndexOutOfBoundsException
            | NullPointerException ex) {
         return;
      }
      
      if ( setList.isEmpty() )
         return;
      
      for ( int j = 0; j < colCount(); j++)
         for ( int i = 0; i < rowCount() - fromTableRow; i++ )
            try {
               String key = colKeys.get(j);
               String text = getMapStr(setList.get(i), key);
               setElement(i+fromTableRow,j,method,text);
            } catch (IndexOutOfBoundsException  ex) { 
               setElement(i+fromTableRow,j,method,"");
            }

      refresh();
   }
   
   public void setListMapRow( String method, List<Map<String,String>> list, 
         int fromTableCol, int fromListIndex, List<String> colKeys) {
      if( fromTableCol<0 || fromTableCol >=colCount() )
         return;
      
      List<Map<String,String>> setList = null;
      int setCols = colCount() - fromTableCol;
      try {
         setList = list.subList(fromListIndex, list.size());
         setList = setList.subList(0, Math.min(setList.size(), setCols ));
      } catch (IllegalArgumentException | IndexOutOfBoundsException
            | NullPointerException ex) {
         return;
      }
      
      if ( setList.isEmpty() )
         return;
      
      for ( int j = 0; j < colCount() - fromTableCol; j++)
         for ( int i = 0; i < rowCount(); i++ )
            try {
               String key = colKeys.get(i);
               String text = getMapStr(setList.get(j), key);
               setElement(i,j+fromTableCol,method,text);
            } catch (IndexOutOfBoundsException  ex) { 
               setElement(i,j+fromTableCol,method,"");
            }

      refresh();
   }
   
   protected String getMapStr( Map<String,String> m, String name ) {
      if (m == null)
         return "";
      String val = m.get( name );
      return (val == null) ? "" : val;
   }
   
   protected Map<String,String> getListMap( List<Map<String,String>> l, int index ) {
      //TODO remove
      if (l == null)
         return null;
      if (index < 0 || index >= l.size() )
         return null;
      return l.get(index);
   }

   
}