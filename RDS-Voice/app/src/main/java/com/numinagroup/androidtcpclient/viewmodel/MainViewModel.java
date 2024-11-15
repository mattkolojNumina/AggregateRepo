package com.numinagroup.androidtcpclient.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.numinagroup.androidtcpclient.BuildConfig;
import com.numinagroup.androidtcpclient.StringManager;
import com.numinagroup.androidtcpclient.serviceobjects.ServerMessage;
import com.numinagroup.androidtcpclient.serviceobjects.ServerMessageServiceObject;
import com.numinagroup.sharedlibrary.audio.SoundProcessor;
import com.numinagroup.sharedlibrary.audio.SoundRecorder;
import com.numinagroup.androidtcpclient.models.LoginUI;
import com.numinagroup.androidtcpclient.models.MainViewUI;
import com.numinagroup.sharedlibrary.audio.PicoVoiceManager;
import com.numinagroup.sharedlibrary.repository.MainRepository;
import com.numinagroup.sharedlibrary.serviceObjects.BaseServiceObject;
import com.numinagroup.sharedlibrary.serviceObjects.ResourceList;
import com.numinagroup.sharedlibrary.serviceObjects.SimpleServiceObject;
import com.numinagroup.sharedlibrary.serviceObjects.VoiceRequest;
import com.numinagroup.sharedlibrary.serviceObjects.VoiceResponse;
import com.numinagroup.sharedlibrary.socket.NetworkManager;
import com.numinagroup.sharedlibrary.util.BluetoothController;
import com.numinagroup.sharedlibrary.util.ErrorEventBus;
import com.numinagroup.sharedlibrary.util.RxHelper;
import com.numinagroup.sharedlibrary.util.TTSManager;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ai.picovoice.rhino.RhinoInference;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.numinagroup.androidtcpclient.Utils.getStackTraceAsString;
import static com.numinagroup.sharedlibrary.util.Constants.DEVICE_ID_KEY;
import static com.numinagroup.sharedlibrary.util.Constants.SEND_ERROR_MESSAGE;
import static com.numinagroup.sharedlibrary.util.Constants.SEND_LOG_MESSAGE;
import static com.numinagroup.sharedlibrary.util.Constants.USE_DETAILED_LOGGING_ENABLED;
import static com.numinagroup.sharedlibrary.util.Constants.YES;

@HiltViewModel
public class MainViewModel extends AndroidViewModel {
    private final RxHelper rxHelper;
    private final Gson gson;
    private final NetworkManager networkManager;
    private final PicoVoiceManager picoVoiceManager;
    private final SavedStateHandle state;
    private final MainViewModelImpl impl;
    private final TTSManager ttsManager;
    private final MainRepository repository;
    private final SoundRecorder soundRecorder;
    private final StringManager stringManager;
    private final String TAG = this.getClass().getSimpleName();

    //login stuff to merge in
    private final LoginUI loginUI = new LoginUI();
    private final MutableLiveData<String> loginEvent = new MutableLiveData<>();

    //these disposables keep track of our subscription threads (heartbeat/connection separate so they can be killed separately)
    private final CompositeDisposable heartBeatDisposable = new CompositeDisposable();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final CompositeDisposable connectionDisposable = new CompositeDisposable();
    private final CompositeDisposable audioRecorderDisposable = new CompositeDisposable();

    //activities and fragments cans subscribe to these events and react when triggered
    private final MutableLiveData<String> printToConsoleEvent = new MutableLiveData<>();//for log output
    private final MutableLiveData<String> printInstructionsEvent = new MutableLiveData<>();//for instructions output
    private final MutableLiveData<Boolean> showCameraScanEvent = new MutableLiveData<>();//to control camera scan button visibility
    private final MutableLiveData<Boolean> enableScanEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> enableScanEvent2 = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loggingInEvent = new MutableLiveData<>();
    private final MutableLiveData<String> loginFailureEvent = new MutableLiveData<>();
    private final MutableLiveData<String> userErrorEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> connectedEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> listeningNotificationEvent = new MutableLiveData<>();
    private final MutableLiveData<String> commandSentEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> exitEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> exitingEvent = new MutableLiveData<>();
    private final MutableLiveData<Integer> networkConnectionStrengthEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> onOffTriggerEvent = new MutableLiveData<>();
    private final MutableLiveData<ServerMessage> serverMessageEvent = new MutableLiveData<>();

    private final SharedPreferences sharedPreferences;

    //use this to post runnable on main UI thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    //this object keeps track of our UI state
    private final MainViewUI mainViewUI = new MainViewUI();

    private final String deviceID;
    //variables to time these per server command to log output
    private long scanInTimer;
    private long scanOutTimer;
    private long voiceInTimer;
    private long voiceOutTimer;
    private boolean detailedLogging;

    @Inject
    public MainViewModel(Application application,
                         SavedStateHandle handle,
                         RxHelper rxHelper,
                         Gson gson,
                         NetworkManager networkManager,
                         PicoVoiceManager picoVoiceManager,
                         MainViewModelImpl impl,
                         TTSManager ttsManager,
                         MainRepository repository,
                         SoundRecorder soundRecorder,
                         SoundProcessor soundProcessor,
                         StringManager stringManager,
                         SharedPreferences sharedPreferences,
                         BluetoothController bluetoothController) {
        super(application);
        this.rxHelper = rxHelper;
        this.gson = gson;
        this.networkManager = networkManager;
        this.picoVoiceManager = picoVoiceManager;
        this.state = handle;
        this.impl = impl;
        this.ttsManager = ttsManager;
        this.repository = repository;
        this.soundRecorder = soundRecorder;
        this.stringManager = stringManager;
        this.sharedPreferences = sharedPreferences;

        soundProcessor.subscribeToProcessEvents(soundRecorder.getFramesToProcess());

        //get device id:
        String deviceId = sharedPreferences.getString(DEVICE_ID_KEY, null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
            sharedPreferences.edit().putString(DEVICE_ID_KEY, deviceId).apply();
        }
        this.deviceID = deviceId;

        //we listen for picovoice log events and push to our log function
        compositeDisposable.add(picoVoiceManager.getErrorOutput()
                .subscribeOn(Schedulers.io()).subscribe(this::log));

        //we listen for bluetooth connection events on io thread and handle in bluetoothConnectionChange
        //initially no headset may be connected and we will listen to onboard mic
        //but if headset disconnects we disable listening and re-enable upon reconnection
        compositeDisposable.add(bluetoothController.getBlueToothHeadsetConnectionEvent().subscribeOn(Schedulers.io()).subscribe(this::bluetoothConnectionChange));

        //starts scan queue with delay that may use queue to push events to queue / scanner if the option has been enabled in resourceList
        startScanQueue(50);

        this.detailedLogging = sharedPreferences.getBoolean(USE_DETAILED_LOGGING_ENABLED, false);

        compositeDisposable.add(ErrorEventBus.getErrors()
                .subscribe(this::logE, Throwable::printStackTrace));
    }

    public void bluetoothConnectionChange(Boolean connected) {
        log("bluetooth headset was " + (!connected ? "dis" : "") + "connected");
        if (!connected) {
            soundRecorder.stopListeningTask();
        } else {
            startListeningTask();
        }
    }

    public String getDeviceID() {
        return deviceID;
    }

    /**
     * This should be called initially, sets up raw files if necessary, and readies them for use
     * Then it initializes the rhino objects with the grammars
     */
    public void initPicoResources(Float sensitivity, boolean repeatWhenDone, boolean isReinitializing) {
        final float finalSensitivity = sensitivity != null ? sensitivity : impl.getSensitivity();
        repository.setCurrentSensitivity(finalSensitivity);
        compositeDisposable.add(
                Completable.defer(() -> {
                    for (int grammar : picoVoiceManager.getValidGrammars()) {
                        picoVoiceManager.initializeRhinoContext(finalSensitivity, grammar, inference -> picoVoiceCallback(grammar, inference));
                        log("init Rhino #: " + grammar + " sensitivity: " + finalSensitivity);
                    }
                    return Completable.complete();
                }).andThen(picoVoiceManager.initPorcupine(finalSensitivity, this::numinaStartCallback)).subscribe(() -> {
                    log("init Porcupine");
                    if (repeatWhenDone) {
                        ttsManager.repeat();
                    }
                    if (!impl.isVoiceLogin()) {
                        //we need to start listener, since it wasn't started previously
                        startListeningTask();
                    }
                    if (isReinitializing) {
                        listen();
                    } else {
                        startSendMessageTask(impl.createVoiceReadyObject(deviceID, mainViewUI.getOperatorID(), getApplication()));
                    }
                }, error -> {
                    log("picoVoice initialization failure");
                    error.printStackTrace();
                    logE("picoVoice initialization failure", error);
                })
        );
    }

    /**
     * This clears our networking threads and closes sockets if disconnection is desired or detected
     */
    public void onDisconnect() {
        mainViewUI.setConnected(false);
        pauseListening();
        log("disconnected with RSSI: " + impl.getWifiSignal() + " last bssid: " + impl.getLastBSSID());
        mainViewUI.setSocketListening(false);
        cancelHeartBeat();
        try {
            networkManager.closeSocket();
        } catch (IOException e) {
            e.printStackTrace();
            log("IO Exception closing socket..");
        }
        enableScanEvent.postValue(false);
        connectedEvent.postValue(false);
    }

    /**
     * This function sets up our socket connection object, and upon completion
     * sets up our socket listener.
     * Currently we are executing this from our connect button (other connect method)
     */
    public void connect() {
        log("connect called");
        connectionDisposable.add(
                rxHelper.schedule(impl.connectionTask())
                        .doOnComplete(() -> {
                            setUpSocketListener();
                            heartBeatStart(impl.getDelay());
                        })
                        .subscribe(() -> {
                                    log("connected");
                                    mainViewUI.setConnected(true);
                                    mainViewUI.setIsConnecting(false);
                                    startSendMessageTask(impl.createConnectionObject(deviceID, mainViewUI.getOperatorID(), getApplication()));
                                    connectedEvent.postValue(true);
                                },
                                error -> {
                                    log("could not connect");
                                    mainViewUI.setIsConnecting(false);
                                }
                        ));
    }


    /**
     * this creates and subscribes to our heartbeat task and handles what to do for each one in
     * onNext callback
     *
     * @param delayInMs how long to delay between heartbeats
     */
    public void heartBeatStart(int delayInMs) {
        heartBeatDisposable.add(
                Observable.interval(0, delayInMs, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(onNext -> {
                                    try {
                                        impl.heartBeat();
                                        networkConnectionStrengthEvent.postValue(impl.determineNetworkIndicatorColor());
                                        enableScanEvent.postValue(mainViewUI.getShouldScan());
                                        listeningNotificationEvent.postValue(repository.getIsListening());
                                    } catch (Exception e) {
                                        onDisconnect();
                                    }
                                },
                                error -> {
                                    error.printStackTrace();
                                    onDisconnect();
                                }));
    }

    /**
     * Posting to this event will notify subscribers(MainActivity) of text to display on screen
     *
     * @return print to console event
     */
    public LiveData<String> getPrintToConsoleEvent() {
        return printToConsoleEvent;
    }

    public LiveData<String> getPrintInstructionsEvent() {
        return printInstructionsEvent;
    }

    /**
     * This method subscribes to our socket listening task, and sets boolean to drive whether
     * or not socket listener keeps running
     */
    private void setUpSocketListener() {
        mainViewUI.setSocketListening(true);
        compositeDisposable.add(
                rxHelper.schedule(socketListener())
                        .subscribe(() -> {},
                                error -> onDisconnect()
                        ));
    }

    /**
     * This is our Socket listener task, while our socketListing boolean is set and uninterrupted,
     * continues listening on socket for new messages
     * handles the logic to respond to json: shows & speaks text, and sets grammar
     *
     * @return socket listener task
     */
    private Completable socketListener() {
        return Completable.defer(() -> {
            mainViewUI.setSocketListening(true);

            while (mainViewUI.isSocketListening()) {
                try {
                    handleServerResponse(networkManager.fetchString());
                } catch (Exception e) {
                    return Completable.error(e);
                }
            }
            return Completable.complete();
        });
    }

    /**
     * Method to handle response from server
     *
     * @param input raw string from server
     */
    private void handleServerResponse(String input) {
        if (input != null && !input.isEmpty()) {
            try {
                Log.d(TAG, input);
                BaseServiceObject serverResponse = gson.fromJson(input, BaseServiceObject.class);
                String command = serverResponse.getCommand();
                if (command != null) {
                    switch (command) {
                        case "voiceRequest":
                            handleVoiceRequest(serverResponse);
                            break;
                        case "resourceList":
                            ResourceList resourceList = gson.fromJson(serverResponse.getData(), ResourceList.class);
                            Float sensitivity = resourceList.getSensitivity();

                            ttsManager.setCurrentPitch(resourceList.getPitch());
                            ttsManager.setCurrentRate(resourceList.getRate());
                            ttsManager.setCurrentVolume(resourceList.getVolume());
                            ttsManager.setLanguage(resourceList.getLocale());

                            if (resourceList.getTranslationMap() != null) {
                                stringManager.setStrings(resourceList.getTranslationMap());
                            }
                            if (resourceList.getModelFilename() != null) {
                                picoVoiceManager.setModelFileName(resourceList.getModelFilename());
                            }
                            impl.handleResourceList(resourceList);
                            if (sensitivity!=null) {
                                repository.setCurrentSensitivity(sensitivity);
                            }
                            if (resourceList.isLogTimes()) {
                                repository.setLogTimes(true);
                            }
                            if (resourceList.isScanAhead()) {
                                repository.setScanAhead(true);
                            }
//                            repository.setCanScan(true);
                            initPicoResources(sensitivity, false, false);


                            //so we wait to this point to change the screen to main mode from initial login, otherwise we probably get a login failure and don't need to change modes yet.
                            loginEvent.postValue(null);
                            break;
                        case "error":
                            mainViewUI.setIsInLogonState(true);
                            String message = gson.fromJson(serverResponse.getData().get("text"), String.class);
                            if ("operator not valid".equals(message)) {
                                loginFailureEvent.postValue(message);
                                mainViewUI.setShouldScan(true);
                                onDisconnect();
                                connectionDisposable.clear();
                                enableScanEvent.postValue(true);
                            }
                            log("error message = " + message);
                            userErrorEvent.postValue(message);
                            break;
                        case "loginError":
                            mainViewUI.setIsInLogonState(true);
                            String loginMessage = gson.fromJson(serverResponse.getData().get("text"), String.class);

                            loginFailureEvent.postValue(loginMessage);
                            mainViewUI.setShouldScan(true);
                            onDisconnect();
                            connectionDisposable.clear();
                            enableScanEvent.postValue(true);

                            log("login error message = " + loginMessage);
                            userErrorEvent.postValue(loginMessage);
                            break;
                        case "quit":
                            logOff();
                            break;
                        case "dumpLogs":
                            dumpLogs();
                            break;
                        case "serverMessage":
                            ServerMessageServiceObject serviceObject = gson.fromJson(serverResponse.getData(), ServerMessageServiceObject.class);

                            changeServerMessage(new ServerMessage(serviceObject.getMessageText(), serviceObject.getTextColorString(), serviceObject.getBackgroundColorString()));
                        default:
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log("error in json deserialization");
            }
        }
    }

    /**
     * handles the logic of voice request from server
     * speaks, adjusts scan visibility, begins listening
     *
     * @param baseServiceObject the server voice request
     */
    private void handleVoiceRequest(BaseServiceObject baseServiceObject) {
        VoiceRequest voiceRequest = gson.fromJson(baseServiceObject.getData(), VoiceRequest.class);
        boolean shouldScan = YES.equals(voiceRequest.getScan());
        if (impl.isCameraEnabled()) {
            showCameraScanEvent.postValue(shouldScan);
        }
        if (repository.isLogTimes()) {
            scanInTimer = System.currentTimeMillis();
        }
        repository.setCanScan(shouldScan);
        mainViewUI.setShouldScanTimed(shouldScan);
        enableScanEvent2.postValue(shouldScan);
        repository.setLastInstructions(voiceRequest.getVoice());
        repository.setLastScanValue(shouldScan);
        printInstructionsEvent.postValue(voiceRequest.getText());


        log("voiceRequestText = " + voiceRequest.getText());
        picoVoiceManager.setCurrentGrammarValue(Integer.parseInt(voiceRequest.getGrammar()));
        log("currentGrammar = " + picoVoiceManager.getCurrentGrammarValue());
        ttsManager.stop();
        ttsManager.speak(voiceRequest.getVoice());
        if (repository.isLogTimes()) {
            voiceInTimer = System.currentTimeMillis();
        }
        listen();
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
                        .subscribe(() -> {},
                                error -> onDisconnect()));
    }

    @Override
    protected void onCleared() {
        log("onCleared");
        pauseListening();
        mainViewUI.setSocketListening(false);
        compositeDisposable.clear();
        connectionDisposable.clear();
        audioRecorderDisposable.clear();
        cancelHeartBeat();

//        picoVoiceManager.clearRhinoContexts();

        ttsManager.release();
        soundRecorder.stopListeningTask();
        super.onCleared();
    }

    /**
     * This is the callback for all picoVoice grammars
     *
     * @param inference the Rhino callback
     */
    private void picoVoiceCallback(int grammar, RhinoInference inference) {

        runOnUiThread(() -> {
            if (grammar == picoVoiceManager.getCurrentGrammarValue()) {

                if (!repository.getIsListening()) {
                    return;
                }
                if (inference.getIsUnderstood()) {
                    if (repository.isLogTimes()) {
                        voiceOutTimer = System.currentTimeMillis();
                        log("voice turnaround time = " + (voiceOutTimer - voiceInTimer) + " ms");
                        voiceInTimer = 0;
                        voiceOutTimer = 0;
                    }
                    ttsManager.stop();

                    String code = inference.getIntent();
                    String command;
                    switch (code.toUpperCase()) {
                        case "SYSCOMMAND":
                            command = inference.getSlots().get("command");
                            if ("stop".equalsIgnoreCase(command)) {
                                onOffTriggerEvent.postValue(false);
                            }
                            break;
                        case "STOP":
                            onOffTriggerEvent.postValue(false);
                            break;
                        case "SYSREPEAT":
                            log("executing local command: repeat");
                            commandSentEvent.postValue("Repeat");
                            ttsManager.repeat();
                            break;
                        default:
                            picoVoiceManager.setCurrentGrammarValue(0);
                            pauseListening();
                            StringBuilder sb = new StringBuilder();
                            if (inference.getSlots() != null && inference.getSlots().entrySet().size() > 0) {
                                for (Map.Entry<String, String> stringStringEntry : inference.getSlots().entrySet()) {
                                    sb.append(stringStringEntry.getValue());
                                }
                            }
                            String slotInfo = sb.toString();
                            //so if we get back a list of numbers, letters, they should construct a string and be sent
                            //else we should send back the string interpretation itself
                            //
                            //example with the QTYFOUR intent:
                            // speaking, "1,2,3,4, ok" may send back an inference with 'code' QTYFOUR  (which we don't care about in this case)
                            // the inference will contain map of values(exactly four in this case), so we can iterate through map and build string "1234" to return
                            // but some other command may not have associated values, for instance:
                            // speaking, "please log off" may send back "QUIT", which we just want to send as is to server
                            String toSend = slotInfo.length() > 0 ? slotInfo : code;
                            log("sending voice response: " + toSend);
                            sendVoiceResponse(toSend);
                    }
                } else {
                    log("did not understand voice command");
                }
            }

        });

    }

    public void sendVoiceResponse(String toSend) {
        VoiceResponse response = new VoiceResponse(toSend, picoVoiceManager.getCurrentGrammarValue(), mainViewUI.getOperatorID(), deviceID);
        startSendMessageTask(impl.createVoiceResponseString(response));
        commandSentEvent.postValue(toSend);
    }

    /**
     * Performs action on main thread (needed for picoVoice here)
     *
     * @param action Runnable to run on main thread
     */
    private void runOnUiThread(Runnable action) {
        mainHandler.post(action);
    }

    public MainViewUI getMainViewUI() {
        return mainViewUI;
    }

    public void cancelHeartBeat() {
        heartBeatDisposable.clear();
    }

    public void pauseListening() {
        repository.setIsListening(false);
        listeningNotificationEvent.postValue(false);
    }

    public void stopListeningAndClearGrammars() {
        pauseListening();
        soundRecorder.stopListeningTask();
        log("stop listening called");
        picoVoiceManager.clearInstanceRhinoContexts();

    }

    public LiveData<Boolean> getShowCameraScanEvent() {
        return showCameraScanEvent;
    }

    public LiveData<Boolean> getEnableScanEvent() {
        return enableScanEvent;
    }

    public LiveData<Boolean> getEnableScanEvent2() {
        return enableScanEvent2;
    }

    public void scanComplete(String result) {
        if (!getScanAhead()) {
            pauseListening();
        }
        if (result != null) {


            if (repository.isLogTimes()) {
                long currentTime = System.currentTimeMillis();
                long diff = scanOutTimer - currentTime;
                log("scan sent instruction acted on in " + diff + " ms");
            }
            if (repository.isScanAhead()) {
                repository.getScanQueue().add(result);
//                log("queue size = "+ repository.getScanQueue().size());
            } else {
                sendScanResponse(result);
            }

        } else {
            log("received no scan result back (no camera?)");
            ttsManager.repeat();
        }
    }


    private void startScanQueue(int delayInMs) {
        compositeDisposable.add(
                Observable.interval(0, delayInMs, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(onNext -> {
                            if (repository.isScanAhead()) {
                                if (!repository.getScanQueue().isEmpty() && repository.isCanScan()) {
                                    repository.setCanScan(false);
                                    String result = repository.getScanQueue().poll();
                                    sendScanResponse(result);
                                }
                            }
                        })
        );
    }

    private void sendScanResponse(String response) {
        log("sending scan response: " + response);
        VoiceResponse voiceResponse = new VoiceResponse(response, picoVoiceManager.getCurrentGrammarValue(), mainViewUI.getOperatorID(), deviceID);
        startSendMessageTask(impl.createVoiceResponseString(voiceResponse));
        commandSentEvent.postValue(response);
    }

    private void numinaStartCallback(int keyValue) {
        if (connectedEvent.getValue()) {
            picoVoiceManager.stopPorcupine();
            onOffTriggerEvent.postValue(true);
            commandSentEvent.postValue("start");
        }
    }

    private void restartListeningAndScanning() {
        picoVoiceManager.stopPorcupine();
        if (mainViewUI.getConnected().get()) {
            log("restarting listening and scanning");
            //reset the previous scan state
            mainViewUI.setShouldScan(repository.getLastScanValue());
            enableScanEvent.postValue(repository.getLastScanValue());


            ttsManager.repeat();
            listen();
        }
        startListeningTask();


    }

    public LiveData<Boolean> getLoggingInEvent() {
        return loggingInEvent;
    }

    public LiveData<String> getLoginFailureEvent() {
        return loginFailureEvent;
    }

    public LiveData<String> getUserErrorEvent() {
        return userErrorEvent;
    }

    public LiveData<Boolean> getConnectedEvent() {
        return connectedEvent;
    }

    public LiveData<Boolean> getListeningNotificationEvent() {
        return listeningNotificationEvent;
    }

    public LiveData<String> getCommandSentEvent() {
        return commandSentEvent;
    }

    public LiveData<Boolean> getExitEvent() {
        return exitEvent;
    }

    public LiveData<Boolean> getExitingEvent() {
        return exitingEvent;
    }

    public LiveData<Integer> getNetworkConnectionStrengthEvent() {
        return networkConnectionStrengthEvent;
    }

    public LiveData<Boolean> getOnOffTriggerEvent() {
        return onOffTriggerEvent;
    }

    private void reconnect() {
        if (!mainViewUI.isConnecting() && !networkManager.isConnected()) {
            log("(re)connecting to Server...");
            mainViewUI.setIsConnecting(true);
            connect();
        }
    }

    public void connectionListenerTaskStart() {
        final int connectionInterval = 1000;//retry every 1 second when disconnected
        connectionDisposable.add(
                Observable.interval(0, connectionInterval, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe(onNext -> reconnect(),
                                error -> {}));
    }

    public void login() {
        Log.d(TAG, "log in clicked");
        login(loginUI.getLoginText());
    }

    public void login(String operatorId) {
        if (operatorId != null && operatorId.length() > 0) {
            pauseListening();
            loggingInEvent.postValue(true);
            loginUI.setLoginText("");
            mainViewUI.setOperatorID(operatorId);
            mainViewUI.setIsInLogonState(false);
            connectionListenerTaskStart();

        } else {
            loginFailureEvent.postValue(null);
            Log.d(TAG, "Login Failure");
        }
    }


    public LoginUI getLoginUI() {
        return loginUI;
    }
    public String getVoiceVersion() {
        return "Rhino: " + BuildConfig.RHINO_VERSION + " Porcupine: " + BuildConfig.PORCUPINE_VERSION;
    }


    //this gets called upon returning to login screen after logout
    public void restart() {
        picoVoiceManager.setLoginGrammar();
        if (impl.isVoiceLogin()) {
            listen();
        }
    }

    private void picoVoiceLoginCallback(RhinoInference inference) {

        runOnUiThread(() -> {
            log("picoVoiceLoginCallback got callback");
            if (inference.getIsUnderstood()) {
                pauseListening();
                StringBuilder loginId = new StringBuilder();
                for (String digit : inference.getSlots().values()) {
                    loginId.append(digit);
                }
                login(loginId.toString());
                log("sending event " + loginId);
            } else {
                log("login: did not understand");
            }
        });

    }

    public void log(String input) {
        printToConsoleEvent.postValue(null);
        mainViewUI.setConsoleText(input + '\n' + mainViewUI.getConsoleText());
        Log.d(TAG, input);
        if (detailedLogging) {
            sendLog(input);
        }
    }

    public void logE(String input, Throwable e) {
        Log.e(TAG, input, e);
        if (e != null) {
            sendError(getStackTraceAsString(e));
        }
    }

    public void logE(Throwable e) {
        if (e != null) {
            sendError(getStackTraceAsString(e));
        }
    }

    private void sendLog(String message) {
        startSendMessageTask(gson.toJson(new SimpleServiceObject(SEND_LOG_MESSAGE, message)));
    }

    public void sendError(String message) {
        startSendMessageTask(gson.toJson(new SimpleServiceObject(SEND_ERROR_MESSAGE, message)));
    }


    public LiveData<String> getLoginEvent() {
        return loginEvent;
    }

    public void initLoginResources() {
        final float sensitivity = impl.getSensitivity();
        compositeDisposable.add(

                picoVoiceManager.initializeLoginRhino(sensitivity, this::picoVoiceLoginCallback)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
//                        .andThen(soundRecorder.listeningTask())
                        .subscribe(this::startListeningTask,
                                error -> {
                                    error.printStackTrace();
                                    logE("initLoginResources", error);
                                }));


    }

    public void stopScanning() {
        showCameraScanEvent.postValue(false);
    }

    private void listen() {
        repository.setIsListening(true);
        listeningNotificationEvent.postValue(true);
    }

    private void logOff() {
        exitingEvent.postValue(true);
        soundRecorder.stopListeningTask();
        ttsManager.speak("goodbye");
        commandSentEvent.postValue("Goodbye!");
        try {
            Thread.sleep(2000);//approximate wait for speech to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log("logOff called");
        connectionDisposable.clear();
        onDisconnect();
        picoVoiceManager.clearInstanceRhinoContexts();
        compositeDisposable.clear();
        exitEvent.postValue(true);
    }

    private void startListeningTask() {
        audioRecorderDisposable.add(soundRecorder.listeningTask().subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(() -> {},
                error -> {
                    error.printStackTrace();
                    logE("startListeningTask", error);
                }));
    }

    public void dumpLogs() {
        log("dump logs clicked");
        startSendMessageTask(impl.createLogDump());
        try {//clear logs for subsequent attempt
            Process process = new ProcessBuilder()
                    .command("logcat", "-c")
                    .redirectErrorStream(true)
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
            logE("dump log error", e);
        }
    }

    //this is called on '<wake word> stop' or manually from UI switch
    //stops listening and scanning
    private void stop() {
        soundRecorder.stopListeningTask();
        log("executing local command: stop");
        commandSentEvent.postValue("stop");
        pauseListening();

        mainViewUI.setShouldScan(false);
        enableScanEvent.postValue(false);
        rxHelper.schedule(impl.delayedPorcupineStart()).subscribe();

    }

    //this executes the code for the start / stop switch functionality
    //that enables/disables the speech and scanning (if they are enabled)
    //when we stop, we start listening for <wake word>
    //and this is again triggered to start: toggles back to scan/listen mode
    public void startStop(boolean enable) {
//        boolean manualStopEnabled = mainViewUI.getIsManualStopEnabled();
        if (enable) {
            restartListeningAndScanning();
        } else {
            stop();
        }
//        mainViewUI.setIsManualStopEnabled(!manualStopEnabled);
    }

    public String getString(String key) {
        return stringManager.getString(key);
    }


    public long getScanInTimer() {
        return scanInTimer;
    }

    public void setScanInTimer(long scanInTimer) {
        this.scanInTimer = scanInTimer;
    }

    public long getScanOutTimer() {
        return scanOutTimer;
    }

    public void setScanOutTimer(long scanOutTimer) {
        this.scanOutTimer = scanOutTimer;
    }

    public boolean getScanAhead() {
        return repository.isScanAhead();
    }

    public boolean getLogTimes() {
        return repository.isLogTimes();
    }


    /**
     * This method is called when we get a new ServerMessage from the server.
     * It notifies the serverMessageEvent that there is a new ServerMessage object and it
     * sets the serverMessage for the mainViewUI to display.
     *
     * @param serverMessage This is the ServerMessage object that we want to display
     **/
    private void changeServerMessage(ServerMessage serverMessage) {
        serverMessageEvent.postValue(serverMessage);
        mainViewUI.setServerMessage(serverMessage);
    }

    /**
     * Getter method that returns the serverMessageEvent
     **/
    public LiveData<ServerMessage> getServerMessageEvent() {return serverMessageEvent;}
}
