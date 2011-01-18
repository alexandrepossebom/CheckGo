package com.possebom.checkgopro;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ManageCard extends Activity {
	public static final String PREFS_NAME = "CheckGoPROPrefs";
	public static final String TAG = "CheckGoPRO";
	private static ArrayList<String> cardNumber = new ArrayList<String>();
	private static ArrayList<String> cardName = new ArrayList<String>();
	private static ArrayList<String> cardSaldo = new ArrayList<String>();
	private static ArrayList<String> cardLast = new ArrayList<String>();
	private static ArrayList<String> cardHistory = new ArrayList<String>();
	
	
	private void removeCard(final String num)
	{
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Remover Cartão")
		.setMessage("Você tem certeza que deseja remover esse cartão ?")
		.setPositiveButton("Sim", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				int i;
				for (i = 0; i < 20; i++) {
					if(settings.getString("card-"+i+"-number", "").equals(num))
						break;
				}

				SharedPreferences.Editor editor = settings.edit();
				editor.remove("card-"+i+"-number");
				editor.remove("card-"+i+"-name");
				editor.remove("card-"+i+"-saldo");
				editor.commit();

				updateList();

				editor.clear();

				for (i = 0; i < cardNumber.size(); i++) {
					editor.putString("card-"+i+"-number", cardNumber.get(i));
					editor.putString("card-"+i+"-name", cardName.get(i));
					editor.putString("card-"+i+"-saldo", cardSaldo.get(i));
					editor.putString("card-"+i+"-last", cardLast.get(i));
					editor.putString("card-"+i+"-history", cardHistory.get(i));
				}		
				editor.commit();

			}

		})
		.setNegativeButton("Não", null)
		.show();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateList();
	}

	
	private void updateList(){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		ListView list = (ListView) findViewById(R.id.ListView01);

		cardNumber.clear();
		cardName.clear();
		cardSaldo.clear();

		int i=0;
		for (i = 0; i < 20; i++) {
			if(settings.getString("card-"+i+"-number", "").length() > 0)
			{
				cardNumber.add( settings.getString("card-"+i+"-number", "") );
				cardName.add(   settings.getString("card-"+i+"-name", "")   );
				cardSaldo.add(  settings.getString("card-"+i+"-saldo", "")  );
				cardLast.add(  settings.getString("card-"+i+"-last", "")  );
				cardHistory.add(  settings.getString("card-"+i+"-history", "")  );
			}
		}
		
		list.setAdapter(new EfficientAdapter(this));
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			    removeCard(cardNumber.get(arg2));
			}
		});
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.managecard);
//		BitmapDrawable image = new BitmapDrawable(BitmapFactory.decodeResource(getResources(),R.drawable.logo));
//		image.setGravity(Gravity.CENTER);
//		image.setAlpha(50);
//		LinearLayout infoLayout = (LinearLayout)findViewById(R.id.info_layout);
//		infoLayout.setBackgroundDrawable(image.getCurrent());
		
		
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

	private static class EfficientAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public EfficientAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return cardNumber.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.listviewcard, null);
				holder = new ViewHolder();
				holder.textCard = (TextView) convertView.findViewById(R.id.textCard);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			StringBuffer sb = new StringBuffer();
			
			sb.append("<b>Nome: </b>");
			sb.append(cardName.get(position));
			sb.append("<BR>");
			sb.append("<b>Número: </b>");
			sb.append(cardNumber.get(position));
			sb.append("<BR>");
			sb.append("<b>Saldo: </b>");
			sb.append(cardSaldo.get(position));
			
			holder.textCard.setText(Html.fromHtml(sb.toString()));			
			//convertView.setBackgroundColor((position & 1) == 1 ? Color.WHITE : Color.LTGRAY);
			return convertView;
		}

		static class ViewHolder {
			TextView textCard;
		}
	}
}
