package com.numinagroup.androidtcpclient.di;

import android.app.Application;

import com.numinagroup.sharedlibrary.util.ErrorEventBus;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            // Log the exception
            ErrorEventBus.postError(throwable);
            // Call the default handler
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }
}
