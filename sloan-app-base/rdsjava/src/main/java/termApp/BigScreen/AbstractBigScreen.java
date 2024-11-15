package termApp.BigScreen;

import rds.*;
import term.TerminalDriver;
import termApp.*;
import termApp.util.*;
import termApp.util.InfoBox.InfoBoxConstructor;

import static rds.RDSLog.*;
import static termApp.util.Constants.*;

import rds.RDSUtil;

import static sloane.SloaneConstants.*;

import termApp.util.TermActionObject.OnActionListener;
import termApp.util.TermActionObject.OnTextActionListener;

import java.util.*;

public class AbstractBigScreen
extends AbstractNuminaScreen {   
   protected InfoBox rightInfo, leftInfo;
   protected TermGroup lines;
   private String startScreen;

   private static final boolean TERM_OPERATORS = false;
   
   private static final String STANDARD = "standard";

   public AbstractBigScreen(TerminalDriver term) {
      super(term);
      reloadParams();

      startScreen = "BigScreen.BigScreen";
   }

   protected void reloadParams() {
   }
   
   protected void loadGlobals() {
      //Hello
   }
   
   protected TextWrap initMsg() {
      TextWrap msg = new TextWrap(MSG_MARGIN, MSG_Y, 1750, FONT_L, 2);
      msg.show();
      return msg;
   }
   
   protected void gotoStartScreen() {
      setNextScreen(startScreen);
   }
   
   /*
    * interface methods
    */

   /** Performs periodic tasks, once per cycle. */
   public void handleTick() {
      super.handleTick();
      tickDisplay();
   }

   /*
    * display methods
    */

   public void initDisplay() {
      super.initDisplay();  
      //header.updateTitle("BIG SCREEN");
   }

   protected void tickDisplay() {

   }


   protected String getBackgroundColor ( List<Map<String,String>> cartonList , int waveNum, String pickType) {
    int waveSeq = Integer.parseInt(cartonList.get(waveNum*5).get("waveSeq"));
    //inform("background color for wavenum [%d] is %s", waveNum, String.format(cartonList.get(waveNum*5).get("color")));

      if (isWaveComplete(waveSeq, pickType))
        return "green";

      return cartonList.get(waveNum*5).get("color");

   }



   protected String getBackgroundColorGeek ( List<Map<String,String>> cartonList , int waveNum, String pickType) {
    int waveSeq = Integer.parseInt(cartonList.get(waveNum*5).get("waveSeq"));
    //inform("background color for wavenum [%d] is %s", waveNum, String.format(cartonList.get(waveNum*5).get("color")));

      if (isWaveCompleteGeek(waveSeq, pickType))
        return "green";

      return cartonList.get(waveNum*5).get("color");

   }

   protected String getBackgroundColor ( List<Map<String,String>> cartonList , int waveNum) {


    return cartonList.get(waveNum*5).get("color");

 }

   protected String getWaveSeq ( List<Map<String,String>> cartonList , int waveNum) {
    return cartonList.get(waveNum*5).get("waveSeq");

   }

   //Returns true if all cartons per pickType per waveSeq have LPN's and all orderLines have labelStamps
   protected boolean isWaveComplete(int waveSeq, String pickType) {
        int orderLinesCount = db.getInt (1,
            "SELECT " +
            "COUNT(DISTINCT CASE WHEN custOrderLines.labelStamp IS NULL THEN custOrderLines.orderLineSeq END) AS orderLinesComplete " +
        "FROM " +
            "custOrderLines " +
        "JOIN " +
            "rdsPicks ON custOrderLines.orderLineSeq = rdsPicks.orderLineSeq " +
        "JOIN " +
            "rdsCartons ON rdsPicks.cartonSeq = rdsCartons.cartonSeq " +
        "JOIN " +
            "custOrders ON rdsCartons.orderID = custOrders.orderID " +
        "WHERE " +
            "custOrders.waveSeq = %d " +
            "AND rdsCartons.cancelStamp IS NULL " +
            "AND rdsCartons.pickType IN ('%s'); ", waveSeq, pickType);

        int cartonCount = db.getInt (1,
            "SELECT " +
            "COUNT(DISTINCT CASE WHEN rdsCartons.lpn IS NULL THEN rdsCartons.cartonSeq END) AS cartonCountComplete " +
        "FROM " +
            "rdsCartons " +
        "JOIN " +
            "custOrders ON rdsCartons.orderID = custOrders.orderID " +
        "WHERE " +
            "custOrders.waveSeq = %d " +
            "AND rdsCartons.cancelStamp IS NULL " +
            "AND rdsCartons.pickType IN ('%s');", waveSeq, pickType);

        //inform("wave [%d] has [%d] remaining cartons/ [%d] remaining orderlines", waveSeq, cartonCount, orderLinesCount);
        if (cartonCount + orderLinesCount <= 0)
            return true;
        
        return false;
    
   }

   protected boolean isWaveCompleteGeek(int waveSeq, String pickType) {
    int orderLinesCount = db.getInt (1,
        "SELECT " +
        "COUNT(DISTINCT CASE WHEN custOrderLines.labelStamp IS NULL THEN custOrderLines.orderLineSeq END) AS orderLinesComplete " +
    "FROM " +
        "custOrderLines " +
    "JOIN " +
        "rdsPicks ON custOrderLines.orderLineSeq = rdsPicks.orderLineSeq " +
    "JOIN " +
        "rdsCartons ON rdsPicks.cartonSeq = rdsCartons.cartonSeq " +
    "JOIN " +
        "custOrders ON rdsCartons.orderID = custOrders.orderID " +
    "WHERE " +
        "custOrders.waveSeq = %d " +
        "AND rdsCartons.cancelStamp IS NULL " +
        "AND rdsCartons.pickType IN ('%s'); ", waveSeq, pickType);

    int cartonCount = db.getInt (1,
        "SELECT " +
        "COUNT(DISTINCT CASE WHEN rdsCartons.geekPickConfirmedStamp IS NULL AND rdsCartons.pickStamp IS NULL AND rdsCartons.pickShortStamp IS NULL THEN rdsCartons.cartonSeq END) AS cartonCountComplete " +
    "FROM " +
        "rdsCartons " +
    "JOIN " +
        "custOrders ON rdsCartons.orderID = custOrders.orderID " +
    "WHERE " +
        "custOrders.waveSeq = %d " +
        "AND rdsCartons.cancelStamp IS NULL " +
        "AND rdsCartons.pickType IN ('%s');", waveSeq, pickType);

        //inform("geek carton count [%d] orderLinesCount [%d] for wave [%d]",cartonCount,orderLinesCount,waveSeq);

    //inform("wave [%d] has [%d] remaining cartons/ [%d] remaining orderlines", waveSeq, cartonCount, orderLinesCount);
    if (cartonCount + orderLinesCount <= 0)
        return true;
    
    return false;

}


   String dateFormat = "OR (SUBSTRING(c.demandDate, 1, 2) = DATE_FORMAT(CURDATE(), '%%m') AND SUBSTRING(c.demandDate, 4, 2) = DATE_FORMAT(CURDATE(), '%%d')) ";
   protected List<Map<String, String>> getBuildListCartonStart() {
    return db.getResultMapList( 
       "WITH ActiveWaves AS ( " +
        "SELECT " +
            "r.waveSeq, " +
            "c.dailyWaveSeq, " +
            "c.demandDate, " +
            "CASE " +
                "WHEN r.cancelStamp IS NOT NULL OR r.errorStamp IS NOT NULL THEN 'red' " +
                "WHEN r.zoneRouteReleaseStamp IS NOT NULL THEN 'yellow' " +
                "ELSE '' " +
            "END AS color " +
        "FROM " +
            "rdsWaves r " +
        "LEFT JOIN  " +
            "custOrders c ON r.waveSeq = c.waveSeq " +
        "WHERE " +
            "r.completeStamp IS NULL " +
            dateFormat +
        "GROUP BY " +
            "r.waveSeq, c.dailyWaveSeq, c.demandDate, r.cancelStamp, r.errorStamp, r.zoneRouteReleaseStamp " +
    ") " +
    
    "SELECT " +
        "ActiveWaves.waveSeq, " +
        "CartonTypes.cartonType AS cartonTypes, " +
        "CONCAT('W', LPAD(ActiveWaves.dailyWaveSeq, 2, '0')) AS dailyWaveSeq, " +
        "ActiveWaves.demandDate, " +
        "CONCAT(COUNT(DISTINCT CASE WHEN rdsCartons.lpn IS NULL AND rdsCartons.cancelStamp IS NULL THEN rdsCartons.cartonSeq END), '/', COUNT(DISTINCT CASE WHEN rdsCartons.cancelStamp IS NULL THEN rdsCartons.cartonSeq END)) AS cartonCount, " +
        "CONCAT(COUNT(DISTINCT CASE WHEN custOrderLines.labelStamp IS NULL AND custOrderLines.cancelStamp IS NULL THEN custOrderLines.orderLineSeq END), '/', COUNT(DISTINCT CASE WHEN custOrderLines.cancelStamp IS NULL THEN custOrderLines.orderLineSeq END )) AS orderLinesCount, " +
        "MAX(ActiveWaves.color) AS color " +
    "FROM " +
        "ActiveWaves " +
    "CROSS JOIN " +
        "(SELECT 'tote' AS cartonType UNION ALL SELECT 'small' UNION ALL SELECT 'medium' UNION ALL SELECT 'large' UNION ALL SELECT 'export') AS CartonTypes " +
    "LEFT JOIN " +
        "custOrders ON custOrders.waveSeq = ActiveWaves.waveSeq " +
    "LEFT JOIN " +
        "rdsCartons ON rdsCartons.orderID = custOrders.orderID AND rdsCartons.cartonType = CartonTypes.cartonType AND rdsCartons.pickType = '%s' " +
    "LEFT JOIN " +
        "rdsPicks ON rdsPicks.cartonSeq = rdsCartons.cartonSeq " +
    "LEFT JOIN " +
        "custOrderLines ON custOrderLines.orderLineSeq = rdsPicks.orderLineSeq " +
    "WHERE 1 " +
    "GROUP BY " +
        "ActiveWaves.waveSeq, CartonTypes.cartonType, ActiveWaves.demandDate " +
    "ORDER BY " +
        "ActiveWaves.waveSeq ASC, " +
        "CASE CartonTypes.cartonType " +
            "WHEN 'tote' THEN 1 " +
            "WHEN 'small' THEN 2 " +
            "WHEN 'medium' THEN 3 " +
            "WHEN 'large' THEN 4 " +
            "WHEN 'export' THEN 5 " +
        "END ASC;",
       PICKTYPE_ZONEROUTE);
 }

 protected List<Map<String, String>> getBuildListDU() {
    return db.getResultMapList( 
       "WITH ActiveWaves AS ( " +
        "SELECT " +
            "r.waveSeq, " +
            "c.dailyWaveSeq, " +
            "c.demandDate, " +
            "CASE " +
                "WHEN r.cancelStamp IS NOT NULL OR r.errorStamp IS NOT NULL THEN 'red' " +
                "WHEN r.cartPickReleaseStamp IS NOT NULL THEN 'yellow' " +
                "ELSE '' " +
            "END AS color " +
        "FROM " +
            "rdsWaves r " +
        "LEFT JOIN  " +
            "custOrders c ON r.waveSeq = c.waveSeq " +
        "WHERE " +
            "r.completeStamp IS NULL " +
            dateFormat +
        "GROUP BY " +
            "r.waveSeq, c.dailyWaveSeq, c.demandDate, r.cancelStamp, r.errorStamp, r.cartPickReleaseStamp " +
    ") " +
    
    "SELECT " +
        "ActiveWaves.waveSeq, " +
        "CartonTypes.cartonType AS cartonTypes, " +
        "CONCAT('W', LPAD(ActiveWaves.dailyWaveSeq, 2, '0')) AS dailyWaveSeq, " +
        "ActiveWaves.demandDate, " +
        "CONCAT(COUNT(DISTINCT CASE WHEN rdsCartons.lpn IS NULL AND rdsCartons.cancelStamp IS NULL THEN rdsCartons.cartonSeq END), '/', COUNT(DISTINCT CASE WHEN rdsCartons.cancelStamp IS NULL THEN rdsCartons.cartonSeq END)) AS cartonCount, " +
        "CONCAT(COUNT(DISTINCT CASE WHEN custOrderLines.labelStamp IS NULL AND custOrderLines.cancelStamp IS NULL THEN custOrderLines.orderLineSeq END), '/', COUNT(DISTINCT CASE WHEN custOrderLines.cancelStamp IS NULL THEN custOrderLines.orderLineSeq END )) AS orderLinesCount, " +
        "MAX(ActiveWaves.color) AS color " +
    "FROM " +
        "ActiveWaves " +
    "CROSS JOIN " +
        "(SELECT 'tote' AS cartonType UNION ALL SELECT 'small' UNION ALL SELECT 'medium' UNION ALL SELECT 'large' UNION ALL SELECT 'export') AS CartonTypes " +
    "LEFT JOIN " +
        "custOrders ON custOrders.waveSeq = ActiveWaves.waveSeq " +
    "LEFT JOIN " +
        "rdsCartons ON rdsCartons.orderID = custOrders.orderID AND rdsCartons.cartonType = CartonTypes.cartonType AND rdsCartons.pickType = '%s' " +
    "LEFT JOIN " +
        "rdsPicks ON rdsPicks.cartonSeq = rdsCartons.cartonSeq " +
    "LEFT JOIN " +
        "custOrderLines ON custOrderLines.orderLineSeq = rdsPicks.orderLineSeq " +
    "WHERE 1 " +
    "GROUP BY " +
        "ActiveWaves.waveSeq, CartonTypes.cartonType, ActiveWaves.demandDate " +
    "ORDER BY " +
        "ActiveWaves.waveSeq ASC, " +
        "CASE CartonTypes.cartonType " +
            "WHEN 'tote' THEN 1 " +
            "WHEN 'small' THEN 2 " +
            "WHEN 'medium' THEN 3 " +
            "WHEN 'large' THEN 4 " +
            "WHEN 'export' THEN 5 " +
        "END ASC;",
       PICKTYPE_AERSOLBOOM);
 }

 protected List<Map<String, String>> getBuildListPQ() {
    return db.getResultMapList( 
       "WITH ActiveWaves AS ( " +
        "SELECT " +
            "r.waveSeq, " +
            "c.dailyWaveSeq, " +
            "c.demandDate, " +
            "ROW_NUMBER() OVER (PARTITION BY c.demandDate ORDER BY r.waveSeq) AS demandDateRank, " +
            "CASE " +
                "WHEN r.cancelStamp IS NOT NULL OR r.errorStamp IS NOT NULL THEN 'red' " +
                "WHEN r.cartPickReleaseStamp IS NOT NULL THEN 'yellow' " +
                "ELSE '' " +
            "END AS color " +
        "FROM " +
            "rdsWaves r " +
        "LEFT JOIN  " +
            "custOrders c ON r.waveSeq = c.waveSeq " +
        "WHERE " +
            "r.completeStamp IS NULL " + dateFormat + 
        "GROUP BY " +
            "r.waveSeq, c.dailyWaveSeq, c.demandDate, r.cancelStamp, r.errorStamp, r.cartPickReleaseStamp " +
    ") " +
    
    "SELECT " +
        "ActiveWaves.waveSeq, " +
        "CartonTypes.cartonType AS cartonTypes, " +
        "CONCAT('W', LPAD(ActiveWaves.dailyWaveSeq, 2, '0')) AS dailyWaveSeq, " +
        "ActiveWaves.demandDate, " +
        "CONCAT(COUNT(DISTINCT CASE WHEN rdsCartons.lpn IS NULL AND rdsCartons.cancelStamp IS NULL THEN rdsCartons.cartonSeq END), '/', COUNT(DISTINCT CASE WHEN rdsCartons.cancelStamp IS NULL THEN rdsCartons.cartonSeq END)) AS cartonCount, " +
        "CONCAT(COUNT(DISTINCT CASE WHEN custOrderLines.labelStamp IS NULL AND custOrderLines.cancelStamp IS NULL THEN custOrderLines.orderLineSeq END), '/', COUNT(DISTINCT CASE WHEN custOrderLines.cancelStamp IS NULL THEN custOrderLines.orderLineSeq END )) AS orderLinesCount, " +
        "MAX(ActiveWaves.color) AS color " +
    "FROM " +
        "ActiveWaves " +
    "CROSS JOIN " +
        "(SELECT 'tote' AS cartonType UNION ALL SELECT 'small' UNION ALL SELECT 'medium' UNION ALL SELECT 'large' UNION ALL SELECT 'export') AS CartonTypes " +
    "LEFT JOIN " +
        "custOrders ON custOrders.waveSeq = ActiveWaves.waveSeq " +
    "LEFT JOIN " +
        "rdsCartons ON rdsCartons.orderID = custOrders.orderID AND rdsCartons.cartonType = CartonTypes.cartonType AND rdsCartons.pickType IN ('%s','%s') " +
    "LEFT JOIN " +
        "rdsPicks ON rdsPicks.cartonSeq = rdsCartons.cartonSeq " +
    "LEFT JOIN " +
        "custOrderLines ON custOrderLines.orderLineSeq = rdsPicks.orderLineSeq " +
    "WHERE 1 " +
    "GROUP BY " +
        "ActiveWaves.waveSeq, CartonTypes.cartonType, ActiveWaves.demandDate " +
    "ORDER BY " +
        "ActiveWaves.waveSeq ASC, " +
        "CASE CartonTypes.cartonType " +
            "WHEN 'tote' THEN 1 " +
            "WHEN 'small' THEN 2 " +
            "WHEN 'medium' THEN 3 " +
            "WHEN 'large' THEN 4 " +
            "WHEN 'export' THEN 5 " +
        "END ASC;",
       PICKTYPE_PERISHABLES, PICKTYPE_LIQUIDS);
 }
       //(DISTINCT CASE WHEN geekPickConfirmedStamp IS NULL AND rdsCartons.pickStamp IS NULL AND rdsCartons.pickShortStamp IS NULL THEN rdsCartons.cartonSeq END)
       protected List<Map<String, String>> getBuildListGeek() {
        return db.getResultMapList( 
           "WITH ActiveWaves AS ( " +
            "SELECT " +
                "r.waveSeq, " +
                "c.dailyWaveSeq, " +
                "c.demandDate, " +
                "CASE " +
                    "WHEN r.cancelStamp IS NOT NULL OR r.errorStamp IS NOT NULL THEN 'red' " +
                    "WHEN r.geekReleaseStamp IS NOT NULL THEN 'yellow' " +
                    "ELSE '' " +
                "END AS color " +
            "FROM " +
                "rdsWaves r " +
            "LEFT JOIN  " +
                "custOrders c ON r.waveSeq = c.waveSeq " +
            "WHERE " +
                "r.completeStamp IS NULL " +
                dateFormat +
            "GROUP BY " +
                "r.waveSeq, c.dailyWaveSeq, c.demandDate, r.cancelStamp, r.errorStamp, r.geekReleaseStamp " +
        ") " +
        
        "SELECT " +
            "ActiveWaves.waveSeq, " +
            "CartonTypes.cartonType AS cartonTypes, " +
            "CONCAT('W', LPAD(ActiveWaves.dailyWaveSeq, 2, '0')) AS dailyWaveSeq, " +
            "ActiveWaves.demandDate, " +
          "CONCAT(COUNT(DISTINCT CASE WHEN rdsCartons.lpn IS NULL AND rdsCartons.cancelStamp IS NULL THEN rdsCartons.cartonSeq END), '/', COUNT(DISTINCT CASE WHEN rdsCartons.cancelStamp IS NULL THEN rdsCartons.cartonSeq END)) AS cartonCount, " +
        "CONCAT(COUNT(DISTINCT CASE WHEN custOrderLines.labelStamp IS NULL AND custOrderLines.cancelStamp IS NULL THEN custOrderLines.orderLineSeq END), '/', COUNT(DISTINCT CASE WHEN custOrderLines.cancelStamp IS NULL THEN custOrderLines.orderLineSeq END )) AS orderLinesCount, " +
            "MAX(ActiveWaves.color) AS color " +
        "FROM " +
            "ActiveWaves " +
        "CROSS JOIN " +
            "(SELECT 'tote' AS cartonType UNION ALL SELECT 'small' UNION ALL SELECT 'medium' UNION ALL SELECT 'large' UNION ALL SELECT 'export') AS CartonTypes " +
        "LEFT JOIN " +
            "custOrders ON custOrders.waveSeq = ActiveWaves.waveSeq " +
        "LEFT JOIN " +
            "rdsCartons ON rdsCartons.orderID = custOrders.orderID AND rdsCartons.cartonType = CartonTypes.cartonType AND rdsCartons.pickType = '%s' " +
        "LEFT JOIN " +
            "rdsPicks ON rdsPicks.cartonSeq = rdsCartons.cartonSeq " +
        "LEFT JOIN " +
            "custOrderLines ON custOrderLines.orderLineSeq = rdsPicks.orderLineSeq " +
        "WHERE 1 " +    
        "GROUP BY " +
            "ActiveWaves.waveSeq, CartonTypes.cartonType, ActiveWaves.demandDate " +
        "ORDER BY " +
            "ActiveWaves.waveSeq ASC, " +
            "CASE CartonTypes.cartonType " +
                "WHEN 'tote' THEN 1 " +
                "WHEN 'small' THEN 2 " +
                "WHEN 'medium' THEN 3 " +
                "WHEN 'large' THEN 4 " +
                "WHEN 'export' THEN 5 " +
            "END ASC;",
           PICKTYPE_GEEK);
     }

protected List<Map<String, String>> getChangeList () {
   return db.getResultMapList(
      "SELECT truckNumber, door, oldDoor FROM cfgTruckSchedule " +
      "WHERE dailyChange = 1;");
}

   protected void initLines( boolean show ) {
      if (lines != null)
         return;
      int t = 5;
      int y1 = INFO_Y;
      int y2 = 340;
      lines = new TermGroup(0,0);
      lines.add(horizontalLine(y1, t));
      lines.add(horizontalLine(y2, t));
      lines.add(verticalLine(y1, y2, W1_2,t));
      if (show)
         showLines();
   }
   
   protected void showLines() {
      lines.show();
   }
   
   /*
    * helper methods
    */
   
}
