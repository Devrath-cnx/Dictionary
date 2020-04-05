package com.cnx.dictionarytool.application.utils

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import java.io.*

class CopyAssets(val context: Context) {

     fun getDictionaryFileUri(): String {
         return if(UtilPath(context).isDictionaryExists()){
             //Get the path of the dictionary file
             UtilPath(context).getDictionaryPath()
         }else{
             //Copy the file from assets
             copyDataFromAssets(context)
             //Get the path from the file location
             UtilPath(context).getDictionaryPath()
         }
    }

    private fun copyDataFromAssets(context: Context) {
        //Copy from assets to the storage
        val assetManager: AssetManager = context.assets
        var files: Array<String>? = null
        try {
            files = assetManager.list("")
        } catch (e: IOException) {
            Log.e("tag", "Failed to get asset file list.", e)
        }
        if (files != null) for (filename in files) {
            var `in`: InputStream? = null
            var out: OutputStream? = null
            try {
                `in` = assetManager.open(filename)
                val outFile = File(context.getExternalFilesDir(null), filename)
                out = FileOutputStream(outFile)
                copyFile(`in`, out)
            } catch (e: IOException) {
                Log.e("tag", "Failed to copy asset file: $filename", e)
            } finally {
                if (`in` != null) {
                    try {
                        `in`.close()
                    } catch (e: IOException) {
                        Log.e("tag", "Failed to copy asset file: $filename", e)
                    }
                }
                if (out != null) {
                    try {
                        out.close()
                    } catch (e: IOException) {
                        Log.e("tag", "Failed to copy asset file: $filename", e)
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int? = null
        while (`in`.read(buffer).also({ read = it }) != -1) {
            read?.let { out.write(buffer, 0, it) }
        }
    }

}