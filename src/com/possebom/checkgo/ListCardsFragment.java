package com.possebom.checkgo;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.possebom.checkgo.dao.CardsDAO;
import com.possebom.checkgo.model.Card;

public class ListCardsFragment extends ListFragment {
	private long number = 0;
	private ListItemSelectedListener selectedListener;
	private CardsDAO cd ;
	private static final SimpleDateFormat FMT = new SimpleDateFormat("dd/MM");
	private static final SimpleDateFormat FMTF = new SimpleDateFormat("dd/MM HH:mm:ss");

	@Override
	public void onResume() {
		super.onResume();
		update();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		number = getListAdapter().getItemId(position);
		selectedListener.onListItemSelected(number);
	}

	public interface ListItemSelectedListener {
		public void onListItemSelected(long number);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("number", number);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			selectedListener = (ListItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ListItemSelectedListener in Activity");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		cd = new CardsDAO( getActivity().getApplicationContext() );

		update();
	}

	private void update()
	{
		setListAdapter(new MyBaseAdapter(getActivity().getApplicationContext(),cd.getAll()));
	}

	private static class MyBaseAdapter extends BaseAdapter {
		private List<Card> listCards;
		private Locale locale = new Locale("pt", "BR");  
		private NumberFormat format;
		private Context ctx;

		public MyBaseAdapter(Context ctx, List<Card> listCards) {
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
			

			Card card = listCards.get(position);
			StringBuffer sb = new StringBuffer();
			sb.append("<b>Nome: </b>");
			sb.append(card.getName());
			sb.append("<BR>");
			sb.append("<b>Número: </b>");
			sb.append(card.getNumber());
			sb.append("<BR>");
			sb.append("<b>Saldo: </b>R$");
			sb.append(format.format(card.getTotal()));
			sb.append("<BR>");
			sb.append("<b>Última Recarga: </b>");
			if(card.getLastCharge() != null){
				sb.append(FMT.format(card.getLastCharge()));
				sb.append(" R$");
				sb.append(format.format(card.getLastChargeValor()));
			}else{
				sb.append("indisponível");
			}
			sb.append("<BR>");
			sb.append("<b>Próxima Recarga: </b>");
			if(card.getNextCharge() != null){
				sb.append(FMT.format(card.getNextCharge()));
				sb.append(" R$");
				sb.append(format.format(card.getNextChargeValor()));
			} else {
				sb.append("indisponível");
			}
			if(card.getLastUpdate() != null){
				sb.append("<BR>");
				sb.append("<b>Última atualização: </b>");
				sb.append(FMTF.format(card.getLastUpdate()));
			}
			textView.setText(Html.fromHtml(sb.toString()));			
			return view;
		}
	}
}
