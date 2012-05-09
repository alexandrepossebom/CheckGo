package com.possebom.checkgo;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.possebom.checkgo.dao.CardsDAO;
import com.possebom.checkgo.model.Card;

public class ManageCard extends Activity {
	public static final String PREFS_NAME = "CheckGoPROPrefs";
	private CardsDAO cd ;
	private void removeCard(final long num)
	{
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Remover Cartão")
		.setMessage("Você tem certeza que deseja remover esse cartão ?")
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cd.deleteByNumber(num);
				updateList();
			}
		})
		.setNegativeButton(android.R.string.no, null)
		.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateList();
	}

	private void updateList(){
		final ListView list = (ListView) findViewById(R.id.ListView01);
		CardsDAO cd = new CardsDAO(getApplicationContext());

		list.setAdapter(new EfficientAdapter(this,cd.getAll()));
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
				removeCard(list.getAdapter().getItemId(position));
			}
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.managecard);

		cd = new CardsDAO(getApplicationContext());

		//Migrar versão antiga para nova
		if(cd.numCards() == 0)
		{
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			int i=0;
			while(settings.getString("card-"+i+"-number", "").length() > 0)
			{
				Card card = new Card();
				card.setName(settings.getString("card-"+i+"-name", ""));
				card.setNumber(Long.valueOf(settings.getString("card-"+i+"-number", "")));
				cd.insertCard(card);
				settings.edit().remove("card-"+i+"-number").commit();
				settings.edit().remove("card-"+i+"-name").commit();
				settings.edit().remove("card-"+i+"-last").commit();
				settings.edit().remove("card-"+i+"-saldo").commit();
				settings.edit().remove("card-"+i+"-history").commit();
				i++;
			}
		}

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getActionBar().setDisplayHomeAsUpEnabled(true);
		final Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				addCard();
			}
		});   
		updateList();
	}

	private void addCard() 
	{
		startActivity(new Intent(this, AddCard.class));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, ListCardsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private static class EfficientAdapter extends BaseAdapter {
		private List<Card> listCards;
		private Context ctx;
		private Locale locale = new Locale("pt", "BR");  
		private NumberFormat format;

		public EfficientAdapter(Context ctx, List<Card> listCards) {
			this.listCards = listCards;
			this.ctx = ctx;
			format= NumberFormat.getInstance(locale);  
			format.setMaximumFractionDigits(2);  
			format.setMinimumFractionDigits(2);
		}

		public int getCount() {
			return listCards.size();
		}

		public Object getItem(int position) {
			return listCards.get(position);
		}

		public long getItemId(int position) {
			return listCards.get(position).getNumber();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
			View view = inflater.inflate(R.layout.listviewcard, null);  
			TextView textView = (TextView) view.findViewById(R.id.textCard);

			StringBuffer sb = new StringBuffer();
			Card card = listCards.get(position);

			sb.append("<b>Nome: </b>");
			sb.append(card.getName());
			sb.append("<BR><b>Número: </b>");
			sb.append(card.getNumber());
			if(card.getTotal() > 0){
				sb.append("<BR><b>Saldo: </b>R$ ");
				sb.append(format.format(card.getTotal()));
			}

			textView.setText(Html.fromHtml(sb.toString()));			
			return view;
		}
	}
}
