package com.possebom.checkgopro;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AddCard extends Activity {
	public static final String PREFS_NAME = "CheckGoPROPrefs";
	public static final String TAG = "CheckGoPRO";
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addcard);    
		
		final Button button = (Button) findViewById(R.id.button);
		
		
		BitmapDrawable image = new BitmapDrawable(BitmapFactory.decodeResource(getResources(),R.drawable.logo));
		image.setGravity(Gravity.TOP);
		image.setAlpha(50);
		LinearLayout infoLayout = (LinearLayout)findViewById(R.id.info_layout);
		infoLayout.setBackgroundDrawable(image.getCurrent());
		
		
		
		
		

		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				addCard();			}
		});   
	}
	
	
	private void addCard()
	{
		TextView textName = (TextView)findViewById(R.id.cardAddName);
		TextView textNumber = (TextView)findViewById(R.id.cardAddNumber);
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		for (int i = 0; i < 200; i++) {
			if(settings.getString("card-"+i+"-number", "").length() > 0)
				continue;
			editor.putString("card-"+i+"-number", textNumber.getText().toString());
			editor.putString("card-"+i+"-name", textName.getText().toString());
			break;
		}		
		editor.commit();
		
		this.finish();
	}
}
