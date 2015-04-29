package com.possebom.checkgo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.snowdream.android.util.Log;
import com.melnykov.fab.FloatingActionButton;
import com.possebom.checkgo.adapter.EntryAdapter;
import com.possebom.checkgo.controller.CGController;
import com.possebom.checkgo.model.Card;


public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private long cardNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_history));

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null && getIntent().hasExtra("cardNumber")) {
            final Bundle bundle = getIntent().getExtras();
            cardNumber = bundle.getLong("cardNumber");
            final Card card = CGController.INSTANCE.getCard(cardNumber);
            mRecyclerView.setAdapter(new EntryAdapter(card));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View v) {
        Log.d("delete card : " + cardNumber);
        final Card card = CGController.INSTANCE.getCard(cardNumber);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        builder.setTitle(R.string.delete_card_title);
        builder.setMessage(R.string.delete_card_body);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (card != null) {
                    CGController.INSTANCE.getCards().remove(card);
                    CGController.INSTANCE.save(getApplicationContext());
                    finish();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();


    }
}
