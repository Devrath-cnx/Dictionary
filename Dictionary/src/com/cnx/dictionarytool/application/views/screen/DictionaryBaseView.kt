package com.cnx.dictionarytool.application.views.screen

import android.content.Context
import android.os.Handler
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
import com.cnx.dictionarytool.application.views.adapters.AdptRecommendation
import com.cnx.dictionarytool.application.views.models.DictonaryData
import com.cnx.dictionarytool.library.activities.DictionaryApplication
import com.cnx.dictionarytool.library.util.collections.StringUtil
import com.cnx.dictionarytool.library.util.engine.Dictionary
import com.cnx.dictionarytool.library.util.engine.Index
import com.cnx.dictionarytool.library.util.engine.Index.IndexEntry
import com.cnx.dictionarytool.library.util.engine.RowBase
import com.cnx.dictionarytool.library.util.engine.TransliteratorManager
import kotlinx.android.synthetic.main.fragment_base_dictionary.view.*
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern
import kotlin.collections.ArrayList


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
    private val uiHandler = Handler()
    private var currentSearchOperation: SearchOperation? = null
    private var rowsToShow: List<RowBase>? = null // if not null, just show these rows.


    private val searchExecutor =  Executors.newSingleThreadExecutor { r -> Thread(r, "searchExecutor") }

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
        setListener(context)
        initDictionaryApplication(context)
        setDictionaryFile()
        initListView(context)
        setEmptyStateOfSearch()
    }

    private fun setListener(context: Context) {

    }

    private fun setEmptyStateOfSearch() {
        TransliteratorManager.init(TransliteratorManager.Callback {
            uiHandler.post(
                Runnable {
                    onSearchTextChange("Hello")
                })
        }, DictionaryApplication.threadBackground)
    }

    /** Initialize the list view **/
    private fun initListView(context: Context) {
        Log.d(currentScreen, "Loading index $indexIndex")
        index = dictionary!!.indices[indexIndex]
        mAdapter =AdptRecommendation(index)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        recycler_view.layoutManager = mLayoutManager
        recycler_view.itemAnimator = DefaultItemAnimator()
        //recycler_view.adapter = mAdapter
        setListAdapter(mAdapter!!)
    }

    private fun setListAdapter(mAdapter: AdptRecommendation) {
        recycler_view.adapter =mAdapter
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

    // --------------------------------------------------------------------------
    // SearchText
    // --------------------------------------------------------------------------
    private fun onSearchTextChange(text: String) {
        currentSearchOperation = SearchOperation(text, index!!)
        searchExecutor.execute(currentSearchOperation)
    }


    // --------------------------------------------------------------------------
    // SearchOperation
    // --------------------------------------------------------------------------
    private fun searchFinished(searchOperation: SearchOperation) {
        if (searchOperation.interrupted.get()) {
            Log.d(currentScreen,"Search operation was interrupted: $searchOperation")
            return
        }
        if (searchOperation != currentSearchOperation) {
            Log.d(currentScreen,"Stale searchOperation finished: $searchOperation")
            return
        }
        val searchResult = searchOperation.searchResult
        Log.d(currentScreen,"searchFinished: $searchOperation, searchResult=$searchResult")
        currentSearchOperation = null
        uiHandler.postDelayed({
            if (currentSearchOperation == null) {
                if (searchResult != null) {
                    if (isFiltered()) {
                        clearFiltered()
                    }
                    jumpToRow(searchResult.startRow)
                } else if (searchOperation.multiWordSearchResult != null) {
                    // Multi-row search....
                    setFiltered(searchOperation)
                } else {
                    throw IllegalStateException("This should never happen.")
                }
            } else {
                Log.d(currentScreen,"More coming, waiting for currentSearchOperation.")
            }
        }, 20)
    }

    private fun setFiltered(searchOperation: SearchOperation) {
        rowsToShow = searchOperation.multiWordSearchResult
        setListAdapter(AdptRecommendation(index, rowsToShow, searchOperation.searchTokens))
    }

    val WHITESPACE = Pattern.compile("\\s+")

    inner class SearchOperation(searchText: String?, index: Index) : Runnable {
        val interrupted =
            AtomicBoolean(false)
        val searchText: String = StringUtil.normalizeWhitespace(searchText)
        var searchTokens // filled in for multiWord.
                : List<String>? = null
        val index: Index
        var searchStartMillis: Long = 0
        var searchResult: IndexEntry? = null
        var multiWordSearchResult: List<RowBase>? = null
        var done = false
        override fun toString(): String {
            return String.format(
                "SearchOperation(%s,%s)",
                searchText,
                interrupted.toString()
            )
        }

        override fun run() {
            try {
                searchStartMillis = System.currentTimeMillis()
                val searchTokenArray = WHITESPACE.split(searchText)
                if (searchTokenArray.size == 1) {
                    searchResult = index.findInsertionPoint(searchText, interrupted)
                } else {
                    searchTokens = Arrays.asList(*searchTokenArray)
                    multiWordSearchResult = index.multiWordSearch(
                        searchText, searchTokens,
                        interrupted
                    )
                }
                Log.d(DictionaryBaseView::class.simpleName,
                    "searchText=" + searchText + ", searchDuration="
                            + (System.currentTimeMillis() - searchStartMillis)
                            + ", interrupted=" + interrupted.get()
                )
                if (!interrupted.get()) {
                    uiHandler.post(Runnable { searchFinished(this@SearchOperation) })
                } else {
                    Log.d(
                         DictionaryBaseView::class.simpleName,
                        "interrupted, skipping searchFinished."
                    )
                }
            } catch (e: java.lang.Exception) {
                Log.e(
                      DictionaryBaseView::class.simpleName,
                    "Failure during search (can happen during Activity close): " + e.message
                )
            } finally {
                synchronized(this) {
                    //done = true
                    //this.notifyAll()
                }
            }
        }

        init {
            this.index = index
        }
    }

    private fun clearFiltered() {
        setListAdapter(AdptRecommendation(index))
        rowsToShow = null
    }

    // --------------------------------------------------------------------------
    // Filtered results.
    // --------------------------------------------------------------------------
    private fun isFiltered(): Boolean {
        return rowsToShow != null
    }

    private fun jumpToRow(row: Int) {
        Log.d(currentScreen,"jumpToRow: $row, refocusSearchText=false")
        recycler_view.layoutManager!!.scrollToPosition(row)
    }
}