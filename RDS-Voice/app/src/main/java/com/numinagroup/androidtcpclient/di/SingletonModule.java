package com.numinagroup.androidtcpclient.di;

import static com.numinagroup.sharedlibrary.util.Constants.USE_SPEAKER_ENABLED;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.numinagroup.sharedlibrary.audio.PicoVoiceManager;
import com.numinagroup.sharedlibrary.audio.SoundProcessor;
import com.numinagroup.sharedlibrary.repository.MainRepository;
import com.numinagroup.sharedlibrary.util.BluetoothController;
import com.numinagroup.sharedlibrary.util.DependencyProvider;
import com.numinagroup.sharedlibrary.util.FileManager;
import com.numinagroup.sharedlibrary.util.RxHelper;
import com.numinagroup.sharedlibrary.util.TTSManager;

import java.util.HashMap;
import java.util.HashSet;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityRetainedComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.android.scopes.ActivityRetainedScoped;

@Module
@InstallIn(ActivityRetainedComponent.class)
public class SingletonModule {


    @ActivityRetainedScoped
    @Provides
    PicoVoiceManager providePicoVoiceManager(@ApplicationContext Context context, RxHelper rxHelper, FileManager fileManager) {
        return new PicoVoiceManager(rxHelper, context, fileManager, new DependencyProvider(), new HashMap<>(), new HashSet<>());
    }


    @ActivityRetainedScoped
    @Provides
    TTSManager provideTTSManager(@ApplicationContext Context context, MainRepository repository) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return new TTSManager(context, repository, prefs.getBoolean(USE_SPEAKER_ENABLED, false), new Bundle(), new DependencyProvider());
    }

    @ActivityRetainedScoped
    @Provides
    FileManager provideFileManager(@ApplicationContext Context context) {
        return new FileManager(context);
    }

    @ActivityRetainedScoped
    @Provides
    SoundProcessor provideSoundProcessor(PicoVoiceManager picoVoiceManager) {
        return new SoundProcessor(picoVoiceManager);
    }

    @ActivityRetainedScoped
    @Provides
    BluetoothController provideBluetoothController(@ApplicationContext Context context) {
        return new BluetoothController(context);
    }
}
