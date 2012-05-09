package com.possebom.checkgo.model;

import java.util.Date;
import java.util.List;

public class Card {
	private String name;
	private Date lastUpdate;
	private Date nextCharge;
	private Date lastCharge;
	private float nextChargeValor;
	private float lastChargeValor;
	private float total;
	private long number;
	private List<Item> itens;
	
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
	public List<Item> getItens() {
		return itens;
	}
	public void setItens(List<Item> itens) {
		this.itens = itens;
	}
}
