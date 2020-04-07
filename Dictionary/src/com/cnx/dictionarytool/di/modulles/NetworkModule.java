package com.cnx.dictionarytool.di.modulles;

import com.cnx.dictionarytool.interfaces.RandomUsersApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.cnx.dictionarytool.application.utils.Constants.CYBERNETYX_URL;
import static com.cnx.dictionarytool.application.utils.Constants.DICTIONARY_FILE_URL;

@Module(includes = OkHttpClientModule.class)
public class NetworkModule {

    @Provides
    public RandomUsersApi randomUsersApi(Retrofit retrofit){
        return retrofit.create(RandomUsersApi.class);
    }

    @Provides
    public Retrofit retrofit(OkHttpClient okHttpClient,GsonConverterFactory gsonConverterFactory, Gson gson){
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(CYBERNETYX_URL)
                .addConverterFactory(gsonConverterFactory)
                .build();
    }

    @Provides
    public Gson gson(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder.create();
    }

    @Provides
    public GsonConverterFactory gsonConverterFactory(Gson gson){
        return GsonConverterFactory.create(gson);
    }

}
