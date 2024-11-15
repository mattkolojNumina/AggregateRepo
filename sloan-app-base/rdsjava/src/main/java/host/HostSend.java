package host;

import rds.*;

public class HostSend {
  // private SendPickConfirmation sendPickConfirmation;
  // private SendPickCompletion sendPickCompletion;
  // private SendDimWeight sendDimWeight;
  // private SendShipConfirmation sendShipConfirmation;
  // private SendPalletConfirmation sendPalletConfirmation;
  // private SendPalletAvailable sendPalletAvailable;

  public HostSend() {
    // sendPickConfirmation = new SendPickConfirmation();
    // sendPickCompletion = new SendPickCompletion();
    // sendDimWeight = new SendDimWeight();
    // sendShipConfirmation = new SendShipConfirmation();
    // sendPalletConfirmation = new SendPalletConfirmation();
    // sendPalletAvailable = new SendPalletAvailable();
  }

  private void poll() {
    while (true) {
      // sendPickConfirmation.cycle();
      // sendPickCompletion.cycle();
      // sendDimWeight.cycle();
      // sendShipConfirmation.cycle();
      // sendPalletConfirmation.cycle();
      // sendPalletAvailable.cycle();

      try {
        Thread.sleep(10 * 1000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    HostSend app = new HostSend();
    app.poll();
  }

}
