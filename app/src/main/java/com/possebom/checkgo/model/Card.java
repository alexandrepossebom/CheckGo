package com.possebom.checkgo.model;

import android.content.Context;

import com.possebom.checkgo.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by alexandre on 05/02/15.
 */
public class Card {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM", Locale.getDefault());
    private static final SimpleDateFormat SDFH = new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault());
    private String name;
    private Date lastUpdate;
    private Date nextCharge;
    private Date lastCharge;
    private float nextChargeValor;
    private float lastChargeValor;
    private float total;
    private long number;
    private List<Entry> entries;

    public Card(String cardName, long cardnumber) {
        this.name = cardName;
        this.number = cardnumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public String getLastUpdateFormatted() {
        return lastUpdate == null ? " -- " : SDFH.format(lastUpdate);
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getNextChargeFormatted(final Context context) {
        if (nextCharge == null) {
            return context.getResources().getString(R.string.not_available);
        }
        return SDF.format(nextCharge) + " - " + NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format((double) nextChargeValor);
    }

    public void setNextCharge(Date nextCharge) {
        this.nextCharge = nextCharge;
    }

    public String getLastChargeFormatted() {
        return lastCharge == null ? " -- " : SDF.format(lastCharge) + " - " + NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format((double) lastChargeValor);
    }

    public void setLastCharge(Date lastCharge) {
        this.lastCharge = lastCharge;
    }

    public void setNextChargeValor(float nextChargeValor) {
        this.nextChargeValor = nextChargeValor;
    }

    public void setLastChargeValor(float lastChargeValor) {
        this.lastChargeValor = lastChargeValor;
    }

    public String getTotalFormatted() {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format((double) total);
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public long getNumber() {
        return number;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public float getTotal() {
        return total;
    }
}
