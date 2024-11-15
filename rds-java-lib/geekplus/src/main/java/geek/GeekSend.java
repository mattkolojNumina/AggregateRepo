package geek ;

import rds.* ;

public class
GeekSend
  {
  private SKSYN sksyn ;
  private URSYN ursyn ;
  private PTOCRE ptocre ;
  private PTOCNL ptocnl ;
  private PKOCRE pkocre ;
  private PKOCNL pkocnl ;
  private SCCR   sccr ;
  private SDACR  sdacr ;
  private CPWRO  cpwro ;
  private SNSYN  snsyn ;
  private SKRIQ  skriq ;
  private DMR    dmr   ;
  private IOM    iom   ;

  public
  GeekSend()
    {
     sksyn = new SKSYN() ;
     ursyn = new URSYN() ;
     ptocre = new PTOCRE() ;
    //ptocre = new PTOCRE() ;
    // ptocnl = new PTOCNL() ;
     pkocre = new PKOCRE() ;
    // pkocnl = new PKOCNL() ;
    // sccr   = new SCCR() ;
    // sdacr  = new SDACR() ;
    // cpwro = new CPWRO() ;
     snsyn = new SNSYN() ;
    // skriq = new SKRIQ() ;
    // dmr   = new DMR() ;
    // iom   = new IOM() ;
    }

  private void
  poll()
    {
    while(true)
      {
       sksyn.cycle() ;
       ursyn.cycle() ;
       ptocre.cycle() ;
      // ptocnl.cycle() ;
       pkocre.cycle() ;
      // pkocnl.cycle() ;
      // sccr.cycle() ;
      // sdacr.cycle() ;
      // cpwro.cycle() ;
       snsyn.cycle() ;
      // skriq.cycle() ;
      // dmr.cycle() ;
      // iom.cycle() ;

      try { Thread.sleep(10*1000) ; }
      catch(Exception e) { e.printStackTrace() ; }
      }
    }

  public static void
  main(String[] args)
    {
    GeekSend app = new GeekSend() ;
    app.poll() ;
    }

  }
