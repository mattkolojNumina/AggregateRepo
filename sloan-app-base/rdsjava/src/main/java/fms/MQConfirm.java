package fms ;

import com.rabbitmq.client.ConnectionFactory ;
import com.rabbitmq.client.Connection ;
import com.rabbitmq.client.Channel ;
import com.rabbitmq.client.DeliverCallback ;

import org.json.* ;

import rds.* ;

public class
MQConfirm
  {
  private final static String QUEUE_NAME = "FMS.CONFIRM-TASK" ;
  private ConnectionFactory factory = null ;
  private Connection connection = null ;
  private Channel channel = null ;
  private RDSDatabase db = null ;
  
  public
  MQConfirm()
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
      channel.queueDeclare(QUEUE_NAME,true,false,false,null) ;

      DeliverCallback deliverCallback = (consumerTag,delivery) -> {
        String message = new String(delivery.getBody(),"UTF-8") ;
        process(message) ;
      } ;

      channel.basicConsume(QUEUE_NAME,true,deliverCallback,consumerTag->{}) ;
      }
    catch(Exception e) { e.printStackTrace() ; }
    }

  private void
  process(String json)
    {
    System.out.println("recv ["+json+"]") ;
    JSONObject object = new JSONObject(json) ;

    String taskId  = object.optString("taskId") ;
    String sourceLocation = object.optString("sourceLocation") ;

    int seq = db.getIntValue("SELECT seq FROM fmsTasks "
                            +"WHERE taskid='"+taskId+"' "
                            +"AND sourceLocation='"+sourceLocation+"' "
                            +"ORDER BY seq DESC "
                            +"LIMIT 1 ",
                             -1) ;

    String robotId = object.optString("robotId") ;
    int robotState= object.optInt("robotState") ;
    int taskStatus = object.optInt("taskStatus") ;
    int subtaskStatus = object.optInt("subTaskStatus") ;

    if(seq!=-1)
      {
      db.execute("UPDATE fmsTasks "
                +"SET robotId='%s', "
                +"robotState=%d, "
                +"taskStatus=%d, "
                +"subtaskStatus=%d "
                +"WHERE seq=%d ",
                robotId,
                robotState,
                taskStatus,
                subtaskStatus,
                seq) ;
      System.out.println("seq "+seq+" "
                        +"taskId "+taskId+" "
                        +"sourceLocation "+sourceLocation+" "
                        +"robotState "+robotState+" "
                        +"taskStatus "+taskStatus+" "
                        +"subtaskStatus "+subtaskStatus) ;
      }
    }

  private void
  loop()
    {
    while(true)
      {
      String now = db.getValue("SELECT NOW() ","") ;
      RDSLog.inform("now "+now) ;
      try { Thread.sleep(60*1000) ; }
      catch(Exception e) { e.printStackTrace() ; } 
      }
    }
  
  public static void
  main(String[] argv)
    {
    MQConfirm app = new MQConfirm() ;
    app.loop() ;
    }

  }
