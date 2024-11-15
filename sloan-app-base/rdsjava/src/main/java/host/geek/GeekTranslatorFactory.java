package host.geek;

public class GeekTranslatorFactory {

    public static GeekTranslator getGeekTranslatorImpl(String appName) {
        if (appName.equals("UserDownload")) {
            return new NoneGeekTranslator();
        }
        if (appName.equals("PutawayOrderDownload")) {
//            return new PutawayOrderGeekTranslator();
            return new NoneGeekTranslator();
        }
        if (appName.equals("ProductDownload")) {
            return new NoneGeekTranslator();
        }
        if (appName.equals("BuyingDeptDownload")) {
            return new NoneGeekTranslator();
        }
        if (appName.equals("BackstockInventoryDownload")) {
            return new NoneGeekTranslator();
        }
        if (appName.equals("BaseData")) {
            return new BaseDataTranslator();
        }
        if (appName.equals("PickTranslator")) {
            return new PickTranslator();
        }
        if (appName.equals("PickTranslator_v2")) {
            return new PickTranslator_v2();
        }
        if (appName.equals("PutawayTranslator")) {
            return new PutawayTranslator();
        }
        if (appName.equals("StocktakeTranslator")) {
            return new StocktakeTranslator();
        }
        if (appName.equals("WorkstationStatusUpdtTranslator")) {
            return new WorkstationStatusUpdateTranslator();
        }
        if (appName.equals("LocationDownload")) {
           return new NoneGeekTranslator();
        }
        if (appName.equals("CustomerDownload")) {
           return new NoneGeekTranslator();
        }
        if (appName.equals("OutboundOrderDownload")) {
           return new NoneGeekTranslator();
        }
        if (appName.equals("FullcaseScanTunnelDownload")) {
           return new NoneGeekTranslator();
        }        
        throw new IllegalArgumentException("The system is not configured for application name [" + appName + "]");
    }
}
