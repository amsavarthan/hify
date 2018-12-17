package com.amsavarthan.hify.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.PostsAdapter;
import com.amsavarthan.hify.adapters.PostsAdapter_v19;
import com.amsavarthan.hify.models.Post;
import com.amsavarthan.hify.ui.activities.MainActivity;
import com.amsavarthan.hify.utils.database.UserHelper;
import com.github.javiersantos.bottomdialogs.BottomDialog;
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

    private List<Post> mPostsList;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EmptyStateRecyclerView mPostsRecyclerView;
    private PostsAdapter mAdapter;
    private View mView;
    private List<String> mFriendIdList=new ArrayList<>();
    private View statsheetView;
    private BottomSheetDialog mmBottomSheetDialog;
    private ProgressBar pbar;
    private CardView request_alert;
    private TextView request_alert_text;
    private DocumentSnapshot lastVisible;
    private boolean isFirstPageFirstLoad=true;
    private PostsAdapter_v19 mAdapter_v19;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.dashboard_fragment, container, false);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(Build.VERSION.SDK_INT<=19) {
            mAdapter_v19.notifyDataSetChanged();
        }else{
            mAdapter.notifyDataSetChanged();
        }
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
        mPostsRecyclerView = view.findViewById(R.id.posts_recyclerview);

        mPostsList = new ArrayList<>();
        if(Build.VERSION.SDK_INT<=19) {

            mAdapter_v19 = new PostsAdapter_v19(mPostsList, view.getContext(), getActivity(), mmBottomSheetDialog, statsheetView, false);
            mPostsRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mPostsRecyclerView.setHasFixedSize(true);
            mPostsRecyclerView.setAdapter(mAdapter_v19);

        }else{

            mAdapter = new PostsAdapter(mPostsList, view.getContext(), getActivity(), mmBottomSheetDialog, statsheetView, false);
            mPostsRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mPostsRecyclerView.setHasFixedSize(true);
            mPostsRecyclerView.setAdapter(mAdapter);

        }

        mPostsRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_EMPTY,
                new TextStateDisplay(view.getContext(),"No posts found","Your friends haven't added any posts or you don't have any friends."));

        mPostsRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR,
                new TextStateDisplay(view.getContext(),"Sorry for inconvenience","Something went wrong :("));

       /* mPostsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                boolean reachedBottom=!recyclerView.canScrollVertically(1);
                if(reachedBottom){
                    loadMorePost();
                    Toast.makeText(mView.getContext(), "Getting next posts....", Toast.LENGTH_LONG).show();
                }

            }
        });*/

        pbar.setVisibility(View.VISIBLE);
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

        mFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if(e!=null){
                            pbar.setVisibility(View.GONE);
                            mPostsRecyclerView.invokeState(EmptyStateRecyclerView.STATE_ERROR);
                            Log.w("Error", "listen:error", e);
                            return;
                        }

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

                                                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                                                    if (documentChange.getDocument().getId().equals(doc.getDocument().get("userId"))) {

                                                                        Post post = doc.getDocument().toObject(Post.class).withId(doc.getDocument().getId());
                                                                        mPostsList.add(post);
                                                                        pbar.setVisibility(View.GONE);
                                                                        if(Build.VERSION.SDK_INT<=19) {
                                                                            mAdapter_v19.notifyDataSetChanged();
                                                                        }else{
                                                                            mAdapter.notifyDataSetChanged();
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            if (mPostsList.isEmpty()) {
                                                                pbar.setVisibility(View.GONE);
                                                            }

                                                        } else {

                                                            pbar.setVisibility(View.GONE);

                                                            if (mPostsList.isEmpty()) {
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
                });

    }

    public void getPosts() {

        mFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(7)
                .addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if(e!=null){
                            pbar.setVisibility(View.GONE);
                            mPostsRecyclerView.invokeState(EmptyStateRecyclerView.STATE_ERROR);
                            Log.w("Error", "listen:error", e);
                            return;
                        }

                        if (!queryDocumentSnapshots.isEmpty()) {

                            if(isFirstPageFirstLoad){
                                lastVisible=queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size()-1);
                                mPostsList.clear();
                            }

                            for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    if(isFirstPageFirstLoad) {

                                        mFirestore.collection("Users")
                                                .document(currentUser.getUid())
                                                .collection("Friends")
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot querySnapshot) {

                                                        if (!querySnapshot.isEmpty()) {

                                                            for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {

                                                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                                                    if (documentChange.getDocument().get("id").equals(doc.getDocument().get("userId"))) {

                                                                        Post post = doc.getDocument().toObject(Post.class).withId(doc.getDocument().getId());
                                                                        mPostsList.add(post);
                                                                        if(Build.VERSION.SDK_INT<=19) {
                                                                            mAdapter_v19.notifyDataSetChanged();
                                                                        }else{
                                                                            mAdapter.notifyDataSetChanged();
                                                                        }
                                                                        pbar.setVisibility(View.GONE);

                                                                    }
                                                                }

                                                            }

                                                            if (mPostsList.isEmpty()) {
                                                                pbar.setVisibility(View.GONE);
                                                            }

                                                        } else {

                                                            pbar.setVisibility(View.GONE);
                                                            if (mPostsList.isEmpty()) {
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

                                    }else{

                                        mFirestore.collection("Users")
                                                .document(currentUser.getUid())
                                                .collection("Friends")
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot querySnapshot) {

                                                        if (!querySnapshot.isEmpty()) {

                                                            for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {

                                                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                                                    if (documentChange.getDocument().getId().equals(doc.getDocument().get("userId"))) {

                                                                        Post post = doc.getDocument().toObject(Post.class).withId(doc.getDocument().getId());
                                                                        mPostsList.add(0,post);
                                                                        pbar.setVisibility(View.GONE);
                                                                        if(Build.VERSION.SDK_INT<=19) {
                                                                            mAdapter_v19.notifyDataSetChanged();
                                                                        }else{
                                                                            mAdapter.notifyDataSetChanged();
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            if (mPostsList.isEmpty()) {
                                                                pbar.setVisibility(View.GONE);
                                                            }

                                                        } else {

                                                            pbar.setVisibility(View.GONE);

                                                            if (mPostsList.isEmpty()) {
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

                            }
                            isFirstPageFirstLoad=false;

                        }else{
                            if(mPostsList.isEmpty())
                            {
                                pbar.setVisibility(View.GONE);
                            }
                        }


                    }
                });

    }

    public void loadMorePost() {

        mFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(5)
                .addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if(e!=null){
                            pbar.setVisibility(View.GONE);
                            mPostsRecyclerView.invokeState(EmptyStateRecyclerView.STATE_ERROR);
                            Log.w("Error", "listen:error", e);
                            return;
                        }

                        if (!queryDocumentSnapshots.isEmpty()) {

                            lastVisible=queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size()-1);
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

                                                            if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                                                if (documentChange.getDocument().getId().equals(doc.getDocument().get("userId"))) {

                                                                    Post post = doc.getDocument().toObject(Post.class).withId(doc.getDocument().getId());
                                                                    mPostsList.add(post);
                                                                    if(Build.VERSION.SDK_INT<=19) {
                                                                        mAdapter_v19.notifyDataSetChanged();
                                                                    }else{
                                                                        mAdapter.notifyDataSetChanged();
                                                                    }
                                                                    pbar.setVisibility(View.GONE);

                                                                }
                                                            }
                                                        }



                                                        if (mPostsList.isEmpty()) {
                                                            pbar.setVisibility(View.GONE);
                                                        }


                                                    } else {

                                                        pbar.setVisibility(View.GONE);
                                                        if (mPostsList.isEmpty()) {
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
