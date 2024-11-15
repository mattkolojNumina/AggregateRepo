package termApp;

import term.TerminalDriver;

import static rds.RDSLog.* ;

public class InitScreen
      extends AbstractScreen {

   public InitScreen( TerminalDriver term ) {
      super( term );
      saveParams();
      String mode = getParam( "mode" );
      String station = getParam( "station" );
      String login = getParam( "login" );
      String scanner = getParam( "scanner" );
      String printer = getParam( "printer" );
      String labeler = getParam( "labeler" );
      String scale = getParam( "scale" );

      trace(  "init, mode = [%s], station = [%s]", mode, station );
      inform( "  login   = [%s]", login); 
      inform( "  scanner = [%s]", scanner); 
      inform( "  printer = [%s]", printer); 
      inform( "  labeler = [%s]", labeler); 
      inform( "  scale   = [%s]", scale); 
      
      db.setRuntime(scanner + "-mode", "barcode");

   }

   /*
    * interface methods
    */

   public void initDisplay() {
      term.saveAtom( "screenName", "Init" );

      setAllDatabase();
      
      doLogout();
   }

   public void handleTick() {

   }

}