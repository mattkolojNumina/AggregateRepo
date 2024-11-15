package victoryApp;

import java.io.*;
import java.net.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.util.Date;
import java.sql.PreparedStatement;
import java.time.Instant;

import rds.*;

import victoryApp.gui.*;
import static victoryApp.gui.GUIConstants.*;

/**
 * Worker thread extension to handle a Victory Voice client connection.
 */
public class VictoryWorker
    extends Thread {
  protected RDSDatabase db;
  protected Gson gson;
  protected Deque<GUIResponse> in;
  protected Deque<String> out;
  protected Socket socket = null;
  private StringBuffer current;
  protected int batteryLevel = 0;
  protected int rssi = 0;
  protected String bssid = "";
  protected long lastMessage = 0;
  protected long lastUpdate = 0;
  protected long lastResponseTime = 0;
  protected int autoLogout = 30 * 60 * 1000; // 30 minutes
  private final long MAX_QUIET = 60 * 1000; // 60 seconds
  protected String deviceID = "";
  protected String deviceName = "";
  protected String ipAddress = "";
  protected String version = "";
  protected String voiceVersion = "";
  protected String releaseMode = "";
  protected String androidVersion = "";
  protected String buildModel = "";
  protected String deviceOSBuild = "";
  protected String pairedDeviceList = "";
  public String resourceDir = "/home/rds/app/resource/";
  protected Screen screen = null;
  protected String operatorID = null;
  protected String lastResponse = null;

  protected Locale locale = Locale.US;
  protected int sessionId;

  /**
   * Worker thread extension to handle a Victory Voice client connection.
   * @param socket TCP socket.
   * @param dbHost RDS Database host.
   * @param sessionId Thread SessionID.
   */
  public VictoryWorker(Socket socket, String dbHost, int sessionId) {
    db = new RDSDatabase(dbHost);
    gson = new Gson();
    current = new StringBuffer();
    this.sessionId = sessionId;

    in = new LinkedList<GUIResponse>();
    out = new LinkedList<String>();

    this.socket = socket;
    int timeout = 100;
    try {
      this.socket.setSoTimeout(timeout);
      inform("socket timeout set to %d milliseconds", timeout);
    } catch (Exception e) {
      alert("unable to set socket timeout");
      e.printStackTrace();
    }

    lastMessage = System.currentTimeMillis();
    lastUpdate = lastMessage;
    lastResponseTime = lastMessage;
    autoLogout = RDSUtil.stringToInt(db.getControl("victory", "autoLogout", ""),30) * 60 * 1000;

    inform("connection made");
  }

  /**
   * Runs a client socket connection in a new thread.
   */
  public void run() {
    try {
      InputStream inStream = socket.getInputStream();
      OutputStream outStream = socket.getOutputStream();
      while (socket.isConnected()) {
        if (!cycle(inStream, outStream))
          break;
      }
      trace("connection closed by client");
      // assume this is a deliberate logout on the part of the client.
      // close out productivity tracking and clear any params
    } catch (Exception e) {
      alert("exception: [%s]", e.getMessage());
      e.printStackTrace();
    } finally {
      try {
        onDisconnect();
        inform("closing connection");
        db.disconnect();
        socket.close();
        inform("connection closed");
      } catch (Exception e) {
        alert("error closing connection: [%s]", e.getMessage());
      }
    }
  }

  /**
   * Cycles input and output over the TCP socket between client/server.
   * @param inStream Input stream from client.
   * @param outStream Output stream to client.
   * @return True if cycle completes, False if error.
   */
  boolean cycle(InputStream inStream, OutputStream outStream) {
    boolean ok = true;

    try {
      while (true) {
        String send = nextSend();
        if (send == null)
          break;
        /*  
         * This traces the full JSON packets we send.
         * They can be very large and make it difficult to find useful messages.
         * This will exclude the resourceList being traced as it can be large.
         */
        if (db.getControl("victory", "traceJson", "false").equals("true")) {
          if(!send.contains("\"command\":\"resourceList\""))
            trace("send [%s]", send);
        }
        outStream.write(send.getBytes());
        outStream.write("\r".getBytes());
        outStream.flush(); // Ensure data is sent immediately
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      int c = inStream.read();
      switch (c) {
        case -1:
          return false;
        case 0:
          break;
        case 0x0d:
          break;
        case 0x0a:
          handleMessage(current.toString());
          current.delete(0, current.length());
          break;
        default:
          current.append((char) c);
          break;
      }
    } catch (SocketTimeoutException e) {
      long now = System.currentTimeMillis();
      if (now - lastMessage > MAX_QUIET) {
        // assume this is an accidental disconnect.
        // VictoryApp.java will close out the productivity tracking, but store params
        Date date = Date.from(Instant.ofEpochMilli(lastMessage));
        alert(
          "client is too quiet: lastMessage time [%s]",
          date, MAX_QUIET
        );
        logVictoryError("clientTooQuiet", String.format("rssi %s bssid %s", rssi, bssid), "");
        return false;
      }
      if (!onTick())
        return false;
    } catch (SocketException e) {
      alert("Connection closed");
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    return ok;
  }

  /**
   * Handles all incoming commands from client, refer to latest API reference for Victory App.
   */
  private void handleMessage(String json) {
    try {
      JsonElement root = JsonParser.parseString(json).getAsJsonObject();
      String command = root.getAsJsonObject().get("command").getAsString();
      JsonElement data = root.getAsJsonObject().get("data");
      if (db.getControl("victory", "traceJson", "false").equals("true")) {
        if (!command.equals("heartBeat"))
          inform(json);
      }
      if (command.equals("connect"))
        handleConnect(data);
      else if (command.equals("voiceReady"))
        handleVoiceReady(data);
      else if (command.equals("heartBeat"))
        handleHeartBeat(data);
      else if (command.equals("guiResponse"))
        handleGuiResponse(data);
      else if (command.equals("voiceResponse"))
        handleVoiceResponse(data);
      else if (command.substring(0, 7).equals("setUser"))
        handleUserPreferenceUpdate(root);
      else if (command.equals("errorMessage"))
        handleErrorMessage(data);
      else if (command.equals("logMessage"))
        handleLogMessage(data);
      else if (command.equals("logDump"))
        handleLogDump(data);
      else
        alert("unknown command %s", command);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates Resource List to send to client.
   * @param resDir Path to Resource folder on RDS server.
   * @return Resources JSON as String.
   */
  protected String resources(String resDir) {
    JsonObject data = new JsonObject();

    Map<String, String> userPreferences = getUserPreferences();
    if ((userPreferences == null) || (userPreferences.isEmpty())) {
      userPreferences = getDefaultPreferences();
      alert("Operator [%s] has no default preferences, creating defaults", getOperatorID());
      setDefaultPreferences();
    }
    String userVolume = userPreferences.get("volume");
    String userPitch = userPreferences.get("pitch");
    String userRate = userPreferences.get("rate");
    String language = userPreferences.get("language");

    inform("language = '%s'", language);
    switch (language) {
      case "spanish":
        locale = new Locale("es", "US");
        break;
      case "english":
        locale = Locale.US;
        break;
      default:
        break;
    }

    JsonElement alphaMap = parseJsonFile(resDir + locale, "alphaMap.json");
    data.add("alphaMap", alphaMap);

    JsonElement numberMap = parseJsonFile(resDir+locale, "numberMap.json");
    data.add("numberMap", numberMap);

    // JsonElement decodingGraph = parseJsonFile(resDir+locale, "decodingGraph.json");
    // data.add("decodingGraph", decodingGraph);

    data.addProperty("locale", locale.toString());
    if (userVolume != null)
      data.addProperty("volume", userVolume);
    if (userPitch != null)
      data.addProperty("pitch", userPitch);
    if (userRate != null)
      data.addProperty("rate", userRate);

    String detailedLogging = db.getControl("victory", "enableGlobalDetailedLogging", "false");
    data.addProperty("detailedLogging ", detailedLogging);

    // KEEN RESOURCE LIST ITEMS
    String timeoutForGoodMatch = db.getControl("victory", "KASRVadTimeoutEndSilenceForGoodMatch", "0.5");
    String timeoutForAnyMatch = db.getControl("victory", "KASRVadTimeoutEndSilenceForAnyMatch", "0.5");
    String timeoutMaxDuration = db.getControl("victory", "KASRVadTimeoutMaxDuration", "30.0");
    String timeoutForNoSpeech = db.getControl("victory", "KASRVadTimeoutForNoSpeech", "5.0");
    JsonElement constantsMap = parseJsonFile(resDir + locale, "constants.json");
    String repeatPhrase = constantsMap.getAsJsonObject().get("repeatPhrase").getAsString();
    String wakeWord = constantsMap.getAsJsonObject().get("wakeWord").getAsString();
    String sleepWord = constantsMap.getAsJsonObject().get("sleepWord").getAsString();
    String terminalPhrase = constantsMap.getAsJsonObject().get("terminalPhrase").getAsString();

    data.addProperty("repeatPhrase", repeatPhrase);
    data.addProperty("wakeWord", wakeWord);
    data.addProperty("sleepWord", sleepWord);
    data.addProperty("terminalPhrase", terminalPhrase);
    
    data.addProperty("KASRVadTimeoutEndSilenceForGoodMatch", timeoutForGoodMatch);
    data.addProperty("KASRVadTimeoutEndSilenceForAnyMatch", timeoutForAnyMatch);
    data.addProperty("KASRVadTimeoutMaxDuration", timeoutMaxDuration);
    data.addProperty("KASRVadTimeoutForNoSpeech", timeoutForNoSpeech);

    data.addProperty("loginUsesTerminalPhrase", true);
    data.addProperty("numericUsesTerminalPhrase", true);
    data.addProperty("alphanumericUsesTerminalPhrase", true);

    JsonObject output = new JsonObject();
    output.addProperty("command", "resourceList");
    output.add("data", data);

    return output.toString();
  }

  /**
   * Parses a given JSON file in a given directory into a JSON element.
   * @param resDir Path to JSON directory.
   * @param fileName JSON file to parse.
   * @return JsonElement of JSON file.
   */
  private JsonElement parseJsonFile(String resDir, String fileName) {
    try(FileReader fileReader = new FileReader(resDir + "/" + fileName)) {
      JsonReader jsonReader = new JsonReader(fileReader);
      JsonElement jsonElement = JsonParser.parseReader(jsonReader);
      fileReader.close();
      return jsonElement;
    } catch (Exception e) {
      if (e instanceof FileNotFoundException) {
        alert("Grammar file not found for grammar [%s]", fileName);
        e.printStackTrace();
        return null;
      } else if (e instanceof IOException) {
        alert("IOException occured trying to load grammar [%s]", fileName);
        e.printStackTrace();
        return null;
      } else if (e instanceof JsonIOException) {
        alert("JsonIOException occured trying to load grammar [%s]", fileName);
        e.printStackTrace();
        return null;
      } else if (e instanceof JsonSyntaxException) {
        alert("JsonSyntaxException occured trying to load grammar [%s]", fileName);
        e.printStackTrace();
        return null;
      } else {
        alert("Exception occured trying to load grammar [%s]", fileName);
        e.printStackTrace();
        return null;
      }
    }
  }

  /**
   * Handles telemetry data recieved from the client on connect.
   * @param data Telemetry data from client.
   */
  private void handleConnect(JsonElement data) {
    //View all connection info from device
    // trace(data.toString()) ;
    deviceID = data.getAsJsonObject().get("deviceID").getAsString();
    inform("  deviceID: %s", deviceID);

    // Victory apk version 1.3.1+ will report device name back to server
    try {
      deviceName = data.getAsJsonObject().get("deviceName").getAsString();
      inform("  deviceName: %s", deviceName);
    } catch (NullPointerException npe) {
      trace("  No deviceName sent by client");
    }
    // Victory apk version 1.3.1+ will report ip back to server
    try {
      ipAddress = data.getAsJsonObject().get("ipAddress").getAsString();
      inform("  ipAddress: %s", ipAddress);
    } catch (NullPointerException npe) {
      trace("  No ipAddress sent by client");
    }
    // Victory apk version 0.3.3+ will report version back to server
    try {
      version = data.getAsJsonObject().get("version").getAsString();
      inform("  apkVersion: %s", version);
    } catch (NullPointerException npe) {
      trace("  No apkVersion number sent by client");
    }
    // Get voiceVersion i.e. "KEEN..."
    try {
      voiceVersion = data.getAsJsonObject().get("voiceVersion").getAsString();
      inform("  voiceVersion: %s", voiceVersion);
    } catch (NullPointerException npe) {
      trace("  No voiceVersion number sent by client");
    }
    // Get Release mode "debug"/"release"
    try {
      releaseMode = data.getAsJsonObject().get("releaseMode").getAsString();
      inform("  releaseMode: %s", releaseMode);
    } catch (NullPointerException npe) {
      trace("  No releaseMode number sent by client");
    }
    // Get android version i.e. "11"
    try {
      androidVersion = data.getAsJsonObject().get("androidVersion").getAsString();
      inform("  androidVersion: %s", androidVersion);
    } catch (NullPointerException npe) {
      trace("  No androidVersion number sent by client");
    }
    // Get build model i.e. "TC21"
    try {
      buildModel = data.getAsJsonObject().get("buildModel").getAsString();
      inform("  buildModel: %s", buildModel);
    } catch (NullPointerException npe) {
      trace("  No buildModel number sent by client");
    }
    // Get device OS i.e. "11-26-05.00-RG-U07-STD-HEL-04"
    try {
      deviceOSBuild = data.getAsJsonObject().get("deviceOSBuild").getAsString();
      inform("  deviceOSBuild: %s", deviceOSBuild);
    } catch (NullPointerException npe) {
      trace("  No deviceOSBuild number sent by client");
    }
    // Get bluetooth devices i.e. "RS5100 S20319523021103 , Zebra HS3100"
    try {
      pairedDeviceList = data.getAsJsonObject().get("pairedDeviceList").getAsString();
      inform("  pairedDeviceList: %s", pairedDeviceList);
    } catch (NullPointerException npe) {
      trace("  No pairedDeviceList number sent by client");
    }

    //Update/create device information in victoryDevices on connection
    handleDeviceParams();

    //Check if the customer has valid licensed connections remaining
    if(!validLicense()) {
      alert( "no remaining licensed connections" );
      quit();
      return;
    }
  }

  /**
   * Determines validity of how many licenses a customer is allowed to use
   * @return True if there are remaining valid licenses. False otherwise.
   */
  protected boolean validLicense() {
    try {
      String val = db.getControl( "victory", "checksum", "9999999" );
      int maxUsers = Integer.valueOf( val.substring( 4, 7 ) );
      int currentUsers = Integer.valueOf( db.getValue( 
        "SELECT COUNT(*) FROM proOperators " +
        "WHERE device <> '' AND device IS NOT NULL", "0" )
      );
      trace("Max users: [%d], Current users: [%d]", maxUsers, currentUsers );
      if(currentUsers+1 > maxUsers)
        return false;
      return true;
    } catch ( Exception e ) {
      alert("Exception occured trying to validate licenses: " + e.toString());
      return false;
    }
  }

  /**
   * Handles setting all device parameters that are passed by the client.
   * If the battery, rssi, or bssid are null/empty, use the last recorded value in db
   */
  protected void handleDeviceParams() {
    db.execute(
      "INSERT INTO victoryDevices " +
      "(deviceID, deviceName, ipAddress, apkVersion, voiceVersion,  " +
      "releaseMode, buildModel, androidVersion, deviceOSBuild, " +
      "pairedDeviceList, battery, rssi, bssid) " +
      "VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', LEFT('%s', 255), '%s', '%s', '%s') " +
      "ON DUPLICATE KEY UPDATE " +
      "deviceID=VALUES(deviceID), " +
      "deviceName=VALUES(deviceName), " +
      "ipAddress=VALUES(ipAddress), " +
      "apkVersion=VALUES(apkVersion), " +
      "voiceVersion=VALUES(voiceVersion), " +
      "releaseMode=VALUES(releaseMode), " +
      "buildModel=VALUES(buildModel), " +
      "androidVersion=VALUES(androidVersion), " +
      "deviceOSBuild=VALUES(deviceOSBuild), " +
      "pairedDeviceList=COALESCE(LEFT(VALUES(pairedDeviceList), 255)), " +
      "battery=COALESCE(VALUES(battery), battery), " +
      "rssi=COALESCE(VALUES(rssi), rssi), " +
      "bssid=COALESCE(VALUES(bssid), bssid)",
      deviceID, deviceName, ipAddress, version, voiceVersion, releaseMode,
      buildModel, androidVersion, deviceOSBuild,
      pairedDeviceList, batteryLevel, rssi, bssid
    );
  }

  /**
   * Recieves the same connection response as "connect" does.
   * Currently not used for anything server side.
   * @param data Data from client.
   */
  private void handleVoiceReady(JsonElement data) {
    // trace(data.toString()) ;
  }

  /**
   * Handles heartbeat message from client.
   * @param data Data from client.
   */
  private void handleHeartBeat(JsonElement data) {
    if ((operatorID != null) && (!operatorID.isEmpty()))
      db.execute(
        "REPLACE INTO victoryParams SET operatorID='%s', " +
        "name='lastHeartbeat', value=NOW()", operatorID
      );
    String batteryLevelString = data.getAsJsonObject().get("batteryLevel").getAsString();
    String rssiString = data.getAsJsonObject().get("rssi").getAsString();
    try {
      batteryLevel = Integer.parseInt(batteryLevelString);
      rssi = Integer.parseInt(rssiString);
      bssid = data.getAsJsonObject().get("bssid").getAsString();
      long now = System.currentTimeMillis();
      //Every 30 seconds update the victoryDevices table with any new information about the device
      if (now - lastUpdate > 30000) {
        handleDeviceParams();
        lastUpdate = now;
      }
      lastMessage = System.currentTimeMillis();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Handles response data from client.
   * @param data Data from client.
   */
  private void handleGuiResponse(JsonElement data) {
    JsonObject object = data.getAsJsonObject();
    int tag = 0;
    if (object.get("tag") != null)
      tag = object.get("tag").getAsInt();
    String source = "";
    if (object.get("source") != null)
      source = object.get("source").getAsString();
    String text = "";
    if (object.get("text") != null)
      text = object.get("text").getAsString();

    debug("response tag [%d] source [%s] text [%s]", tag, source, text);
    GUIResponse guiResponse = new GUIResponse(tag, source, text);
    in.add(guiResponse);
  }

  /**
   * Handles Voice Responses from client. Base Level.
   * @param data Data from client.
   */
  private void handleVoiceResponse(JsonElement data) {
    trace(data.toString());
  }

  /**
   * Handles the updating of an Operator preference in victoryUserPreferences table.
   * @param data Data from client.
   */
  private void handleUserPreferenceUpdate(JsonElement data) {
    String param = data.getAsJsonObject().get("command").getAsString().substring(7).toLowerCase();
    double value = data.getAsJsonObject().get("data").getAsDouble();
    if (!exists(screen.getOperatorID()))
      return;
    trace("update %s to %.3f for user %s", param, value, screen.getOperatorID());
    db.execute(
      "REPLACE INTO victoryUserPreferences " +
      "SET operatorID='%s', name='%s', value='%.3f'",
      screen.getOperatorID(), param, value
    );
  }

  /**
   * Handles error messages recevied from client.
   * @param data Error message from client.
   */
  private void handleErrorMessage(JsonElement data) {
    if(data.toString().contains("Deviation")) {
      debug("warning:" + data.toString()); //Datawedge key deviations will only show as debug statements.
    }
    else {
      alert("errorMessage:" + data.toString());
    }
  }

  /**
   * Handles log messages received from client.
   * @param data Log message from client.
   */
  private void handleLogMessage(JsonElement data) {
    inform("logMessage:" + data.toString());
  }

  /**
   * Recevies a complete log dump from a device
   * WARNING: These can become quite long, depending  
   * on how long the app has been running.
   * @param data Complete logcat output from client's device.
   */
  private void handleLogDump(JsonElement data) {
    inform("Device dumped logs back to server");
    //Potentially log dump to a databse table?
    try {
      String prep = "INSERT INTO victoryLog SET operatorId=?, deviceID=?, `log`=?" ;
      PreparedStatement pstmt = db.connect().prepareStatement(prep);
      pstmt.setString(1,getOperatorID());
      pstmt.setString(2,deviceID);
      pstmt.setString(3,data.toString());
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch(Exception e) {
      alert("Error dumping device logs");  
      e.printStackTrace() ; 
    }
  }

  /**
   * Sends the Resource List to the client.
   * @param resDir Path to the resources directory on RDS server.
   */
  public void sendResourceList(String resDir) {
    try {
      String message = resources(resDir);
      message = message.replace("\\", "");
      out.add(message);
    } catch (Exception ex) {
      alert("Failed to send resourceList JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Sends a GUIState object (entire screen) to client.
   */
  public void sendScreen(Screen screen) {
    try {
      send("GUIState", gson.toJsonTree(screen.getGUIState()));
    } catch (Exception ex) {
      alert("Failed to send screen JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Sends a GUIState object (entire screen) to client with phrases removed.
   * @param screen
   */
  public void updateScreen(Screen screen) {
    try {
      screen.getGUIState().phrases = null;
      send("GUIState", gson.toJsonTree(screen.getGUIState()));
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Sends a vibrate request to client device for specified msec.
   * @param msec Number of milliseconds to vibrate device.
   */
  public void vibrateUnit(int msec) {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "vibrate");
      JsonObject msObject = new JsonObject();
      msObject.addProperty("ms", msec);
      output.add("data", msObject);
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send vibrate JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Prompts a device to dump it's log and send to server
   */
  public void dumpLogs() {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "dumpLogs");
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send dumpLogs JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Creates a toast event popup on device with message
   * @param message Error message to display.
   */
  public void error(String message) {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "error");
      JsonObject text = new JsonObject();
      text.addProperty("text", message);
      output.add("data", text);
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send error JSON packet");
      ex.printStackTrace();
    }
  }

  /** 
   * Creates a toast event popup on device with message
   * @param message Formatted string.
   * @param args Argument for formatted string.
   */
  public void error(String message, Object... args) {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "error");
      JsonObject text = new JsonObject();
      text.addProperty("text", String.format(message, args));
      output.add("data", text);
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send error JSON packet");
      ex.printStackTrace();
    }
  }

  /** 
   * Closes the Victory app on the client device.
   */
  public void quit() {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "quit");
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send quit JSON packet");
      ex.printStackTrace();
    }
  }

  // NOTE: if you want to put one of these methods in a handleTick,
  // you'll essentially blast the Android device with more JSON packets
  // than it can keep up with. As such, please limit the rate
  // at which you call update___() methods to once per 10 ticks or fewer.

  /**
   * Updates/creates a GUIText object on screen.
   * @param text GUIText object to update.
   */
  public void updateText(GUIText text) {
    try {
      send("GUIText", gson.toJsonTree(text));
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Deletes a GUIText object on screen.
   * @param obj GUIText object to delete.
   */
  public void deleteText(GUIText obj) {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "deleteGUIText");
      JsonObject tagObject = new JsonObject();
      tagObject.addProperty("tag", obj.tag);
      output.add("data", tagObject);
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Updates/creates a GUICircle object on screen.
   * @param circle GUICircle object to update.
   */
  public void updateCircle(GUICircle circle) {
    try {
      send("GUICircle", gson.toJsonTree(circle));
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Deletes a GUICircle object on screen.
   * @param obj GUICircle object to delete.
   */
  public void deleteCircle(GUICircle obj) {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "deleteGUICircle");
      JsonObject tagObject = new JsonObject();
      tagObject.addProperty("tag", obj.tag);
      output.add("data", tagObject);
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Updates/creates a GUIRectangle object on screen.
   * @param rectangle GUIRectangle object to update.
   */
  public void updateRectangle(GUIRectangle rectangle) {
    try {
      send("GUIRectangle", gson.toJsonTree(rectangle));
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Deletes a GUIRectangle object on screen.
   * @param obj GUIRectangle object to delete.
   */
  public void deleteRectangle(GUIRectangle obj) {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "deleteGUIRectangle");
      JsonObject tagObject = new JsonObject();
      tagObject.addProperty("tag", obj.tag);
      output.add("data", tagObject);
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Updates/creates a GUIButton on screen.
   * @param button GUIButton to update.
   */
  public void updateButton(GUIButton button) {
    try {
      send("GUIButton", gson.toJsonTree(button));
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Deletes a GUIButton object on screen.
   * @param obj GUIButton object to delete.
   */
  public void deleteButton(GUIButton obj) {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "deleteGUIButton");
      JsonObject tagObject = new JsonObject();
      tagObject.addProperty("tag", obj.tag);
      output.add("data", tagObject);
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Updates/creates a GUIEntry object on screen.
   * @param entry GUIEntry object to update.
   */
  public void updateEntry(GUIEntry entry) {
    try {
      send("GUIEntryPW", gson.toJsonTree(entry));
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Deletes a GUIEntry object on screen.
   * @param obj GUIEntry object to delete.
   */
  public void deleteEntry(GUIEntry obj) {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "deleteGUIEntry");
      JsonObject tagObject = new JsonObject();
      tagObject.addProperty("tag", obj.tag);
      output.add("data", tagObject);
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Updates/creates a GUIEntryPW object on screen.
   * @param entry GUIEntryPW object to update.
   */
  public void updateEntryPW(GUIEntryPW entry) {
    try {
      send("GUIEntryPW", gson.toJsonTree(entry));
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Deletes a GUIEntryPW object on screen.
   * @param obj GUIEntryPW object to delete.
   */
  public void deleteEntryPW(GUIEntryPW obj) {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "deleteGUIEntryPW");
      JsonObject tagObject = new JsonObject();
      tagObject.addProperty("tag", obj.tag);
      output.add("data", tagObject);
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Updates/creates a GUIImage object on screen.
   * @param entry GUIImage object to update.
   */
  public void updateImage(GUIImage image) {
    try {
      send("GUIImage", gson.toJsonTree(image));
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Deletes a GUIImage object on screen.
   * @param obj GUIImage object to delete.
   */
  public void deleteImage(GUIImage obj) {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "deleteGUIImage");
      JsonObject tagObject = new JsonObject();
      tagObject.addProperty("tag", obj.tag);
      output.add("data", tagObject);
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Updates/creates a GUIImageBase64 object on screen.
   * @param entry GUIImageBase64 object to update.
   */
  public void updateImageBase64(GUIImageBase64 image) {
    try {
      send("GUIImageBase64", gson.toJsonTree(image));
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Deletes a GUIImageBase64 object on screen.
   * @param obj GUIImageBase64 object to delete.
   */
  public void deleteImageBase64(GUIImageBase64 obj) {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "deleteGUIImageBase64");
      JsonObject tagObject = new JsonObject();
      tagObject.addProperty("tag", obj.tag);
      output.add("data", tagObject);
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Updates/creates a GUISpinner object on screen.
   * @param entry GUISpinner object to update.
   */
  public void updateSpinner(GUISpinner spinner) {
    try {
      send("GUISpinner", gson.toJsonTree(spinner));
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Deletes a GUISpinner object on screen.
   * @param obj GUISpinner object to delete.
   */
  public void deleteSpinner(GUISpinner obj) {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "deleteGUISpinner");
      JsonObject tagObject = new JsonObject();
      tagObject.addProperty("tag", obj.tag);
      output.add("data", tagObject);
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Creates a TTS request for want the client to SAY something.
   * @param phrase Phrase with SSML tags to say.
   */
  public void updatePhrase(String[] phrase) {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "speechRequest");
      JsonObject phraseObject = new JsonObject();
      phraseObject.add("phrases", gson.toJsonTree(phrase));
      output.add("data", phraseObject);
      out.add(output.toString());
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Creates a request for the client to listen for voice input.
   */
  public void requestVoiceResponse() {
    if (this.screen.getGUIState().grammar == null)
      return;
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "voiceRequest");
      output.add("data", gson.toJsonTree(this.screen.getGUIState().grammar));
      String message = output.toString().replace("\\", "");
      out.add(message);
    } catch (Exception ex) {
      alert("Failed to send voiceResponse JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Creates a request for the client to enable a Bluetooth scanner.
   */
  public void requestScan() {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "scanRequest");
      JsonObject scanEnabledObject = new JsonObject();
      scanEnabledObject.add("scanEnabled", gson.toJsonTree(this.screen.getGUIState().scanEnabled));
      output.add("data", scanEnabledObject);
      String message = output.toString().replace("\\", "");
      out.add(message);
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Creates a request to show the slide out settings panel on client.
   */
  public void requestSettings() {
    try {
      JsonObject output = new JsonObject();
      output.addProperty("command", "enableSettings");
      JsonObject enableSettingsObject = new JsonObject();
      enableSettingsObject.add("enableSettings", gson.toJsonTree(this.screen.getGUIState().enableSettings));
      output.add("data", enableSettingsObject);
      String message = output.toString().replace("\\", "");
      out.add(message);
    } catch (Exception ex) {
      alert("Failed to send JSON packet");
      ex.printStackTrace();
    }
  }

  /**
   * Determines if a GUIRepsonse is voice source.
   * @param response GUIResponse from client.
   * @return True if voice response, False otherwise.
   */
  public boolean isVoice(GUIResponse response) {
    return (response != null) && (response.source.equals("Voice"));
  }

  /**
   * Determines if a GUIRepsonse is scan source.
   * @param response GUIResponse from client.
   * @return True if scan response, False otherwise.
   */
  public boolean isScan(GUIResponse response) {
    return (response != null) && (response.source.equals("Scan"));
  }

  /**
   * Determines if a GUIRepsonse is button source.
   * @param response GUIResponse from client.
   * @return True if button response, False otherwise.
   */
  public boolean isButton(GUIResponse response, int tag) {
    return (response != null) && (response.source.equals("Button")) && (response.tag == tag);
  }

  /**
   * Determines if a GUIRepsonse is text source.
   * @param response GUIResponse from client.
   * @return True if text response, False otherwise.
   */
  public boolean isEntry(GUIResponse response, int tag) {
    return (response != null) && (response.source.equals("Text")) && (response.tag == tag);
  }

  /**
   * Gets the text from a GUIResponse from client.
   * @param response GUIResponse from client.
   * @return String of GUIRepsonse text.
   */
  public String getText(GUIResponse response) {
    if (response != null)
      return response.text;
    else
      return "";
  }

  /**
   * Generic method to send a command to a client.
   * @param command Command to send to client.
   * @param data Data properties for the command.
   */
  public void send(String command, JsonElement data) {
    JsonObject output = new JsonObject();
    output.addProperty("command", command);
    output.add("data", data);
    String message = output.toString();
    out.add(message);
  }

  /**
   * Gets the next command to send to client.
   * @return Next command.
   */
  public String nextSend() {
    try {
      return out.removeFirst();
    } catch (Exception e) {
      return null;
    }
  }

  /** Gets the next response from input stream.
   * @return GUIResponse
   */
  public GUIResponse nextResponse() {
    try {
      return in.removeFirst();
    } catch (Exception e) {
      return null;
    }
  }

  /** Clears the input stream from client. */
  public void clearResponse() {
    in.clear();
  }

  /** Sets the operatorID for the session.
   * @param id OperatorID
   */
  protected void setOperatorID(String id) {
    this.operatorID = id;
    setParam("operatorID", id);
  }

  /** Gets the operatorID for the session.
   * @return OperatorID
   */
  protected String getOperatorID() {
    return this.operatorID;
  }

  /**
   * @return deviceID
   */
  protected String getDeviceID() {
    return this.deviceID;
  }
  
  /** Gets the current sessionID.
   * @return SessionID
   */
  protected int getSessionId() {
	  return this.sessionId;
  }

  /**
   * Gets the operator's preferences.
   * @return Map of each preference and value.
   */
  protected Map<String, String> getUserPreferences() {
    if (!exists(operatorID))
      return getDefaultPreferences();
    return db.getMap(
      "SELECT name,value FROM victoryUserPreferences " +
      "WHERE operatorID='%s' ", operatorID
    );
  }

  /**
   * Gets a specific operator preference.
   * @param name Preference name.
   * @return Preference value.
   */
  protected String getUserPreference(String name) {
    if (!exists(operatorID))
      return getDefaultPreferences().get(name);
    return db.getString("", 
      "SELECT value FROM victoryUserPreferences " +
      "WHERE operatorID='%s' AND name='%s'",
      operatorID, name
    );
  }

  /**
   * Sets a specific operator preference.
   * @param name Preference name.
   * @param value Preference value.
   */
  protected void setUserPreference(String name, String value) {
    if (!exists(operatorID))
      return;
    db.execute(
      "UPDATE victoryUserPreferences " +
      "SET value = '%s' " +
      "WHERE operatorID='%s' AND name='%s'",
      value, operatorID, name
    );
  }

  /**
   * Gets default placeholder operator preferences.
   * @return Map of preference names and values.
   */
  protected Map<String, String> getDefaultPreferences() {
    Map<String, String> defaultPreferences = new LinkedHashMap<String, String>();
    defaultPreferences.put("volume", getDefaultPreference("volume", DEFAULT_VOLUME));
    defaultPreferences.put("pitch", getDefaultPreference("pitch", DEFAULT_PITCH));
    defaultPreferences.put("sensitivity", getDefaultPreference("sensitivity", DEFAULT_SENSITIVITY));
    defaultPreferences.put("rate", getDefaultPreference("rate", DEFAULT_RATE));
    defaultPreferences.put("language", getDefaultPreference("language", DEFAULT_LANGUAGE));
    return defaultPreferences;
  }

  /**
   * Sets the default preferences for an operator in db based on "default" operatorID.
   */
  protected void setDefaultPreferences() {
    db.execute(
      "INSERT INTO victoryUserPreferences " +
      "(operatorID, `name`, `value`) " +
      "SELECT REPLACE(REPLACE('%s', 0x00, ''),0x0D,''), `name`, `value` FROM " +
      "victoryUserPreferences WHERE operatorID='default'", operatorID
    );
    db.execute(
      "UPDATE victoryUserPreferences SET `value`= " +
      "(SELECT operatorName FROM proOperators WHERE operatorID='%s') " +
      "WHERE `name`='operatorName' AND operatorID='%s'", operatorID, operatorID
    );
  }

  /**
   * Gets a specific default preference from db.
   * @param name Preference name.
   * @param otherwise Value if not found in db.
   * @return Preference value.
   */
  protected String getDefaultPreference(String name, String otherwise) {
    return db.getString(otherwise,
      "SELECT value FROM victoryUserPreferences " +
      "WHERE operatorID='default' AND name='%s'",
      name
    );
  }

  /**
   * Gets all param for current operator from db.
   * @return Map of param names and values.
   */
  protected Map<String, String> getParams() {
    return db.getMap(
      "SELECT name,value FROM victoryParams " +
      "WHERE operatorID='%s' ", operatorID
    );
  }

  /**
   * Gets a specific param for current operator from db.
   * @param name Param name.
   * @return Param value.
   */
  protected String getParam(String name) {
    return db.getString("",
      "SELECT value FROM victoryParams " +
      "WHERE operatorID='%s' " +
      "AND name='%s' ",
      operatorID, name
    );
  }

  /**
   * Sets a param for the current operator in db.
   * @param name Param name.
   * @param value Param value.
   */
  protected void setParam(String name, String value) {
    if (!exists(operatorID))
      return;
    if (!exists(value)) {
      clearParam(name);
      return;
    }
    db.execute(
      "REPLACE INTO victoryParams " +
      "SET operatorID='%s', name='%s', value='%s'",
      operatorID, name, value
    );
  }

  /**
   * Clears a specific param for current operator in db.
   * @param name Param name.
   */
  protected void clearParam(String name) {
    db.execute(
      "DELETE FROM victoryParams " +
      "WHERE operatorID='" + operatorID + "' " +
      "AND name='" + name + "' "
    );
  }

  /**
   * Deletes all params for current operator in db.
   */
  protected void clearParams() {
    db.execute(
      "DELETE FROM victoryParams " +
      "WHERE operatorID='" + operatorID + "' "
    );
  }

  /**
   * Clears all param for specified operator in db.
   * @param user OperatorID
   */
  protected void clearParams(String user) {
    db.execute(
      "DELETE FROM victoryParams " +
      "WHERE operatorID='" + user + "' "
    );
  }

  /**
   * Base method for each tick. Overridden in VictoryApp.java.
   * @return False
   */
  protected boolean onTick() {
    // overridden in VictoryApp.java
    return false;
  }

  /**
   * Base onDisconnect. Overridden in VictoryApp.java.
   */
  protected void onDisconnect() {
    // overridden in VictoryApp.java
  }

  /**
   * Base login. Overridden in VictoryApp.java.
   * @param text OperatorID
   * @return Result of login.
   */
  protected String login(String text) {
    alert("login() needs to be overridden in VictoryApp.java");
    return "";
  }

  /**
   * Base logout. Overridden in VictoryApp.java.
   */
  protected void logout() {
    alert("logout() needs to be overridden in VictoryApp.java");
  }

  /**
   * Checks if a string is not null and not empty.
   * @param s String to check
   * @return True if checks pass. False otherwise.
   */
  public boolean exists(String s) {
    return ((s != null) && (!s.isEmpty()));
  }

  /**
   * Logs any sort of Victory Error that can be viewed later for debugging purposes.
   * @param errorType Type of error.
   * @param error Error name.
   * @param location Operator's location.
   */
  protected void logVictoryError(String errorType, String error, String location) {
    //Default to location param if no location is provided
    if(!exists(location)) {
      if(!getParam("location").equals("")) location = getParam("location");
      else if (!getParam("locationAlias").equals("")) location = getParam("locationAlias");
    }
    db.execute(
      "INSERT INTO victoryErrors " +
      "SET errorType='%s', error='%s', task='%s', area='%s', " +
      "location='%s', deviceID='%s', operatorID='%s'",
      errorType, error, getParam("task"), getParam("area"), location, deviceID, operatorID
    );
    alert("Logged errorSeq [%d] to victoryErrors table", db.getSequence());
  }

  /**
   * Prints extra messages to assist with debugging if the 'debugMsg'
   * control parameter is set to 'true' 
   * @param format Formatted string
   * @param args Formatted string arguments.
   */
  protected void debug(String format, Object... args) {
    if(db.getControl("victory", "debugMsg", "false").equals("true"))
      inform( format, args);
  }

  /**
   * RDSLog trace a string with Victory formatting.
   */
  public void trace(String format) {
    String user = "";
    if(exists(this.deviceID)) {
      user = this.deviceID + ": ";
    }
    if(exists(getOperatorID())) {
      user = getOperatorID() + ": ";
    }
    RDSLog.trace("S-" + sessionId + ": " + user + format);
  }

  /**
   * RDSLog trace a string with Victory formatting.
   * @param format Formatted string.
   * @param args Formatted string arguments.
   */
  public void trace(String format, Object... args) {
    String user = "";
    if(exists(this.deviceID)) {
      user = this.deviceID + ": ";
    }
    if(exists(getOperatorID())) {
      user = getOperatorID() + ": ";
    }
    RDSLog.trace("S-" + sessionId + ": " + user + format, args);
  }

  /**
   * RDSLog alert a string with Victory formatting.
   */
  public void alert(String format) {
    String user = "";
    if(exists(this.deviceID)) {
      user = this.deviceID + ": ";
    }
    if(exists(getOperatorID())) {
      user = getOperatorID() + ": ";
    }
    RDSLog.alert("S-" + sessionId + ": " + user + format);
  }

  /**
   * RDSLog alert a string with Victory formatting.
   * @param format Formatted string.
   * @param args Formatted string arguments.
   */
  public void alert(String format, Object... args) {
    String user = "";
    if(exists(this.deviceID)) {
      user = this.deviceID + ": ";
    }
    if(exists(getOperatorID())) {
      user = getOperatorID() + ": ";
    }
    RDSLog.alert("S-" + sessionId + ": " + user + format, args);
  }

  /**
   * RDSLog inform a string with Victory formatting.
   */
  public void inform(String format) {
    String user = "";
    if(exists(this.deviceID)) {
      user = this.deviceID + ": ";
    }
    if(exists(getOperatorID())) {
      user = getOperatorID() + ": ";
    }
    RDSLog.inform("S-" + sessionId + ": " + user + format);
  }

  /**
   * RDSLog inform a string with Victory formatting.
   * @param format Formatted string.
   * @param args Formatted string arguments.
   */
  public void inform(String format, Object... args) {
    String user = "";
    if(exists(this.deviceID)) {
      user = this.deviceID + ": ";
    }
    if(exists(getOperatorID())) {
      user = getOperatorID() + ": ";
    }
    RDSLog.inform("S-" + sessionId + ": " + user + format, args);
  }
}
