package com.numinagroup.androidtcpclient.activities;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static com.numinagroup.sharedlibrary.util.Constants.PORTRAIT_ORIENTATION;
import static com.numinagroup.sharedlibrary.util.Constants.VOICE_LOGIN_ENABLED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.client.android.BuildConfig;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.numinagroup.androidtcpclient.ConfigHelper;
import com.numinagroup.androidtcpclient.R;
import com.numinagroup.androidtcpclient.databinding.ActivityMainBinding;
import com.numinagroup.androidtcpclient.fragments.LoginFragment;
import com.numinagroup.androidtcpclient.fragments.MainFragment;
import com.numinagroup.androidtcpclient.viewmodel.MainViewModel;
import com.numinagroup.sharedlibrary.repository.MainRepository;
import com.numinagroup.sharedlibrary.util.BluetoothController;
import com.numinagroup.sharedlibrary.util.Constants;
import com.numinagroup.sharedlibrary.util.Scanner;

import java.lang.reflect.Method;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements Scanner.ZebraScan {

    private final String TAG = this.getClass().getSimpleName();
    private SharedPreferences prefs;
    private MainViewModel mainViewModel;
    @Inject
    BluetoothController bluetoothController;
    private Scanner scanner;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    @Inject
    MainRepository repository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        boolean isPortraitOrientation = prefs.getBoolean(PORTRAIT_ORIENTATION, true);
        setRequestedOrientation(isPortraitOrientation ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        final int orientation = getResources().getConfiguration().orientation;
        if (orientation == ORIENTATION_LANDSCAPE && isPortraitOrientation ||
                orientation == ORIENTATION_PORTRAIT && !isPortraitOrientation) {
            //we still need to adjust orientation, delay initialization
        } else {
            init();
        }
    }

    private void init() {
        scanner = new Scanner(getApplicationContext(), this);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getEnableScanEvent().observe(this, shouldEnableScanner -> {
            if (mainViewModel.getScanAhead()) {
                scanner.scanOn();
            } else if (shouldEnableScanner) {
                scanner.scanOn();
            } else {
                scanner.scanOff();
            }
        });
        mainViewModel.getEnableScanEvent2().observe(this, shouldEnableScanner -> {
            if (mainViewModel.getLogTimes()) {
                long currentTime = System.currentTimeMillis();
                long diff = currentTime - mainViewModel.getScanInTimer();
                mainViewModel.log("scan enable/disable instruction acted on in " + diff + " ms");
            }
            if (mainViewModel.getScanAhead()) {
                scanner.scanOn();
            } else if (shouldEnableScanner) {
                scanner.scanOn();
            } else {
                scanner.scanOff();
            }
        });
        mainViewModel.getLoginEvent().observe(this, (id) -> showMain());
        if (mainViewModel.getMainViewUI().getIsInLogonState().get()) {//unsure if this is necessary
            if (hasPermissions()) {
                //since this login initialization triggers the login listener loop, we wait until we definitely have microphone
                //permission to init (either here or in the permission callback)
                if (prefs.getBoolean(VOICE_LOGIN_ENABLED, false)) {
                    mainViewModel.initLoginResources();
                }
            } else {
                requestPermissions();
            }
        }
        mainViewModel.getUserErrorEvent().observe(this, errorText -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            alertDialog = new AlertDialog.Builder(MainActivity.this).setTitle("Error").setMessage(errorText).show();

        });


        mainViewModel.getLoggingInEvent().observe(this, showShowDialog -> {
            if (showShowDialog) {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("logging in");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.show();
            }
        });
        mainViewModel.getExitEvent().observe(this, v -> finish());
        mainViewModel.getExitingEvent().observe(this, v -> {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("logging out");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            forgetBluetooth();

        });
    }

    private void showLogin() {
        scanner.scanOn();
        getSupportFragmentManager().beginTransaction().replace(R.id.contentView, LoginFragment.class, null).commit();
        getSupportFragmentManager().executePendingTransactions();
        mainViewModel.restart();
    }

    private void showMain() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (scanner != null) {
            if (!mainViewModel.getScanAhead()) {
                scanner.scanOff();
            }
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.contentView, MainFragment.class, null).commit();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void log(String input) {Log.d(TAG, input);}

    @Override
    protected void onStart() {
        super.onStart();
        if (bluetoothController != null) {
            bluetoothController.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scanner != null) {
            scanner.register();
        }
        if (mainViewModel != null) {
            if (mainViewModel.getMainViewUI().getIsInLogonState().get()) {
                if (hasPermissions()) {
                    showLogin();
                    if (scanner != null) {
                        scanner.scanOn();
                    }
                } else {
                    requestPermissions();
                }
            } else {
                showMain();
                if (scanner != null) {
                    if (mainViewModel.getScanAhead()) {
                        scanner.scanOn();
                    } else if (mainViewModel.getMainViewUI().getShouldScan()) {
                        scanner.scanOn();
                    } else {
                        scanner.scanOff();
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanner != null) {
            scanner.unregister();
        }
        if (mainViewModel != null && mainViewModel.getMainViewUI().getIsInLogonState().get()) {
            if (scanner != null) {
                scanner.scanOff();
            }
        }
        if (bluetoothController != null) {
            bluetoothController.stop();
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        forgetBluetooth();
    }

    private boolean hasPermissions() {
        boolean bluetoothConnect = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothConnect = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }
        return bluetoothConnect &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT}, 0);
    }

    @Override
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
                    requestPermissions();
                }
                return;
            }
            ConfigHelper.readConfigFile(prefs, getApplication());
        }
    }


    /**
     * this is the code triggered from scan button to launch camera scanner
     *
     * @param view view that was clicked
     */
    public void scan(View view) {
        repository.setIsListening(false);
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt("Scan a barcode");
//        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(false);
//        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    /**
     * Get the results from the (camera) scanner activity:
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag("mainFragment");
        if (fragment != null) {
            fragment.onCameraScanComplete();
        }
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String resultContents = result.getContents();
            mainViewModel.scanComplete(resultContents);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * This will override default behavior of allowing 'back' button to exit our main activity
     * if this is not overridden, pressing back will exit session without logging off
     */
    @Override
    public void onBackPressed() {
        if (mainViewModel.getMainViewUI().getIsInLogonState().get()) {
            super.onBackPressed();
        }
    }

    @Override
    public void handleScan(String data) {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        if (mainViewModel.getMainViewUI().getIsInLogonState().get()) {
            mainViewModel.login(data);
        } else {
            MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag("mainFragment");
            if (fragment != null) {
                fragment.onCameraScanComplete();
            }
            if (mainViewModel.getLogTimes()) {
                mainViewModel.setScanOutTimer(System.currentTimeMillis());
            }
            mainViewModel.scanComplete(data);
        }
    }

    @Override
    public void handleError(String error) {
        if (mainViewModel != null) {
            mainViewModel.sendError(error);
        }
    }

    @SuppressLint("MissingPermission")
    private void forgetBluetooth() {
        if (prefs.getBoolean(Constants.FORGET_BLUETOOTH_ON_EXIT, Constants.FORGET_BLUETOOTH_DEFAULT)) {
            // Disconnect Bluetooth
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            // Note: The following line disables Bluetooth on the device.
            // It's commented out as it seems to be not needed per your Kotlin comment
            // bluetoothAdapter.disable();

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (!pairedDevices.isEmpty()) {
                for (BluetoothDevice device : pairedDevices) {
                    try {
                        Method removeBondMethod = device.getClass().getMethod("removeBond");
                        removeBondMethod.invoke(device);
                    } catch (Exception e) {
                        // Log the exception using your ViewModel. Replace with your actual logging method.
                        if (mainViewModel != null) {
                            mainViewModel.logE("Error removing bond", e);
                        }
                    }
                }
            }
        }
    }
}