package com.possebom.checkgo.model;

import android.content.Context;

import com.possebom.checkgo.R;

import java.util.Date;
import java.util.List;

/**
 * Created by alexandre on 05/02/15.
 */
public class Card {
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

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getNextChargeFormatted(final Context context){
        if(nextCharge == null){
            return context.getResources().getString(R.string.not_avaliable);
        }
        return nextCharge.toString();
    }

    public Date getNextCharge() {
        return nextCharge;
    }

    public void setNextCharge(Date nextCharge) {
        this.nextCharge = nextCharge;
    }

    public Date getLastCharge() {
        return lastCharge;
    }

    public void setLastCharge(Date lastCharge) {
        this.lastCharge = lastCharge;
    }

    public float getNextChargeValor() {
        return nextChargeValor;
    }

    public void setNextChargeValor(float nextChargeValor) {
        this.nextChargeValor = nextChargeValor;
    }

    public float getLastChargeValor() {
        return lastChargeValor;
    }

    public void setLastChargeValor(float lastChargeValor) {
        this.lastChargeValor = lastChargeValor;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }
}
