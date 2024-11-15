package victory;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import victoryApp.*;
import rds.*;

public class Victory {
  private String dbHost = null;
  private RDSDatabase db = null;
  private String port = null;
  private ServerSocket serverSocket = null;
  private static int globalSessionId = 0;

  /**
   * Constructs a new Victory server instance.
   * @param dbHost Database host address.
   */
  public Victory(String dbHost) {
    this.dbHost = dbHost;
    db = new RDSDatabase(dbHost);
    port = db.getControl("victory", "port", "10200");

    try {
      serverSocket = new ServerSocket(Integer.valueOf(port));
      RDSUtil.trace("Server started db [%s] port [%s]", dbHost, port);
    } catch (IOException e) {
      RDSUtil.alert("Could not listen on port [%s] with exception %s", port, e.toString());
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Continuously accepts new connections and creates a new thread 
   * to handle each one until the server socket is closed.
   */
  private void poll() {
    try {
      while (!serverSocket.isClosed()) {
        Socket socket = serverSocket.accept();
        RDSUtil.trace("New connection accepted");
        globalSessionId++;
        if (globalSessionId > 99999999)
          globalSessionId = 1;
        handleConnection(socket);
      }
    } catch (Exception e) {
      RDSUtil.alert(e);
      e.printStackTrace();
    } finally {
      try {
        serverSocket.close();
      } catch (IOException e) {
        RDSUtil.alert("Error closing ServerSocket: " + e.getMessage());
      }
    }
  }

  /**
   * Handles a single connection by creating a new {@link VictoryApp} thread.
   * @param socket Client socket.
   */
  private void handleConnection(Socket socket) {
    try {
      VictoryApp app = new VictoryApp(socket, dbHost, globalSessionId);
      app.start();
    } catch (Exception e) {
      try {
        socket.close();
      } catch (IOException e2) {
        RDSUtil.alert("Session [%d] : Error closing socket: " + e2.getMessage(), globalSessionId);
        e2.printStackTrace();
      }
      RDSUtil.alert("Session [%d] : Error handling connection: " + e.getMessage(), globalSessionId);
      e.printStackTrace();
    }
  }

  /**
   * Main entry point for the Victory Voice server. Optionally takes in 
   * a database host address, otherwise uses default host "db". 
   * Initializes the server, and starts listening for connections.
   * @param args Database host address.
   */
  public static void main(String[] args) {
    if (args.length > 1) {
      RDSUtil.alert("syntax: java victory.Victory [ id ]");
      System.exit(1);
    }

    String dbHost = "db";
    if (args.length > 0)
      dbHost = args[0];

    // start - open port (default 10200) and listen for new connections
    Victory server = new Victory(dbHost);
    server.poll();
  }
}
