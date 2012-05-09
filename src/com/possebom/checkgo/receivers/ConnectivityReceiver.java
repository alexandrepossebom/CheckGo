package com.possebom.checkgo.receivers;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.possebom.checkgo.ManageCard;
import com.possebom.checkgo.services.UpdateService;

public class ConnectivityReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(final Context ctx, Intent intent) {
		if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if(networkInfo.isConnected()) {
				SharedPreferences mPrefs = ctx.getSharedPreferences(ManageCard.PREFS_NAME, Context.MODE_PRIVATE);
				long lastRunMillis = mPrefs.getLong("lastRun", 0);
				Calendar now = Calendar.getInstance();
				Calendar lastRun = Calendar.getInstance();
				lastRun.setTimeInMillis(lastRunMillis);
				if(now.get(Calendar.DAY_OF_YEAR) != lastRun.get(Calendar.DAY_OF_YEAR) || (lastRun.get(Calendar.HOUR_OF_DAY) < 12 && now.get(Calendar.HOUR_OF_DAY) > 12)){
					Handler handler = new Handler(); 
					handler.postDelayed(new Runnable() { 
						public void run() { 
							Intent i = new Intent(ctx, UpdateService.class);
							ctx.startService(i);
						} 
					}, 20000); 
				}
			}
		} 
	}

}
