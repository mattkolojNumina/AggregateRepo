package fms;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.json.*;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.*;
//import rds.*;
import rds.RDSDatabase;
import static rds.RDSLog.*;
import rds.RDSUtil;
import rds.parse.ParseEndOnly;

public class MQTaskUpdate {
  private final static String QUEUE_NAME = "FMS.CONFIRM-TASK";
  private final static String QUEUE_USER = "numina";
  private final static String QUEUE_PASSWORD = "numina@123!";
  private final static String QUEUE_VIRTUAL_HOST = "/";
  private final static String QUEUE_HOST = "10.30.8.143";
  private final static int QUEUE_PORT = 5672;
  private final static String FLEET_TYPE = "dynamo";

  private final static int POLL_PERIOD = 300;

  private ConnectionFactory factory = null;
  private Connection connection = null;
  private Channel channel = null;
  private RDSDatabase db = null;

  private String host;
  private int port;
  private String user;
  private String password;
  private String virtualHost;


  private int poll;

  private String fleetType;
  private String queueName;

  public MQTaskUpdate(String fleetType) {
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

         queueName = paramMap.get( "queueNameConfirm" );
         poll = RDSUtil.stringToInt( paramMap.get( "pollPeriodUpdate" ), POLL_PERIOD );

         alert("queue [%s] host [%s] port [%s] virtualHost [%s] for user [%s]", queueName, host, port, virtualHost, user);
         if(poll <=0)
            poll = 500;
         
         alert("poll at init = [%d]", poll);

      }

      factory = new ConnectionFactory();
      factory.setUsername(user);
      factory.setPassword(password);
      factory.setVirtualHost(virtualHost);
      factory.setHost(host);
      factory.setPort(port);

      connection = factory.newConnection();
      channel = connection.createChannel();
      Map<String, Object> arguments = new HashMap<String, Object>();
      arguments.put("x-single-active-consumer", true);
      //channel.queueDeclare(queueName, true, false, false, null); //use this if single active consumer is not set to true
      channel.queueDeclare(queueName, true, false, false, arguments);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        process(message);
      };

      channel.basicConsume(
          queueName, true, deliverCallback, consumerTag -> {});

      if (channel != null)
          inform("channel ok");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void process(String json) {
    System.out.println("recv [" + json + "]");
    JSONObject object = new JSONObject(json);

    String taskId = object.optString("taskId");
    String sourceLocation = object.optString("sourceLocation");

    int seq = db.getIntValue("SELECT seq FROM fmsTasks "
            + "WHERE taskid='" + taskId + "' "
            + "AND sourceLocation='" + sourceLocation + "' "
            + "ORDER BY seq DESC "
            + "LIMIT 1 ",
        -1);

    String robotId = object.optString("robotId");
    int robotState = object.optInt("robotState");
    int taskStatus = object.optInt("taskStatus");
    int subtaskStatus = object.optInt("subTaskStatus");

    if (seq != -1) {
      db.execute("UPDATE fmsTasks "
              + "SET robotId='%s', "
              + "robotState=%d, "
              + "taskStatus=%d, "
              + "subtaskStatus=%d "
              + "WHERE seq=%d ",
          robotId, robotState, taskStatus, subtaskStatus, seq);
      System.out.println("seq " + seq + " "
          + "taskId " + taskId + " "
          + "sourceLocation " + sourceLocation + " "
          + "robotState " + robotState + " "
          + "taskStatus " + taskStatus + " "
          + "subtaskStatus " + subtaskStatus);
    }
  }

  private void loop() {
    while (true) {
      String now = db.getValue("SELECT NOW() ", "");
      //inform("now " + now);
      try {
        Thread.sleep(poll);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    String fleetType = (args.length > 0) ? args[0] : FLEET_TYPE;
    MQTaskUpdate app = new MQTaskUpdate(fleetType);
    app.loop();
  }
}
