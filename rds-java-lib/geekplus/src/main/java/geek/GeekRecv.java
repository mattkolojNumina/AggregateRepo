package geek ;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Request;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.service.LogService ;

public class 
GeekRecv 
extends Application 
  {
  public static final int port = 8111 ;

  public
  GeekRecv()
    {
    }

  @Override
  public Restlet 
  createInboundRoot() 
    {
    // Create a root router
    Router router = new Router(getContext());
 
    // Attach the handlers to the root router
    router.attach("/ptocon",PTOCON.class);
    router.attach("/pkobcn",PKOBCN.class);
    router.attach("/pkocbo",PKOCBO.class);
    router.attach("/sccf",  SCCF.class) ;
    router.attach("/sdacf", SDACF.class) ;
    router.attach("/isvaf", ISVAF.class) ;
    router.attach("/invsr", INVSR.class) ;
    router.attach("/pkocbo",PKOCBO.class) ;
    router.attach("/pkocbc",PKOCBC.class) ;
    router.attach("/mcm",   MCM.class) ;

    // Return the root router
    return router;
    }

  // logging
  public static class
  NullLogService
  extends LogService
    {
    @Override
    public boolean isLoggable(Request request)
      {
      return false ;
      }
    }

  // main
  public static void 
  main(String[] args) 
  throws Exception 
    {
    // Create a component
    Component component = new Component();
    component.getServers().add(Protocol.HTTP, port);

    // Create an application
    Application application = new GeekRecv();

    // Attach the application to the component and start it
    component.getDefaultHost().attach(application);
    component.setLogService(new NullLogService()) ;
    component.start();
    }

  }

