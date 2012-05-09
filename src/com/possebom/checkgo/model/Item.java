package com.possebom.checkgo.model;

import java.util.Date;

public class Item {
	private int id;
	private long itemCard;
	private float valor;
	private String place;
	private Date dia;
	private boolean charge = false;
	public float getValor() {
		return valor;
	}
	public void setValor(float valor) {
		this.valor = valor;
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public Date getDia() {
		return dia;
	}
	public void setDia(Date dia) {
		this.dia = dia;
	}
	public boolean isCharge() {
		return charge;
	}
	public void setCharge(boolean charge) {
		this.charge = charge;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getItemCard() {
		return itemCard;
	}
	public void setItemCard(long itemCard) {
		this.itemCard = itemCard;
	}
}
