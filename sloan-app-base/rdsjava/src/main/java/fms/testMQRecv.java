package fms ;

import com.rabbitmq.client.ConnectionFactory ;
import com.rabbitmq.client.Connection ;
import com.rabbitmq.client.Channel ;
import com.rabbitmq.client.DeliverCallback ;

import org.json.* ;

public class
testMQRecv
  {
  private final static String QUEUE_NAME = "FMS.CONFIRM-TASK" ;
  private ConnectionFactory factory = null ;
  private Connection connection = null ;
  private Channel channel = null ;
  
  public
  testMQRecv()
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
      }
    catch(Exception e) { e.printStackTrace() ; }

    if(channel!=null)
      System.out.println("channel ok") ; 
    }

  public void
  test()
    {
    if(channel==null)
      return ;

    try
      {
      JSONObject object = new JSONObject() ;
      object.put("taskId","1") ;
      object.put("robotId","R");
      object.put("sourceId","S");
      object.put("destinationId","D") ;
      object.put("timeTaken","30.5") ;
      object.put("status","assigned") ;
      
      String json = object.toString() ;
      byte[] send = json.getBytes() ;
      channel.basicPublish("",QUEUE_NAME,null,send) ;
      System.out.println("done") ;
      }
    catch(Exception e) { e.printStackTrace() ; }
    }

  public void
  loop()
    {
    while(true)
      {
      test() ;
      try { Thread.sleep(10000) ; }
      catch(Exception e) { e.printStackTrace() ; } 
      }
    } 

  public static void
  main(String[] argv)
    {
    testMQRecv app = new testMQRecv() ;
    app.loop() ;
    }

  }
