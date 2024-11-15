package host;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import org.json.*;

import rds.*;

public class HostUpdate {
  RDSDatabase db = null;
  ArrayList<Field> fields;
  JSONObject object;
  String table;

  public HostUpdate(RDSDatabase db,
      String table,
      JSONObject object) {
    this.db = db;
    this.table = table;
    this.object = object;
    fields = new ArrayList<Field>();
  }

  class Field {
    public String type;
    public String element;
    public String column;
    public boolean key = false;
    public boolean literal = false;
    public String s_value;
    public int i_value;
    public long l_value;
    public float f_value;
    public boolean b_value;
    public byte[] byte_array;

    Field(String type, String element, String column) {
      this.type = type;
      this.element = element;
      this.column = column;
    }

    public Field(String type, String element) {
      this.type = type;
      this.element = element;
      this.column = column;
    }

  }

  // strings

  public void addString(String element, String column) {
    if (!object.isNull(element)) {
      Field f = new Field("string", element, column);
      fields.add(f);
    }
  }

  public void addString(String element) {
    addString(element, element);
  }

  public void addStringLiteral(String s_value, String column) {
    Field f = new Field("string", "", column);
    f.literal = true;
    f.s_value = s_value;
    fields.add(f);
  }

  public void addKeyString(String element, String column) {
    Field f = new Field("string", element, column);
    f.key = true;
    fields.add(f);
  }

  public void addKeyString(String element) {
    addKeyString(element, element);
  }

  public void addKeyStringLiteral(String s_value, String column) {
    Field f = new Field("string", "", column);
    f.literal = true;
    f.key = true;
    f.s_value = s_value;
    fields.add(f);
  }

  // ints

  public void addInt(String element, String column) {
    if (!object.isNull(element)) {
      Field f = new Field("int", element, column);
      fields.add(f);
    }
  }

  public void addInt(String element) {
    addInt(element, element);
  }

  public void addIntLiteral(int i_value, String column) {
    Field f = new Field("int", "", column);
    f.literal = true;
    f.i_value = i_value;
    fields.add(f);
  }

  public void addKeyInt(String element, String column) {
    Field f = new Field("int", element, column);
    f.key = true;
    fields.add(f);
  }

  public void addKeyInt(String element) {
    addKeyInt(element, element);
  }

  public void addKeyIntLiteral(int i_value, String column) {
    Field f = new Field("int", "", column);
    f.literal = true;
    f.key = true;
    f.i_value = i_value;
    fields.add(f);
  }

  // byte Array
  public void addByteArray(byte[] byte_array, String column) {
    Field f = new Field("byte", "", column);
    f.literal = true;
    f.byte_array = byte_array;
    fields.add(f);
  }

  // longDate

  public void addLongDate(String element, String column) {
    if (!object.isNull(element)) {
      Field f = new Field("longDate", element, column);
      fields.add(f);
    }
  }

  public void addLongDate(String element) {
    addLongDate(element, element);
  }

  // floats

  public void addFloat(String element, String column) {
    if (!object.isNull(element)) {
      Field f = new Field("float", element, column);
      fields.add(f);
    }
  }

  public void addFloat(String element) {
    addFloat(element, element);
  }

  public void addFloatLiteral(float f_value, String column) {
    Field f = new Field("float", "", column);
    f.literal = true;
    f.f_value = f_value;
    fields.add(f);
  }

  public void addKeyFloat(String element, String column) {
    Field f = new Field("float", element, column);
    f.key = true;
    fields.add(f);
  }

  public void addKeyFloat(String element) {
    addKeyFloat(element, element);
  }

  public void addKeyFloatLiteral(float f_value, String column) {
    Field f = new Field("float", "", column);
    f.literal = true;
    f.key = true;
    f.f_value = f_value;
    fields.add(f);
  }

  // boolInts

  public void addBoolInt(String element, String column) {
    if (!object.isNull(element)) {
      Field f = new Field("boolInt", element, column);
      fields.add(f);
    }
  }

  public void addBoolInt(String element) {
    addBoolInt(element, element);
  }

  public void addBoolIntLiteral(boolean b_value, String column) {
    Field f = new Field("boolInt", "", column);
    f.literal = true;
    f.b_value = b_value;
    fields.add(f);
  }

  public void addKeyBoolInt(String element, String column) {
    Field f = new Field("boolInt", element, column);
    f.key = true;
    fields.add(f);
  }

  public void addKeyBoolInt(String element) {
    addKeyBoolInt(element, element);
  }

  public void addKeyBoolIntLiteral(boolean b_value, String column) {
    Field f = new Field("boolInt", "", column);
    f.literal = true;
    f.key = true;
    f.b_value = b_value;
    fields.add(f);
  }

  public void list() {
    for (Field f : fields) {
      RDSLog.inform("field %s %s %s %s", f.type, f.element, f.column, f.key);
    }
  }

  public String execute() {
    String message = "";

    // find last update column
    String lastUpdate = "";
    for (Field f : fields) {
      if (!f.key) {
        lastUpdate = f.column;
      }
    }

    // return if no fields to update
    if (lastUpdate.equals(""))
      return "";

    // find first key column
    String firstKey = "";
    for (Field f : fields) {
      if (f.key) {
        firstKey = f.column;
        break;
      }
    }

    // build the sql
    String sql = "UPDATE " + table + " SET ";
    for (Field f : fields) {
      if (!f.key) {
        if (f.type.equals("longDate"))
          sql += f.column
              + "=DATE_ADD(FROM_UNIXTIME(0),INTERVAL ?/1000 SECOND) ";
        else
          sql += "`" + f.column + "`=?";
        if (f.column.equals(lastUpdate))
          sql += " ";
        else
          sql += ", ";
      }
    }
    for (Field f : fields) {
      if (f.key) {
        if (f.column.equals(firstKey))
          sql += "WHERE `" + f.column + "`=? ";
        else
          sql += "AND `" + f.column + "`=? ";
      }
    }

    try {
      int fn = 0;

      // create statement
      PreparedStatement pstmt = db.connect().prepareStatement(sql);

      // add update fields
      for (Field f : fields) {
        if (!f.key) {
          if (!f.literal) {
            if (f.type.equals("string"))
              pstmt.setString(++fn, object.getString(f.element));
            else if (f.type.equals("int"))
              pstmt.setInt(++fn, object.getInt(f.element));
            else if (f.type.equals("float"))
              pstmt.setFloat(++fn, object.getFloat(f.element));
            else if (f.type.equals("boolInt"))
              pstmt.setInt(++fn, object.getBoolean(f.element) ? 1 : 0);
            else if (f.type.equals("longDate"))
              pstmt.setLong(++fn, object.getLong(f.element));
          } else {
            if (f.type.equals("string"))
              pstmt.setString(++fn, f.s_value);
            else if (f.type.equals("int"))
              pstmt.setInt(++fn, f.i_value);
            else if (f.type.equals("float"))
              pstmt.setFloat(++fn, f.f_value);
            else if (f.type.equals("boolInt"))
              pstmt.setInt(++fn, f.b_value ? 1 : 0);
            else if (f.type.equals("byte"))
              pstmt.setBytes(++fn, f.byte_array);
          }
        }
      }

      // add key fields
      for (Field f : fields) {
        if (f.key) {
          if (!f.literal) {
            if (f.type.equals("string"))
              pstmt.setString(++fn, object.getString(f.element));
            else if (f.type.equals("int"))
              pstmt.setInt(++fn, object.getInt(f.element));
            else if (f.type.equals("float"))
              pstmt.setFloat(++fn, object.getFloat(f.element));
            else if (f.type.equals("boolInt"))
              pstmt.setInt(++fn, object.getBoolean(f.element) ? 1 : 0);
          } else {
            if (f.type.equals("string"))
              pstmt.setString(++fn, f.s_value);
            else if (f.type.equals("int"))
              pstmt.setInt(++fn, f.i_value);
            else if (f.type.equals("float"))
              pstmt.setFloat(++fn, f.f_value);
            else if (f.type.equals("boolInt"))
              pstmt.setInt(++fn, f.b_value ? 1 : 0);
          }
        }
      }

      // execute the update
      pstmt.executeUpdate();

      // close the statement
      pstmt.close();
    } catch (Exception e) {
      e.printStackTrace();
      message = e.getMessage();
    }

    return message;
  }
}
