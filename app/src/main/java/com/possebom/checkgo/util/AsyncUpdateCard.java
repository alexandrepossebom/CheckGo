package com.possebom.checkgo.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.github.snowdream.android.util.Log;
import com.possebom.checkgo.MainActivity;
import com.possebom.checkgo.R;
import com.possebom.checkgo.controller.CGController;
import com.possebom.checkgo.model.Card;
import com.possebom.checkgo.model.Entry;
import com.possebom.checkgo.widget.CheckGoWidget;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by alexandre on 05/02/15.
 */
public class AsyncUpdateCard extends AsyncTask<Card, Void, Boolean> {
    private final UpdateCards.UpdateInterface updateInterface;
    private final Context context;
    private boolean isCharged = false;

    public AsyncUpdateCard(final Context context, final UpdateCards.UpdateInterface updateInterface) {
        this.updateInterface = updateInterface;
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Card... cards) {
        boolean ok = true;
        final OkHttpClient client = new OkHttpClient();
        for (final Card card : cards) {
            final float total = card.getTotal();
            final String url = String.format(Locale.getDefault(), "http://m.alelo.com.br/android/ajax-se.do?cartao=%s&tipo=3", card.getNumber());
            final Request request = new Request.Builder().url(url).build();
            try {
                final Response response = client.newCall(request).execute();
                final Date lastUpdate = card.getLastUpdate();
                boolean updateOk = response != null && updateCard(card, response.body().string());
                if (updateOk && card.getTotal() > total && lastUpdate != null) {
                    isCharged = true;
                } else if (!updateOk) {
                    ok = false;
                }
            } catch (final Exception e) {
                Log.e("Error updating: " + e.getMessage());
                ok = false;
            }
        }
        return ok;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(final Boolean result) {

        if (result) {
            CGController.INSTANCE.save(context);
            if (updateInterface != null) {
                updateInterface.updateSuccess();
            }

            final int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, CheckGoWidget.class));
            if (ids != null && ids.length > 0) {
                final Intent intent = new Intent(context, CheckGoWidget.class);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                context.sendBroadcast(intent);
            }


            if (isCharged) {
                final Intent intent = new Intent(context, MainActivity.class);
                final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                final Notification notification = new Notification.Builder(context)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(context.getString(R.string.card_charged))
                        .setSmallIcon(R.drawable.ic_coins)
                        .setContentIntent(pendingIntent)
                        .build();

                final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notificationManager.notify(22, notification);
            }
        } else {
            if (updateInterface != null) {
                updateInterface.updateError();
            }
        }

    }

    private static boolean updateCard(final Card card, final String json) {
        final List<Entry> list = new ArrayList<>();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        try {

            final JSONObject jo = new JSONObject(json);
            final JSONObject joSaldo = jo.getJSONObject("Saldo");

            final String valDisp = joSaldo.getString("valDisp");
            final String dtUltMov = joSaldo.getString("dtUltMov");
            final String valUltMov = joSaldo.getString("valUltMov");
            final String dtProxMov = joSaldo.getString("dtProxMov");
            final String valProxMov = joSaldo.getString("valProxMov");

            float total;
            try {
                total = Float.valueOf(valDisp);
            } catch (Exception ex) {
                total = 0;
            }

            card.setTotal(total);

            Date lastCharge;
            try {
                lastCharge = sdf.parse(dtUltMov);
            } catch (ParseException ex) {
                lastCharge = null;
            }
            card.setLastCharge(lastCharge);

            float lastChargeValor;
            try {
                lastChargeValor = Float.valueOf(valUltMov);
            } catch (Exception ex) {
                lastChargeValor = 0;
            }

            card.setLastChargeValor(lastChargeValor);

            Date nextCharge;
            try {
                nextCharge = sdf.parse(dtProxMov);
            } catch (ParseException ex) {
                nextCharge = null;
            }

            float nextChargeValor;
            try {
                nextChargeValor = Float.valueOf(valProxMov);
            } catch (Exception ex) {
                nextChargeValor = 0;
            }

            card.setNextCharge(nextCharge);
            card.setNextChargeValor(nextChargeValor);

            if (jo.has("Extrato")) {
                final JSONObject joExtrato = jo.getJSONObject("Extrato");
                final JSONArray ja = joExtrato.getJSONArray("lancamentos");

                for (int i = 0; i < ja.length(); ++i) {
                    final JSONObject jsonObjectEntry = ja.getJSONObject(i);

                    Date day;
                    try {
                        day = sdf.parse(jsonObjectEntry.getString("dt"));
                    } catch (Exception ex) {
                        day = null;
                    }

                    float amount;
                    try {
                        amount = Float.valueOf(jsonObjectEntry.getString("val"));
                    } catch (Exception ex) {
                        amount = 0;
                    }

                    final String place = jsonObjectEntry.getString("estab")
                            .replaceAll("&amp;", "&");

                    final Entry entry = new Entry();
                    entry.setDay(day);
                    entry.setAmount(amount);

                    if (jsonObjectEntry.getInt("tipoMov") == 3) {
                        entry.setPlace("** Recarga **");
                        entry.setCharge(true);
                    } else {
                        entry.setPlace(place);
                    }

                    list.add(entry);
                }
                card.setEntries(list);
            }
            card.setLastUpdate(new Date());
        } catch (JSONException e) {
            e.printStackTrace();

            Log.e("Json is : " + json);
            return false;
        }
        return true;
    }
}
