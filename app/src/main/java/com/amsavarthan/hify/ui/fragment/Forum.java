package com.amsavarthan.hify.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.feature_ai.fragment.AllQuestions;
import com.amsavarthan.hify.feature_ai.fragment.MyQuestions;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class Forum extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener{

    View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_forum, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        BottomNavigationView bottomNavigationView=mView.findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        loadfragment(new AllQuestions());

    }

    public void loadfragment(android.support.v4.app.Fragment fragment) {
        ((AppCompatActivity)getActivity()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.action_global:
                loadfragment(new AllQuestions());
                break;

            case R.id.action_my:
                loadfragment(new MyQuestions());
                break;

        }
        return true;
    }
}
