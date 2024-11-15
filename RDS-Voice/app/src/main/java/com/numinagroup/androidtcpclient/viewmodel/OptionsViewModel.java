package com.numinagroup.androidtcpclient.viewmodel;

import static com.numinagroup.sharedlibrary.util.Constants.DEVICE_ID_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.PITCH_MAX;
import static com.numinagroup.sharedlibrary.util.Constants.PITCH_MIN;
import static com.numinagroup.sharedlibrary.util.Constants.SET_USER_PITCH;
import static com.numinagroup.sharedlibrary.util.Constants.SET_USER_RATE;
import static com.numinagroup.sharedlibrary.util.Constants.SET_USER_SENSITIVITY;
import static com.numinagroup.sharedlibrary.util.Constants.SET_USER_VOLUME;
import static com.numinagroup.sharedlibrary.util.Constants.SPEED_MAX;
import static com.numinagroup.sharedlibrary.util.Constants.SPEED_MIN;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.numinagroup.androidtcpclient.BuildConfig;
import com.numinagroup.androidtcpclient.StringManager;
import com.numinagroup.sharedlibrary.repository.MainRepository;
import com.numinagroup.sharedlibrary.serviceObjects.SimpleServiceObject;
import com.numinagroup.sharedlibrary.socket.NetworkManager;
import com.numinagroup.sharedlibrary.util.RxHelper;
import com.numinagroup.sharedlibrary.util.TTSManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.disposables.CompositeDisposable;

@HiltViewModel
public class OptionsViewModel extends ViewModel {

    private final TTSManager ttsManager;
    private final MainRepository repository;
    private final NetworkManager networkManager;
    private final RxHelper rxHelper;
    private final Gson gson;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final String TAG = this.getClass().getSimpleName();
    private final StringManager stringManager;
    private final SharedPreferences sharedPreferences;

    private String deviceID;

    @Inject
    OptionsViewModel(TTSManager ttsManager, MainRepository repository, NetworkManager networkManager, RxHelper rxHelper, Gson gson, StringManager stringManager, SharedPreferences sharedPreferences) {
        this.ttsManager = ttsManager;
        this.repository = repository;
        this.networkManager = networkManager;
        this.rxHelper = rxHelper;
        this.gson = gson;
        this.stringManager = stringManager;
        this.sharedPreferences = sharedPreferences;
    }

    public String ipAddress() {
        return "IP: " + networkManager.getIpAddress();
    }

    public String getDeviceID() {
        if (deviceID == null) {
            this.deviceID = sharedPreferences.getString(DEVICE_ID_KEY, null);
        }
        return "DEVICE ID: " + deviceID;
    }

    public String getVoiceVersion() {
        return "Rhino: " + BuildConfig.RHINO_VERSION + " Porcupine: " + BuildConfig.PORCUPINE_VERSION;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();


    }

    /**
     * this method subscribes to our send message task
     * to send a message through our socket connection
     *
     * @param currentMessage raw string to send ( task will package string in json)
     */
    public void startSendMessageTask(String currentMessage) {
        compositeDisposable.add(
                rxHelper.schedule(networkManager.sendTask(currentMessage))
                        .subscribe(() -> {}, Throwable::printStackTrace));
    }

    private void sendMessage(String key, Object value) {
        startSendMessageTask(gson.toJson(new SimpleServiceObject(key, value)));
    }

    public void updateSensitivity(float newValue) {
        ttsManager.stop();
        log("sensitivity changed to: " + newValue);
        sendMessage(SET_USER_SENSITIVITY, newValue);
        repository.setCurrentSensitivity(newValue);
    }

    public void updateVolume(float newVolume) {
        ttsManager.stop();
        log("volume changed to: " + newVolume);
        sendMessage(SET_USER_VOLUME, newVolume);
        ttsManager.setCurrentVolume(newVolume);
        ttsManager.repeat();
    }

    public void updatePitch(float newPitch) {
        ttsManager.stop();
        if (newPitch > PITCH_MAX) newPitch = PITCH_MAX;
        if (newPitch < PITCH_MIN) newPitch = PITCH_MIN;
        log("pitch changed to: " + newPitch);
        sendMessage(SET_USER_PITCH, newPitch);
        ttsManager.setCurrentPitch(newPitch);
        ttsManager.repeat();
    }

    public void updateRate(float newRate) {
        ttsManager.stop();
        if (newRate > SPEED_MAX) newRate = SPEED_MAX;
        if (newRate < SPEED_MIN) newRate = SPEED_MIN;
        log("rate changed to: " + newRate);
        sendMessage(SET_USER_RATE, newRate);
        ttsManager.setCurrentRate(newRate);
        ttsManager.repeat();
    }

    void log(String toLog) {
        Log.d(TAG, toLog);
    }

    public float getSensitivity() {
        return repository.getCurrentSensitivity();
    }

    public float getVolume() {
        return repository.getCurrentVolume();
    }

    public float getPitch() {
        return repository.getCurrentPitch();
    }

    public float getRate() {
        return repository.getCurrentRate();
    }

    public String getString(String key) {
        return stringManager.getString(key);
    }
}
