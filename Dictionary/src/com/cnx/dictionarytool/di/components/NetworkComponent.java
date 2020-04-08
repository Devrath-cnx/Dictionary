package com.cnx.dictionarytool.di.components;

import com.cnx.dictionarytool.di.modulles.NetworkModule;
import com.cnx.dictionarytool.interfaces.RandomUsersApi;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {NetworkModule.class})
public interface NetworkComponent {
    RandomUsersApi getService();
}
