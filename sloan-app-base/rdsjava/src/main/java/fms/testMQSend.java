package fms ;

import com.rabbitmq.client.ConnectionFactory ;
import com.rabbitmq.client.Connection ;
import com.rabbitmq.client.Channel ;
import com.rabbitmq.client.DeliverCallback ;

import org.json.* ;

public class
testMQSend
  {
  private final static String QUEUE_NAME = "FMS.RECEIVE-TASK" ;
  private ConnectionFactory factory = null ;
  private Connection connection = null ;
  private Channel channel = null ;
  
  public
  testMQSend()
    {
    try
      {
      factory = new ConnectionFactory() ;
      factory.setUsername("numina") ;
      factory.setPassword("Numina@123!") ;
      factory.setVirtualHost("/numina") ;
      factory.setHost("35.192.175.65") ;
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
    JSONObject object = new JSONObject(json) ;
    String taskId = object.optString("taskId") ;
    System.out.println("taskId: "+taskId) ;    
    }

  private void
  loop()
    {
    while(true)
      {
      try{Thread.sleep(1000);}
      catch(Exception e) { e.printStackTrace() ; }
      }
    }
  
  public static void
  main(String[] argv)
    {
    testMQSend app = new testMQSend() ;
    app.loop() ;
    }

  }
