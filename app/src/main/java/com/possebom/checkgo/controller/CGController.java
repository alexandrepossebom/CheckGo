package com.possebom.checkgo.controller;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.snowdream.android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.possebom.checkgo.model.Card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alexandre on 05/02/15.
 */
public enum CGController {
    INSTANCE;

    private static final String PREFS_NAME = "CGPrefs";
    private static final String PREFS_KEY = "Cards";
    private List<Card> cards = new ArrayList<>();

    public List<Card> getCards() {
        return cards;
    }

    public void save(final Context context) {
        final Gson gson = new Gson();
        final String serializedData = gson.toJson(cards);
        final SharedPreferences preferencesReader = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferencesReader.edit();
        editor.putString(PREFS_KEY, serializedData);
        editor.apply();
    }

    public boolean restore(final Context context) {
        final SharedPreferences preferencesReader = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return restore(preferencesReader.getString(PREFS_KEY, null));
    }

    private boolean restore(final String serializedData) {
        final Gson gson = new Gson();
        try {
            final Card[] cardArray = gson.fromJson(serializedData, Card[].class);
            if (cardArray != null) {
                cards = new ArrayList<>();
                cards.addAll(Arrays.asList(cardArray));
            }
        } catch (final JsonSyntaxException exception) {
            Log.e("Error restoring : " + exception.getMessage() + " json: " + serializedData);
        }

        if (cards == null) {
            cards = new ArrayList<>();
            return false;
        }
        return true;
    }

    public Card getCard(long cardNumber) {
        for (final Card card : getCards()) {
            if (card.getNumber() == cardNumber) return card;
        }
        return null;
    }
}
