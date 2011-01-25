package com.possebom.checkgopro;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	private static ArrayList<String> cardNextRec = new ArrayList<String>();
	private static ArrayList<String> cardLastRec = new ArrayList<String>();
	
	private int erros = 0;
	
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
		cardNextRec.clear();
		cardLastRec.clear();
		
		for (int i = 0; i < 200; i++) {
			if(settings.getString("card-"+i+"-number", "").length() > 0)
			{
				cardNumber.add( settings.getString("card-"+i+"-number", "") );
				cardName.add(   settings.getString("card-"+i+"-name", "")   );
				cardSaldo.add(  settings.getString("card-"+i+"-saldo", "indisponível")  );
				cardLast.add(  settings.getString("card-"+i+"-last", "indisponível")  );
				cardNextRec.add(  settings.getString("card-"+i+"-nextrec", "indisponível")  );
				cardLastRec.add(  settings.getString("card-"+i+"-lastrec", "indisponível")  );
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
		
		if(erros > 0)
		{
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setIcon(R.drawable.icon);
		alertDialog.setTitle("Check Go");
		if (erros == 1)
			alertDialog.setMessage("Ocorreu um erro verifique sua a internet.");
		else
			alertDialog.setMessage("Ocorreram "+erros+" erros verifique sua a internet.");
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.dismiss();
			}
		});
		alertDialog.show();
		erros = 0;
		}
    }
    
    private void seeHistory() {
    	startActivity(new Intent(this, History.class));
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		initView();
	}

	
	private String getLastRec(String str)
	{
		for (String line: str.split("\n")) {
			if(line.contains("!!"))
			{
				line = line.replaceAll("\\!\\!", "");
				if(line.split("\\|")[0].length() == 0)
					return "  indisponível";
				else
					return line.replaceAll("Valor:", "").replaceAll("\\|", " ");
			}
		}
		return "indisponível";
	}
	
	
	private String getNextRec(String str)
	{
		for (String line: str.split("\n")) {
			if(line.contains("**"))
			{
				line = line.replaceAll("\\*\\*", "");
				if(line.split("\\|")[0].length() == 0)
					return "  indisponível";
				else
					return line.replaceAll("Valor:", "").replaceAll("\\|", " ");
			}
		}
		return "indisponível";
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
			if(data == null)
				erros++;
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
     					editor.putString("card-"+i+"-nextrec", getNextRec(data));
     					editor.putString("card-"+i+"-lastrec", getLastRec(data));
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
 
 private boolean haveInternet(){ 
	 NetworkInfo info=((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();  
	 if(info==null || !info.isConnected()){  
		 return false;  
	 }  
	 return true;  
 }  
 
 
 private String getUrl(String number) {
	 if(!haveInternet())
		 return null;
	 
	 String result = null;
	 try {
		 URL url = new URL("http://www.cbss.com.br/inst/convivencia/SaldoExtrato.jsp?numeroCartao=" + number + "&periodoSelecionado=4");

		 URLConnection connection = url.openConnection();
		 InputStream inputStream = connection.getInputStream();
		 BufferedInputStream bufferedInput = new BufferedInputStream(inputStream);

		 ByteArrayBuffer byteArray = new ByteArrayBuffer(50);
		 int current = 0;
		 while((current = bufferedInput.read()) != -1){
			 byteArray.append((byte)current);
		 }

		 result = new String(byteArray.toByteArray(), "ISO-8859-1");
	 } catch (Exception e) {
		 Log.d(TAG,e.getMessage());
	 }


	 StringBuffer sb = new StringBuffer(50);
	 
	 
	 if(result == null || result.length() == 0 || ! result.contains("\n"))
		 return null;
	 
	 String[] lineArr = result.split("\n");
	 
	 for (int i = 0; i < lineArr.length; i++) {
		String line = lineArr[i];

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
		 
		 if(line.contains("Data da &uacute;ltima disponibiliza&ccedil;&atilde;o"))
		 {
			 sb.append("!!").append(lineArr[i+1].replaceAll("\\<.*?>","").replaceAll("\\&nbsp\\;", " ").trim());
			 sb.append("|").append(lineArr[i+2].replaceAll("\\<.*?>","").replaceAll("\\&nbsp\\;", " ").trim());
			 sb.append("\n");
		 }
		
		 if(line.contains("Data da pr"))
		 {
			 sb.append("**").append(lineArr[i+1].replaceAll("\\<.*?>","").replaceAll("\\&nbsp\\;", " ").trim());
			 sb.append("|").append(lineArr[i+2].replaceAll("\\<.*?>","").replaceAll("\\&nbsp\\;", " ").trim());
			 sb.append("\n");
		 }

		 if(line.contains("Saldo d") )
		 {
			 sb.append(line.replaceAll("\\<.*?>","").replaceAll("dispo.*vel:", " : ").trim());
			 sb.append("\n");
			 sb.append(getDate());
		 }
	 }
	 return sb.toString();
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
			sb.append("<b>Última Recarga: </b>");
			sb.append(cardLastRec.get(position));
			sb.append("<BR>");
			sb.append("<b>Próxima Recarga: </b>");
			sb.append(cardNextRec.get(position));
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