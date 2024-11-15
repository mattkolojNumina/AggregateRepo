package fms ;

import com.rabbitmq.client.ConnectionFactory ;
import com.rabbitmq.client.Connection ;
import com.rabbitmq.client.Channel ;
import com.rabbitmq.client.DeliverCallback ;

import org.json.* ;

//import rds.* ;
import rds.RDSDatabase;
import static rds.RDSLog.*;
import rds.RDSUtil;
import rds.parse.ParseEndOnly;

public class
MQRobots
  {
  private final static String QUEUE_NAME = "FMS.ROBOTS" ;
  private final static String EXCHANGE_NAME = "amq.topic" ;
  private ConnectionFactory factory = null ;
  private Connection connection = null ;
  private Channel channel = null ;
  private RDSDatabase db = null ;
  
  public
  MQRobots()
    {
    try
      {
      db = new RDSDatabase("db") ;

      factory = new ConnectionFactory() ;
      factory.setUsername("numina") ;
      factory.setPassword("numina@123!") ;
      factory.setVirtualHost("/") ;
      factory.setHost("10.30.8.143") ;
      factory.setPort(5672) ;
     
      connection = factory.newConnection() ;
      channel    = connection.createChannel() ;
      channel.exchangeDeclare(EXCHANGE_NAME,"topic",true) ;
      String queueName = channel.queueDeclare().getQueue() ;
      channel.queueBind(queueName,EXCHANGE_NAME,"amr200") ;

      if (channel != null)
        inform("channel ok");

      DeliverCallback deliverCallback = (consumerTag,delivery) -> {
        String message = new String(delivery.getBody(),"UTF-8") ;
        process(message) ;
      } ;

      channel.basicConsume(queueName,true,deliverCallback,consumerTag->{}) ;
      }
    catch(Exception e) { e.printStackTrace() ; }
    }

  private void
  process(String json)
    {
//    System.out.println("recv ["+json+"]") ;
    JSONObject object = new JSONObject(json) ;

    int count = object.optInt("count") ;
    if(count>0)
      {
      JSONArray array = object.getJSONArray("robots") ;
      for(int i=0 ; i<count ; i++)
        {
        JSONObject robot = array.getJSONObject(i) ;
        int id = robot.optInt("id") ;
        int number = robot.optInt("number") ;
        String type = robot.optString("type") ;
        String robotName = robot.optString("robotName") ;
        String viewName = robot.optString("view_name") ;
        boolean isConnected = robot.getBoolean("isConnected") ;
        int state = robot.optInt("state") ;
        String current_task = robot.optString("current_task") ;
        String sourceLocation = robot.optString("sourceLocation") ;
        String destinationLocation = robot.optString("destinationLocation") ;
        JSONObject pose = robot.getJSONObject("pose") ;
        JSONObject position = pose.getJSONObject("position") ;
        Double x = position.getDouble("x") ;
        Double y = position.getDouble("y") ;
        JSONObject orientation = pose.getJSONObject("orientation") ;
        Double z = orientation.getDouble("z") ;
        Double w = orientation.getDouble("w") ;
        Double battery = robot.getDouble("battery") ;
        String workHours = robot.optString("workHours") ; 
        String description = robot.optString("discription") ; 

        String eDescription = "" ;
        String eFix = "" ;
        String eName = "" ;
        String eCode = "" ;

        if(robot.has("robot_errors"))
          {
          JSONObject robot_errors = robot.getJSONObject("robot_errors") ;
          if(robot_errors.has("DOCKING"))
            {
            JSONArray docking = robot_errors.getJSONArray("DOCKING") ;
            if(docking.length()>0)
              {
              JSONObject error = docking.getJSONObject(0) ;
              eDescription = error.optString("description") ;
              eFix         = error.optString("fix") ;
              eName        = error.optString("name") ;
              eCode        = error.optString("code") ;
              }
            }
          } 
         
        String sql 
          = "REPLACE INTO fmsRobots "
          + "SET id="+id+", "
          + "number="+number+", "
          + "type='"+type+"', "
          + "robotName='"+robotName+"', "
          + "viewName='"+viewName+"', "
          + "isConnected='"+(isConnected?"true":"false")+"', "
          + "state="+state+", "
          + "currentTask='"+current_task+"', "
          + "sourceLocation='"+sourceLocation+"', "
          + "destinationLocation='"+destinationLocation+"', "
          + "x="+x+", "
          + "y="+y+", "
          + "z="+z+", "
          + "w="+w+", "
          + "battery="+battery+", "
          + "workHours='"+workHours+"', "
          + "description='"+description+"'"; 
          //+ "description='"+description+"', " 
          //+ "errorDescription='"+eDescription+"', "
          //+ "errorFix='"+eFix+"', "
          //+ "errorName='"+eName+"', "
          //+ "errorCode='"+eCode+"' " ; 
        db.execute(sql) ;
        }
      }

    }

  private void
  loop()
    {
    }
  
  public static void
  main(String[] argv)
    {
    MQRobots app = new MQRobots() ;
    app.loop() ;
    }

  }
