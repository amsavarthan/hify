package com.amsavarthan.hify.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.tylersuehr.esr.EmptyStateRecyclerView;
import com.tylersuehr.esr.ImageTextStateDisplay;
import com.tylersuehr.esr.TextStateDisplay;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FlipInTopXAnimator;

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
        tab_1=mView.findViewById(R.id.text);
        tab_2=mView.findViewById(R.id.text_reply);
        tab_3=mView.findViewById(R.id.image);
        tab_4=mView.findViewById(R.id.image_reply);

        mRecyclerView.setItemAnimator(new FlipInTopXAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));

        tab_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.clear();
                mRecyclerView.setAdapter(messageTextAdapter);
                getTextMessage();
            }
        });

        tab_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageReplies.clear();
                mRecyclerView.setAdapter(messageTextReplyAdapter);
                getTextReplyMessage();
            }
        });

        tab_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.clear();
                mRecyclerView.setAdapter(messageImageAdapter);
                getImageMessage();
            }
        });

        tab_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageReplies.clear();
                mRecyclerView.setAdapter(messageImageReplyAdapter);
                getImageReplyMessage();
            }
        });

        tab_1.performClick();

    }

    public void getTextMessage(){

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Notifications")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                        if(e!=null){
                            Log.w("error","listen",e);
                            return;
                        }

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType()== DocumentChange.Type.ADDED){

                                    Message message=doc.getDocument().toObject(Message.class);
                                    messages.add(message);
                                    messageTextAdapter.notifyDataSetChanged();

                                }

                                messageTextAdapter.notifyDataSetChanged();

                            }

                        }else
                            Toast.makeText(mView.getContext(), "No messages found.", Toast.LENGTH_SHORT).show();


                    }
                });

    }

    public void getTextReplyMessage(){

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Notifications_reply")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                        if(e!=null){
                            Log.w("error","listen",e);
                            return;
                        }

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType()== DocumentChange.Type.ADDED){

                                    MessageReply messageReply=doc.getDocument().toObject(MessageReply.class);
                                    messageReplies.add(messageReply);
                                    messageTextReplyAdapter.notifyDataSetChanged();

                                }

                                messageTextReplyAdapter.notifyDataSetChanged();

                            }

                        }else
                            Toast.makeText(mView.getContext(), "No replies found.", Toast.LENGTH_SHORT).show();



                    }
                });

    }

    public void getImageMessage(){

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Notifications_image")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                        if(e!=null){
                            Log.w("error","listen",e);
                            return;
                        }

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType()== DocumentChange.Type.ADDED){

                                    Message message=doc.getDocument().toObject(Message.class);
                                    messages.add(message);
                                    messageImageAdapter.notifyDataSetChanged();

                                }

                                messageImageAdapter.notifyDataSetChanged();

                            }

                        }else
                            Toast.makeText(mView.getContext(), "No image messages found.", Toast.LENGTH_SHORT).show();



                    }
                });

    }

    public void getImageReplyMessage(){

        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Notifications_reply_image")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                        if(e!=null){
                            Log.w("error","listen",e);
                            return;
                        }

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType()== DocumentChange.Type.ADDED){

                                    MessageReply messageReply=doc.getDocument().toObject(MessageReply.class);
                                    messageReplies.add(messageReply);
                                    messageImageReplyAdapter.notifyDataSetChanged();

                                }

                                messageImageReplyAdapter.notifyDataSetChanged();

                            }

                        }else
                            Toast.makeText(mView.getContext(), "No image replies found.", Toast.LENGTH_SHORT).show();



                    }
                });


    }


}
