package com.cnx.dictionarytool.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import com.cnx.dictionarytool.R

@SuppressLint("AppCompatCustomView")
class CustTxtVwPoppinsRegular (context: Context?, attrs: AttributeSet?) : TextView(context, attrs) {
    init {
        val typeface = Typeface.createFromAsset(getContext().assets, getContext().resources.getString(R.string.fontRegular))
        setTypeface(typeface)
    }
}