package com.amsavarthan.hify.ui.activities.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.NotificationsAdapter;
import com.amsavarthan.hify.models.Notification;
import com.amsavarthan.hify.ui.activities.MainActivity;
import com.amsavarthan.hify.ui.activities.forum.AddQuestion;
import com.amsavarthan.hify.utils.NotificationUtil;
import com.amsavarthan.hify.utils.database.NotificationsHelper;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class Notifications extends AppCompatActivity {

    private List<Notification> notificationsList;
    private NotificationsAdapter notificationsAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout refreshLayout;
    private NotificationsHelper notificationsHelper;

    public void getNotifications() {
        notificationsList.clear();
        notificationsAdapter.notifyDataSetChanged();

        findViewById(R.id.default_item).setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);


        if(notificationsHelper.getCount()==0){
            findViewById(R.id.default_item).setVisibility(View.VISIBLE);
            refreshLayout.setRefreshing(false);
            return;
        }

        notificationsList.addAll(notificationsHelper.getAllNotifications());
        refreshLayout.setRefreshing(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_notifications, menu);

        return super.onCreateOptionsMenu(menu);    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:

                new DialogSheet(this)
                        .setTitle("Clear all")
                        .setMessage("Are you sure do you want to clear all notifications?")
                        .setRoundedCorners(true)
                        .setColoredNavigationBar(true)
                        .setCancelable(true)
                        .setPositiveButton("Yes", v -> {

                            deleteAll();

                        })
                        .setNegativeButton("No", v -> {

                        })
                        .show();

               return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAll() {

        notificationsHelper.deleteAll();
        getNotifications();
        Toasty.success(this,"Notifications cleared",Toasty.LENGTH_SHORT,true).show();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView = findViewById(R.id.recyclerView);
        refreshLayout=findViewById(R.id.refreshLayout);
        notificationsHelper=new NotificationsHelper(this);

        notificationsList = new ArrayList<>();
        notificationsAdapter = new NotificationsAdapter(notificationsList, this,notificationsHelper);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(notificationsAdapter);

        refreshLayout.setOnRefreshListener(() -> getNotifications());

        NotificationUtil.read=true;
        getNotifications();

    }
}
