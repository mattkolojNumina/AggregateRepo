package host.geek;

import dao.AbstractDAO;
import polling.AbstractPollingApp;
import rds.RDSEvent;

import static rds.RDSLog.alert;
import static rds.RDSLog.trace;

public class GeekProcess extends AbstractPollingApp {

    static String appName = "";
    GeekTranslator geekTranslator;

    public GeekProcess(String id, String rdsDb) {
        super(id, rdsDb);
        AbstractDAO.setDatabase(db);
        geekTranslator = GeekTranslatorFactory.getGeekTranslatorImpl(appName);
    }


    @Override
    protected void poll() {
        try {
      	  geekTranslator.moveFromGeek();
      	  geekTranslator.moveToGeek();
      	  geekTranslator.acknowledgedByGeek();
      	  RDSEvent.stop(appName + " Geek Translator");
        } catch(Exception ex) {
      	  alert( "%s Geek Translator exception: %s",appName,ex.getMessage() );
      	  RDSEvent.start(appName + " Geek Translator");
        }
    }

    public static void main(String[] args) {

        if (args.length < 1 || args[0].isBlank()) {
            throw new IllegalArgumentException("Application name is a required parameter.");
        }

        appName = args[0];
        String rdsDb = (args.length > 1) ? args[1] : DEFAULT_RDS_DB;
        trace("application started, id = [%s], db = [%s]", appName, rdsDb);

        GeekProcess obj = new GeekProcess(appName, rdsDb);
        obj.run();

    }
}
