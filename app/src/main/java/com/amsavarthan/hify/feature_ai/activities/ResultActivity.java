package com.amsavarthan.hify.feature_ai.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.feature_ai.adapter.SolutionAdapter;
import com.amsavarthan.hify.feature_ai.api.WolframAlphaAPI;
import com.amsavarthan.hify.feature_ai.models.Solution;
import com.amsavarthan.hify.feature_ai.utils.RecentsDatabase;
import com.amsavarthan.hify.feature_ai.utils.Utils;
import com.amsavarthan.hify.ui.activities.post.PostText;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class ResultActivity extends AppCompatActivity {

    private String output,type;
    private String TAG=ResultActivity.class.getSimpleName();

    private SearchTask searchTask;
    private SolutionAdapter solutionAdapter;
    private RecyclerView recyclerView;
    private LinearLayout layout;
    private TextToSpeech textToSpeech;

    private final int QUERY = 1;
    private RecentsDatabase recentsDatabase;
    private String search_input;


    public static void startActivity(Context context,String output){
        context.startActivity(new Intent(context,ResultActivity.class).putExtra("output",output));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/bold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        setContentView(R.layout.activity_result);

        Toolbar toolbar=findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recentsDatabase=new RecentsDatabase(this);

        recyclerView=findViewById(R.id.recyclerView);
        layout=findViewById(R.id.layout);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        output = getIntent().getStringExtra("output");
        type= getIntent().getStringExtra("type");

        if(StringUtils.isNotEmpty(output)){

            recentsDatabase.insertQuery(output);
            initiateSearch(output, QUERY);

        }
    }

    private class SearchTask extends AsyncTask<String, Void, ArrayList<Solution>> {

        Context context;
        ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            context=ResultActivity.this;

            mDialog=new ProgressDialog(context);
            mDialog.setTitle("Searching...");
            mDialog.setMessage("Searching in my brain for the answers, Please wait....");
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

        }

        @Override
        protected ArrayList<Solution> doInBackground(String... params) {

            if (!Utils.isNetworkAvailable(context))
                return null;

            int searchType = Integer.parseInt(params[1]);

            if (searchType == QUERY && !StringUtils.isEmpty(params[0])) {

                return WolframAlphaAPI.getQueryResult(params[0]);

            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Solution> solutions) {
            super.onPostExecute(solutions);

            mDialog.dismiss();

            if (solutions != null && solutions.size() > 0) {
                populateResult(solutions);
            } else if (!Utils.isNetworkAvailable(context)) {
                new MaterialDialog.Builder(ResultActivity.this)
                        .title("Apologies")
                        .content(getString(R.string.error_network_not_available))
                        .positiveText("Ok")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                       .show();
            } else {
                new MaterialDialog.Builder(ResultActivity.this)
                        .title("Apologies")
                        .content("Sorry i don't know about that, But you can ask it to your friends")
                        .positiveText("Ask it in forum")
                        .negativeText("Add as post")
                        .neutralText("Do nothing")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                finish();
                                startActivity(new Intent(ResultActivity.this,AddQuestion.class).putExtra("question",search_input));
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                finish();
                                PostText.startActivity(ResultActivity.this,search_input);
                            }
                        })
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                finish();
                            }
                        }).show();

            }
        }
    }

    private void initiateSearch(String query, int searchType) {

        if (searchTask != null) {
            searchTask.cancel(true);
            searchTask = null;
        }

        search_input=query;

        String[] queryParameter = {query, searchType + ""};
        searchTask = new SearchTask();
        searchTask.execute(queryParameter);
    }

    private void populateResult(ArrayList<Solution> solutions) {

        solutionAdapter = new SolutionAdapter(solutions);
        recyclerView.setAdapter(solutionAdapter);

        String mainResult = null;

        try {
            mainResult = solutions.get(1).getDescription();
        } catch (Exception e) {
            mainResult = solutions.get(0).getDescription();
        }

        if (StringUtils.isNotEmpty(mainResult)) {

            stopTextToSpeech();
            textToSpeech.speak(mainResult, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY).replace(" ","");
            initiateSearch(query, QUERY);
        }
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });
    }

    private void stopTextToSpeech() {
        if (textToSpeech != null && textToSpeech.isSpeaking())
            textToSpeech.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        initTextToSpeech();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (searchTask != null) {
            searchTask.cancel(true);
            searchTask = null;
        }
    }

}
