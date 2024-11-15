package fms ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;

import com.rabbitmq.client.ConnectionFactory ;
import com.rabbitmq.client.Connection ;
import com.rabbitmq.client.Channel ;
import com.rabbitmq.client.DeliverCallback ;

import org.json.* ;

import rds.* ;

public class
MQReceive
  {
  private final static String QUEUE_NAME = "FMS.RECEIVE-TASK" ;
  private ConnectionFactory factory = null ;
  private Connection connection = null ;
  private Channel channel = null ;
  private RDSDatabase db ;
  
  public
  MQReceive()
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
//      channel.queueDeclare(QUEUE_NAME,true,false,false,null) ;
      }
    catch(Exception e) { e.printStackTrace() ; }

    if(channel!=null)
      System.out.println("channel ok") ; 
    }

  private void
  loop()
    {
    while(true)
      {
      List<Map<String,String>> tasks
        = db.getResultMapList("SELECT * FROM fmsTasks "
                             +"WHERE sent='no' ") ;
      if((tasks!=null) && (!tasks.isEmpty()))
        {
        for(Map<String,String>task : tasks)
          {
          String seq = task.get("seq") ;

          try
            {
            JSONObject out  = new JSONObject() ;
            out.put("taskId",task.get("taskId")) ;
            out.put("priority",Integer.parseInt(task.get("priority"))) ;
            out.put("sourceLocation",task.get("sourceLocation")) ;
            out.put("destinationLocation",task.get("destinationLocation")) ;
            out.put("deadline",task.get("deadline")) ;
            out.put("dependsOn",task.get("dependsOn")) ;
            out.put("baggage",task.get("baggage")) ;
            out.put("taskType",task.get("taskType")) ;
  
            String json = out.toString() ;
            System.out.println(json) ;
            byte[] send = json.getBytes() ;

            channel.basicPublish("",QUEUE_NAME,null,send) ;
            }
          catch(Exception e) { e.printStackTrace() ; }

          db.execute("UPDATE fmsTasks SET sent='yes' "
                    +"WHERE seq = %s ",seq) ;
          }
        }
  
      try{ Thread.sleep(200) ; }
      catch(Exception e) { e.printStackTrace() ; }
      }
    }
 
  public static void
  main(String[] argv)
    {
    MQReceive app = new MQReceive() ;
    app.loop() ;
    }

  }
