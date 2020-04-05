package com.cnx.dictionarytool.application.views

import android.content.Context
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.cnx.dictionarytool.R
import com.cnx.dictionarytool.application.utils.CopyAssets
import com.cnx.dictionarytool.application.utils.UtilPath
import com.cnx.dictionarytool.library.activities.DictionaryApplication
import com.cnx.dictionarytool.library.util.engine.Dictionary
import java.io.File
import java.nio.channels.FileChannel


class DictionaryBaseView : FrameLayout {

    private var application: DictionaryApplication? = null

    private val newPath = "/storage/dictionary/EN.quickdic"

    private var dictionary: Dictionary? = null
    private var dictFile: File? = null
    private var dictRaf: FileChannel? = null
    private var dictFileTitleName: String? = null


    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.fragment_base_dictionary, this)
        initOnCreate(context)
    }



    private fun initOnCreate(context: Context) {
        //Set up the dictionary file for search
        CopyAssets(context).getDictionaryFileUri()

        DictionaryApplication.INSTANCE.init(context)
        application = DictionaryApplication.INSTANCE
        val filepath: String = Environment.getExternalStorageDirectory().path
        Log.d("","")
        val externalStorageVolumes: Array<out File> = ContextCompat.getExternalFilesDirs(context, null)
        val primaryExternalStorage = externalStorageVolumes[0]
        Log.d("","")



        if(CopyAssets(context).getDictionaryFileUri().isNotEmpty()){
            val dictionaryFile = CopyAssets(context).getDictionaryFileUri()
            Toast.makeText(context,dictionaryFile,Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(context,"Dictionary file not available",Toast.LENGTH_LONG).show()
        }

    }






}