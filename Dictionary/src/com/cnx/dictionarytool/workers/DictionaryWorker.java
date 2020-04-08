package com.cnx.dictionarytool.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.cnx.dictionarytool.di.components.DaggerNetworkComponent;
import com.cnx.dictionarytool.di.components.NetworkComponent;
import com.cnx.dictionarytool.di.modulles.ContextModule;
import com.cnx.dictionarytool.di.modulles.NetworkModule;
import com.cnx.dictionarytool.di.modulles.OkHttpClientModule;
import com.cnx.dictionarytool.interfaces.RandomUsersApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class DictionaryWorker extends Worker {

    private final String CURRENT_SCREEN =  DictionaryWorker.this.getClass().getSimpleName();
    private static final String WORK_RESULT = "work_result";

    private Context context;
    private NetworkComponent networkComponent;

    public DictionaryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        initCnxNetworkConnection(context);

        Data outputData = new Data.Builder().putString(WORK_RESULT, "Jobs Finished").build();
        return Result.success(outputData);
    }



    private RandomUsersApi getNetworkService(Context context) {
        if(networkComponent==null){
            networkComponent = DaggerNetworkComponent.builder()
                    .contextModule(new ContextModule(context))
                    .networkModule(new NetworkModule())
                    .okHttpClientModule(new OkHttpClientModule())
                    .build();

        }
        return networkComponent.getService();
    }


    /********************************************************** RETROFIT *************************************************/
    private void initCnxNetworkConnection(Context context) {
        Call<ResponseBody> call = getNetworkService(context).downloadDictionary();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Timber.d(CURRENT_SCREEN, "server contacted and has file");
                    boolean writtenToDisk = false;
                    if(response.body()!=null){
                        writtenToDisk = writeResponseBodyToDisk(response.body());
                    }

                    Timber.d(CURRENT_SCREEN, "file download was a success? %s", writtenToDisk);

                    //Timber.d(CURRENT_SCREEN, "file download was a success? " + writtenToDisk);
                } else {
                    Timber.d(CURRENT_SCREEN, "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Timber.e(CURRENT_SCREEN, "error");
            }
        });

    }
    /********************************************************** RETROFIT *************************************************/

    /********************************************************** WRITE TO STORAGE *****************************************/
    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(context.getExternalFilesDir(null) + File.separator + "test.png");

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Timber.d(CURRENT_SCREEN, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
    /********************************************************** WRITE TO STORAGE *****************************************/


}
