package com.amsavarthan.hify.ui.activities.post;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.PostsAdapter;
import com.amsavarthan.hify.models.Post;
import com.amsavarthan.hify.ui.activities.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.tylersuehr.esr.EmptyStateRecyclerView;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SinglePostView extends AppCompatActivity {

    private List<Post> mPostsList;
    private PostsAdapter mAdapter;
    private View statsheetView;
    private BottomSheetDialog mmBottomSheetDialog;
    private ProgressBar pbar;
    private FirebaseFirestore mFirestore;



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
        setContentView(R.layout.activity_single_post_view);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/bold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        String post_id=getIntent().getStringExtra("post_id");

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Post");

        getSupportActionBar().setTitle("Post");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(!TextUtils.isEmpty(post_id)){

            boolean forComment=getIntent().getBooleanExtra("forComment",false);

            pbar=findViewById(R.id.pbar);
            mFirestore=FirebaseFirestore.getInstance();

            statsheetView = getLayoutInflater().inflate(R.layout.stat_bottom_sheet_dialog, null);
            mmBottomSheetDialog = new BottomSheetDialog(this);
            mmBottomSheetDialog.setContentView(statsheetView);
            mmBottomSheetDialog.setCanceledOnTouchOutside(true);

            mPostsList = new ArrayList<>();

            if(forComment)
                mAdapter = new PostsAdapter(mPostsList, this,this,mmBottomSheetDialog,statsheetView,true);
            else
                mAdapter = new PostsAdapter(mPostsList, this,this,mmBottomSheetDialog,statsheetView,false);


            RecyclerView mRecyclerView=findViewById(R.id.recyclerView);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(mAdapter);

            pbar.setVisibility(View.VISIBLE);
            getPosts(post_id);



        }else{
            finish();
        }

    }

    private void getPosts(final String post_id) {

        mFirestore.collection("Posts")
                .document(post_id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(!documentSnapshot.exists()){
                            Toast.makeText(SinglePostView.this, "The post does not exist.", Toast.LENGTH_SHORT).show();
                        }else{

                            Post post = new Post(documentSnapshot.getString("userId"),documentSnapshot.getString("name"),documentSnapshot.getString("timestamp"),documentSnapshot.getString("likes"),documentSnapshot.getString("favourites"),documentSnapshot.getString("description"),documentSnapshot.getString("color"),documentSnapshot.getString("username"),documentSnapshot.getString("userimage"),Integer.parseInt(String.valueOf(documentSnapshot.get("image_count"))),documentSnapshot.getString("image_url_0"),documentSnapshot.getString("image_url_1"),documentSnapshot.getString("image_url_2"),documentSnapshot.getString("image_url_3"),documentSnapshot.getString("image_url_4"),documentSnapshot.getString("image_url_5"),documentSnapshot.getString("image_url_6")).withId(post_id);
                            mPostsList.add(post);
                            mAdapter.notifyDataSetChanged();
                            pbar.setVisibility(View.GONE);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(SinglePostView.this, "Some error occured opening the post", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

    }
}
