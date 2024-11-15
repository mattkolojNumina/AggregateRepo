package geek ;

import java.sql.PreparedStatement ;

import rds.* ;

public class
GeekLog
  {
  public String trans = "" ;
  public int transSeq = -1 ;
  public boolean logging = true ;
  RDSDatabase db = null ;

  public
  GeekLog(String trans)
    {
    this.trans = trans ;
    db = new RDSDatabase("db") ;
    }

  public int 
  start()
    {
    if(logging)
      {
      db.execute("INSERT INTO geekLog "
                +"(trans) "
                +"VALUES "
                +"('"+trans+"') ") ;
      transSeq = db.getSequence() ;
      }
    return transSeq ;
    }

  public void
  url(String url)
    {
    if(logging)
      {
      try
        {
        String prep = "UPDATE geekLog SET url=? "
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
    }

  public void
  request(String request)
    {
    if(logging)
      {
      try
        {
        String prep = "UPDATE geekLog SET request=? "
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
    }

  public void
  response(String response)
    {
    if(logging)
      {
      try
        {
        
        String prep = "UPDATE geekLog SET response=? "
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
    }
  }

