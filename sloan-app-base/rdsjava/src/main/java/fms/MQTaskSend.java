package fms;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.*;
//import rds.*;
import rds.RDSDatabase;
import static rds.RDSLog.*;
import rds.RDSUtil;
import rds.parse.ParseEndOnly;

public class MQTaskSend {
  private final static String QUEUE_NAME = "FMS.RECEIVE-TASK-DYNAMO";
  private final static String QUEUE_USER = "numina";
  private final static String QUEUE_PASSWORD = "numina@123!";
  private final static String QUEUE_VIRTUAL_HOST = "/";
  private final static String QUEUE_HOST = "10.30.8.143";
  private final static int QUEUE_PORT = 5672;
  private final static String FLEET_TYPE = "dynamo";

  private final static int POLL_PERIOD = 200;

  private ConnectionFactory factory = null;
  private Connection connection = null;
  private Channel channel = null;
  private RDSDatabase db;

  private String host;
  private int port;
  private String user;
  private String password;
  private String virtualHost;


  private int poll;

  private String fleetType;
  private String queueName;

  public MQTaskSend(String fleetType) {
    this.fleetType = fleetType;
    
    try {

      db = new RDSDatabase("db");

      Map<String,String> paramMap = db.getControlMap( fleetType );
      if (paramMap == null || paramMap.isEmpty()) {
         alert( "unable to configure for zone [%s]", fleetType );
      } else {
         host = paramMap.get( "queueHost" );
         port = RDSUtil.stringToInt( paramMap.get( "queuePort" ), QUEUE_PORT );
         virtualHost = paramMap.get( "queueVirtualHost" );
         user = paramMap.get( "queueUser" );
         password = paramMap.get( "queuePassword" );

         queueName = paramMap.get( "queueNameReceive" );
         poll = RDSUtil.stringToInt( paramMap.get( "pollPeriodSend" ), POLL_PERIOD );

         alert("queue [%s] host [%s] port [%s] virtualHost [%s] for user [%s]", queueName, host, port, virtualHost, user);
        
         if(poll <=0)
            poll = 500;
         
         alert("poll at init = [%d]", poll);
         
         //trace( "connect to %s:%d, start at %s", host, port, startScreen );
      }

      

      factory = new ConnectionFactory();
      factory.setUsername(user);
      factory.setPassword(password);
      factory.setVirtualHost(virtualHost);
      factory.setHost(host);
      factory.setPort(port);

      connection = factory.newConnection();
      channel = connection.createChannel();
      //      channel.queueDeclare(QUEUE_NAME,true,false,false,null) ;
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (channel != null)
      inform("channel ok");
  }

  private void loop() {
    while (true) {
      List<Map<String, String>> tasks =
          db.getResultMapList("SELECT * FROM fmsTasks "
              + "WHERE sent='no' AND fleetType='" + fleetType + "'");
      if ((tasks != null) && (!tasks.isEmpty())) {
        for (Map<String, String> task : tasks) {
          String seq = task.get("seq");

          try {
            JSONObject out = new JSONObject();
            out.put("taskId", task.get("taskId"));
            out.put("priority", Integer.parseInt(task.get("priority")));
            out.put("sourceLocation", task.get("sourceLocation"));
            out.put("destinationLocation", task.get("destinationLocation"));
            out.put("deadline", task.get("deadline"));
            out.put("dependsOn", task.get("dependsOn"));
            out.put("baggage", task.get("baggage"));
            out.put("taskType", task.get("taskType"));
            out.put("robotId", Integer.parseInt(task.get("robotId"))); //send robotId as INT 

            String json = out.toString();
            System.out.println(json);
            byte[] send = json.getBytes();

            channel.basicPublish("", queueName, null, send);
          } catch (Exception e) {
            e.printStackTrace();
          }

          db.execute("UPDATE fmsTasks SET sent='yes' "
                  + "WHERE seq = %s ",
              seq);
        }
      } 

      try {
        Thread.sleep(poll);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    String fleetType = (args.length > 0) ? args[0] : FLEET_TYPE;
    MQTaskSend app = new MQTaskSend(fleetType);
    app.loop();
  }
}
