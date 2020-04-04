package com.cnx.dictionarytool.application

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.cnx.dictionarytool.R
import com.cnx.dictionarytool.library.activities.DictionaryApplication

class DictionaryBaseView : FrameLayout {

    private var application: DictionaryApplication? = null

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
        DictionaryApplication.INSTANCE.init(context)
        application = DictionaryApplication.INSTANCE
    }


}