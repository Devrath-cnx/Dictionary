package com.cnx.dictionarytool.di.components;

import android.content.SharedPreferences;

import com.cnx.dictionarytool.di.modulles.NetworkModule;
import com.cnx.dictionarytool.di.modulles.SharedPreferencesModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {SharedPreferencesModule.class})
public interface SharedPreferencesComponent {

    SharedPreferences prefManager();
}
