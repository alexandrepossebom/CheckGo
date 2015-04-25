package com.possebom.checkgo.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.github.snowdream.android.util.Log;
import com.possebom.checkgo.MainActivity;
import com.possebom.checkgo.R;
import com.possebom.checkgo.controller.CGController;
import com.possebom.checkgo.model.Card;
import com.possebom.checkgo.util.UpdateCards;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChechGoWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        Log.i("ChechGoWidget onUpdate");
        for (final int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (!UpdateCards.isUpdated()) {
                Log.d("Need Update !!!");
                UpdateCards.start(context);
            }
        }
    }

    @Override
    public void onDeleted(final Context context, final int[] appWidgetIds) {
        Log.i("ChechGoWidget onDeleted");
        for (final int appWidgetId : appWidgetIds) {
            ChechGoWidgetConfigureActivity.deleteCardPref(context, appWidgetId);
        }
    }

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);

        long cardNumber = ChechGoWidgetConfigureActivity.loadCardPref(context, appWidgetId);

        final Card card = CGController.INSTANCE.getCard(cardNumber);

        if (card != null) {
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.chech_go_widget);

            final String total = card.getTotal() == 0 ? " -- " : "R$ " + numberFormat.format(card.getTotal());

            views.setTextViewText(R.id.textViewCardTotal, total);
            views.setTextViewText(R.id.textViewCardName, String.format(Locale.getDefault(), "%s - %s", context.getString(R.string.app_name), card.getName()));

            if (card.getLastUpdate() != null) {
                views.setTextViewText(R.id.textViewCardLastUpdate, dateFormat.format(card.getLastUpdate()));
            }

            final Intent intent = new Intent(context, MainActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            views.setOnClickPendingIntent(R.id.layout_widget_small, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}