package com.possebom.checkgo.util;

import android.content.Context;

import com.github.snowdream.android.util.Log;
import com.possebom.checkgo.controller.CGController;
import com.possebom.checkgo.model.Card;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by alexandre on 05/02/15.
 */
public final class UpdateCards {

    public static void start(Context context, UpdateInterface updateInterface) {

        final AsyncUpdateCard asyncUpdateCard = new AsyncUpdateCard(context, updateInterface);

        final List<Card> cards = CGController.INSTANCE.getCards();

        asyncUpdateCard.execute(cards.toArray(new Card[cards.size()]));
    }

    public static void start(Context context) {
        start(context, null);
    }

    public interface UpdateInterface {
        void updateSuccess();

        void updateError();
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


}
