package com.amsavarthan.hify.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.PostsAdapter;
import com.amsavarthan.hify.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.amsavarthan.hify.ui.activities.MainActivity.currentuser;
import static com.amsavarthan.hify.ui.activities.MainActivity.showFragment;
import static com.amsavarthan.hify.ui.activities.MainActivity.toolbar;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class Dashboard extends Fragment {

    private List<Post> mPostsList;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private RecyclerView mPostsRecyclerView;
    private View mView;
    private List<String> mFriendIdList=new ArrayList<>();
    private View statsheetView;
    private BottomSheetDialog mmBottomSheetDialog;
    private SwipeRefreshLayout refreshLayout;
    private CardView request_alert;
    private TextView request_alert_text;
    private DocumentSnapshot lastVisible;
    private boolean isFirstPageFirstLoad=true;
    private PostsAdapter mAdapter_v19;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_dashboard, container, false);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter_v19.notifyDataSetChanged();

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        refreshLayout=view.findViewById(R.id.refreshLayout);
        request_alert=view.findViewById(R.id.friend_req_alert);
        request_alert_text=view.findViewById(R.id.friend_req_alert_text);

        request_alert.setVisibility(View.GONE);

        statsheetView = ((AppCompatActivity)getActivity()).getLayoutInflater().inflate(R.layout.stat_bottom_sheet_dialog, null);
        mmBottomSheetDialog = new BottomSheetDialog(view.getContext());
        mmBottomSheetDialog.setContentView(statsheetView);
        mmBottomSheetDialog.setCanceledOnTouchOutside(true);
        mPostsRecyclerView = view.findViewById(R.id.posts_recyclerview);

        mPostsList = new ArrayList<>();

        mAdapter_v19 = new PostsAdapter(mPostsList, view.getContext(), getActivity(), mmBottomSheetDialog, statsheetView, false);
        mPostsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPostsRecyclerView.setHasFixedSize(true);
        mPostsRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(),DividerItemDecoration.VERTICAL));
        mPostsRecyclerView.setAdapter(mAdapter_v19);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mPostsList.clear();
                mAdapter_v19.notifyDataSetChanged();
                getAllPosts();
                checkFriendRequest();

            }
        });

        getAllPosts();

        checkFriendRequest();
        
    }

    public void checkFriendRequest(){

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Friend_Requests")
                .addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if(e!=null){
                            e.printStackTrace();
                            return;
                        }

                        if(!queryDocumentSnapshots.isEmpty()){
                            try {
                                request_alert_text.setText(String.format(getString(R.string.you_have_d_new_friend_request_s), queryDocumentSnapshots.size()));
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
                                        if (currentuser.isEmailVerified()) {
                                            toolbar.setTitle("Manage Friends");
                                            try {
                                                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Manage Friends");
                                            } catch (Exception e) {
                                                Log.e("Error", e.getMessage());
                                            }
                                            showFragment(FriendsFragment.newInstance("request"));
                                        } else {
                                            showDialog();
                                        }
                                    }
                                });
                            }catch (Exception e1){
                                e1.printStackTrace();
                            }
                        }

                    }
                });

    }

    public void getAllPosts() {

        mView.findViewById(R.id.default_item).setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);

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

                                                    if (!querySnapshot.isEmpty()) {

                                                        for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                                                            if (documentChange.getDocument().getId().equals(doc.getDocument().get("userId"))) {

                                                                Post post = doc.getDocument().toObject(Post.class).withId(doc.getDocument().getId());
                                                                mPostsList.add(post);
                                                                refreshLayout.setRefreshing(false);
                                                                mAdapter_v19.notifyDataSetChanged();

                                                            }
                                                        }

                                                    } else {

                                                       getCurrentUsersPosts();

                                                    }

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    refreshLayout.setRefreshing(false);
                                                    Toasty.error(mView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT,true).show();
                                                    Log.w("Error", "listen:error", e);
                                                }
                                            });

                                }
                            }


                        }else{
                            refreshLayout.setRefreshing(false);
                            mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshLayout.setRefreshing(false);
                        Toasty.error(mView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT,true).show();
                        Log.w("Error", "listen:error", e);
                    }
                });

    }

    private void getCurrentUsersPosts() {

        mFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(queryDocumentSnapshots.isEmpty()){

                            refreshLayout.setRefreshing(false);
                            mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);

                        }else{

                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                if (documentChange.getDocument().getString("userId").equals(mAuth.getCurrentUser().getUid())) {

                                    Post post = documentChange.getDocument().toObject(Post.class).withId(documentChange.getDocument().getId());
                                    mPostsList.add(post);
                                    refreshLayout.setRefreshing(false);
                                    mAdapter_v19.notifyDataSetChanged();

                                }
                            }

                            if (mPostsList.isEmpty()) {
                                mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                                refreshLayout.setRefreshing(false);
                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshLayout.setRefreshing(false);
                        Toasty.error(mView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT,true).show();
                        Log.w("Error", "listen:error", e);
                    }
                });


    }

    public void showDialog(){

        new DialogSheet(mView.getContext())
                .setTitle("Information")
                .setMessage("Email has not been verified, please verify and continue.")
                .setPositiveButton("Send again", v -> mAuth.getCurrentUser().sendEmailVerification()
                        .addOnSuccessListener(aVoid -> Toasty.success(mView.getContext(), "Verification email sent", Toasty.LENGTH_SHORT,true).show())
                        .addOnFailureListener(e -> Log.e("Error", e.getMessage())))
                .setNegativeButton("No", v -> {})
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .setCancelable(true)
                .show();

    }


}
