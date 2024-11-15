package victoryApp.gui;

import java.util.*;

import static victoryApp.gui.GUIConstants.*;

public class Footer {

  public GUIRectangle footerBox;
  public GUIText operatorInfo;
  public GUIText lastResponseInfo;
  public GUIButton logout;
  public GUIButton message;
  public GUIButton changeArea;
  public GUIButton changeTask;
  public GUIState state;

  public static final float DEFAULT_FOOTER_TEXT_SIZE = 125f;

  public Footer(GUIState state) {
      this.state = state;

      footerBox = 
      new GUIRectangle(FOOTER_BOX_TAG, -4, FOOTER_Y, -1f,
                       SCREEN_WIDTH+8,SCREEN_HEIGHT-FOOTER_Y,WHITE,
                       1, BLACK);
  
      if (state.rectangles==null)
        state.rectangles = new ArrayList<GUIRectangle>();
      state.rectangles.add(footerBox);

      operatorInfo =
      new GUIText(OPERATOR_ID_TAG, 
                  50, FOOTER_Y+10, 0f,
                  DEFAULT_FOOTER_TEXT_SIZE, BLACK, "Operator ID: ",
                  DEFAULT_IS_BOLD, DEFAULT_IS_ITALIC);
             
      lastResponseInfo =
      new GUIText(LAST_RESPONSE_TAG, 
                  50, FOOTER_Y+60, 0f,
                  DEFAULT_FOOTER_TEXT_SIZE, BLACK, "Last Response: ",
                  DEFAULT_IS_BOLD, DEFAULT_IS_ITALIC);
      
      if (state.texts==null)
        state.texts = new ArrayList<GUIText>();
      state.texts.add(operatorInfo);
      state.texts.add(lastResponseInfo);

      message =
      new GUIButton(SEND_MESSAGE_BUTTON_TAG, 
                    SCREEN_WIDTH/2 - 315, FOOTER_Y+20, 0f,
                    300, 100,
                    DEFAULT_BUTTON_BACKGROUND,
                    DEFAULT_BUTTON_TEXT_COLOR,
                    DEFAULT_BUTTON_BORDER_COLOR, -1,
                    "Message", 90f);

      logout =
      new GUIButton(LOGOUT_BUTTON_TAG, 
                    SCREEN_WIDTH/2 + 600, FOOTER_Y+20, 0f,
                    300, 100,
                    DEFAULT_BUTTON_BACKGROUND,
                    DEFAULT_BUTTON_TEXT_COLOR,
                    DEFAULT_BUTTON_BORDER_COLOR, -1,
                    "Logout", 90f);

      changeArea =
      new GUIButton(CHANGE_AREA_BUTTON_TAG, 
                    SCREEN_WIDTH/2 + 295, FOOTER_Y+20, 0f,
                    300, 100,
                    DEFAULT_BUTTON_BACKGROUND,
                    DEFAULT_BUTTON_TEXT_COLOR,
                    DEFAULT_BUTTON_BORDER_COLOR, -1,
                    "Area", 90f);

      changeTask =
      new GUIButton(CHANGE_TASK_BUTTON_TAG, 
                    SCREEN_WIDTH/2-10, FOOTER_Y+20, 0f,
                    300, 100,
                    DEFAULT_BUTTON_BACKGROUND,
                    DEFAULT_BUTTON_TEXT_COLOR,
                    DEFAULT_BUTTON_BORDER_COLOR, -1,
                    "Task", 90f);

      if (state.buttons==null)
        state.buttons = new ArrayList<GUIButton>();
      state.buttons.add(logout); 
      state.buttons.add(message);   
      state.buttons.add(changeArea);   
      state.buttons.add(changeTask);   
  }

  //this changes the operator ID tied to the object,
  //but doesn't push it to the screen.
  //if you find that you need to update this outside of a screen transition,
  //which would pre-set this data for you, 
  //call app.updateText(this.getGUIState().getFooter().operatorInfo)
  //right after you call this setOperatorID method
  public void setOperatorID(String operatorID) {
    //Trim the operator ID if it is too long (EX: Full name for operator)
    operatorID = (operatorID.length()>12) ? operatorID.substring(0, 9).concat("...") : operatorID;
    operatorInfo.text += " " + operatorID;
    for(GUIText t : state.texts) {
      if(t.tag == OPERATOR_ID_TAG) {
        state.texts.set(state.texts.indexOf(t), operatorInfo);      
        break;
      }
    }
  }

  //changes the objects text, but does not push to screen.
  //use app.updateText(this.getGUIState().getFooter().lastResponseInfo)
  //to make the screen update. Screen.java handle____ methods
  //will do this for you
  //To prevent bug of text appending, update the value of the text to the phrase first
  //Stored in Screen: String lastResponse = interpretPhrase("FOOTER_LAST_RESPONSE");
  //   state.getFooter().lastResponseInfo.text = lastResponse;
  //   app.updateText(this.getGUIState().getFooter().lastResponseInfo)
  public void setLastResponse(String lastResponse) {
    //Trim the last repsonse message if it is too long.
    lastResponse = (lastResponse.length()>12) ? lastResponse.substring(0, 9).concat("...") : lastResponse;
    lastResponseInfo.text += " " + lastResponse;
    for(GUIText t : state.texts) {
      if(t.tag == LAST_RESPONSE_TAG) {
        state.texts.set(state.texts.indexOf(t), lastResponseInfo);      
        break;
      }
    }
  }

  public void hideLogoutButton() {
    this.state.buttons.remove(this.logout);
  }
  public void hideMessageButton() {
    this.state.buttons.remove(this.message);
  }
  public void hideChangeTaskButton() {
    this.state.buttons.remove(this.changeTask);
  }
  public void hideChangeAreaButton() {
    this.state.buttons.remove(this.changeArea);
  }
  public void showChangeAreaButton() {
    this.state.buttons.add(this.changeArea);
  }
  public void showChangeTaskButton() {
    this.state.buttons.add(this.changeTask);
  }

  public void clearOperatorID() {
    setOperatorID("");
  }
}
