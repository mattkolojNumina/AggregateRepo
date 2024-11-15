package com.numinagroup.androidtcpclient.models;

import android.graphics.Color;

import androidx.databinding.BaseObservable;
import androidx.databinding.ObservableBoolean;

import com.numinagroup.androidtcpclient.serviceobjects.ServerMessage;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * This object will hold state of our view model, views will observe these values and update immediately when changed
 */
public class MainViewUI extends BaseObservable {

    private String deviceID;
    private String operatorID;
    private String consoleText = "log begin";

    private ServerMessage ServerMessage;

    private ServerMessage serverMessage;

    private final ObservableBoolean connected = new ObservableBoolean(false);
    private final ObservableBoolean isInLogonState = new ObservableBoolean(true);
    private final ObservableBoolean shouldScan = new ObservableBoolean(false);
    private final ObservableBoolean isManualStopEnabled = new ObservableBoolean(false);
    //setting this to false will stop our socket listener loop
    private final AtomicBoolean socketListening = new AtomicBoolean(false);
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);

    public boolean isConnecting() {
        return isConnecting.get();
    }
    public void setIsConnecting(boolean newValue) {
        isConnecting.set(newValue);
    }
    public boolean isSocketListening() {
        return socketListening.get();
    }
    public void setSocketListening(boolean newValue) {
        socketListening.set(newValue);
    }

    public ObservableBoolean getConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected.set(connected);
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public void setOperatorID(String operatorID) {
        this.operatorID = operatorID;
    }

    public String getOperatorID() {
        return operatorID;
    }

    public ObservableBoolean getIsInLogonState() {
        return isInLogonState;
    }

    public void setIsInLogonState(boolean input) {
        isInLogonState.set(input);
    }

    public String getConsoleText() {
        return consoleText;
    }

    public void setConsoleText(String consoleText) {
        this.consoleText = consoleText;
    }

    public Boolean getShouldScan() {
        return shouldScan.get();
    }

    public void setShouldScan(Boolean newValue) {
        shouldScan.set(newValue);
    }
    public void setShouldScanTimed(Boolean newValue) {
        shouldScan.set(newValue);
    }

    public boolean getIsManualStopEnabled() {
        return isManualStopEnabled.get();
    }

    public void setIsManualStopEnabled(boolean value) {
        isManualStopEnabled.set(value);
    }

    public ServerMessage getServerMessage() { return serverMessage; }
    public void setServerMessage(ServerMessage serverMessage) { this.serverMessage = serverMessage; }
}
