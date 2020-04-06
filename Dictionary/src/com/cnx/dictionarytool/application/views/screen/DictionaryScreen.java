package com.cnx.dictionarytool.application.views.screen;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cnx.dictionarytool.R;
import com.cnx.dictionarytool.application.utils.CopyAssets;
import com.cnx.dictionarytool.library.activities.DictionaryApplication;
import com.cnx.dictionarytool.library.activities.HtmlDisplayActivity;
import com.cnx.dictionarytool.library.util.collections.NonLinkClickableSpan;
import com.cnx.dictionarytool.library.util.collections.StringUtil;
import com.cnx.dictionarytool.library.util.engine.Dictionary;
import com.cnx.dictionarytool.library.util.engine.HtmlEntry;
import com.cnx.dictionarytool.library.util.engine.Index;
import com.cnx.dictionarytool.library.util.engine.PairEntry;
import com.cnx.dictionarytool.library.util.engine.RowBase;
import com.cnx.dictionarytool.library.util.engine.TokenRow;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DictionaryScreen extends FrameLayout {


    private DictionaryApplication application;
    private String dictionaryFile = "";
    private String dictFileTitleName = null;

    private FileChannel dictRaf = null;
    private File dictFile = null;
    private Dictionary dictionary = null;
    private Index index = null;
    private int fontSizeSp = 14;

    private boolean clickOpensContextMenu = false;
    private final Handler uiHandler = new Handler();

    private int indexIndex = 0;
    private Context context;
    private EditText searchId;

    private List<RowBase> rowsToShow = null; // if not null, just show these rows.

    private SearchOperation currentSearchOperation = null;

    private final ExecutorService searchExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "searchExecutor");
        }
    });

    private DictionaryApplication.Theme theme = DictionaryApplication.Theme.LIGHT;


    private ListView listView;
    private ListView getListView() {
        if (listView == null) {
            listView = findViewById(android.R.id.list);
        }
        return listView;
    }

    private EditText getSearchView() {
        if (searchId == null) {
            searchId = findViewById(R.id.searchId);
        }
        return searchId;
    }



    private void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
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



    private void initScreen(Context context) {
        this.context = context;
        DictionaryApplication.INSTANCE.init(context);
        application = DictionaryApplication.INSTANCE;
        context.setTheme(application.getSelectedTheme().themeId);
        inflate(context, R.layout.screen_dictionary, this);
        findViewsInScreen();
        setListener();
        setDictionaryFile(context);
    }

    private void setListener() {
        getSearchView().addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) { onSearchTextChange(s.toString().trim());}

            @Override
            public void beforeTextChanged(CharSequence s, int start,int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start,int before, int count) { }
        });
    }

    private void setDictionaryFile(Context context) {
        dictionaryFile = new CopyAssets(context).getDictionaryFileUri();
        if (dictRaf == null){
            dictFile = new File(dictionaryFile);
        }
        try {
            if (dictRaf == null) {
                dictFileTitleName = application.getDictionaryName(dictFile.getName());
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
        Log.d("LOG", "Loading index " + indexIndex);
        index = dictionary.indices.get(indexIndex);
        getListView().setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        getListView().setEmptyView(findViewById(android.R.id.empty));
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int row, long id) {
                onListItemClick(getListView(), view, row, id);
            }
        });
        setListAdapter(new IndexAdapter(index));
    }

    private void onListItemClick(ListView l, View v, int rowIdx, long id) {
        defocusSearchText();
    }

    private void defocusSearchText() {
        getListView().requestFocus();
    }

    private void showHtml(final List<HtmlEntry> htmlEntries, final String htmlTextToHighlight) {
        String html = HtmlEntry.htmlBody(htmlEntries, index.shortName);
        // Log.d(LOG, "html=" + html);
       /* startActivityForResult(
                HtmlDisplayActivity.getHtmlIntent(getApplicationContext(), String.format(
                        "<html><head><meta name=\"viewport\" content=\"width=device-width\"></head><body>%s</body></html>", html),
                        htmlTextToHighlight, false),
                0);*/
    }

    final class IndexAdapter extends BaseAdapter {

        private static final float PADDING_DEFAULT_DP = 8;

        private static final float PADDING_LARGE_DP = 16;

        final Index index;

        final List<RowBase> rows;

        final Set<String> toHighlight;

        private int mPaddingDefault;

        private int mPaddingLarge;

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
                layoutParams.leftMargin = mPaddingLarge;

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

                col1.setTypeface(Typeface.DEFAULT);
                col2.setTypeface(Typeface.DEFAULT);
                col1.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);
                col2.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);
                // col2.setBackgroundResource(theme.otherLangBg);

                if (index.swapPairEntries) {
                    col2.setOnLongClickListener(textViewLongClickListenerIndex0);
                    col1.setOnLongClickListener(textViewLongClickListenerIndex1);
                } else {
                    col1.setOnLongClickListener(textViewLongClickListenerIndex0);
                    col2.setOnLongClickListener(textViewLongClickListenerIndex1);
                }

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

                    Log.d("CLICK","<------------------- CLICK ------------------->");
                    //DictionaryActivity.this.onListItemClick(getListView(), v, position, position);
                }
            });

            return result;
        }

        private TextView getPossibleLinkToHtmlEntryView(final boolean isTokenRow,
                                                        final String text, final boolean hasMainEntry, final List<HtmlEntry> htmlEntries,
                                                        final String htmlTextToHighlight, ViewGroup parent, TextView textView) {
            final Context context = parent.getContext();
            if (textView == null) {
                textView = new TextView(context);
                // set up things invariant across one ItemViewType
                // ItemViewTypes handled here are:
                // 2: isTokenRow == true, htmlEntries.isEmpty() == true
                // 3: isTokenRow == true, htmlEntries.isEmpty() == false
                // 4: isTokenRow == false, htmlEntries.isEmpty() == false
                textView.setPadding(isTokenRow ? mPaddingDefault : mPaddingLarge, mPaddingDefault, mPaddingDefault, 0);
                textView.setOnLongClickListener(indexIndex > 0 ? textViewLongClickListenerIndex1 : textViewLongClickListenerIndex0);
                textView.setLongClickable(true);

                textView.setTypeface(Typeface.DEFAULT);
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

    private static final Pattern CHAR_DASH = Pattern.compile("['\\p{L}\\p{M}\\p{N}]+");

    private void createTokenLinkSpans(final TextView textView, final Spannable spannable,
                                      final String text) {
        // Saw from the source code that LinkMovementMethod sets the selection!
        // http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3.1_r1/android/text/method/LinkMovementMethod.java#LinkMovementMethod
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        final Matcher matcher = CHAR_DASH.matcher(text);
        while (matcher.find()) {
            spannable.setSpan(new NonLinkClickableSpan(), matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    private String selectedSpannableText = null;

    private int selectedSpannableIndex = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        selectedSpannableText = null;
        selectedSpannableIndex = -1;
        return super.onTouchEvent(event);
    }

    private class TextViewLongClickListener implements OnLongClickListener {
        final int index;

        private TextViewLongClickListener(final int index) {
            this.index = index;
        }

        @Override
        public boolean onLongClick(final View v) {
            final TextView textView = (TextView) v;
            final int start = textView.getSelectionStart();
            final int end = textView.getSelectionEnd();
            if (start >= 0 && end >= 0) {
                selectedSpannableText = textView.getText().subSequence(start, end).toString();
                selectedSpannableIndex = index;
            }
            return false;
        }
    }

    private final TextViewLongClickListener textViewLongClickListenerIndex0 = new TextViewLongClickListener(
            0);

    private final TextViewLongClickListener textViewLongClickListenerIndex1 = new TextViewLongClickListener(
            1);

    private void onSearchTextChange(final String text) {
        currentSearchOperation = new SearchOperation(text, index);
        searchExecutor.execute(currentSearchOperation);
    }

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private boolean isFiltered() {
        return rowsToShow != null;
    }

    private void searchFinished(final SearchOperation searchOperation) {
        if (searchOperation.interrupted.get()) {
            Log.d("LOG", "Search operation was interrupted: " + searchOperation);
            return;
        }
        if (searchOperation != this.currentSearchOperation) {
            Log.d("LOG", "Stale searchOperation finished: " + searchOperation);
            return;
        }

        final Index.IndexEntry searchResult = searchOperation.searchResult;
        Log.d("LOG", "searchFinished: " + searchOperation + ", searchResult=" + searchResult);

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
                    Log.d("LOG", "More coming, waiting for currentSearchOperation.");
                }
            }
        }, 20);
    }

    private void clearFiltered() {
        setListAdapter(new IndexAdapter(index));
        rowsToShow = null;
    }

    private void setFiltered(final SearchOperation searchOperation) {
        rowsToShow = searchOperation.multiWordSearchResult;
        setListAdapter(new IndexAdapter(index, rowsToShow, searchOperation.searchTokens));
    }

    private final void jumpToRow(final int row) {
        Log.d("LOG", "jumpToRow: " + row + ", refocusSearchText=" + false);
        // getListView().requestFocusFromTouch();
        getListView().setSelectionFromTop(row, 0);
        getListView().setSelected(true);
    }

    final class SearchOperation implements Runnable {

        final AtomicBoolean interrupted = new AtomicBoolean(false);

        final String searchText;

        List<String> searchTokens; // filled in for multiWord.

        final Index index;

        long searchStartMillis;

        Index.IndexEntry searchResult;

        List<RowBase> multiWordSearchResult;

        boolean done = false;

        SearchOperation(final String searchText, final Index index) {
            this.searchText = StringUtil.normalizeWhitespace(searchText);
            this.index = index;
        }

        public String toString() {
            return String.format("SearchOperation(%s,%s)", searchText, interrupted.toString());
        }

        @Override
        public void run() {
            try {
                searchStartMillis = System.currentTimeMillis();
                final String[] searchTokenArray = WHITESPACE.split(searchText);
                if (searchTokenArray.length == 1) {
                    searchResult = index.findInsertionPoint(searchText, interrupted);
                } else {
                    searchTokens = Arrays.asList(searchTokenArray);
                    multiWordSearchResult = index.multiWordSearch(searchText, searchTokens,
                            interrupted);
                }
                Log.d("LOG",
                        "searchText=" + searchText + ", searchDuration="
                                + (System.currentTimeMillis() - searchStartMillis)
                                + ", interrupted=" + interrupted.get());
                if (!interrupted.get()) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            searchFinished(SearchOperation.this);
                        }
                    });
                } else {
                    Log.d("LOG", "interrupted, skipping searchFinished.");
                }
            } catch (Exception e) {
                Log.e("LOG", "Failure during search (can happen during Activity close): " + e.getMessage());
            } finally {
                synchronized (this) {
                    done = true;
                    this.notifyAll();
                }
            }
        }
    }

    private void findViewsInScreen() {
        getListView();
        getSearchView();
    }


}
