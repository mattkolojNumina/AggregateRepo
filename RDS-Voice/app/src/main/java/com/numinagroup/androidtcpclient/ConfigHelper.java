package com.numinagroup.androidtcpclient;

import static com.numinagroup.sharedlibrary.util.Constants.CAMERA_SCAN_ENABLED;
import static com.numinagroup.sharedlibrary.util.Constants.DELAY_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.FORGET_BLUETOOTH_ON_EXIT;
import static com.numinagroup.sharedlibrary.util.Constants.IP_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.IS_FIRST_RUN_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.PORT_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.SENSITIVITY_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.USE_DETAILED_LOGGING_ENABLED;
import static com.numinagroup.sharedlibrary.util.Constants.USE_PICKLIST_ENABLED;
import static com.numinagroup.sharedlibrary.util.Constants.USE_SPEAKER_ENABLED;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ConfigHelper {

    private static final String TAG = ConfigHelper.class.getSimpleName();

    public static void readConfigFile(SharedPreferences prefs, Application application) {
        if (prefs.getBoolean(IS_FIRST_RUN_KEY, true)) {
            prefs.edit().putBoolean(IS_FIRST_RUN_KEY, false).apply();
            //is first run
            File sdcard = Environment.getExternalStorageDirectory();

            //Get the text file
            File file = new File(sdcard,"config.txt");

            //Read text from file
            ArrayList<String> result = new ArrayList<>();

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    result.add(line);
                }
                br.close();

                //Try each preference up to n length, and if m value doesn't exist, exit and apply.
                SharedPreferences.Editor editor = prefs.edit();
                int i = 0;
                try {
                    //Loop through max length of options instead of results.size() so we can give error if not all options are in config.txt
                    for (i = 0; i < 9; i++) {
                        String value = result.get(i);
                        switch (i) {
                            case 0:
                                editor.putString(IP_KEY, value);
                                Log.i(TAG, "IP: " + value);
                                break;
                            case 1:
                                editor.putInt(PORT_KEY, Integer.parseInt(value));
                                Log.i(TAG, "Port: " + value);
                                break;
                            case 2:
                                editor.putBoolean(CAMERA_SCAN_ENABLED, Boolean.parseBoolean(value));
                                Log.i(TAG, "Camera Scan: " + value);
                                break;
                            case 3:
                                editor.putBoolean(USE_SPEAKER_ENABLED, Boolean.parseBoolean(value));
                                Log.i(TAG, "Use Speaker: " + value);
                                break;
                            case 4:
                                editor.putBoolean(USE_PICKLIST_ENABLED, Boolean.parseBoolean(value));
                                Log.i(TAG, "Picklist: " + value);
                                break;
                            case 5:
                                editor.putBoolean(USE_DETAILED_LOGGING_ENABLED, Boolean.parseBoolean(value));
                                Log.i(TAG, "Detailed Logging: " + value);
                                break;
                            case 6:
                                editor.putBoolean(FORGET_BLUETOOTH_ON_EXIT, Boolean.parseBoolean(value));
                                Log.i(TAG, "Forget Bluetooth: " + value);
                                break;
                            case 7:
                                editor.putInt(DELAY_KEY, Integer.parseInt(value));
                                Log.i(TAG, "Heartbeat: " + value);
                                break;
                            case 8:
                                editor.putInt(SENSITIVITY_KEY, Integer.parseInt(value));
                                Log.i(TAG, "Sensitivity: " + value);
                                break;
                        }
                    }
                    editor.apply();
                    Log.i(TAG, "All config settings loaded successfully");
                } catch (IndexOutOfBoundsException e) {
                    //Config file may not have all expected values, EX: missing device name
                    Toast.makeText(application, "Config File Error: Missing Config Value(s)", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "Config File Error: Missing value at index: " + i ); //, e);
                }
            } catch (FileNotFoundException e) {
                // Handle the file not found exception
                Log.w(TAG, "Config File Not Found"); //, e);
                Toast.makeText(application, "Config File Not Found", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                //may not be connected to server, showing a pop up
                Toast.makeText(application, "Config File Error", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Config File Error", e);
            }
        }
    }
}

