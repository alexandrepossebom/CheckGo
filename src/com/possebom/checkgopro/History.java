package com.possebom.checkgopro;

import java.text.NumberFormat;
import java.util.Locale;

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
		
		float media = 0;
		int itens = 0;

		for (String item : items)
		{
			if(item.contains(" - "))
			{
				String[] dados = item.split(" - ");
				String dia = dados[0];
				String nome = dados[1];
				String valor = dados[2];
				
				String tmp = valor.replaceAll("R\\$", "");
				
				if(nome.contains("Disponibilização"))
				{
					sb.append("<font color=green>");
					nome = "** Recarga **";
				}else{
					itens++;
					if(itens%2==0)
						sb.append("<font color=white>");
					else
						sb.append("<font color=yellow>");
					media += Float.parseFloat(tmp.replaceAll("\\.", "").replaceAll(",", "."));
				}
				
				sb.append(dia);
				sb.append(" ");
				
				if(valor.length() < 9)
				{
					while(tmp.length() < 7)
						tmp = " " + tmp;
					valor = "R$" + tmp;
				}
				valor = valor.replaceAll(" ", "&nbsp;");
				

				if(nome.length() > 20)
					nome = nome.substring(0, 20);
				else
					nome = String.format("%1$-20s", nome);
				
				nome = nome.replaceAll(" ", "&nbsp;");
				
				sb.append(nome);
				sb.append(" ");
				sb.append(valor);
				sb.append("<br>\n");	
				sb.append("</font>");
			}
			else
			{
				if(!item.contains("**"))
					if(!item.contains("!!"))
					sbSaldo.append(item).append("\n");
			}
		}

		view.setText(Html.fromHtml(sb.toString()));
		
		Locale locale = new Locale("pt", "BR");  
		NumberFormat format = NumberFormat.getInstance(locale);  
		format.setMaximumFractionDigits(2);  
		
		viewSaldo.setText("Média de Consumo : R$ " + format.format(media/itens) + "\n" +  sbSaldo.toString());
	}
}