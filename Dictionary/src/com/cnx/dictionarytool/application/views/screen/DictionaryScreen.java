package com.cnx.dictionarytool.application.views.screen;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.cnx.dictionarytool.R;
import com.cnx.dictionarytool.application.utils.CopyAssets;
import com.cnx.dictionarytool.di.components.DaggerNetworkComponent;
import com.cnx.dictionarytool.di.components.NetworkComponent;
import com.cnx.dictionarytool.di.modulles.ContextModule;
import com.cnx.dictionarytool.di.modulles.NetworkModule;
import com.cnx.dictionarytool.di.modulles.OkHttpClientModule;
import com.cnx.dictionarytool.interfaces.RandomUsersApi;
import com.cnx.dictionarytool.library.others.DictionaryApplication;
import com.cnx.dictionarytool.library.collections.NonLinkClickableSpan;
import com.cnx.dictionarytool.library.collections.StringUtil;
import com.cnx.dictionarytool.library.engine.Dictionary;
import com.cnx.dictionarytool.library.engine.HtmlEntry;
import com.cnx.dictionarytool.library.engine.Index;
import com.cnx.dictionarytool.library.engine.PairEntry;
import com.cnx.dictionarytool.library.engine.RowBase;
import com.cnx.dictionarytool.library.engine.TokenRow;
import com.cnx.dictionarytool.library.engine.TransliteratorManager;
import com.cnx.dictionarytool.workers.DictionaryWorker;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.cnx.dictionarytool.application.utils.Constants.DICTIONARY_FILE_URL;


public class DictionaryScreen extends FrameLayout {

    private String CURRENT_SCREEN =  DictionaryScreen.this.getClass().getSimpleName();
    private NetworkComponent networkComponent;

    private DictionaryApplication application;
    private String dictionaryFile = "";

    private FileChannel dictRaf = null;
    private File dictFile = null;
    private Dictionary dictionary = null;
    private Index index = null;
    private int fontSizeSp = 14;

    private final Handler uiHandler = new Handler();

    private int indexIndex = 0;
    private Context context;
    private EditText searchId;
    private View listContainerId;
    private View listNoDataContainerId;
    private View listWebViewContainerId;
    private LinearLayout rootId;
    private ListView listView;
    private WebView webView;
    private TextView searchedNameId;
    private ImageView speakerIconId;

    private TextToSpeech textToSpeech;

    String displayText = "";
    private static final Pattern CHAR_DASH = Pattern.compile("['\\p{L}\\p{M}\\p{N}]+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private List<RowBase> rowsToShow = null;

    private SearchOperation currentSearchOperation = null;

    private final ExecutorService searchExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "searchExecutor");
        }
    });

    private DictionaryApplication.Theme theme = DictionaryApplication.Theme.LIGHT;

    private ImageView getSpeakerImageId() { if (speakerIconId == null) { speakerIconId = findViewById(R.id.speakerIconId); } return speakerIconId; }
    private TextView getSearchedTextView() { if (searchedNameId == null) { searchedNameId = findViewById(R.id.searchedNameId); } return searchedNameId; }
    private WebView getWebView() { if (webView == null) { webView = findViewById(R.id.webView); } return webView; }
    private LinearLayout getRootId() { if (rootId == null) { rootId = findViewById(R.id.rootId); } return rootId; }
    private ListView getListView() { if (listView == null) { listView = findViewById(android.R.id.list); } return listView; }
    private EditText getSearchView() { if (searchId == null) { searchId = findViewById(R.id.searchId); } return searchId; }
    private View getListContainer() {  if (listContainerId == null) { listContainerId = findViewById(R.id.listContainerId);} return listContainerId; }
    private View getEmptyContainer() { if (listNoDataContainerId == null) { listNoDataContainerId = findViewById(R.id.listNoDataContainerId); } return listNoDataContainerId; }
    private View getWebViewContainer() {  if (listWebViewContainerId == null) { listWebViewContainerId = findViewById(R.id.listWebViewContainerId);} return listWebViewContainerId; }


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

    /******************************* Constructors **************************************************/
    public DictionaryScreen(@NonNull Context context) {
        super(context);
        initScreen(context);
    }

    public DictionaryScreen(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initScreen(context);
    }

    public DictionaryScreen(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initScreen(context);
    }
    /******************************* Constructors **************************************************/


    /******************************* Init functions  ***********************************************/
    /** INITIALIZE  Entire Screen **/
    private void initScreen(Context context) {
        this.context = context;
        DictionaryApplication.INSTANCE.init(context);
        application = DictionaryApplication.INSTANCE;
        context.setTheme(application.getSelectedTheme().themeId);
        inflate(context, R.layout.screen_dictionary, this);
        findViewsInScreen();
        setListener();
        setDictionaryFile(context);
        initListView();
        setInitialListState();

        final WorkManager mWorkManager = WorkManager.getInstance();
        final OneTimeWorkRequest mRequest = new OneTimeWorkRequest.Builder(DictionaryWorker.class).build();
        mWorkManager.enqueue(mRequest);

    }

    /** INITIALIZE  List View **/
    private void initListView() {
        Timber.d(CURRENT_SCREEN, "Loading index " + indexIndex);
        index = dictionary.indices.get(indexIndex);
        getListView().setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        getListView().setEmptyView(findViewById(android.R.id.empty));
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int row, long id) { onListItemClick(getListView(), view, row, id);    }
        });
        setListAdapter(new IndexAdapter(index));
    }

    /** INITIALIZE Web View  **/
    private void initWebView(String mhtml, String displayText) {
        String formattedWebView =  mhtml.replaceAll("<h1>.*</h1>", "");

        getSearchedTextView().setText(displayText);
        getSearchedTextView().setText(displayText.substring(0, 1).toUpperCase() + displayText.substring(1).toLowerCase());

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String fontSize = prefs.getString(context.getString(R.string.fontSizeKey), "14");
        int fontSizeSp;
        try {
            fontSizeSp = Integer.parseInt(fontSize.trim());
        } catch (NumberFormatException e) {
            fontSizeSp = 14;
        }
        getWebView().getSettings().setDefaultFontSize(fontSizeSp);
        try {
            // No way to get pure UTF-8 data into WebView
            formattedWebView = Base64.encodeToString(formattedWebView.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Missing UTF-8 support?!", e);
        }
        // Use loadURL to allow specifying a charset
        getWebView().loadUrl("data:text/html;charset=utf-8;base64," + formattedWebView);


        getEmptyContainer().setVisibility(View.GONE);
        getListContainer().setVisibility(View.GONE);
        getWebViewContainer().setVisibility(View.VISIBLE);
        getRootId().requestLayout();
    }
    /******************************* Init functions  ***********************************************/

    /** Finding  all the views in screen **/
    private void findViewsInScreen() {
        getListView();
        getSearchView();
        getEmptyContainer();
        getListContainer();
        getWebView();
        getWebViewContainer();
        getSearchedTextView();
    }

    /** LISTENERS: Set all the listeners for the dictionary **/
    private void setListener() {
        getSearchView().addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) { onSearchTextChange(s.toString().trim());}

            @Override
            public void beforeTextChanged(CharSequence s, int start,int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start,int before, int count) { }
        });


        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });

        textToSpeech= new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        getSpeakerImageId().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak(displayText, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    /** DICTIONARY: Get the  dictionary from the storage to current class **/
    private void setDictionaryFile(Context context) {
        dictionaryFile = new CopyAssets(context).getDictionaryFileUri();
        if (dictRaf == null){
            dictFile = new File(dictionaryFile);
        }
        try {
            if (dictRaf == null) {
                dictRaf = new RandomAccessFile(dictFile, "r").getChannel();
            }
            dictionary = new Dictionary(dictRaf);
        } catch (Exception e) {
            Log.e("ERROR",""+e.getMessage());
        }
        indexIndex = 0;
        for (int i = 0; i < dictionary.indices.size(); ++i) {
            if (dictionary.indices.get(i).shortName.equals("EN")) {
                indexIndex = i;
                break;
            }
        }
    }

    /** ON TEXT CHANGE : Handle the  visibility of containers based on visibility **/
    private void onSearchTextChange(final String text) {

        if(text.length()>3||text.length()==3){
            //Set the container states
            getEmptyContainer().setVisibility(View.GONE);
            getListContainer().setVisibility(View.VISIBLE);
            //Perform search
            currentSearchOperation = new SearchOperation(text, index);
            searchExecutor.execute(currentSearchOperation);
        }else{
            //Set the container states
            getEmptyContainer().setVisibility(View.VISIBLE);
            getListContainer().setVisibility(View.GONE);
        }

    }

    /** RESET FILTERED LIST : Change the result of list base on data changed in edit view  **/
    private void setFiltered(final SearchOperation searchOperation) {
        rowsToShow = searchOperation.multiWordSearchResult;
        setListAdapter(new IndexAdapter(index, rowsToShow, searchOperation.searchTokens));
    }

    /** SET LIST POSITION OF RESULT : Display the position of the result in the list   **/
    private void jumpToRow(final int row) {
        getListView().setSelectionFromTop(row, 0);
        getListView().setSelected(true);
    }

    /** SET THE LIST ADAPTER : Set the adapter of the list **/
    private void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
        getRootId().requestLayout();
    }

    /** Set the initial state of the list result, after getting the data from the database **/
    private void setInitialListState() {
        TransliteratorManager.init(new TransliteratorManager.Callback() {
            @Override
            public void onTransliteratorReady() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onSearchTextChange("");
                    }
                });
            }
        }, DictionaryApplication.threadBackground);
    }

    /** She the Webview **/
    private void showHtml(final List<HtmlEntry> htmlEntries, final String htmlTextToHighlight) {
        String html = HtmlEntry.htmlBody(htmlEntries, index.shortName);
        String mData = String.format("<html><head><meta name=\"viewport\" content=\"width=device-width\"></head><body>%s</body></html>", html);
        displayText = html;
        displayText = StringUtils.substringBefore(html.substring(22), "\"");
        String formattedData = displayText.replaceAll(Pattern.quote("+"), " ");
        displayText = formattedData;
        initWebView(mData,formattedData);
    }

    private void onListItemClick(ListView l, View v, int rowIdx, long id) { getListView().requestFocus(); }

    private void createTokenLinkSpans(final TextView textView, final Spannable spannable, final String text) {
        // Saw from the source code that LinkMovementMethod sets the selection!
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        final Matcher matcher = CHAR_DASH.matcher(text);
        while (matcher.find()) {
            spannable.setSpan(new NonLinkClickableSpan(), matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    private boolean isFiltered() {  return rowsToShow != null; }

    private void searchFinished(final SearchOperation searchOperation) {
        if (searchOperation.interrupted.get()) {
            Timber.d(CURRENT_SCREEN, "Search operation was interrupted: " + searchOperation);
            return;
        }
        if (searchOperation != this.currentSearchOperation) {
            Timber.d(CURRENT_SCREEN, "Stale searchOperation finished: " + searchOperation);
            return;
        }

        final Index.IndexEntry searchResult = searchOperation.searchResult;
        Timber.d(CURRENT_SCREEN, "searchFinished: " + searchOperation + ", searchResult=" + searchResult);

        currentSearchOperation = null;
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentSearchOperation == null) {
                    if (searchResult != null) {
                        if (isFiltered()) {
                            clearFiltered();
                        }
                        jumpToRow(searchResult.startRow);
                    } else if (searchOperation.multiWordSearchResult != null) {
                        // Multi-row search....
                        setFiltered(searchOperation);
                    } else {
                        throw new IllegalStateException("This should never happen.");
                    }
                } else {
                    Timber.d(CURRENT_SCREEN, "More coming, waiting for currentSearchOperation.");
                }
            }
        }, 20);
    }

    private void clearFiltered() {
        setListAdapter(new IndexAdapter(index));
        rowsToShow = null;
    }

    /** ******************************************** CLASS IMPLEMENTATIONS ******************************************** **/
    /** Adapter class:: This class is used to display the row elements in dictionary list **/
    final class IndexAdapter extends BaseAdapter {

        final Index index;
        final List<RowBase> rows;
        final Set<String> toHighlight;

        IndexAdapter(final Index index) {
            this.index = index;
            rows = index.rows;
            this.toHighlight = null;
        }

        IndexAdapter(final Index index, final List<RowBase> rows, final List<String> toHighlight) {
            this.index = index;
            this.rows = rows;
            this.toHighlight = new LinkedHashSet<>(toHighlight);
        }

        @Override
        public int getCount() {
            return rows.size();
        }

        @Override
        public RowBase getItem(int position) {
            return rows.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).index();
        }

        @Override
        public int getViewTypeCount() {
            return 5;
        }

        @Override
        public int getItemViewType(int position) {
            final RowBase row = getItem(position);
            if (row instanceof PairEntry.Row) {
                final PairEntry entry = ((PairEntry.Row)row).getEntry();
                final int rowCount = entry.pairs.size();
                return rowCount > 1 ? 1 : 0;
            } else if (row instanceof TokenRow) {
                final Index.IndexEntry indexEntry = ((TokenRow)row).getIndexEntry();
                return indexEntry.htmlEntries.isEmpty() ? 2 : 3;
            } else if (row instanceof HtmlEntry.Row) {
                return 4;
            } else {
                throw new IllegalArgumentException("Unsupported Row type: " + row.getClass());
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final RowBase row = getItem(position);
            if (row instanceof PairEntry.Row) {
                return getView(position, (PairEntry.Row) row, parent, (TableLayout)convertView);
            } else if (row instanceof TokenRow) {
                return getView((TokenRow) row, parent, (TextView)convertView);
            } else if (row instanceof HtmlEntry.Row) {
                return getView((HtmlEntry.Row) row, parent, (TextView)convertView);
            } else {
                throw new IllegalArgumentException("Unsupported Row type: " + row.getClass());
            }
        }

        private void addBoldSpans(String token, String col1Text, Spannable col1Spannable) {
            int startPos = 0;
            while ((startPos = col1Text.indexOf(token, startPos)) != -1) {
                col1Spannable.setSpan(new StyleSpan(Typeface.BOLD), startPos, startPos
                        + token.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                startPos += token.length();
            }
        }

        private TableLayout getView(final int position, PairEntry.Row row, ViewGroup parent,
                                    TableLayout result) {
            final Context context = parent.getContext();

            Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/Poppins-Regular.ttf");


            final PairEntry entry = row.getEntry();
            final int rowCount = entry.pairs.size();


            if (result == null) {
                result = new TableLayout(context);
                result.setStretchAllColumns(true);
                // Because we have a Button inside a ListView row:
                // http://groups.google.com/group/android-developers/browse_thread/thread/3d96af1530a7d62a?pli=1
                result.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                result.setClickable(true);
                result.setFocusable(false);
                result.setLongClickable(true);
//                result.setBackgroundResource(android.R.drawable.menuitem_background);

                result.setBackgroundResource(theme.normalRowBg);
            } else if (result.getChildCount() > rowCount) {
                result.removeViews(rowCount, result.getChildCount() - rowCount);
            }

            for (int r = result.getChildCount(); r < rowCount; ++r) {
                final TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);

                final TableRow tableRow = new TableRow(result.getContext());

                final TextView col1 = new TextView(tableRow.getContext());
                final TextView col2 = new TextView(tableRow.getContext());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    col1.setTextIsSelectable(true);
                    col2.setTextIsSelectable(true);
                }
                col1.setTextColor(Color.BLACK);
                col2.setTextColor(Color.BLACK);

                col1.setWidth(1);
                col2.setWidth(1);

                col1.setTypeface(type);
                col2.setTypeface(type);
                col1.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);
                col2.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);
                // col2.setBackgroundResource(theme.otherLangBg);


                // Set the columns in the table.
                if (r == 0) {
                    tableRow.addView(col1, layoutParams);
                    tableRow.addView(col2, layoutParams);
                } else {
                    for (int i = 0; i < 2; i++) {
                        final TextView bullet = new TextView(tableRow.getContext());
                        bullet.setText(" • ");
                        LinearLayout wrapped = new LinearLayout(context);
                        wrapped.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
                        wrapped.addView(bullet, p1);
                        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        wrapped.addView(i == 0 ? col1 : col2, p2);
                        tableRow.addView(wrapped, layoutParams);
                    }
                }

                result.addView(tableRow);
            }

            for (int r = 0; r < rowCount; ++r) {
                final TableRow tableRow = (TableRow)result.getChildAt(r);
                View left = tableRow.getChildAt(0);
                View right = tableRow.getChildAt(1);
                if (r > 0) {
                    left = ((ViewGroup)left).getChildAt(1);
                    right = ((ViewGroup)right).getChildAt(1);
                }
                final TextView col1 = (TextView)left;
                final TextView col2 = (TextView)right;

                // Set what's in the columns.
                final PairEntry.Pair pair = entry.pairs.get(r);
                final String col1Text = index.swapPairEntries ? pair.lang2 : pair.lang1;
                final String col2Text = index.swapPairEntries ? pair.lang1 : pair.lang2;
                final Spannable col1Spannable = new SpannableString(col1Text);
                final Spannable col2Spannable = new SpannableString(col2Text);

                // Bold the token instances in col1.
                if (toHighlight != null) {
                    for (final String token : toHighlight) {
                        addBoldSpans(token, col1Text, col1Spannable);
                    }
                } else
                    addBoldSpans(row.getTokenRow(true).getToken(), col1Text, col1Spannable);

                createTokenLinkSpans(col1, col1Spannable, col1Text);
                createTokenLinkSpans(col2, col2Spannable, col2Text);

                col1.setText(col1Spannable);
                col2.setText(col2Spannable);
            }

            result.setOnClickListener(new TextView.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Timber.d("CLICK","<------------------- CLICK ------------------->");
                    //DictionaryActivity.this.onListItemClick(getListView(), v, position, position);
                }
            });

            return result;
        }

        private TextView getPossibleLinkToHtmlEntryView(final boolean isTokenRow,
                                                        final String text, final boolean hasMainEntry, final List<HtmlEntry> htmlEntries,
                                                        final String htmlTextToHighlight, ViewGroup parent, TextView textView) {
            final Context context = parent.getContext();

            Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/Poppins-Regular.ttf");

            if (textView == null) {
                textView = new TextView(context);
                textView.setLongClickable(true);
                textView.setTypeface(type);
                if (isTokenRow) {
                    textView.setTextAppearance(context, theme.tokenRowFg);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 4 * fontSizeSp / 3);
                } else {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);
                }
                textView.setTextColor(Color.BLACK);
                if (!htmlEntries.isEmpty()) {
                    textView.setClickable(true);
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }

            textView.setBackgroundResource(hasMainEntry ? theme.tokenRowMainBg : theme.tokenRowOtherBg);

            // Make it so we can long-click on these token rows, too:
            final Spannable textSpannable = new SpannableString(text);
            createTokenLinkSpans(textView, textSpannable, text);

            if (!htmlEntries.isEmpty()) {
                final ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                    }
                };
                textSpannable.setSpan(clickableSpan, 0, text.length(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                textView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showHtml(htmlEntries, htmlTextToHighlight);
                    }
                });
            }
            textView.setText(textSpannable);
            return textView;
        }

        private TextView getView(TokenRow row, ViewGroup parent, final TextView result) {
            final Index.IndexEntry indexEntry = row.getIndexEntry();
            return getPossibleLinkToHtmlEntryView(true, indexEntry.token, row.hasMainEntry,
                    indexEntry.htmlEntries, null, parent, result);
        }

        private TextView getView(HtmlEntry.Row row, ViewGroup parent, final TextView result) {
            final HtmlEntry htmlEntry = row.getEntry();
            final TokenRow tokenRow = row.getTokenRow(true);
            return getPossibleLinkToHtmlEntryView(false,
                    context.getString(R.string.seeAlso, htmlEntry.title, htmlEntry.entrySource.getName()),
                    false, Collections.singletonList(htmlEntry), tokenRow.getToken(), parent,
                    result);
        }

    }

    /** Search Operation::  This class runs a separate thread to get the data from the db file  **/
    final class SearchOperation implements Runnable {

        private final AtomicBoolean interrupted = new AtomicBoolean(false);
        private final String searchText;
        private List<String> searchTokens;
        private final Index index;
        private Index.IndexEntry searchResult;
        private List<RowBase> multiWordSearchResult;

        SearchOperation(final String searchText, final Index index) {
            this.searchText = StringUtil.normalizeWhitespace(searchText);
            this.index = index;
        }

        public String toString() { return String.format("SearchOperation(%s,%s)", searchText, interrupted.toString());  }

        @Override
        public void run() {
            try {
                long searchStartMillis = System.currentTimeMillis();
                final String[] searchTokenArray = WHITESPACE.split(searchText);
                if (searchTokenArray.length == 1) {
                    searchResult = index.findInsertionPoint(searchText, interrupted);
                } else {
                    searchTokens = Arrays.asList(searchTokenArray);
                    multiWordSearchResult = index.multiWordSearch(searchText, searchTokens, interrupted);
                }
                Timber.d(CURRENT_SCREEN,"searchText=" + searchText + ", searchDuration="+ (System.currentTimeMillis() - searchStartMillis)+ ", interrupted=" + interrupted.get());
                if (!interrupted.get()) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            searchFinished(SearchOperation.this);
                        }
                    });
                } else {
                    Timber.d(CURRENT_SCREEN, "interrupted, skipping searchFinished.");
                }
            } catch (Exception e) {
                Log.e(CURRENT_SCREEN, "Failure during search (can happen during Activity close): " + e.getMessage());
            } finally {
                synchronized (this) {
                    this.notifyAll();
                }
            }
        }
    }
    /** ******************************************** CLASS IMPLEMENTATIONS ******************************************** **/

}
