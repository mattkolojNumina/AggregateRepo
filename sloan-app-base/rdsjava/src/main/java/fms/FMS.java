package fms ;

import java.util.Date ;
import java.util.Calendar ;
import java.text.SimpleDateFormat ;

import rds.* ;

public class
FMS
  {
  RDSDatabase db ;
  public
  FMS()
    {
    db = new RDSDatabase("db") ;

    }

  private String
  deadline()
    {
    Date now = new Date() ;
    Calendar cal = Calendar.getInstance() ;
    cal.setTime(now) ;
    cal.add(Calendar.YEAR,1) ;
    Date target = cal.getTime() ;

    String pattern = "yyyy-MM-dd HH:mm:ss" ;
    SimpleDateFormat sdf = new SimpleDateFormat(pattern) ;
    return sdf.format(target) ; 
    }

  private void
  push(String[] args)
    {
    if(args.length < 3)
      {
      System.out.println("usage: fms push <taskId> <location> (<taskType>)") ;
      return ;
      }
    String taskId = args[1] ;
    String sourceLocation = args[2] ;
    String taskType = "MOVE" ;
    if(args.length >=4)
      taskType = args[3] ;
    String deadline = deadline() ;

System.out.println(deadline) ;     
    }

  public static void
  main(String[] args)
    {
    FMS app = new FMS() ;

    if(args.length==0)
      {
      System.out.println("commands: push ") ;
      return ;
      }
    else if(args[0].toLowerCase().equals("push"))
      app.push(args) ;
    else
      System.out.println("unknown command "+args[0]) ;

    }

  }
