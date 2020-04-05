package com.cnx.dictionarytool.application.views.screen

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cnx.dictionarytool.R
import com.cnx.dictionarytool.application.utils.CopyAssets
import com.cnx.dictionarytool.application.views.models.DictonaryData
import com.cnx.dictionarytool.application.views.adapters.AdptRecommendation
import com.cnx.dictionarytool.library.activities.DictionaryApplication
import com.cnx.dictionarytool.library.util.engine.Dictionary
import com.cnx.dictionarytool.library.util.engine.Index
import kotlinx.android.synthetic.main.fragment_base_dictionary.view.*
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel


class DictionaryBaseView : FrameLayout {

    private val currentScreen : String = DictionaryBaseView::class.simpleName!!

    private var dictionaryFile = ""
    private var index: Index? = null

    private var application: DictionaryApplication? = null
    private var dictionary: Dictionary? = null
    private var dictFile: File? = null
    private var dictRaf: FileChannel? = null
    private var dictFileTitleName: String? = null

    private val indexIndex = 0

    private var mAdapter: AdptRecommendation? = null
    private val movieList: ArrayList<DictonaryData> = ArrayList()

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
        initDictionaryApplication(context)
        setDictionaryFile()
        initListView(context)
    }

    /** Initialize the list view **/
    private fun initListView(context: Context) {
        mAdapter =
            AdptRecommendation(
                movieList
            )
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        recycler_view.layoutManager = mLayoutManager
        recycler_view.itemAnimator = DefaultItemAnimator()
        recycler_view.adapter = mAdapter
        prepareMovieData()
    }

    /** Initialize dictionary application **/
    private fun initDictionaryApplication(context: Context) {
        DictionaryApplication.INSTANCE.init(context)
        application = DictionaryApplication.INSTANCE
    }

    /** Set the dictionary for current screen **/
    private fun setDictionaryFile() {
        dictionaryFile = CopyAssets(context).getDictionaryFileUri()
        if(dictionaryFile.isNotEmpty()){

            if (dictRaf != null) {
                try {
                    dictRaf!!.close()
                } catch (e: IOException) {
                    Log.e(currentScreen, "Failed to close dictionary", e)
                }
                dictRaf = null
            }

            if (dictRaf == null) {
                dictFile = File(dictionaryFile)
            }

            try {
                if (dictRaf == null) {
                    dictFileTitleName = application!!.getDictionaryName(dictFile!!.name)
                    dictRaf = RandomAccessFile(dictFile, "r").channel
                }
                dictionary = Dictionary(dictRaf)
                Log.e(currentScreen, "$dictionary")
            } catch (e: Exception) {
                return
            }
        }else{
            Toast.makeText(context,"Dictionary file not available",Toast.LENGTH_LONG).show()
        }

    }

    private fun prepareMovieData() {
        var movie =
            DictonaryData(
                "Mad Max: Fury Road",
                "Action & Adventure",
                "2015"
            )
        movieList.add(movie)
        movie = DictonaryData(
            "Inside Out",
            "Animation, Kids & Family",
            "2015"
        )
        movieList.add(movie)
        movie = DictonaryData(
            "Star Wars: Episode VII - The Force Awakens",
            "Action",
            "2015"
        )
        movieList.add(movie)
        movie = DictonaryData(
            "Shaun the Sheep",
            "Animation",
            "2015"
        )
        movieList.add(movie)
        movie = DictonaryData(
            "The Martian",
            "Science Fiction & Fantasy",
            "2015"
        )
        movieList.add(movie)
        movie = DictonaryData(
            "Mission: Impossible Rogue Nation",
            "Action",
            "2015"
        )
        movieList.add(movie)
        movie = DictonaryData(
            "Up",
            "Animation",
            "2009"
        )
        movieList.add(movie)
        movie = DictonaryData(
            "Star Trek",
            "Science Fiction",
            "2009"
        )
        movieList.add(movie)
        movie = DictonaryData(
            "The LEGO Movie",
            "Animation",
            "2014"
        )
        movieList.add(movie)
        movie = DictonaryData(
            "Iron Man",
            "Action & Adventure",
            "2008"
        )
        movieList.add(movie)
        movie =
            DictonaryData(
                "Aliens",
                "Science Fiction",
                "1986"
            )
        movieList.add(movie)
        movie =
            DictonaryData(
                "Chicken Run",
                "Animation",
                "2000"
            )
        movieList.add(movie)
        movie = DictonaryData(
            "Back to the Future",
            "Science Fiction",
            "1985"
        )
        movieList.add(movie)
        movie = DictonaryData(
            "Raiders of the Lost Ark",
            "Action & Adventure",
            "1981"
        )
        movieList.add(movie)
        movie = DictonaryData(
            "Goldfinger",
            "Action & Adventure",
            "1965"
        )
        movieList.add(movie)
        movie = DictonaryData(
            "Guardians of the Galaxy",
            "Science Fiction & Fantasy",
            "2014"
        )
        movieList.add(movie)
        mAdapter!!.notifyDataSetChanged()
    }




}