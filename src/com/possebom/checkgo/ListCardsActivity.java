package com.possebom.checkgo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.possebom.checkgo.dao.CardsDAO;
import com.possebom.checkgo.model.Card;
import com.possebom.checkgo.utils.Parser;

public class ListCardsActivity extends FragmentActivity implements ListCardsFragment.ListItemSelectedListener {
	private final String ANALYTICS = "UA-418412-5";
	private GoogleAnalyticsTracker tracker;
	private ProgressDialog progressDialog;
	private CardsDAO cd ;

	@Override
	protected void onResume() {
		super.onResume();
		tracker.trackPageView(getClass().getSimpleName());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tracker.dispatch();
		tracker.stopSession();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession(ANALYTICS, getApplication());
		tracker.setDispatchPeriod(60);

		cd = new CardsDAO(this);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getString(R.string.updating));	

		if(cd.numCards() == 0)
			startActivity(new Intent(this, ManageCard.class));
		else
			tracker.setCustomVar(1, "NumCards", String.valueOf(cd.numCards()) , 2);
		setContentView(R.layout.main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.main, menu);
		return(super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_update:
			if(haveInternet()){
				tracker.trackPageView("/reload");
				new UpdateCards().execute();
				tracker.dispatch();
			}else{
				final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setIcon(R.drawable.ic_launcher);
				alertDialog.setTitle(R.string.app_name);
				alertDialog.setMessage("Sem internet, tente mais tarde");
				alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						alertDialog.dismiss();
					}
				});
				alertDialog.show();
			}
			return true;
		case R.id.menu_card:
			tracker.trackPageView("/ManageCard");
			startActivity(new Intent(this, ManageCard.class));
			return true;
		case R.id.menu_about:
			tracker.trackPageView("/About");
			final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setIcon(R.drawable.ic_launcher);
			alertDialog.setTitle(R.string.app_name);
			alertDialog.setMessage("Desenvolvido por Alexandre Possebom <alexandrepossebom@gmail.com>");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					alertDialog.dismiss();
				}
			});
			alertDialog.show();
			return true;
		}

		return(super.onOptionsItemSelected(item));
	}

	private boolean haveInternet(){ 
		NetworkInfo info=((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();  
		if(info==null || !info.isConnected()){  
			return false;  
		}  
		return true;  
	} 

	@Override
	public void onListItemSelected(long number) {
		tracker.trackPageView("/History");
		HistoryFragment view = (HistoryFragment) getSupportFragmentManager().findFragmentById(R.id.history_fragment);
		if (view == null || !view.isInLayout()) {
			Intent i = new Intent(getApplicationContext(),HistoryActivity.class);
			i.putExtra("number", number);
			startActivity(i);
		} else {
			view.update(number);
		}
	}

	private class UpdateCards extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... arg0) {
			for (final Card card : cd.getAll())
			{
				Parser.updateCard(card);
				cd.update(card);
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected void onPostExecute(Void result) {
			if (progressDialog.isShowing()) progressDialog.dismiss();
			((ListCardsFragment) getSupportFragmentManager().findFragmentById(R.id.list_cards_fragment)).onResume();
		}
	}
}