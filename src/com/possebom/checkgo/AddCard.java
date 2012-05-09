package com.possebom.checkgo;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.possebom.checkgo.dao.CardsDAO;
import com.possebom.checkgo.model.Card;

public class AddCard extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addcard);    

		final Button button = (Button) findViewById(R.id.button);

		BitmapDrawable image = new BitmapDrawable(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher));
		image.setGravity(Gravity.TOP);
		image.setAlpha(50);
		LinearLayout infoLayout = (LinearLayout)findViewById(R.id.info_layout);
		infoLayout.setBackgroundDrawable(image.getCurrent());

		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v){
				addCard();			
			}
		});   
	}

	private void addCard()
	{
		TextView textName = (TextView)findViewById(R.id.cardAddName);
		TextView textNumber = (TextView)findViewById(R.id.cardAddNumber);

		Card c = new Card();
		c.setName(textName.getText().toString().trim());
		try{
			c.setNumber(Long.parseLong(textNumber.getText().toString()));
			if(c.getName().isEmpty())
				c.setName(String.valueOf(c.getNumber()));
			CardsDAO cd = new CardsDAO(getApplicationContext());
			cd.insertCard(c);
		}catch (Exception e) {}
		finish();
	}
}
