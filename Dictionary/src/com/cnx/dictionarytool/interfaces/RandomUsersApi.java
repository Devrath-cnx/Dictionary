package com.cnx.dictionarytool.interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

import static com.cnx.dictionarytool.utils.Constants.CYBERNETYX_URL_API;

public interface RandomUsersApi {
    @GET(CYBERNETYX_URL_API)
    Call<ResponseBody> downloadDictionary();
}
