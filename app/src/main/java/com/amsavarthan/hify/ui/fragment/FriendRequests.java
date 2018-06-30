package com.amsavarthan.hify.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.FriendRequestAdapter;
import com.amsavarthan.hify.models.FriendRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.tylersuehr.esr.EmptyStateRecyclerView;
import com.tylersuehr.esr.ImageTextStateDisplay;
import com.tylersuehr.esr.TextStateDisplay;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class FriendRequests extends Fragment {

    View mView;
    private List<FriendRequest> requestList;
    private FriendRequestAdapter requestAdapter;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private Query mRequestQuery;
    private EmptyStateRecyclerView mRequestView;

    public void getUsers() {
        requestList.clear();

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Friend_Requests")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()) {

                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    FriendRequest friendRequest = doc.getDocument().toObject(FriendRequest.class).withId(doc.getDocument().getId());
                                    requestList.add(friendRequest);
                                    requestAdapter.notifyDataSetChanged();

                                }

                            }
                        }else{
                            Toast.makeText(mView.getContext(), "No friend requests found.", Toast.LENGTH_SHORT).show();
                            mRequestView.invokeState(EmptyStateRecyclerView.STATE_EMPTY);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mRequestView.invokeState(EmptyStateRecyclerView.STATE_ERROR);
                        Log.w("Error", "listen:error", e);
                    }
                });

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.friends_req_frag, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mRequestView = mView.findViewById(R.id.recyclerView);

        requestList = new ArrayList<>();
        requestAdapter = new FriendRequestAdapter(requestList, view.getContext(), getActivity());

        mRequestView.setItemAnimator(new SlideInLeftAnimator());
        mRequestView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRequestView.setHasFixedSize(true);
        mRequestView.setAdapter(requestAdapter);

        mRequestView.setStateDisplay(EmptyStateRecyclerView.STATE_EMPTY,
                new TextStateDisplay(view.getContext(),"No friend requests","Add some friends to see their posts."));

        mRequestView.setStateDisplay(EmptyStateRecyclerView.STATE_LOADING,
                new TextStateDisplay(view.getContext(),"There are some friend request","We are getting information of those users.."));

        mRequestView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR,
                new TextStateDisplay(view.getContext(),"Sorry for inconvenience","Something went wrong :("));

        getUsers();

    }
}
