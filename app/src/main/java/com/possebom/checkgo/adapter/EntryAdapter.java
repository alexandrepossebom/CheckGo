package com.possebom.checkgo.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.possebom.checkgo.R;
import com.possebom.checkgo.model.Card;
import com.possebom.checkgo.model.Entry;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by alexandre on 06/02/15.
 */
public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.ViewHolder> {

    private final Card card;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("[dd/MM]");
    private NumberFormat numberFormat;
    private static final int COLOR_GREY = 394759;

    public EntryAdapter(Card card) {
        this.card = card;
        numberFormat = NumberFormat.getInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.entries_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Entry entry = card.getEntries().get(position);

        holder.itemView.setBackgroundColor(position % 2 == 0 ? COLOR_GREY : Color.WHITE);
        holder.textViewAmount.setText("R$" + numberFormat.format(entry.getAmount()));
        holder.textViewPlace.setText(entry.getPlace());
        holder.textViewDay.setText(SIMPLE_DATE_FORMAT.format(entry.getDay()));
    }

    @Override
    public int getItemCount() {
        return card.getEntries() == null ? 0 : card.getEntries().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewDay;
        public TextView textViewPlace;
        public TextView textViewAmount;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewDay = (TextView) itemView.findViewById(R.id.textViewDay);
            textViewPlace = (TextView) itemView.findViewById(R.id.textViewPlace);
            textViewAmount = (TextView) itemView.findViewById(R.id.textViewAmount);
        }
    }
}
