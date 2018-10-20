package com.amsavarthan.hify.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.renderscript.Matrix2f;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.PostsAdapter;
import com.amsavarthan.hify.models.Post;
import com.amsavarthan.hify.ui.activities.MainActivity;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.tylersuehr.esr.EmptyStateRecyclerView;
import com.tylersuehr.esr.ImageTextStateDisplay;
import com.tylersuehr.esr.TextStateDisplay;

import java.util.ArrayList;
import java.util.List;

import static com.amsavarthan.hify.ui.activities.MainActivity.currentuser;
import static com.amsavarthan.hify.ui.activities.MainActivity.showFragment;
import static com.amsavarthan.hify.ui.activities.MainActivity.toolbar;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class Dashboard extends Fragment {

    List<Post> mPostsList;
    Query mQuery;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    EmptyStateRecyclerView mPostsRecyclerView;
    PostsAdapter mAdapter;
    View mView;
    private List<String> mFriendIdList=new ArrayList<>();
    private View statsheetView;
    private BottomSheetDialog mmBottomSheetDialog;
    private ProgressBar pbar;
    private CardView request_alert;
    private TextView request_alert_text;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.dashboard_fragment, container, false);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        pbar=view.findViewById(R.id.pbar);
        request_alert=view.findViewById(R.id.friend_req_alert);
        request_alert_text=view.findViewById(R.id.friend_req_alert_text);

        request_alert.setVisibility(View.GONE);

        statsheetView = ((AppCompatActivity)getActivity()).getLayoutInflater().inflate(R.layout.stat_bottom_sheet_dialog, null);
        mmBottomSheetDialog = new BottomSheetDialog(view.getContext());
        mmBottomSheetDialog.setContentView(statsheetView);
        mmBottomSheetDialog.setCanceledOnTouchOutside(true);

        mPostsList = new ArrayList<>();
        mAdapter = new PostsAdapter(mPostsList, view.getContext(),getActivity(),mmBottomSheetDialog,statsheetView);
        mPostsRecyclerView = view.findViewById(R.id.posts_recyclerview);

        mPostsRecyclerView.setItemAnimator(new DefaultItemAnimator());
       // mPostsRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPostsRecyclerView.setHasFixedSize(true);
        mPostsRecyclerView.setAdapter(mAdapter);

        mPostsRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_EMPTY,
                new ImageTextStateDisplay(view.getContext(),R.mipmap.no_posts,"No posts found","Your friends haven't added any posts or you don't have any friends."));

        mPostsRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR,
                new ImageTextStateDisplay(view.getContext(),R.mipmap.sad,"Sorry for inconvenience","Something went wrong :("));


        pbar.setVisibility(View.VISIBLE);
        getPosts();

        checkFriendRequest();

    }

    public void checkFriendRequest(){

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Friend_Requests")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            request_alert_text.setText(String.format(getString(R.string.you_have_d_new_friend_request_s),queryDocumentSnapshots.size()));
                            request_alert.setVisibility(View.VISIBLE);
                            request_alert.setAlpha(0.0f);

                            request_alert.animate()
                                    .setDuration(300)
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .alpha(1.0f)
                                    .start();

                            request_alert.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(currentuser.isEmailVerified()) {
                                        toolbar.setTitle("Manage Friends");
                                        try {
                                            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Manage Friends");
                                        } catch (Exception e) {
                                            Log.e("Error", e.getMessage());
                                        }
                                        showFragment(FriendsFragment.newInstance("request"));
                                    }else{
                                        showDialog();
                                    }
                                }
                            });

                        }
                    }
                });

    }


    public void getPosts() {


        mFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    mFirestore.collection("Users")
                                            .document(currentUser.getUid())
                                            .collection("Friends")
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot querySnapshot) {

                                                    if(!querySnapshot.isEmpty()) {

                                                        for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {

                                                            if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                                                if (documentChange.getDocument().getId().equals(doc.getDocument().get("userId"))) {

                                                                        Post post = doc.getDocument().toObject(Post.class).withId(doc.getDocument().getId());
                                                                        mPostsList.add(post);
                                                                        mAdapter.notifyDataSetChanged();
                                                                        pbar.setVisibility(View.GONE);

                                                                }
                                                                mAdapter.notifyDataSetChanged();
                                                            }
                                                            mAdapter.notifyDataSetChanged();
                                                        }

                                                        if(mPostsList.isEmpty()){
                                                            pbar.setVisibility(View.GONE);
                                                        }

                                                    }else{

													pbar.setVisibility(View.GONE);
                                                        if(mPostsList.isEmpty()){
                                                            pbar.setVisibility(View.GONE);
                                                        }
                                                    }

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    pbar.setVisibility(View.GONE);
                                                    mPostsRecyclerView.invokeState(EmptyStateRecyclerView.STATE_ERROR);
                                                    Log.w("Error", "listen:error", e);
                                                }
                                            });

                                }

                            }

                        }else{
                            if(mPostsList.isEmpty())
							{
                                pbar.setVisibility(View.GONE);
                            }
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pbar.setVisibility(View.GONE);
                        mPostsRecyclerView.invokeState(EmptyStateRecyclerView.STATE_ERROR);
                        Log.w("Error", "listen:error", e);
                    }
                });

    }


    public void showDialog(){

        new BottomDialog.Builder(mView.getContext())
                .setTitle("Information")
                .setContent("Email has not been verified, please verify and continue.")
                .setPositiveText("Send again")
                .setPositiveBackgroundColorResource(R.color.colorAccentt)
                .setCancelable(true)
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull final BottomDialog dialog) {
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dialog.dismiss();
                                        Toast.makeText(mView.getContext(), "Verification email sent", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Error", e.getMessage());
                                    }
                                });
                    }
                })
                .setNegativeText("Ok")
                .onNegative(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .show();

    }


}
