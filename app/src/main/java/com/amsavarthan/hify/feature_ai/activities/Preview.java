package com.amsavarthan.hify.feature_ai.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.amsavarthan.hify.R;
import com.bumptech.glide.Glide;

import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class Preview extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        ImageView imageView=findViewById(R.id.image);

        Glide.with(this)
                .load(getIntent().getStringExtra("url"))
                .into(imageView);

    }
}
