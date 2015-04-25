package com.possebom.checkgo.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.possebom.checkgo.R;
import com.possebom.checkgo.adapter.CardsAdapter;
import com.possebom.checkgo.interfaces.CardCallback;
import com.possebom.checkgo.model.Card;


/**
 * The configuration screen for the {@link ChechGoWidget ChechGoWidget} AppWidget.
 */
public class ChechGoWidgetConfigureActivity extends Activity implements CardCallback {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static final String PREFS_NAME = "com.possebom.checkgo.ChechGoWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";

    public ChechGoWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.chech_go_widget_configure);

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        final CardsAdapter mAdapter = new CardsAdapter(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);


        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();

        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    static void saveCardPref(Context context, int appWidgetId, long number) {
        final SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putLong(PREF_PREFIX_KEY + appWidgetId, number);
        prefs.apply();
    }

    static long loadCardPref(Context context, int appWidgetId) {
        final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getLong(PREF_PREFIX_KEY + appWidgetId, 0);
    }

    static void deleteCardPref(Context context, int appWidgetId) {
        final SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void touched(Card card) {
        final Context context = ChechGoWidgetConfigureActivity.this;

        saveCardPref(context, mAppWidgetId, card.getNumber());

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ChechGoWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

}



