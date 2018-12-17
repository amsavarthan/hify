package com.amsavarthan.hify.ui.activities.lottie;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.friends.SendActivity;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FestivalActivity extends AppCompatActivity {

    LottieAnimationView lottieAnimationView;
    TextView text;
    Button send_btn;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    String festival_name,festival_text,send,reason,dev_id;
    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/bold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_festival);

        activity=this;
        festival_name=getIntent().getStringExtra("festival_name");
        festival_text=getIntent().getStringExtra("festival_text");
        send=getIntent().getStringExtra("send_text");
        reason=getIntent().getStringExtra("reason");
        dev_id=getIntent().getStringExtra("dev_id");

        lottieAnimationView=findViewById(R.id.lottieView);
        text=findViewById(R.id.text);
        send_btn=findViewById(R.id.send);

        text.setText(festival_text);

        lottieAnimationView.useHardwareAcceleration(true);
        if(festival_name.equals("christmas")) {
            lottieAnimationView.setAnimation("christmas.json");
            lottieAnimationView.playAnimation();
        }else{
            lottieAnimationView.setAnimation(festival_name+".json");
            lottieAnimationView.playAnimation();
        }

        send_btn.setText(send);

        send_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SendingActivity.startActivity(FestivalActivity.this,reason,dev_id);
                    }
                }
        );

    }
}
