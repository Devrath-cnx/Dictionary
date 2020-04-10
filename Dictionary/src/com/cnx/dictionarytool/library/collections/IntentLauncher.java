
package com.cnx.dictionarytool.library.collections;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class IntentLauncher implements OnClickListener {

    private final Context context;
    private final Intent intent;

    public IntentLauncher(final Context context, final Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    protected void onGo() {
    }

    private void go() {
        onGo();
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        go();
    }

}
