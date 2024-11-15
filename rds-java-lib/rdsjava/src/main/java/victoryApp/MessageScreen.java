package victoryApp;

import java.sql.PreparedStatement;

import static victoryApp.gui.GUIConstants.*;

public class MessageScreen extends AbstractVictoryScreen {

  public MessageScreen(VictoryApp app) {
    super(app);
  }  


  //handle_______

  public boolean handleInit() {
    super.handleInit();
    this.getGUIState().getFooter().hideMessageButton();
    this.getGUIState().getFooter().hideChangeAreaButton();
    this.getGUIState().getFooter().hideChangeTaskButton();

    int padding = 100;
    setPromptBoxDimensions(PROMPT_BOX_X, 2*padding+20, PROMPT_BOX_WIDTH-GUI_PADDING, SCREEN_HEIGHT-(5*padding));
    promptEntry.x = PROMPT_BOX_X+(2*GUI_PADDING);
    promptEntry.y = SCREEN_HEIGHT/4-20;
    promptEntry.width = PROMPT_BOX_WIDTH-(2*PROMPT_BOX_X)-GUI_PADDING;
    promptEntry.height = SCREEN_HEIGHT/4+padding-20;

    addCancelButton(SCREEN_WIDTH-BUTTON_WIDTH-120, SCREEN_HEIGHT-BUTTON_WIDTH-30);
    return true;
  }

  public boolean handleTick() {
    return true;
  }

  public boolean handleText(int tag, String text) {
    try {
      String prep = "INSERT victoryMessages SET message=?, toOperator='web', fromOperator=?" ;
      PreparedStatement pstmt = db.connect().prepareStatement(prep);
      pstmt.setString(1,text);
      pstmt.setString(2,getOperatorID());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch(Exception e) {
      e.printStackTrace() ; 
    }
    trace("user sent a message to the webpage");
    this.setNextScreen(getScreenParam("previousScreen"));
    return true;
  }

  @Override
  public boolean handleCancel() {
    this.setNextScreen(getScreenParam("previousScreen"));
    return false;
  }
}
