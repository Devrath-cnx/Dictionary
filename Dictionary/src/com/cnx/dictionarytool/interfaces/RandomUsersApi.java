package com.cnx.dictionarytool.interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface RandomUsersApi {
    @Streaming
    @GET("/download/test.png")
    Call<ResponseBody> downloadDictionary();
}
