package com.numinagroup.androidtcpclient.viewmodel;

import static com.numinagroup.sharedlibrary.util.Constants.CAMERA_SCAN_ENABLED;
import static com.numinagroup.sharedlibrary.util.Constants.CONNECT_COMMAND;
import static com.numinagroup.sharedlibrary.util.Constants.DELAY_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.HEARTBEAT_COMMAND;
import static com.numinagroup.sharedlibrary.util.Constants.INITIAL_HEARTBEAT_DELAY_MS;
import static com.numinagroup.sharedlibrary.util.Constants.INITIAL_PORT;
import static com.numinagroup.sharedlibrary.util.Constants.INITIAL_SENSITIVITY;
import static com.numinagroup.sharedlibrary.util.Constants.INITIAL_SERVER_IP;
import static com.numinagroup.sharedlibrary.util.Constants.IP_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.LOG_DUMP;
import static com.numinagroup.sharedlibrary.util.Constants.PORT_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.SENSITIVITY_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.VOICE_LOGIN_ENABLED;
import static com.numinagroup.sharedlibrary.util.Constants.VOICE_READY_COMMAND;
import static com.numinagroup.sharedlibrary.util.Constants.VOICE_RESPONSE_COMMAND;

import android.animation.ArgbEvaluator;
import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;

import com.google.gson.Gson;
import com.numinagroup.androidtcpclient.BuildConfig;
import com.numinagroup.androidtcpclient.StringManager;
import com.numinagroup.androidtcpclient.models.ConnectionResponse;
import com.numinagroup.sharedlibrary.audio.PicoVoiceManager;
import com.numinagroup.sharedlibrary.serviceObjects.BaseServiceObject;
import com.numinagroup.sharedlibrary.serviceObjects.HeartBeatObject;
import com.numinagroup.sharedlibrary.serviceObjects.Resource;
import com.numinagroup.sharedlibrary.serviceObjects.ResourceList;
import com.numinagroup.sharedlibrary.serviceObjects.SimpleServiceObject;
import com.numinagroup.sharedlibrary.serviceObjects.VoiceResponse;
import com.numinagroup.sharedlibrary.socket.NetworkManager;
import com.numinagroup.sharedlibrary.util.BluetoothController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.Completable;


public class MainViewModelImpl {

    private final WifiManager wifiManager;
    private final BatteryManager batteryManager;
    private final NetworkManager networkManager;
    private final SharedPreferences sh;
    private final Gson gson;
    private final PicoVoiceManager picoVoiceManager;
    private final StringManager stringManager;
    private String bssid;

    private final BluetoothController bluetoothController;

    @Inject
    public MainViewModelImpl(WifiManager wifiManager, BatteryManager batteryManager, NetworkManager networkManager, SharedPreferences sh, Gson gson, PicoVoiceManager picoVoiceManager, StringManager stringManager, BluetoothController bluetoothController) {
        this.wifiManager = wifiManager;
        this.batteryManager = batteryManager;
        this.networkManager = networkManager;
        this.sh = sh;
        this.gson = gson;
        this.picoVoiceManager = picoVoiceManager;
        this.stringManager = stringManager;
        this.bluetoothController = bluetoothController;
    }

    public String getWifiSignal() {
        return String.valueOf(wifiManager.getConnectionInfo().getRssi());
    }

    public String getBSSID() {
        bssid = wifiManager.getConnectionInfo().getBSSID();
        return bssid;
    }

    public String getLastBSSID() {
        return bssid;
    }

    private Integer getWifiSignalRssi() {
        return wifiManager.getConnectionInfo().getRssi();
    }

    String getBatteryLevel() {
        return String.valueOf(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
    }

    /**
     * The connection task to be executed by connect()
     *
     * @return connection task
     */
    Completable connectionTask() {
        return Completable.defer(() -> {
            networkManager.connect(getIp(), getPort());
            return Completable.complete();
        });
    }

    String getIp() {
        return sh.getString(IP_KEY, INITIAL_SERVER_IP);
    }

    float getSensitivity() {
//        String sensitivityString = sh.getString(SENSITIVITY_KEY, String.valueOf(INITIAL_SENSITIVITY));
//        int sensitivityInt = Integer.parseInt(sensitivityString);
        int sensitivityInt = sh.getInt(SENSITIVITY_KEY, INITIAL_SENSITIVITY);
        return Float.intBitsToFloat(sensitivityInt) / 100.0f;
    }

    int getPort() {
        return sh.getInt(PORT_KEY, INITIAL_PORT);
    }

    int getDelay() {
        return sh.getInt(DELAY_KEY, INITIAL_HEARTBEAT_DELAY_MS);
    }

    String createConnectionObject(String deviceId, String operatorID, Application application) {
        ConnectionResponse data = new ConnectionResponse(deviceId, operatorID, application, "Rhino: " + BuildConfig.RHINO_VERSION + " Porcupine: " + BuildConfig.PORCUPINE_VERSION, bluetoothController.getPairedDevices());
        BaseServiceObject baseServiceObject = new BaseServiceObject(CONNECT_COMMAND,
                gson.toJsonTree(data).getAsJsonObject());
        return gson.toJson(baseServiceObject);
    }

    String createVoiceReadyObject(String deviceID, String operatorID, Application application) {
        ConnectionResponse data = new ConnectionResponse(deviceID, operatorID, application, "Rhino: " + BuildConfig.RHINO_VERSION + " Porcupine: " + BuildConfig.PORCUPINE_VERSION, bluetoothController.getPairedDevices());
        BaseServiceObject baseServiceObject = new BaseServiceObject(VOICE_READY_COMMAND,
                gson.toJsonTree(data).getAsJsonObject());
        return gson.toJson(baseServiceObject);
    }


    void heartBeat() throws IOException {
        HeartBeatObject heartBeatObject = new HeartBeatObject(getWifiSignal(), getBatteryLevel(), getBSSID());
        BaseServiceObject baseServiceObject = new BaseServiceObject(HEARTBEAT_COMMAND,
                gson.toJsonTree(heartBeatObject).getAsJsonObject());
        networkManager.sendString(gson.toJson(baseServiceObject));
    }

    String createVoiceResponseString(VoiceResponse voiceResponse) {
        BaseServiceObject baseServiceObject = new BaseServiceObject(VOICE_RESPONSE_COMMAND,
                gson.toJsonTree(voiceResponse).getAsJsonObject());
        return gson.toJson(baseServiceObject);
    }

    String createLogDump() {
        SimpleServiceObject simpleServiceObject = new SimpleServiceObject(LOG_DUMP, dumpLogs());
        return gson.toJson(simpleServiceObject);
    }

    void handleResourceList(ResourceList resourceList) {
        for (Resource resource : resourceList.getResources()) {
            picoVoiceManager.createPicoVoiceResourceFile(resource);
        }
    }

    public Completable delayedPorcupineStart() {
        return Completable.defer(() -> {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                return Completable.error(e);
            }
            picoVoiceManager.startPorcupine();
            return Completable.complete();
        });
    }


    boolean isVoiceLogin() {
        return sh.getBoolean(VOICE_LOGIN_ENABLED, false);
    }

    private String dumpLogs() {
        try {
            //TODO this should work though android 11 but getExternalStorageDirectory is deprecated and needs to be replaced
            //so this function needs to be refactored to use :
            /* Use methods on Context, such as getExternalFilesDir(), to get at directories on external storage into which your app can write. You do not need any permissions to use those directories on Android 4.4+. However, the data that you store there gets removed when your app is uninstalled. */
            File filename = new File(Environment.getExternalStorageDirectory() + "/logs.txt");
            filename.createNewFile();
            String cmd = "logcat -d -f " + filename.getAbsolutePath();
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File sdcard = Environment.getExternalStorageDirectory();

//Get the text file
        File file = new File(sdcard, "logs.txt");

//Read text from file
        StringBuilder text = new StringBuilder();
        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));


            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    boolean isCameraEnabled() {
        return sh.getBoolean(CAMERA_SCAN_ENABLED, false);
    }

    int determineNetworkIndicatorColor() {
        int rssiValue = getWifiSignalRssi();
        float maxRssi = -67f; //this and anything above shows green (actual max -30)
        float minRssi = -80f; //this shows yellow for anything below (red shows when actually disconnects) (actual min -90)
        float midPoint = ((maxRssi - minRssi) / 2) + minRssi;
        ///
        int color = 0;
        if (rssiValue >= maxRssi) {
            color = Color.GREEN;
        } else if (rssiValue < minRssi) {
            color = Color.RED;
        } else if (rssiValue == midPoint) {
            color = Color.YELLOW;
        } else if (rssiValue < midPoint) {
            float percentage = ((float) rssiValue - minRssi) / (midPoint - minRssi);
            color = (Integer) new ArgbEvaluator().evaluate(percentage, Color.RED, Color.YELLOW);
        } else if (rssiValue >= midPoint) {
            float percentage = ((float) rssiValue - midPoint) / (maxRssi - midPoint);
            color = (Integer) new ArgbEvaluator().evaluate(percentage, Color.YELLOW, Color.GREEN);

        }
        return color;
    }

    public String getString(String key) {
        return stringManager.getString(key);
    }
}