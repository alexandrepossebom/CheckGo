package com.possebom.checkgo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.possebom.checkgo.adapter.CardsAdapter;
import com.possebom.checkgo.controller.CGController;
import com.possebom.checkgo.interfaces.CardCallback;
import com.possebom.checkgo.model.Card;
import com.possebom.checkgo.util.UpdateCards;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, UpdateCards.UpdateInterface, CardCallback {

    private final CardsAdapter mAdapter = new CardsAdapter(this);
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                UpdateCards.start(MainActivity.this, MainActivity.this);
            }
        });

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.add_new_card)
                .customView(R.layout.dialog_new_card, true)
                .autoDismiss(false)
                .cancelable(false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(final MaterialDialog dialog) {
                        final EditText editTextCardName = (EditText) dialog.getCustomView().findViewById(R.id.editTextCardName);
                        final EditText editTextCardNumber = (EditText) dialog.getCustomView().findViewById(R.id.editTextCardNumber);

                        if (TextUtils.isEmpty(editTextCardName.getText())) {
                            editTextCardName.setError("Invalid card name");
                            return;
                        }

                        final long cardNumber;
                        if (editTextCardNumber.getText().length() != 16) {
                            editTextCardNumber.setError("Invalid card number");
                            return;
                        }

                        try {
                            cardNumber = Long.parseLong(editTextCardNumber.getText().toString());
                        } catch (final NumberFormatException e) {
                            editTextCardNumber.setError("Invalid card number");
                            return;
                        }

                        final String cardName = editTextCardName.getText().toString();

                        final Card card = new Card(cardName, cardNumber);
                        CGController.INSTANCE.getCards().add(card);
                        CGController.INSTANCE.save(getApplicationContext());
                        mAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(final MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                }).build();
        dialog.show();
    }

    @Override
    public void updateSuccess() {
        if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateError() {
        if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(getApplicationContext(), "Error updating", Toast.LENGTH_LONG).show();
    }

    @Override
    public void touched(final Card card) {
        final Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra("cardNumber", card.getNumber());
        startActivity(intent);
    }
}
