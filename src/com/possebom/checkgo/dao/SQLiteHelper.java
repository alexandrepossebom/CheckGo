package com.possebom.checkgo.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper{
	private final String scriptDbCreateCard;
	private final String scriptDbCreateItem;
	private final String scriptDbDeleteCard;
	private final String scriptDbDeleteItem;

	public SQLiteHelper(Context ctx, String nomeBd,int versaoBanco, String scriptDbCreateCard, String scriptDbCreateItem, String scriptDbDeleteCard, String scriptDbDeleteItem) {  
		super(ctx, nomeBd, null, versaoBanco);  
		this.scriptDbCreateCard = scriptDbCreateCard;
		this.scriptDbCreateItem = scriptDbCreateItem;
		this.scriptDbDeleteCard = scriptDbDeleteCard;
		this.scriptDbDeleteItem = scriptDbDeleteItem;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(scriptDbCreateCard);
		db.execSQL(scriptDbCreateItem);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(scriptDbDeleteItem);  
		db.execSQL(scriptDbDeleteCard);
		onCreate(db);  
	}

}
