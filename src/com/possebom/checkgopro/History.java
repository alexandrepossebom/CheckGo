package com.possebom.checkgopro;

import com.possebom.checkgopro.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class History extends Activity {
	public static final String PREFS_NAME = "CheckGoPROPrefs";
	public static final String TAG = "CheckGoPRO";
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		final TextView view = (TextView)findViewById(R.id.view);
		final TextView viewSaldo = (TextView)findViewById(R.id.viewSaldo);
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String cardNumber = settings.getString("seeHistory", "");
		String data = settings.getString("card-"+cardNumber+"-history", "");
		
		String[] items = data.split("\n");
		StringBuffer sb = new StringBuffer();
		StringBuffer sbSaldo = new StringBuffer();


		int i=0;
		for (String item : items)
		{
			if(item.contains(" - "))
			{
				i++;
				if(i%2==0)
					sb.append("<font color=white>");
				else
					sb.append("<font color=yellow>");
					
				String[] dados = item.split(" - ");
				String dia = dados[0];
				String nome = dados[1];
				String valor = dados[2];

				sb.append(dia);
				sb.append(" ");

				if(nome.length() > 20)
					sb.append(nome.substring(0, 20));
				else
					sb.append(String.format("%1$-20s", nome).replaceAll(" ", "&nbsp;"));
				sb.append(" ");
				sb.append(valor);
				sb.append("<br>\n");	
				sb.append("</font>");
			}
			else
				sbSaldo.append(item).append("\n");			
		}

		view.setText(Html.fromHtml(sb.toString()));
		viewSaldo.setText(sbSaldo.toString());
	}
}