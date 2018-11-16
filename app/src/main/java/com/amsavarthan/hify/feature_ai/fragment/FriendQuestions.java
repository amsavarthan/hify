package com.amsavarthan.hify.feature_ai.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.feature_ai.adapter.QuestionAdapter;
import com.amsavarthan.hify.feature_ai.models.AllQuestionsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.tylersuehr.esr.EmptyStateRecyclerView;
import com.tylersuehr.esr.TextStateDisplay;

import java.util.ArrayList;
import java.util.List;

public class FriendQuestions extends Fragment {


    private EmptyStateRecyclerView recyclerView;
    private Context context;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;
    private QuestionAdapter adapter;
    private static String TAG = FriendQuestions.class.getSimpleName();
    private List<AllQuestionsModel> allQuestionsModelList = new ArrayList<>();
    private View view;
    private TextView et0,et1,et2,et3,et4,et5,et6,et7,et8,et9,et10,et11,et12,et13;
    private String userId;

    public FriendQuestions() { }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_answered, container, false);
        return view;
    }

    public static FriendQuestions newInstance(String user_id){

        Bundle args=new Bundle();
        args.putString("user_id",user_id);

        FriendQuestions fragment=new FriendQuestions();
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = view.getContext();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        adapter = new QuestionAdapter(allQuestionsModelList);

        if (mCurrentUser != null) {

            mFirestore = FirebaseFirestore.getInstance();

            if(getArguments()!=null){
                userId=getArguments().getString("user_id");
            }else{
                userId=mCurrentUser.getUid();
            }

            et0=view.findViewById(R.id.all);
            et1=view.findViewById(R.id.accountancy);
            et2=view.findViewById(R.id.astronomy);
            et3=view.findViewById(R.id.biology);
            et4=view.findViewById(R.id.business_maths);
            et5=view.findViewById(R.id.computer_science);
            et6=view.findViewById(R.id.commerce);
            et7=view.findViewById(R.id.chemistry);
            et8=view.findViewById(R.id.economics);
            et9=view.findViewById(R.id.geography);
            et10=view.findViewById(R.id.history);
            et11=view.findViewById(R.id.physics);
            et12=view.findViewById(R.id.p_science);
            et13=view.findViewById(R.id.maths);

            recyclerView = view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            view.findViewById(R.id.progressbar).setVisibility(View.VISIBLE);

            TextStateDisplay error_style = new TextStateDisplay(context, "Sorry for the inconvenience", "Something went wrong :(");
            TextStateDisplay empty_style = new TextStateDisplay(context, "It's Empty", "All your questions will appear here");

            recyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR, error_style);
            recyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_EMPTY, empty_style);

            allQuestionsModelList.clear();
            recyclerView.setAdapter(adapter);

            setUpOnClick();
            getQuestions();

        }
    }

    private void getQuestions() {

        Query firstQuery = mFirestore.collection("Questions")
                .orderBy("timestamp", Query.Direction.DESCENDING);
        firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {


                if (!documentSnapshots.isEmpty()) {

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            if (doc.getDocument().getString("id").equals(userId)) {
                                AllQuestionsModel question = doc.getDocument().toObject(AllQuestionsModel.class).withId(doc.getDocument().getId());
                                allQuestionsModelList.add(question);
                                adapter.notifyDataSetChanged();
                                view.findViewById(R.id.progressbar).setVisibility(View.GONE);
                            }

                        }


                        if(allQuestionsModelList.isEmpty()){
                            Toast.makeText(context, "No questions found", Toast.LENGTH_SHORT).show();
                            view.findViewById(R.id.progressbar).setVisibility(View.GONE);
                        }

                    }


                } else {
                    Toast.makeText(context, "No questions found", Toast.LENGTH_SHORT).show();
                    view.findViewById(R.id.progressbar).setVisibility(View.GONE);
                }

            }

        });


    }

    public void filterResult(String subject){

        recyclerView.clearStateDisplays();

        TextStateDisplay error_style = new TextStateDisplay(context, "Sorry for the inconvenience", "Something went wrong :(");
        TextStateDisplay empty_style = new TextStateDisplay(context, "It's Empty", "No questions found");

        recyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR, error_style);
        recyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_EMPTY, empty_style);

        allQuestionsModelList.clear();
        adapter.notifyDataSetChanged();

        if(subject.equals("All")){
            getQuestions();
        }else{

            Query firstQuery = mFirestore.collection("Questions")
                    .whereEqualTo("subject",subject)
                    .orderBy("timestamp", Query.Direction.DESCENDING);
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    try {
                        if (!documentSnapshots.isEmpty()) {

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    if (doc.getDocument().getString("id").equals(userId)) {
                                        AllQuestionsModel question = doc.getDocument().toObject(AllQuestionsModel.class).withId(doc.getDocument().getId());
                                        allQuestionsModelList.add(question);
                                        adapter.notifyDataSetChanged();
                                        view.findViewById(R.id.progressbar).setVisibility(View.GONE);
                                    }

                                }


                            }

                            if(allQuestionsModelList.isEmpty()){
                                Toast.makeText(context, "No questions found", Toast.LENGTH_SHORT).show();
                                view.findViewById(R.id.progressbar).setVisibility(View.GONE);
                            }

                        } else {
                            Toast.makeText(context, "No questions found", Toast.LENGTH_SHORT).show();
                            view.findViewById(R.id.progressbar).setVisibility(View.GONE);
                        }
                    }catch (NullPointerException eee){
                        if(allQuestionsModelList.isEmpty()){
                            view.findViewById(R.id.progressbar).setVisibility(View.GONE);
                            Toast.makeText(context, "No questions found", Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception ee){
                        recyclerView.invokeState(EmptyStateRecyclerView.STATE_ERROR);
                        if(allQuestionsModelList.isEmpty()){
                            view.findViewById(R.id.progressbar).setVisibility(View.GONE);
                        }
                        ee.printStackTrace();
                    }

                }

            });


        }

    }

    public void setUpOnClick(){

        et0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                getQuestions();
            }
        });
        et1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                filterResult("Accountancy");
            }
        });
        et2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                filterResult("Astronomy");
            }
        });
        et3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                filterResult("Biology");
            }
        });
        et4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                filterResult("Business Maths");
            }
        });
        et5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                filterResult("Computer Science");
            }
        });
        et6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                filterResult("Commerce");
            }
        });
        et7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                filterResult("Chemistry");
            }
        });
        et8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                filterResult("Economics");
            }
        });
        et9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                filterResult("Geography");
            }
        });
        et10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                filterResult("History");
            }
        });
        et11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                filterResult("Physics");
            }
        });
        et12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                filterResult("Political Science");
            }
        });
        et13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                filterResult("Maths");
            }
        });

    }
}

