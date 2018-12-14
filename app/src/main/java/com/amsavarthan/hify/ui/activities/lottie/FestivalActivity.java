package com.amsavarthan.hify.ui.activities.lottie;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.amsavarthan.hify.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FestivalActivity extends AppCompatActivity {

    LottieAnimationView lottieAnimationView;
    TextView text;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    String festival_name,festival_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_festival);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/bold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        festival_name=getIntent().getStringExtra("festival_name");
        festival_text=getIntent().getStringExtra("festival_text");

        lottieAnimationView=findViewById(R.id.lottieView);
        text=findViewById(R.id.text);

        text.setText(festival_text);

        lottieAnimationView.useHardwareAcceleration(true);
        if(festival_name.equals("christmas")) {
            lottieAnimationView.setAnimation("christmas.json");
            lottieAnimationView.playAnimation();
        }

    }
}
