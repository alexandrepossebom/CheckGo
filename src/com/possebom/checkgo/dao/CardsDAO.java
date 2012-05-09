package com.possebom.checkgo.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.possebom.checkgo.model.Card;
import com.possebom.checkgo.model.Item;

public class CardsDAO {
	private SQLiteDatabase db;
	private SQLiteHelper dbHelper; 

	private static final String DATABASE = "cards";
	private static final String SCRIPT_DB_DELETE_CARD = "DROP TABLE IF EXISTS card";
	private static final String SCRIPT_DB_DELETE_ITEM = "DROP TABLE IF EXISTS item";
	private static final String SCRIPT_DB_CREATE_CARD =
			"create table card (number integer primary key, " +
					"name text not null, "+
					"lastcharge date," +
					"nextcharge date," +
					"saldo double default '0'," +
					"nextchargevalor double default '0'," +
					"lastchargevalor double default '0'," +
					"updatetime date" +
					");";
	private static final String SCRIPT_DB_CREATE_ITEM =
			"create table item (id integer primary key autoincrement," +
					"itemcard integer not null," +
					"valor double," +
					"place text," +
					"dia date," +
					"charge integer default '0' not null," +
					"FOREIGN KEY(itemcard) REFERENCES card(number));";

	public CardsDAO (Context ctx){
		dbHelper = new SQLiteHelper(ctx,DATABASE, 15,SCRIPT_DB_CREATE_CARD,SCRIPT_DB_CREATE_ITEM, SCRIPT_DB_DELETE_CARD,SCRIPT_DB_DELETE_ITEM);
	}

	public long insertCard(Card c){
		ContentValues cv = new ContentValues();
		cv.put("name", c.getName());
		cv.put("number", c.getNumber());
		db = dbHelper.getWritableDatabase();
		long id = -1;
		try{
			id = db.insertOrThrow("card", null, cv);
		}catch (Exception e) {
		}
		db.close();
		return id;
	}

	public int numCards(){
		int count = 0;
		db = dbHelper.getWritableDatabase();
		Cursor c = db.rawQuery("select count(number) as count from card", null);
		c.moveToFirst();
		count = c.getInt(0);
		c.close();
		db.close();
		return count;
	}

	public float avgGlobal(long number){
		float avg = 0;
		db = dbHelper.getWritableDatabase();
		Cursor c = db.rawQuery("select avg(valor) from item where charge = 0 and itemcard = ?",  new String[]{String.valueOf(number)});
		c.moveToFirst();
		avg = c.getFloat(0);
		c.close();
		db.close();
		return avg;
	}

	public float avgByDays(long number){
		float sum = 0;
		db = dbHelper.getWritableDatabase();
		Cursor c = db.rawQuery("select sum(valor) from item where charge = 0 and itemcard = ?",  new String[]{String.valueOf(number)});
		c.moveToFirst();
		sum = c.getFloat(0);
		c.close();
		db.close();
		return sum/diffDays(number);
	}

	public float diffDays(long number){
		long days = 0;
		db = dbHelper.getWritableDatabase();
		Cursor c = db.rawQuery("select distinct dia from item where charge = 0 and itemcard = ?", new String[]{String.valueOf(number)});
		days = c.getCount();
		c.close();
		db.close();
		return days ;
	}

	public long update(Card c){
		ContentValues cv = new ContentValues();
		cv.put("updatetime",System.currentTimeMillis());
		cv.put("saldo",c.getTotal());
		cv.put("nextchargevalor",c.getNextChargeValor());
		cv.put("lastchargevalor",c.getLastChargeValor());
		if(c.getLastCharge() != null)
			cv.put("lastcharge", c.getLastCharge().getTime());
		if(c.getNextCharge() != null)
			cv.put("nextcharge", c.getNextCharge().getTime());
		db = dbHelper.getWritableDatabase();
		long rows = db.update("card", cv, "number = ?", new String[]{ String.valueOf(c.getNumber())});
		db.close();
		if(c.getItens() != null && c.getItens().size() > 0)
			insertItems(c.getItens());
		return rows;
	} 

	private void insertItems(List<Item> itens) {
		deleteItemsByNumber(itens.get(0).getItemCard());
		db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		for (Item item : itens) {
			ContentValues cv = new ContentValues();
			cv.put("valor", item.getValor());
			cv.put("place", item.getPlace());
			cv.put("itemcard", item.getItemCard());
			cv.put("dia", item.getDia().getTime());
			cv.put("charge", item.isCharge());
			db.insert("item", null, cv);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	private int deleteItemsByNumber(long number){
		db = dbHelper.getWritableDatabase();
		int rows = db.delete("item", "itemcard = ?", new String[]{ String.valueOf(number) });
		db.close();
		return rows;
	}

	public int deleteByNumber(long number){
		db = dbHelper.getWritableDatabase();
		int rows = db.delete("card", "number = ?", new String[]{ String.valueOf(number) });
		db.close();
		return rows;
	}

	public List<Card> getAll(){
		List<Card> list = new ArrayList<Card>();
		String[] columns = new String[]{"number", "name", "lastcharge","nextchargevalor","lastchargevalor","saldo", "nextcharge", "updatetime"};
		db = dbHelper.getWritableDatabase();
		Cursor c = db.query("card", columns,null, null, null, null,"name");
		c.moveToFirst();
		while(!c.isAfterLast()){
			Card card = fillCard(c);
			list.add(card);
			c.moveToNext();
		}
		c.close();
		db.close();
		return list;
	}

	public Card fillCard(Cursor c) {
		Card card = new Card();
		card.setNumber(c.getLong(0));
		card.setName(c.getString(1));
		card.setNextChargeValor(c.getFloat(3));
		card.setLastChargeValor(c.getFloat(4));
		card.setTotal(c.getFloat(5));
		if(c.getLong(2) == 0)
			card.setLastCharge(null);
		else
			card.setLastCharge(new Date( c.getLong(2)));
		if(c.getLong(6) == 0)
			card.setNextCharge(null);
		else
			card.setNextCharge(new Date( c.getLong(6)));
		if(c.getLong(7) == 0)
			card.setLastUpdate(null);
		else
			card.setLastUpdate(new Date( c.getLong(7)));
		return card;
	}

	public List<Item> getItems(long number){
		List<Item> list = new ArrayList<Item>();
		String[] columns = new String[]{"itemcard", "valor", "place","dia","charge"};
		db = dbHelper.getWritableDatabase();
		String[] args = new String[]{String.valueOf(number)};
		Cursor c = db.query("item", columns,"itemcard = ?", args, null, null,null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			Item i = fillItem(c);
			list.add(i);
			c.moveToNext();
		}
		c.close();
		db.close();
		return list;
	}

	public Item fillItem(Cursor c) {
		Item i = new Item();
		i.setItemCard(c.getLong(0));
		i.setValor(c.getFloat(1));
		i.setPlace(c.getString(2));
		i.setDia(new Date(c.getLong(3)));
		if(c.getInt(4) == 0)
			i.setCharge(false);
		else
			i.setCharge(true);
		return i;
	}

	public Card getCardByNumber(long number){
		String[] columns = new String[]{"number", "name", "lastcharge","nextchargevalor","lastchargevalor","saldo", "nextcharge", "updatetime"};
		String[] args = new String[]{String.valueOf(number)};
		db = dbHelper.getWritableDatabase();
		Cursor c = db.query("card", columns, "number = ?", args, null, null, null);
		Card card = null;
		if(c.moveToFirst())
			card = fillCard(c);
		c.close();
		db.close();
		if(card != null)
			card.setItens(getItems(card.getNumber()));
		return card;
	}
}
