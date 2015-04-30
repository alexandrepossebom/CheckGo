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
import com.possebom.checkgo.service.UpgradeService;
import com.possebom.checkgo.widget.CheckGoWidget;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by alexandre on 05/02/15.
 */
public final class UpdateCards {

    public interface UpdateInterface {
        void updateSuccess();

        void updateError();
    }

    private static class AsyncUpdateCards extends AsyncTask<Void, Void, Boolean> {
        private final UpdateInterface updateInterface;
        private final Context context;
        private boolean isCharged = false;

        public AsyncUpdateCards(final Context context, final UpdateInterface updateInterface) {
            this.updateInterface = updateInterface;
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            boolean ok = true;
            final OkHttpClient client = new OkHttpClient();
            for (final Card card : CGController.INSTANCE.getCards()) {
                final float total = card.getTotal();
                final String url = String.format(Locale.getDefault(), "http://m.alelo.com.br/android/ajax-se.do?cartao=%s&tipo=3", card.getNumber());
                final Request request = new Request.Builder().url(url).build();
                try {
                    final Response response = client.newCall(request).execute();
                    ok = response != null && updateCard(card, response.body().string());
                    if(ok && card.getTotal() > total){
                        isCharged = true;
                    }
                } catch (final IOException e) {
                    Log.e("Error updating: "+ e.getMessage());
                    ok = false;
                    e.printStackTrace();
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


                if(isCharged) {
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
    }

    public static boolean isUpdated() {
        if (CollectionUtils.isEmpty(CGController.INSTANCE.getCards())) {
            Log.e("getCards is empty");
            return true;
        }

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault());
        final Calendar now = Calendar.getInstance();
        final Calendar lastRun = Calendar.getInstance();

        final Card card = CGController.INSTANCE.getCards().get(0);
        if (card != null) {
            final Date dateLastUpdate = card.getLastUpdate();
            lastRun.setTimeInMillis(dateLastUpdate == null ? 0 : dateLastUpdate.getTime());
        }

        Log.i("Last Run : " + simpleDateFormat.format(lastRun.getTime()));

        return !(now.get(Calendar.DAY_OF_YEAR) != lastRun.get(Calendar.DAY_OF_YEAR) || (lastRun.get(Calendar.HOUR_OF_DAY) < 12 && now.get(Calendar.HOUR_OF_DAY) > 12));
    }

    public static void start(final Context context) {
        start(context, null);
    }

    public static void start(final Context context, final UpdateInterface updateInterface) {
        new AsyncUpdateCards(context, updateInterface).execute();
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

            if(jo.has("Extrato")) {
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

                    final String place = jsonObjectEntry.getString("estab");

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

            Log.e("Json is : "+ json);
            return false;
        }
        return true;
    }
}
