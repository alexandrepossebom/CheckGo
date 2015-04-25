package com.possebom.checkgo.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.possebom.checkgo.R;
import com.possebom.checkgo.controller.CGController;
import com.possebom.checkgo.interfaces.CardCallback;
import com.possebom.checkgo.model.Card;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by alexandre on 06/02/15.
 */
public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.ViewHolder> {

    private final CardCallback cardCallback;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM", Locale.getDefault());
    private static final SimpleDateFormat SDFH = new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault());
    private final NumberFormat numberFormat;

    public CardsAdapter(CardCallback cardCallback) {
        this.cardCallback = cardCallback;
        numberFormat = NumberFormat.getInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cards_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Card card = CGController.INSTANCE.getCards().get(position);
        final Context context = holder.itemView.getContext();

        setFormattedText(holder.textViewCardNumber, R.string.label_number, card.getNumber());
        setFormattedText(holder.textViewCardTotal, R.string.label_balance, "R$" + numberFormat.format(card.getTotal()));
        setFormattedText(holder.textViewCardName, R.string.label_name, card.getName());
        setFormattedText(holder.textViewCardNextCharge, R.string.label_next_charge, card.getNextChargeFormatted(context));

        final String textLastCharge = card.getLastCharge() == null ? " -- " : SDF.format(card.getLastCharge());
        setFormattedText(holder.textViewCardLastCharge, R.string.label_last_charge, textLastCharge);

        final String textLastUpdate = card.getLastUpdate() == null ? " -- " : SDFH.format(card.getLastUpdate());
        setFormattedText(holder.textViewCardLastUpdate, R.string.label_last_update, textLastUpdate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardCallback.touched(card);
            }
        });

    }

    private static void setFormattedText(final TextView textView, final int resId, final Object arg) {
        final Resources res = textView.getResources();
        final String text = String.format(res.getString(resId), arg);
        final CharSequence styledText = Html.fromHtml(text);
        textView.setText(styledText);
    }

    @Override
    public int getItemCount() {
        return CGController.INSTANCE.getCards().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textViewCardLastUpdate;
        public final TextView textViewCardNextCharge;
        public final TextView textViewCardLastCharge;
        public final TextView textViewCardNumber;
        public final TextView textViewCardTotal;
        public final TextView textViewCardName;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewCardNumber = (TextView) itemView.findViewById(R.id.textViewCardNumber);
            textViewCardTotal = (TextView) itemView.findViewById(R.id.textViewCardTotal);
            textViewCardName = (TextView) itemView.findViewById(R.id.textViewCardName);
            textViewCardLastCharge = (TextView) itemView.findViewById(R.id.textViewCardLastCharge);
            textViewCardNextCharge = (TextView) itemView.findViewById(R.id.textViewCardNextCharge);
            textViewCardLastUpdate = (TextView) itemView.findViewById(R.id.textViewCardLastUpdate);
        }
    }
}
