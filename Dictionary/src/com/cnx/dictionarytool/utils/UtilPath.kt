package com.cnx.dictionarytool.utils

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.cnx.dictionarytool.utils.Constants.DICTIONARY_FILE
import java.io.File

class UtilPath(val context: Context) {


    fun getDictionaryPath() : String {
        val mFile = ""
        val externalStorageVolumes: Array<out File> = ContextCompat.getExternalFilesDirs(context, null)
        for (item: File in externalStorageVolumes) {
            val primaryExternalStorage = item
            Log.d("","")
            return traverseForGettingTheFile(item)
        }
        return mFile
    }


    /** Check if dictionary file exists **/
    fun isDictionaryExists(): Boolean {
        val isFileFound = false
        val externalStorageVolumes: Array<out File> = ContextCompat.getExternalFilesDirs(context, null)
        for (item: File in externalStorageVolumes) {
            val primaryExternalStorage = item
            Log.d("","")
            return traverseForCheckingTheFile(item)
        }
        return isFileFound
    }

    /** Traverse files recursively for checking the file if it exists**/
    private fun traverseForCheckingTheFile(dir: File): Boolean {
        if (dir.exists()) {
            val files = dir.listFiles()
            for (i in files.indices) {
                val file = files[i]
                if (file.isDirectory) {
                    Log.d("","")
                    traverseForCheckingTheFile(file)
                } else {
                    Log.d("","")
                    if(file.endsWith(DICTIONARY_FILE)){
                        //Dictionary file found
                        return true
                    }
                }
            }
        }
        return false
    }

    /** Traverse files recursively for getting file **/
    private fun traverseForGettingTheFile(dir: File): String {
        if (dir.exists()) {
            val files = dir.listFiles()
            for (i in files.indices) {
                val file = files[i]
                if (file.isDirectory) {
                    Log.d("","")
                    traverseForGettingTheFile(file)
                } else {
                    Log.d("","")
                    if(file.endsWith(DICTIONARY_FILE)){
                        //Dictionary file found
                        return file.path
                    }
                }
            }
        }
        return ""
    }
}