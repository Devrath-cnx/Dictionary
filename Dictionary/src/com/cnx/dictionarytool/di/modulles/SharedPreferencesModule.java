package com.cnx.dictionarytool.di.modulles;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.cnx.dictionarytool.utils.Constants.SHARED_PREFERENCES_FILE_NAME;

@Module(includes = ContextModule.class)
public class SharedPreferencesModule {

    @Provides
    SharedPreferences provideSharedPreference(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }
}
