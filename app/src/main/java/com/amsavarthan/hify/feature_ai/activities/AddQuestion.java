package com.amsavarthan.hify.feature_ai.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddQuestion extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText question;
    String subject;
    FirebaseFirestore mFirestore;
    FirebaseUser mCurrentUser;
    private ProgressDialog mDialog;
    String question_intent;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return super.onSupportNavigateUp();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/bold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        Toolbar toolbar=findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mFirestore=FirebaseFirestore.getInstance();
        mCurrentUser=FirebaseAuth.getInstance().getCurrentUser();
        question_intent=getIntent().getStringExtra("question");

        Spinner spinner = findViewById(R.id.spinner);
        question=findViewById(R.id.question);

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,R.layout.spinner_item,getResources().getStringArray(R.array.subject));
        arrayAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        spinner.setAdapter(arrayAdapter);

        if(!TextUtils.isEmpty(question_intent)){
            question.setText(question_intent);
        }else{
            question.setText("");
        }

        spinner.setOnItemSelectedListener(this);

        mDialog=new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        subject = parent.getItemAtPosition(position).toString();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        Snackbar.make(findViewById(R.id.layout),"Select a subject",Snackbar.LENGTH_SHORT).show();
    }

    public void sendQuestion(View view) {

        if(mCurrentUser!=null) {


            if (TextUtils.isEmpty(question.getText().toString())) {
                Snackbar.make(findViewById(R.id.layout), "Question empty", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(subject)) {
                Snackbar.make(findViewById(R.id.layout), "Select a subject", Snackbar.LENGTH_SHORT).show();
                return;
            }

            mDialog.show();

            mFirestore.collection("Users")
                    .document(mCurrentUser.getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            Map<String,Object> questionMap=new HashMap<>();
                            questionMap.put("name",documentSnapshot.getString("name"));
                            questionMap.put("id",documentSnapshot.getString("id"));
                            questionMap.put("question",question.getText().toString());
                            questionMap.put("subject",subject);
                            questionMap.put("timestamp",String.valueOf(System.currentTimeMillis()));

                            mFirestore.collection("Questions")
                                    .add(questionMap)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(AddQuestion.this, "Question added", Toast.LENGTH_SHORT).show();
                                            mDialog.dismiss();
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mDialog.dismiss();
                                            Log.e("AddQuestion",e.getLocalizedMessage());
                                        }
                                    });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Log.e("AddQuestion",e.getLocalizedMessage());
                        }
                    });

        }

    }
}
