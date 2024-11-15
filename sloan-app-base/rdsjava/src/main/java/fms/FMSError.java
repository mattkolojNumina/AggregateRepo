package fms ;

import java.sql.PreparedStatement ;

import rds.* ;

public class
FMSError
  {
  public String trans = "" ;
  public int transSeq = -1 ;
  public boolean logging = true ;
  RDSDatabase db = null ;

  public
  FMSError(String trans)
    {
    this.trans = trans ;
    db = new RDSDatabase("db") ;
    }

  public int 
  log(String id, String status, String description,int logSeq)
    {
    if(logging)
      {
      String sql = "INSERT INTO fmsError "
                 +"(trans,id,status,description,logSeq) "
                 +"VALUES "
                 +"(?,?,?,?,?) " ;
      db.executePreparedStatement(sql,trans,id,status,description,""+logSeq) ;
      transSeq = db.getSequence() ;
      }
    return transSeq ;
    }

  }

