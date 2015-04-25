package com.possebom.checkgo.model;

import java.util.Date;

/**
 * Created by alexandre on 05/02/15.
 */
public class Entry {
    private float amount;
    private String place;
    private Date day;
    private boolean charge = false;

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public boolean isCharge() {
        return charge;
    }

    public void setCharge(boolean charge) {
        this.charge = charge;
    }

    @Override
    public String toString() {
        return amount + " " + place + " " + day + " " + charge;
    }
}
