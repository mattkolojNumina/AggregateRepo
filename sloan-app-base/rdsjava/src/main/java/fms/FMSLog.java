package fms ;

import java.sql.PreparedStatement ;

import rds.* ;

public class
FMSLog
  {
  public String trans = "" ;
  public int transSeq = -1 ;
  public boolean logging = true ;
  RDSDatabase db = null ;

  public
  FMSLog(String trans)
    {
    this.trans = trans ;
    db = new RDSDatabase("db") ;
    }

  public int 
  start()
    {
    if(logging)
      {
      db.execute("INSERT INTO fmsLog "
                +"(trans) "
                +"VALUES "
                +"('"+trans+"') ") ;
      transSeq = db.getSequence() ;
      }
    return transSeq ;
    }

  public int
  url(String url)
    {
    if(logging)
      {
      try
        {
        String prep = "UPDATE fmsLog SET url=? "
                    + "WHERE seq=? " ;
        PreparedStatement pstmt 
          = db.connect().prepareStatement(prep) ;
        pstmt.setString(1,url) ;
        pstmt.setInt(2,transSeq) ;
        pstmt.executeUpdate() ;
        pstmt.close() ;
        }
      catch(Exception e) { e.printStackTrace() ; } ;
      }
    return transSeq ;
    }

  public int 
  request(String request)
    {
    if(logging)
      {
      try
        {
        String prep = "UPDATE fmsLog SET request=? "
                    + "WHERE seq=? " ;
        PreparedStatement pstmt 
          = db.connect().prepareStatement(prep) ;
        pstmt.setString(1,request) ;
        pstmt.setInt(2,transSeq) ;
        pstmt.executeUpdate() ;
        pstmt.close() ;
        }
      catch(Exception e) { e.printStackTrace() ; } ;
      }
    return transSeq ;
    }

  public int 
  response(String response)
    {
    if(logging)
      {
      try
        {
        String prep = "UPDATE fmsLog SET response=? "
                    + "WHERE seq=? " ;
        PreparedStatement pstmt 
          = db.connect().prepareStatement(prep) ;
        pstmt.setString(1,response) ;
        pstmt.setInt(2,transSeq) ;
        pstmt.executeUpdate() ;
        pstmt.close() ;
        }
      catch(Exception e) { e.printStackTrace() ; } ;
      }
    return transSeq ;
    }
  }

