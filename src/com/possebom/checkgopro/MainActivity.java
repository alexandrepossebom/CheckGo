package com.possebom.checkgopro;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.possebom.checkgopro.R;

public class MainActivity extends Activity implements Runnable {
	public static final String PREFS_NAME = "CheckGoPROPrefs";
	public static final String TAG = "CheckGoPRO";
	
	private static ArrayList<String> cardNumber = new ArrayList<String>();
	private static ArrayList<String> cardName = new ArrayList<String>();
	private static ArrayList<String> cardSaldo = new ArrayList<String>();
	private static ArrayList<String> cardLast = new ArrayList<String>();
	
	private ProgressDialog progressDialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);    
		final Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				updateView();
			}
		});   
		initView();
    }
    
    public void initView()
    {
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	ListView list = (ListView) findViewById(R.id.listView);
    	
    	cardNumber.clear();
		cardName.clear();
		cardSaldo.clear();
		cardLast.clear();
		
		for (int i = 0; i < 200; i++) {
			if(settings.getString("card-"+i+"-number", "").length() > 0)
			{
				cardNumber.add( settings.getString("card-"+i+"-number", "") );
				cardName.add(   settings.getString("card-"+i+"-name", "")   );
				cardSaldo.add(  settings.getString("card-"+i+"-saldo", "")  );
				cardLast.add(  settings.getString("card-"+i+"-last", "")  );	
			}
			else{
				break;
			}
		}
		
		list.setAdapter(new EfficientAdapter(this));
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("seeHistory", arg2+"");
				editor.commit();
				seeHistory();
			}
		});
    }
    
    private void seeHistory() {
    	startActivity(new Intent(this, History.class));
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		initView();
	}

	private String clearHistory(String str)
	{
		String[] oi = str.split("\n");
		if(oi.length < 3)
			return str;		
		str = oi[oi.length-2].split(":")[1];
		
		return str;
	}
	
	private void updateView() {         
		Thread thread = new Thread(this);
		thread.start();
		progressDialog = ProgressDialog.show(this, "Atualizando Saldo", "Carregando dados", true, false);
	}
	
	public void run() {
		for (int i = 0; i < getNumberCards(); i++) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String number = settings.getString("card-"+i+"-number", "");
			String data = getUrl(number);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("card-"+i+"-status", data);
			editor.commit();
		}
		handler.sendEmptyMessage(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_quit:			
			finish();
			break;
		case R.id.menu_about:
			final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setIcon(R.drawable.icon);
			alertDialog.setTitle("Check Refeição");
			alertDialog.setMessage("Desenvolvido por Alexandre Possebom <alexandrepossebom@gmail.com>");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					alertDialog.dismiss();
				}
			});
			alertDialog.show();
			break;
		case R.id.menu_card:
			startActivity(new Intent(this, ManageCard.class));
			break;
		}

		return true;
	}
	
	private int getNumberCards(){
		return cardNumber.size();
	}
	
	 private Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
        	 SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        	 
        	 for (int i = 0; i < 200; i++) {
     			if(settings.getString("card-"+i+"-number", "").length() > 0)
     			{
     				String data = settings.getString("card-"+i+"-status", "");
     				if(data.contains("Saldo"))
     				{
     					SharedPreferences.Editor editor = settings.edit();
     					editor.putString("card-"+i+"-saldo", clearHistory(data));
     					editor.putString("card-"+i+"-history", data);
     					editor.putString("card-"+i+"-last", getDate());
     					editor.commit();
     				}
     				else
     				{
     					SharedPreferences.Editor editor = settings.edit();
     					editor.putString("card-"+i+"-last", "FAIL");
     					editor.commit();
     				}
     			}
     			else{
     				break;
     			}
     		}
        	 initView();
        	 progressDialog.dismiss();
         }
 };
 
 private String getUrl(String number) {
		//final EditText cardNumber = (EditText)findViewById(R.id.cardNumber); 
	 
	 	Log.d(TAG,"Getting card number : "+ number);
		URI myURL = null;
		try {
			myURL = new URI("http://www.cbss.com.br/inst/convivencia/SaldoExtrato.jsp?numeroCartao=" + number);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			Log.d(TAG,e1.getMessage());
		}

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getMethod = new HttpGet(myURL);
		HttpResponse httpResponse;

		String result = "Erro pegando dados.";

		try {
			httpResponse = httpClient.execute(getMethod);
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				BufferedReader reader = new BufferedReader( new InputStreamReader(instream));
				StringBuilder sb = new StringBuilder();
				String line = null;
				try {					
					while ((line = reader.readLine()  ) != null) {
						
						//disponibiliza&ccedil;&atilde;o do benef&iacute;cio:</td>
						line = line.replaceAll("&ccedil;", "ç");
						line = line.replaceAll("&atilde;", "ã");
						line = line.replaceAll("&iacute;", "í");
						
						if(line.contains("lido."))
						{
							sb.append("Cartão Inválido");
							Log.d(TAG,"Cartão inválido");
							break;
						}
						if(line.contains("topTable"))
							continue;
						if(line.contains("400px") )
						{
							sb.append(line.replaceAll("\\<.*?>","").replaceAll("\\&nbsp\\;", " ").trim()).append(" - ");
						}
						if(line.contains("50px") )
						{
							if(line.contains("R"))
								sb.append(line.replaceAll("\\<.*?>","").replaceAll("\\&nbsp\\;", " ").trim()).append("\n");
							else if (line.contains("/"))
								sb.append(line.replaceAll("\\<.*?>","").replaceAll("\\&nbsp\\;", " ").trim()).append(" - ");
						}

						if(line.contains("Saldo d") )
						{
							sb.append(line.replaceAll("\\<.*?>","").replaceAll("dispo.*vel:", " : ").trim());
							sb.append("\n");
							sb.append(getDate());
						}

					}
				} catch (Exception e) {
					Log.d(TAG,e.getMessage());
					result = "Erro carregando dados.";
				} finally {
					try {
						instream.close();
					} catch (Exception e) {
						Log.d(TAG,e.getMessage());
						result = "Erro carregando dados.";
					}
				}
				result = sb.toString();
			}
		} catch (Exception e) {
			Log.d(TAG,e.getMessage());
			result = "Erro carregando dados.";
		}
		return result;
	}
 
	public static String getDate() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
		return sdf.format(cal.getTime());
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
			sb.append("<BR>");
			sb.append("<b>Última atualização: </b>");
			sb.append(cardLast.get(position));
			
			holder.textCard.setText(Html.fromHtml(sb.toString()));			
			//convertView.setBackgroundColor((position & 1) == 1 ? Color.WHITE : Color.LTGRAY);
			return convertView;
		}

		static class ViewHolder {
			TextView textCard;
		}
	}
	
}