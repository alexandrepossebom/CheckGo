package com.possebom.checkgo;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.possebom.checkgo.dao.CardsDAO;
import com.possebom.checkgo.model.Card;
import com.possebom.checkgo.model.Item;

public class HistoryFragment extends Fragment {
	private static final SimpleDateFormat FMT = new SimpleDateFormat("[dd/MM]");
	private NumberFormat format;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		Locale locale = new Locale("pt", "BR");  
		format = NumberFormat.getInstance(locale);  
		format.setMaximumFractionDigits(2);  
		format.setMinimumFractionDigits(2);
		return inflater.inflate(R.layout.history, container, false);
	}

	public void update(long number) {
		int itens = 0;

		TableLayout table = (TableLayout) getView().findViewById(R.id.TableLayout01);

		table.removeAllViews();

		CardsDAO cd = new CardsDAO(getActivity().getApplicationContext());

		Card card = cd.getCardByNumber(number);

		if (card == null) return;

		for (Item item : card.getItens())
		{
			TableRow row = new TableRow(getActivity().getApplicationContext());

			if(item.isCharge())
			{
				row.setBackgroundColor(android.graphics.Color.BLUE);
			}else{
				itens++;
				if(itens%2==0)
					row.setBackgroundColor(android.graphics.Color.DKGRAY);
			}

			TextView textDia = new TextView(getActivity().getApplicationContext());
			textDia.setText(FMT.format(item.getDia()));
			textDia.setPadding(0, 0, 5, 0);
			row.addView(textDia);

			TextView textNome = new TextView(getActivity().getApplicationContext());
			textNome.setText(item.getPlace());
			textNome.setPadding(5, 0, 5, 0);
			row.addView(textNome);
			TextView textValor = new TextView(getActivity().getApplicationContext());

			textValor.setText("R$"+format.format(item.getValor()));
			row.addView(textValor);

			table.addView(row);
		}

		TextView viewSaldo = (TextView)getView().findViewById(R.id.viewSaldo);

		viewSaldo.setText("Média de Consumo por refeição: R$ " + format.format( cd.avgGlobal(card.getNumber()) )+
				"\n" +
				"Média de Consumo por dia: R$ " + format.format(cd.avgByDays(card.getNumber())) +
				"\n Saldo R$" + format.format(card.getTotal()));
	}
}
