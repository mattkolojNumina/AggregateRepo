package com.numinagroup.androidtcpclient.models;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.numinagroup.sharedlibrary.BuildConfig;

public class ConnectionResponse {

    private final String deviceID;

    private final String version;
    private final String operatorID;
    private final String deviceOSBuild;
    private final String buildModel;

    private final String voiceVersion;

    private final String androidVersion;

    private final String releaseMode;

    private final String pairedDeviceList;

    public ConnectionResponse(String deviceID, String operatorID, Application application, String voiceVersion, String pairedDeviceList) {
        this.deviceID = deviceID;
        this.operatorID = operatorID;
        this.androidVersion = Build.VERSION.RELEASE;
        this.releaseMode = BuildConfig.DEBUG ? "Debug" : "Release";
        this.buildModel = Build.MODEL;
        this.pairedDeviceList = pairedDeviceList;

        String versionName = "";
        try {
            PackageInfo packageInfo = application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        this.version = versionName;
        this.deviceOSBuild = Build.DISPLAY;
        this.voiceVersion = voiceVersion;
    }
}
