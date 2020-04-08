package com.cnx.dictionarytool.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleObserver;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.cnx.dictionarytool.R;
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

import static com.cnx.dictionarytool.utils.Constants.DICTIONARY_FILE;
import static com.cnx.dictionarytool.utils.Constants.INTENT_DOWNLOAD_DICTIONARY_PARAM;
import static com.cnx.dictionarytool.utils.Constants.LOCAL_BROADCAST_DICTIONARY;

public class DictionaryWorker extends Worker implements LifecycleObserver {

    private final String CURRENT_SCREEN =  DictionaryWorker.this.getClass().getSimpleName();
    private static final String WORK_RESULT = "work_result";
    private static final String CHANNEL_ID = "128";


    private Context context;
    private NetworkComponent networkComponent;
    private NotificationCompat.Builder mBuilder;
    private  NotificationManagerCompat notificationManager;
    private boolean isDownloadSuccessful  = false;



    public DictionaryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try{
            /** Initilize the notification channel **/
            initNotificationChannel();
            /** Connect to cnx server , download the file and write the file to storage **/
            initCnxNetworkConnection(context);

            if(isDownloadSuccessful){
                sendMessage(true);
                Data outputData = new Data.Builder().putString(WORK_RESULT, "Jobs Finished").build();
                return Result.success(outputData);
            }else{
                sendMessage(false);
                Data outputData = new Data.Builder().putString(WORK_RESULT, "Jobs Finished").build();
                return Result.failure(outputData);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        sendMessage(false);
        Data outputData = new Data.Builder().putString(WORK_RESULT, "Jobs Finished").build();
        return Result.failure(outputData);

    }

    private void sendMessage(boolean value) {
        Intent intent = new Intent(LOCAL_BROADCAST_DICTIONARY);
        // You can also include some extra data.
        intent.putExtra(INTENT_DOWNLOAD_DICTIONARY_PARAM, value);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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
    private void initCnxNetworkConnection(final Context context) {
        notifyDictionaryProgress(context.getResources().getString(R.string.str_kneura), "Connecting to kneura server");

        try{
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
                        incompleteDownload();
                        Timber.d(CURRENT_SCREEN, "server contact failed");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    incompleteDownload();
                    Timber.e(CURRENT_SCREEN, "error");
                }
            });
        }catch (Exception ex){
            incompleteDownload();
            Timber.e(CURRENT_SCREEN, "error%s", ex);
        }


    }
    /********************************************************** RETROFIT *************************************************/

    /********************************************************** WRITE TO STORAGE *****************************************/
    private boolean writeResponseBodyToDisk(ResponseBody body) {
        notifyDictionaryProgress(context.getResources().getString(R.string.str_kneura),
                                 context.getResources().getString(R.string.str_kneura_dict_sync_write_to_device));
        try {
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(context.getExternalFilesDir(null) + File.separator + DICTIONARY_FILE);

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

                }
                notificationComplete(context.getResources().getString(R.string.str_kneura),
                                     context.getResources().getString(R.string.str_kneura_dict_sync_complete));
                outputStream.flush();
                isDownloadSuccessful  = true;
                return true;
            } catch (IOException e) {
                incompleteDownload();
                Timber.e(CURRENT_SCREEN, e.getMessage());
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
            incompleteDownload();
            Timber.e(CURRENT_SCREEN, e.getMessage());
            return false;
        }
    }
    /********************************************************** WRITE TO STORAGE *****************************************/

    /********************************************************** Notifications ********************************************/
    /** Initialize the notification channel  **/
    private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.str_kneura);
            String description = context.getString(R.string.str_kneura_dict_sync);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /** Display state - Updating intermediate message states **/
    private void notifyDictionaryProgress(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_dictionary)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setProgress(0, 0, true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            notificationManager = NotificationManagerCompat.from(context);

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(0, mBuilder.build());
        }
    }

    /** Display state - when dictionary have finished downloading **/
    private void notificationComplete(String title, String message) {
        notifyDictionaryProgress(title, message);
        mBuilder.setProgress(0, 0, false);
        notificationManager.notify(0, mBuilder.build());
    }

    /** Incomplete download state **/
    private void incompleteDownload() {
        isDownloadSuccessful  = false;
        notificationComplete(context.getResources().getString(R.string.str_kneura),
                context.getResources().getString(R.string.str_kneura_dict_sync_in_complete));
    }
    /********************************************************** Notifications ********************************************/


}
