/*
 * Notifier.java
 * 
 * Automatically sends emails and texts
 * 
 * (c) 2017, Numina Group, Inc.
 */

package notifier;


import java.util.*;

import rds.*;
import polling.AbstractPollingApp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;



public class Notifier
      extends AbstractPollingApp {
   
   final static String TEST_MSG = "This is a test message from RDS.";
   final static String ATTACHMENT_FILE = "/tmp/notificationAttachment";
   
   public Notifier(String id ) {
      super(id, "db" );
   }


   public void send( String recipient, String notificationMessage, byte[] attachment ) {
      final String smtpHost = db.getControl( "notifier", "smtpHost", "smtp.emailsrvr.com" );
      final String smtpPort = db.getControl( "notifier", "smtpPort", "587");
      final String username = db.getControl( "notifier", "username", "rds@numinagroup.com" );
      final String password = db.getControl( "notifier", "password", "D7u-ka7-Lz8-87s" );

      Properties props = new Properties();
      props.put( "mail.smtp.auth", "true" );
      props.put( "mail.smtp.starttls.enable", "true" );
      props.put( "mail.smtp.host", smtpHost );
      props.put( "mail.smtp.port", smtpPort );

      Session session = Session.getInstance( props,
            new javax.mail.Authenticator() {
         protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
         }
      });

      try {
         Message message = new MimeMessage( session );
         message.setFrom(new InternetAddress( "rds@numinagroup.com" ) );
         message.setSubject( "RDS notification" );

         String notificationType = "";
         String toAddress = db.getValue( "SELECT email FROM notificationIndividuals WHERE individual='" + recipient + "'", "" );
         if ( !toAddress.isEmpty() )
            notificationType = "email";
         else {
            String carrier = db.getValue( "SELECT carrier FROM notificationIndividuals WHERE individual='" + recipient + "'", "" );
            String phone = db.getValue( "SELECT phone FROM notificationIndividuals WHERE individual='" + recipient + "'", "" );
            phone = phone.replaceAll("[^0-9]", "");
            toAddress = phone + "@"
                  + db.getValue( "SELECT domain FROM notificationCarriers WHERE carrier='" + carrier+"'", "" );
            notificationType = "text";
         }
         
         message.setRecipients( Message.RecipientType.TO, InternetAddress.parse( toAddress ) );

         if ( notificationType.equals( "email" ) ) {
            // create message part
            Multipart multipart = new MimeMultipart();
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText( notificationMessage );
            multipart.addBodyPart( messageBodyPart );

            // add attachments
            if ( attachment != null && attachment.length > 0 ) {
               FileOutputStream fos = null;
               try {
                  fos = new FileOutputStream( ATTACHMENT_FILE );
                  fos.write( attachment );
                  fos.flush();
                  fos.close();
               } catch ( Exception e ) {
                  e.printStackTrace();
               }
               MimeBodyPart attachPart = new MimeBodyPart();
               try {
                  attachPart.attachFile( ATTACHMENT_FILE );
                  attachPart.setFileName( "attachment" );
               } catch ( IOException ex ) {
                  ex.printStackTrace();
               }
               multipart.addBodyPart( attachPart );
            }

            // set the multi-part as e-mail's content
            message.setContent( multipart );
         } else if ( notificationType.equals( "text" ) ) {
            message.setText( notificationMessage );
         }

         Transport.send( message );
      } catch ( MessagingException e ) {
         throw new RuntimeException( e );
      }

   }


   public byte[] getBLOB( RDSDatabase db, String sql ) {
      if ( sql == null || sql.isEmpty() )
         return null;

      byte[] value = null;
      Statement stmt = null;
      ResultSet res = null;
      try {
         stmt = db.connect().createStatement();
         res = db.executeTimedQuery( stmt, sql );
         if (res.next())
            value = res.getBytes( 1 );
      } catch (SQLException ex) {
         RDSLog.alert( "sql error, query = [%s]", sql );
         RDSLog.alert( ex );
      } finally {
         RDSDatabase.closeQuietly( res );
         RDSDatabase.closeQuietly( stmt );
      }

      return value;
   }


   @Override
   protected void poll() {
      // get a list of unsent notifications
      List<Map<String,String>> results = db.getResultMapList( 
            "SELECT * FROM notifications WHERE sent=FALSE ORDER BY seq");
      for (Map<String,String> result : results ) {
         if ( result.get( "recipient" ).isEmpty() ) {
            RDSLog.trace( "ignore email/text to [] recipient" );
            continue;
         }
         RDSLog.trace( "sending email/text to " + result.get( "recipient" ) );
         byte[] attachment = getBLOB( db, "SELECT attachment FROM notifications WHERE seq=" + result.get( "seq" ) );
         List<String> individuals= db.getValueList( 
               "SELECT individual FROM notificationGroups "
             + "WHERE groupName='" + result.get( "recipient" ) + "'" );
         try {
            if ( individuals.size() > 0 )
               // the recipient is a group; send to each individual member of the group
               for ( String individual : individuals )
                  send(individual, 
                       result.get( "message" ),
                       attachment );
            else
               // the recipient is an individual
               send(result.get( "recipient" ), 
                    result.get( "message" ),
                    attachment );
            RDSEvent.stop( "notifier" );
         } catch ( Exception e ) {
            RDSLog.alert( "exception thrown sending notification" );
            RDSEvent.start( "notifier" );
         }
         db.execute( "UPDATE notifications SET sent=TRUE WHERE seq=" + result.get( "seq" ) );
      }

      results = db.getResultMapList( "SELECT * FROM notificationIndividuals WHERE sendTest=TRUE");
      String hostAlias = db.getControl( "notifier", "hostAlias", "RDS" );
      for (Map<String,String> result : results ) {
         try {
            RDSLog.trace( "sending test email/text to " + result.get( "individual" ) );
            send(result.get( "individual" ), 
                 hostAlias + "\n" + TEST_MSG,
                 null );
            RDSEvent.stop( "notifier" );
         } catch ( Exception e ) {
            RDSLog.alert( "exception thrown sending notification" );
            RDSLog.alert( e.toString() );
            RDSEvent.start( "notifier" );
         }
         db.execute( "UPDATE notificationIndividuals SET sendTest=FALSE "
                   + "WHERE individual='" + result.get( "individual" ) + "'" );
      }
      
   }

   
   /*
    * --- main ---
    */

   /**
    * Application entry point.
    * 
    * @param   args  command-line arguments
    */
   public static void main( String... args ) {
      String id = "notifier";      
      RDSLog.trace( "application started, id = [%s]", id );

      Notifier app = new Notifier( id );

      app.run();
   }



}
