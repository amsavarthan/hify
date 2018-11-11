package com.amsavarthan.hify.feature_ai.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.feature_ai.adapter.AnswersAdapter;
import com.amsavarthan.hify.feature_ai.models.Answers;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AnswersActivity extends AppCompatActivity {

    private static final String TAG = AnswersActivity.class.getSimpleName();
    String author_id,author,question,timestamp,doc_id;
    TextView author_textview,question_textview;
    EditText answer;
    RecyclerView mRecyclerView;
    FirebaseFirestore mFirestore;
    FirebaseUser mCurrentUser;
    AnswersAdapter adapter;
    List<Answers> answers=new ArrayList<>();
    private String answered_by;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }

    public static void startActivity(Context context,String question_id){
        context.startActivity(new Intent(context,AnswersActivity.class).putExtra("question_id",question_id));
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
        setContentView(R.layout.activity_answers);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Forum");

        getSupportActionBar().setTitle("Forum");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirestore=FirebaseFirestore.getInstance();
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        mRecyclerView=findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(StringUtils.isNotEmpty(getIntent().getStringExtra("question_id"))){

            Log.i(TAG,getIntent().getStringExtra("question_id"));

            mFirestore.collection("Questions")
                    .document(getIntent().getStringExtra("question_id"))
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            author_id = documentSnapshot.getString("id");
                            author = documentSnapshot.getString("name");
                            doc_id = documentSnapshot.getId();
                            timestamp = documentSnapshot.getString("timestamp");
                            answered_by = documentSnapshot.getString("id");
                            question = documentSnapshot.getString("question");
                            Log.i(TAG,"timestamp: "+timestamp);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });

        }else{

            author_id=getIntent().getStringExtra("user_id");
            author=getIntent().getStringExtra("author");
            doc_id=getIntent().getStringExtra("doc_id");
            timestamp=getIntent().getStringExtra("timestamp");
            answered_by=getIntent().getStringExtra("answered_by");
            question=getIntent().getStringExtra("question");

        }

        adapter=new AnswersAdapter(answers,author_id, doc_id, "Questions", answered_by);
        mRecyclerView.setAdapter(adapter);

        author_textview=findViewById(R.id.auth_sub);
        question_textview=findViewById(R.id.question);
        answer=findViewById(R.id.answer);

        question_textview.setText(question);
        
        author_textview.setText(String.format("Asked by %s ( %s )", author, TimeAgo.using(Long.parseLong(timestamp))));
        toolbar.setSubtitle("Asked by " + author + " ( " + TimeAgo.using(Long.parseLong(timestamp)) + " )");
        getSupportActionBar().setSubtitle("Asked by " + author + " ( " + TimeAgo.using(Long.parseLong(timestamp)) + " )");
        
        mFirestore.collection("Questions")
                .document(doc_id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(!TextUtils.isEmpty(documentSnapshot.getString("answered_by"))){
                            answer.setEnabled(false);
                            answer.setHint("Question closed by "+author);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("error",e.getLocalizedMessage());
                    }
                });

        getAnswers();

    }

    private void getAnswers() {

        mFirestore.collection("Answers")
                .whereEqualTo("question_id",doc_id)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(e!=null){
                            Log.e(TAG,e.getLocalizedMessage());
                            return;
                        }

                        for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                            if(doc.getType()== DocumentChange.Type.ADDED){


                                    Answers answer = doc.getDocument().toObject(Answers.class).withId(doc.getDocument().getId());
                                    if (!TextUtils.isEmpty(doc.getDocument().getString("is_answer"))) {
                                        answers.add(0, answer);
                                    } else {
                                        answers.add(answer);
                                    }
                                    adapter.notifyDataSetChanged();


                            }

                        
                        }

                    }
                });

    }

    public void sendAnswer(View view) {
        if(!TextUtils.isEmpty(answer.getText().toString())) {

            final ProgressDialog mDialog=new ProgressDialog(this);
            mDialog.setMessage("Please wait....");
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();

           mFirestore.collection("Users")
                   .document(mCurrentUser.getUid())
                   .get()
                   .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                       @Override
                       public void onSuccess(final DocumentSnapshot documentSnapshot) {
                           Map<String,Object> answerMap=new HashMap<>();
                           answerMap.put("user_id",documentSnapshot.getString("id"));
                           answerMap.put("name",documentSnapshot.getString("name"));
                           answerMap.put("timestamp",String.valueOf(System.currentTimeMillis()));
                           answerMap.put("answer",answer.getText().toString());
						   answerMap.put("question_id",doc_id);
                           answerMap.put("is_answer","no");
                           answerMap.put("answered_by", "");
                           answerMap.put("answered_by_id", "");

                           mFirestore.collection("Answers")
                                   .add(answerMap)
                                   .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                       @Override
                                       public void onSuccess(DocumentReference documentReference) {

                                           Map<String, Object> notificationMap = new HashMap<>();
                                           notificationMap.put("answered_user_id",documentSnapshot.getString("id"));
                                           notificationMap.put("timestamp",String.valueOf(System.currentTimeMillis()));
                                           notificationMap.put("question_id",doc_id);

                                           FirebaseFirestore.getInstance()
                                                   .collection("Answered_Notifications")
                                                   .add(notificationMap)
                                                   .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                       @Override
                                                       public void onSuccess(DocumentReference documentReference) {

                                                           mDialog.dismiss();
                                                           adapter.notifyDataSetChanged();

                                                       }
                                                   });


                                       }
                                   })
                                   .addOnFailureListener(new OnFailureListener() {
                                       @Override
                                       public void onFailure(@NonNull Exception e) {
                                           mDialog.dismiss();
                                           Log.e(TAG,e.getLocalizedMessage());
                                       }
                                   });
                       }
                   })
                   .addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           mDialog.dismiss();
                           Log.e(TAG,e.getLocalizedMessage());
                       }
                   });
        }
    }
}
