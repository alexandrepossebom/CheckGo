package com.possebom.checkgo.services;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.possebom.checkgo.ManageCard;
import com.possebom.checkgo.dao.CardsDAO;
import com.possebom.checkgo.model.Card;
import com.possebom.checkgo.utils.Parser;

public class UpdateService extends Service {

	@Override
	public void onStart(Intent intent, int startId) {
		SharedPreferences mPrefs = getApplicationContext().getSharedPreferences(ManageCard.PREFS_NAME, Context.MODE_PRIVATE);
		mPrefs.edit().putLong("lastRun", System.currentTimeMillis()).commit();

		final CardsDAO cd = new CardsDAO(getApplicationContext());
		new Thread(new Runnable() {
			public void run() {
				for (final Card card : cd.getAll())
				{
					Parser.updateCard(card);
					cd.update(card);
				}
			}
		}).start();
		stopSelf();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
