package host.geek;

import rds.RDSDatabase;

public interface GeekTranslator {

    /**
    * sentToGeek
    */
    void moveToGeek();

    /**
     * ackToGeek
     */
    void acknowledgedByGeek();

    /**
     * confirmation from Geek
     */
    void moveFromGeek();

//    void setDatabase(RDSDatabase db);
    
}
