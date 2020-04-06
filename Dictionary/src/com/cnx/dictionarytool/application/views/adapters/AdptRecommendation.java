package com.cnx.dictionarytool.application.views.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cnx.dictionarytool.R;
import com.cnx.dictionarytool.application.views.models.DictonaryData;
import com.cnx.dictionarytool.library.activities.DictionaryApplication;
import com.cnx.dictionarytool.library.activities.HtmlDisplayActivity;
import com.cnx.dictionarytool.library.util.collections.NonLinkClickableSpan;
import com.cnx.dictionarytool.library.util.engine.HtmlEntry;
import com.cnx.dictionarytool.library.util.engine.Index;
import com.cnx.dictionarytool.library.util.engine.PairEntry;
import com.cnx.dictionarytool.library.util.engine.RowBase;
import com.cnx.dictionarytool.library.util.engine.TokenRow;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdptRecommendation extends RecyclerView.Adapter<AdptRecommendation.MyViewHolder> {

    private List<RowBase> rows;
    private Context context;

    private DictionaryApplication.Theme theme = DictionaryApplication.Theme.LIGHT;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootId;

        MyViewHolder(View view) {
            super(view);
            rootId = view.findViewById(R.id.rootId);
        }
    }


    public AdptRecommendation(Index data) {
        this.rows = data.rows;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final RowBase row = getItem(position);
        LinearLayout  displayContainer = holder.rootId;
        // holder.rootId

        if (row instanceof PairEntry.Row) {
            displayContainer.addView(getView(position, (PairEntry.Row) row, context, new TableLayout(context)));
        } else if (row instanceof TokenRow) {
            displayContainer.addView(getView((TokenRow) row, context, new TextView(context)));
        } else if (row instanceof HtmlEntry.Row) {
            displayContainer.addView(getView((HtmlEntry.Row) row, context, new TextView(context)));
        } else {
            throw new IllegalArgumentException("Unsupported Row type: " + row.getClass());
        }

    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    public RowBase getItem(int position) { return rows.get(position); }

    @Override
    public long getItemId(int position) { return getItem(position).index(); }









    private TableLayout getView(final int position, PairEntry.Row row, Context context, TableLayout result) {
        final PairEntry entry = row.getEntry();
        final int rowCount = entry.pairs.size();

        for (int r = result.getChildCount(); r < rowCount; ++r) {
            final TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = 5;

            final TableRow tableRow = new TableRow(result.getContext());

            final TextView col1 = new TextView(tableRow.getContext());
            final TextView col2 = new TextView(tableRow.getContext());
            col1.setTextIsSelectable(true);
            col2.setTextIsSelectable(true);
            col1.setTextColor(Color.BLACK);
            col2.setTextColor(Color.BLACK);

            col1.setWidth(1);
            col2.setWidth(1);

            col1.setTypeface(Typeface.DEFAULT);
            col2.setTypeface(Typeface.DEFAULT);

            col1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            col2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
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

        return result;
    }

    private TextView getView(TokenRow row, Context parent, final TextView result) {
        final Index.IndexEntry indexEntry = row.getIndexEntry();
        return getPossibleLinkToHtmlEntryView(true, indexEntry.token, row.hasMainEntry,
                indexEntry.htmlEntries, null, parent, result);
    }

    private TextView getPossibleLinkToHtmlEntryView(final boolean isTokenRow,
                                                    final String text, final boolean hasMainEntry, final List<HtmlEntry> htmlEntries,
                                                    final String htmlTextToHighlight, Context context, TextView textView) {
        if (textView == null) {
            textView = new TextView(context);
            // set up things invariant across one ItemViewType
            // ItemViewTypes handled here are:
            // 2: isTokenRow == true, htmlEntries.isEmpty() == true
            // 3: isTokenRow == true, htmlEntries.isEmpty() == false
            // 4: isTokenRow == false, htmlEntries.isEmpty() == false
            textView.setPadding(isTokenRow ? 5 : 5, 5, 5, 0);
            //textView.setOnLongClickListener(indexIndex > 0 ? textViewLongClickListenerIndex1 : textViewLongClickListenerIndex0);
            textView.setLongClickable(true);

            textView.setTypeface(Typeface.DEFAULT);
            if (isTokenRow) {
                //textView.setTextAppearance(context, theme.tokenRowFg);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 4 * 14 / 3);
            } else {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            }
            //textView.setTextColor(textColorFg);
            if (!htmlEntries.isEmpty()) {
                textView.setClickable(true);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }

        //textView.setBackgroundResource(hasMainEntry ? theme.tokenRowMainBg : theme.tokenRowOtherBg);

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
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHtml(htmlEntries, htmlTextToHighlight);
                }
            });
        }
        textView.setText(textSpannable);
        return textView;
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

    private void showHtml(final List<HtmlEntry> htmlEntries, final String htmlTextToHighlight) {
        //String html = HtmlEntry.htmlBody(htmlEntries, index.shortName);
        String html = HtmlEntry.htmlBody(htmlEntries, "EN");
        /*startActivityForResult(
                HtmlDisplayActivity.getHtmlIntent(getApplicationContext(), String.format(
                        "<html><head><meta name=\"viewport\" content=\"width=device-width\"></head><body>%s</body></html>", html),
                        htmlTextToHighlight, false),
                0);*/
    }

    private TextView getView(HtmlEntry.Row row, Context context, final TextView result) {
        final HtmlEntry htmlEntry = row.getEntry();
        final TokenRow tokenRow = row.getTokenRow(true);
        assert htmlEntry.entrySource != null;
        return getPossibleLinkToHtmlEntryView(false,
                context.getString(R.string.seeAlso, htmlEntry.title, htmlEntry.entrySource.getName()),
                false, Collections.singletonList(htmlEntry), tokenRow.getToken(), context,
                result);
    }

}
