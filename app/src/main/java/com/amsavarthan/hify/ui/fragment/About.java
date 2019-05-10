package com.amsavarthan.hify.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.amsavarthan.hify.R;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class About extends Fragment {

    View mView;
    LinearLayout email,website,instagram,google,github;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_about, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        email=mView.findViewById(R.id.email);
        website=mView.findViewById(R.id.website);
        instagram=mView.findViewById(R.id.instagram);
        github=mView.findViewById(R.id.github);

        email.setOnClickListener(v -> {

            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"amsavarthan.a@gmail.com"});
            email.putExtra(Intent.EXTRA_SUBJECT, "Sent from Hify ( "+ Build.BRAND+"("+Build.VERSION.SDK_INT+") )");
            email.putExtra(Intent.EXTRA_TEXT, "");
            email.setType("message/rfc822");
            startActivity(Intent.createChooser(email, "Send using..."));

        });

        website.setOnClickListener(v -> {

            String url = "http://lvamsavarthan.github.io/lvstore";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);

        });

        instagram.setOnClickListener(v -> {

            String url = "https://www.instagram.com/lvamsavarthan";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);

        });


        github.setOnClickListener(v -> {

            String url = "https://github.com/lvamsavarthan/Hify";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);

        });


    }

}
