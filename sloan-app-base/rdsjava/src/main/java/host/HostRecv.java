package host;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Request;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.service.LogService;

public class HostRecv
    extends Application {
  public static final int port = 8888; // make a control param

  public HostRecv() {
  }

  @Override
  public Restlet createInboundRoot() {
    // Create a root router
    Router router = new Router(getContext());

    // TO DO - avoid creating new DB connections

    // Attach the handlers to the root router
    router.attach("/sendShipment", SendShipment.class); // make a control param
    // router.attach("/sendSku", SendSku.class); // make a control param
    // router.attach("/sendLocations", SendLocations.class); // make a control param
    // router.attach("/sendDocument", SendDocument.class); // make a control param

    // Return the root router
    return router;
  }

  // logging
  public static class NullLogService
      extends LogService {
    @Override
    public boolean isLoggable(Request request) {
      return false;
    }
  }

  // main
  public static void main(String[] args)
      throws Exception {
    // Create a component
    Component component = new Component();
    component.getServers().add(Protocol.HTTP, port);

    // Create an application
    Application application = new HostRecv();

    // Attach the application to the component and start it
    component.getDefaultHost().attach(application);
    component.setLogService(new NullLogService());
    component.start();
  }

}
