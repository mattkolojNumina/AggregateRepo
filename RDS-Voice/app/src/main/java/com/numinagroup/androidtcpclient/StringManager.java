package com.numinagroup.androidtcpclient;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StringManager {

    public final String CONNECTING_KEY;
    public final String INFO_TAB_NAME_KEY;
    public final String LOG_TAB_NAME_KEY;
    public final String OPTION_TAB_NAME_KEY;
    public final String CAMERA_SCAN_BUTTON_KEY;
    public final String DUMP_LOGS_BUTTON_LABEL_KEY;
    public final String LAST_COMMAND_DESCRIPTION_KEY;
    public final String OPTIONS_COLON_KEY;
    public final String PITCH_COLON_KEY;
    public final String SPEED_COLON_KEY;
    public final String VOLUME_COLON_KEY;
    public final String CURRENT_INSTRUCTIONS_COLON_KEY;
    public final String SPEECH_SENSITIVITY_COLON_KEY;
    public final String OFF_KEY;
    public final String ON_KEY;
    public final String VERSION_COLON_KEY;

    Map<String, String> stringsMap = new HashMap<>();

    @Inject
    public StringManager(Application application) {
        CONNECTING_KEY = application.getString(R.string.connecting);
        stringsMap.put(CONNECTING_KEY, CONNECTING_KEY);
        INFO_TAB_NAME_KEY = application.getString(R.string.info_tab_name);
        stringsMap.put(INFO_TAB_NAME_KEY, INFO_TAB_NAME_KEY);
        LOG_TAB_NAME_KEY = application.getString(R.string.log_tab_name);
        stringsMap.put(LOG_TAB_NAME_KEY, LOG_TAB_NAME_KEY);
        OPTION_TAB_NAME_KEY = application.getString(R.string.option_tab_name);
        stringsMap.put(OPTION_TAB_NAME_KEY, OPTION_TAB_NAME_KEY);
        CAMERA_SCAN_BUTTON_KEY = application.getString(R.string.camera_scan_button_label);
        stringsMap.put(CAMERA_SCAN_BUTTON_KEY, CAMERA_SCAN_BUTTON_KEY);
        DUMP_LOGS_BUTTON_LABEL_KEY = application.getString(R.string.dump_logs_button_label);
        stringsMap.put(DUMP_LOGS_BUTTON_LABEL_KEY, DUMP_LOGS_BUTTON_LABEL_KEY);
        LAST_COMMAND_DESCRIPTION_KEY = application.getString(R.string.last_command_description);
        stringsMap.put(LAST_COMMAND_DESCRIPTION_KEY, LAST_COMMAND_DESCRIPTION_KEY);
        OPTIONS_COLON_KEY = application.getString(R.string.options_colon);
        stringsMap.put(OPTIONS_COLON_KEY, OPTIONS_COLON_KEY);
        PITCH_COLON_KEY = application.getString(R.string.pitch_colon);
        stringsMap.put(PITCH_COLON_KEY, PITCH_COLON_KEY);
        SPEED_COLON_KEY = application.getString(R.string.speed_colon);
        stringsMap.put(SPEED_COLON_KEY, SPEED_COLON_KEY);
        VOLUME_COLON_KEY = application.getString(R.string.volume_colon);
        stringsMap.put(VOLUME_COLON_KEY, VOLUME_COLON_KEY);
        CURRENT_INSTRUCTIONS_COLON_KEY = application.getString(R.string.current_instructions_colon);
        stringsMap.put(CURRENT_INSTRUCTIONS_COLON_KEY, CURRENT_INSTRUCTIONS_COLON_KEY);
        SPEECH_SENSITIVITY_COLON_KEY = application.getString(R.string.speech_sensitivity_colon);
        stringsMap.put(SPEECH_SENSITIVITY_COLON_KEY, SPEECH_SENSITIVITY_COLON_KEY);
        OFF_KEY = application.getString(R.string.off);
        stringsMap.put(OFF_KEY, OFF_KEY);
        ON_KEY = application.getString(R.string.on);
        stringsMap.put(ON_KEY, ON_KEY);
        VERSION_COLON_KEY = application.getString(R.string.version_colon);
        stringsMap.put(VERSION_COLON_KEY, VERSION_COLON_KEY);
    }

    public String getString(String key) {
        return stringsMap.get(key);
    }

    public void setStrings(Map<String, String> newMap) {
        this.stringsMap = newMap;
    }
}
