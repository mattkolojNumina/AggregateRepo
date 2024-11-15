package com.numinagroup.androidtcpclient.activities;

import static com.numinagroup.sharedlibrary.util.Constants.CAMERA_SCAN_ENABLED;
import static com.numinagroup.sharedlibrary.util.Constants.DELAY_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.FORGET_BLUETOOTH_DEFAULT;
import static com.numinagroup.sharedlibrary.util.Constants.FORGET_BLUETOOTH_ON_EXIT;
import static com.numinagroup.sharedlibrary.util.Constants.INITIAL_HEARTBEAT_DELAY_MS;
import static com.numinagroup.sharedlibrary.util.Constants.INITIAL_PORT;
import static com.numinagroup.sharedlibrary.util.Constants.INITIAL_SENSITIVITY;
import static com.numinagroup.sharedlibrary.util.Constants.INITIAL_SERVER_IP;
import static com.numinagroup.sharedlibrary.util.Constants.IP_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.PICKLIST_ENABLED_DEFAULT;
import static com.numinagroup.sharedlibrary.util.Constants.PORTRAIT_ORIENTATION;
import static com.numinagroup.sharedlibrary.util.Constants.PORT_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.SENSITIVITY_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.USE_DETAILED_LOGGING_ENABLED;
import static com.numinagroup.sharedlibrary.util.Constants.USE_DETAILED_LOGGING_ENABLED_DEFAULT;
import static com.numinagroup.sharedlibrary.util.Constants.USE_PICKLIST_ENABLED;
import static com.numinagroup.sharedlibrary.util.Constants.VOICE_LOGIN_ENABLED;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.numinagroup.androidtcpclient.BuildConfig;
import com.numinagroup.androidtcpclient.ConfigHelper;
import com.numinagroup.androidtcpclient.R;
import com.numinagroup.androidtcpclient.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED) {
            newInit();
        } else {
            ActivityCompat.requestPermissions(
                    this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int i = 0, len = permissions.length; i < len; i++) {
            String permission = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                // user rejected the permission
                boolean showRationale = shouldShowRequestPermissionRationale(permission);
                if (!showRationale) {
                    // user also CHECKED "never ask again"
                    // in this case, android app can no longer request permissions, which are needed for program operation.
                    // so we detect, and put link before login to app settings where user can manually enable the permission
                    // else manual deletion and re-installation or clearing the app data is required for it to ask for permission on start again

                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "You have previously declined this permission.\n" +
                            "You must approve this permission in \"Permissions\" in the app settings on your device.", Snackbar.LENGTH_INDEFINITE).setAction("Settings", view -> startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID))));
                    View snackbarView = snackbar.getView();
                    TextView textView = (TextView) snackbarView.findViewById(R.id.snackbar_text);
                    textView.setMaxLines(5);  //Or as much as you need
                    snackbar.show();
                } else {
                    //in this case, user denied permission, but is needed to continue
                    //we are simply asking them again here in a loop, but should be handled more gracefully:
                    //TODO show a dialog here to explain why we need permissions before looping to ask for permissions again
//                    requestPermissions();
                }
                return;
            }
            newInit();
        }
    }

    private void newInit() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ConfigHelper.readConfigFile(prefs, getApplication());
        updateViewsFromPreferences();
        boolean isPortraitOrientation = prefs.getBoolean(PORTRAIT_ORIENTATION, true);
        boolean isVoiceLogin = prefs.getBoolean(VOICE_LOGIN_ENABLED, false);
        boolean isCameraScanEnabled = prefs.getBoolean(CAMERA_SCAN_ENABLED, false);
        setRequestedOrientation(isPortraitOrientation ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        binding.portraitSwitch.setChecked(isPortraitOrientation);
        binding.portraitSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(PORTRAIT_ORIENTATION, isChecked).apply();
            setRequestedOrientation(isChecked ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        });
        binding.voiceLoginSwitch.setChecked(isVoiceLogin);
        binding.voiceLoginSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> prefs.edit().putBoolean(VOICE_LOGIN_ENABLED, isChecked).apply()));
        binding.cameraScanSwitch.setChecked(isCameraScanEnabled);
        binding.cameraScanSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> prefs.edit().putBoolean(CAMERA_SCAN_ENABLED, isChecked).apply()));
        binding.usePicklistSwitch.setChecked(prefs.getBoolean(USE_PICKLIST_ENABLED, PICKLIST_ENABLED_DEFAULT));
        binding.usePicklistSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> prefs.edit().putBoolean(USE_PICKLIST_ENABLED, isChecked).apply()));
        binding.detailedLoggingSwitch.setChecked(prefs.getBoolean(USE_DETAILED_LOGGING_ENABLED, USE_DETAILED_LOGGING_ENABLED_DEFAULT));
        binding.detailedLoggingSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> prefs.edit().putBoolean(USE_DETAILED_LOGGING_ENABLED, isChecked).apply()));
        binding.disconnectBluetoothSwitch.setChecked(prefs.getBoolean(FORGET_BLUETOOTH_ON_EXIT, FORGET_BLUETOOTH_DEFAULT));
        binding.disconnectBluetoothSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> prefs.edit().putBoolean(FORGET_BLUETOOTH_ON_EXIT, isChecked).apply()));

        try {
            binding.versionName.setText(getString(R.string.version, getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateViewsFromPreferences() {
        binding.ipEditText.setText(prefs.getString(IP_KEY, INITIAL_SERVER_IP));
        binding.portEditText.setText(String.valueOf(prefs.getInt(PORT_KEY, INITIAL_PORT)));
        binding.heartBeatDelayEditText.setText(String.valueOf(prefs.getInt(DELAY_KEY, INITIAL_HEARTBEAT_DELAY_MS)));
        binding.sensitivityEditText.setText(String.valueOf(prefs.getInt(SENSITIVITY_KEY, INITIAL_SENSITIVITY)));
        binding.voiceVersionName.setText("Rhino: " + BuildConfig.RHINO_VERSION + " Porcupine: " + BuildConfig.PORCUPINE_VERSION);
    }

    public void save(View view) { //TODO input validation
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(IP_KEY, binding.ipEditText.getText().toString());
        editor.putInt(PORT_KEY, Integer.parseInt(binding.portEditText.getText().toString()));
        editor.putInt(DELAY_KEY, Integer.parseInt(binding.heartBeatDelayEditText.getText().toString()));
        editor.putInt(SENSITIVITY_KEY, Integer.parseInt(binding.sensitivityEditText.getText().toString()));
        editor.apply();
        Toast.makeText(getApplicationContext(), "Settings Saved, Exiting", Toast.LENGTH_LONG).show();
        finish();
    }
}