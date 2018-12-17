package com.amsavarthan.hify.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.MessageImageAdapter;
import com.amsavarthan.hify.adapters.MessageImageReplyAdapter;
import com.amsavarthan.hify.adapters.MessageTextAdapter;
import com.amsavarthan.hify.adapters.MessageTextReplyAdapter;
import com.amsavarthan.hify.adapters.UsersAdapter;
import com.amsavarthan.hify.models.Message;
import com.amsavarthan.hify.models.MessageReply;
import com.amsavarthan.hify.models.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
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

import static android.view.View.GONE;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class MessageHistory extends Fragment {

    private View mView;
    private List<Message> messages;
    private List<MessageReply> messageReplies;
    private MessageImageAdapter messageImageAdapter;
    private MessageImageReplyAdapter messageImageReplyAdapter;
    private MessageTextAdapter messageTextAdapter;
    private MessageTextReplyAdapter messageTextReplyAdapter;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private EmptyStateRecyclerView mRecyclerView;
    private TextView tab_1,tab_2,tab_3,tab_4;
    private ProgressBar pbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.send_message_fragment, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        messages=new ArrayList<>();
        messageReplies=new ArrayList<>();
        messageTextAdapter=new MessageTextAdapter(messages,mView.getContext());
        messageImageAdapter=new MessageImageAdapter(messages,mView.getContext());
        messageImageReplyAdapter=new MessageImageReplyAdapter(messageReplies,mView.getContext());
        messageTextReplyAdapter=new MessageTextReplyAdapter(messageReplies,mView.getContext());

        mRecyclerView = mView.findViewById(R.id.messageList);
        pbar=mView.findViewById(R.id.pbar);
        tab_1=mView.findViewById(R.id.text);
        tab_2=mView.findViewById(R.id.text_reply);
        tab_3=mView.findViewById(R.id.image);
        tab_4=mView.findViewById(R.id.image_reply);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));

        tab_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.clear();
                pbar.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(messageTextAdapter);
                mRecyclerView.clearStateDisplays();
                mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR,
                        new TextStateDisplay(view.getContext(),"Sorry for inconvenience","Something went wrong :("));

                mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR,
                        new TextStateDisplay(view.getContext(),"No messages found",""));

                getTextMessage();
            }
        });

        tab_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageReplies.clear();
                pbar.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(messageTextReplyAdapter);
                mRecyclerView.clearStateDisplays();

                mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR,
                        new TextStateDisplay(view.getContext(),"Sorry for inconvenience","Something went wrong :("));

                mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR,
                        new TextStateDisplay(view.getContext(),"No messages found",""));

                getTextReplyMessage();
            }
        });

        tab_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.clear();
                pbar.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(messageImageAdapter);
                mRecyclerView.clearStateDisplays();

                mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR,
                        new TextStateDisplay(view.getContext(),"Sorry for inconvenience","Something went wrong :("));

                mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR,
                        new TextStateDisplay(view.getContext(),"No messages found",""));

                getImageMessage();
            }
        });

        tab_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageReplies.clear();
                pbar.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(messageImageReplyAdapter);
                mRecyclerView.clearStateDisplays();

                mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR,
                        new TextStateDisplay(view.getContext(),"Sorry for inconvenience","Something went wrong :("));

                mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR,
                        new TextStateDisplay(view.getContext(),"No messages found",""));

                getImageReplyMessage();
            }
        });

        tab_1.performClick();

    }

    public void getTextMessage(){

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                        if(e!=null){
                            mRecyclerView.invokeState(EmptyStateRecyclerView.STATE_ERROR);
                            Log.w("error","listen",e);
                            return;
                        }

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType()== DocumentChange.Type.ADDED){

                                    Message message=doc.getDocument().toObject(Message.class).withId(doc.getDocument().getId());
                                    messages.add(message);
                                    messageTextAdapter.notifyDataSetChanged();
                                    pbar.setVisibility(GONE);

                                }


                            }

                        }else {
                            pbar.setVisibility(GONE);
                            mRecyclerView.invokeState(EmptyStateRecyclerView.STATE_EMPTY);
                        }

                    }
                });

    }

    public void getTextReplyMessage(){

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Notifications_reply")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                        if(e!=null){
                            mRecyclerView.invokeState(EmptyStateRecyclerView.STATE_ERROR);
                            Log.w("error","listen",e);
                            return;
                        }

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType()== DocumentChange.Type.ADDED){

                                    MessageReply messageReply=doc.getDocument().toObject(MessageReply.class).withId(doc.getDocument().getId());
                                    messageReplies.add(messageReply);
                                    messageTextReplyAdapter.notifyDataSetChanged();
                                    pbar.setVisibility(GONE);

                                }


                            }

                        }else {
                            pbar.setVisibility(GONE);
                            mRecyclerView.invokeState(EmptyStateRecyclerView.STATE_EMPTY);
                        }


                    }
                });

    }

    public void getImageMessage(){

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Notifications_image")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                        if(e!=null){
                            mRecyclerView.invokeState(EmptyStateRecyclerView.STATE_ERROR);
                            Log.w("error","listen",e);
                            return;
                        }

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType()== DocumentChange.Type.ADDED){

                                    Message message=doc.getDocument().toObject(Message.class).withId(doc.getDocument().getId());
                                    messages.add(message);
                                    messageImageAdapter.notifyDataSetChanged();
                                    pbar.setVisibility(GONE);
                                }


                            }

                        }else {
                            pbar.setVisibility(GONE);
                            mRecyclerView.invokeState(EmptyStateRecyclerView.STATE_EMPTY);
                        }


                    }
                });

    }

    public void getImageReplyMessage(){

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Notifications_reply_image")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                        if(e!=null){
                            mRecyclerView.invokeState(EmptyStateRecyclerView.STATE_ERROR);
                            Log.w("error","listen",e);
                            return;
                        }

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType()== DocumentChange.Type.ADDED){

                                    MessageReply messageReply=doc.getDocument().toObject(MessageReply.class).withId(doc.getDocument().getId());
                                    messageReplies.add(messageReply);
                                    messageImageReplyAdapter.notifyDataSetChanged();

                                }


                            }

                        }else {
                            mRecyclerView.invokeState(EmptyStateRecyclerView.STATE_EMPTY);
                            pbar.setVisibility(GONE);
                        }


                    }
                });


    }


}
