package com.amsavarthan.hify.ui.activities.friends;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.Friends;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddUserDetail extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String userid = null, image = null, name = null, email = null, current_user_id = null, user_token = null;
    private CircleImageView profile;
    private TextView nameT, emailT, friendsT;
    private HashMap<String, Object> userMap;
    private View view;

    public static void startActivity(Context context, String userid, String name, String email, String image, String token) {

        Intent intent = new Intent(context, AddUserDetail.class)
                .putExtra("f_id", userid)
                .putExtra("f_name", name)
                .putExtra("f_email", email)
                .putExtra("f_image", image)
                .putExtra("f_token", token);
        context.startActivity(intent);

    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_detail);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#212121"));
        }

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        current_user_id = mAuth.getCurrentUser().getUid();
        userid = getIntent().getStringExtra("f_id");
        name = getIntent().getStringExtra("f_name");
        email = getIntent().getStringExtra("f_email");
        image = getIntent().getStringExtra("f_image");
        user_token = getIntent().getStringExtra("f_token");

        profile = findViewById(R.id.profile_pic);
        nameT = findViewById(R.id.username);
        emailT = findViewById(R.id.email);
        friendsT = findViewById(R.id.friends);
        view = findViewById(R.id.layout);

        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(image)
                .into(profile);

        getDetails();
        getFriendsCount();

    }

    private void getDetails() {

        mFirestore.collection("Users").document(userid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {

                nameT.setVisibility(View.VISIBLE);
                nameT.setAlpha(0.0f);
                nameT.animate()
                        .alpha(1.0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);

                                name = documentSnapshot.getString("name");
                                nameT.setText(name);

                            }
                        }).start();

                /*emailT.setVisibility(View.VISIBLE);
                emailT.setAlpha(0.0f);
                emailT.animate()
                        .alpha(1.0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);

                                email = documentSnapshot.getString("email");
                                emailT.setText(email);

                            }
                        }).start();*/

            }
        });

    }


    private void getFriendsCount() {
        mFirestore.collection("Users").document(userid).collection("Friends").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot documentSnapshots) {

                friendsT.setVisibility(View.VISIBLE);
                friendsT.setAlpha(0.0f);
                friendsT.animate()
                        .alpha(1.0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);

                                friendsT.setText(String.format(Locale.ENGLISH, "Total Friends: %d", documentSnapshots.size()));

                            }
                        }).start();

            }
        });
    }

    public void onSendClick(View view) {

        new BottomDialog.Builder(this)
                .setTitle("Add Friend")
                .setContent("Are you sure do you want to add " + name + " to your friend list?")
                .setPositiveText("Yes")
                .setNegativeText("No")
                .setPositiveBackgroundColorResource(R.color.colorAccentt)
                .setCancelable(false)
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog dialog) {
                        addUser();
                        dialog.dismiss();
                    }
                }).onNegative(new BottomDialog.ButtonCallback() {
            @Override
            public void onClick(@NonNull BottomDialog dialog) {
                dialog.dismiss();
            }
        }).show();


    }

    private void addUser() {

        FirebaseFirestore.getInstance().collection("Users").document(userid)
                .collection("Friend_Requests").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {
                    executeFriendReq();
                } else {
                    Snackbar.make(view, "Friend request has been sent already", Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }

    private void executeFriendReq() {

        userMap = new HashMap<>();

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        final String email=documentSnapshot.getString("email");

                        userMap.put("name", documentSnapshot.getString("name"));
                        userMap.put("id", documentSnapshot.getString("id"));
                        userMap.put("email", email);
                        userMap.put("image", documentSnapshot.getString("image"));
                        userMap.put("token", documentSnapshot.getString("token_id"));
                        userMap.put("notification_id", String.valueOf(System.currentTimeMillis()));
                        userMap.put("timestamp", String.valueOf(System.currentTimeMillis()));

                        //Add to user
                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(userid)
                                .collection("Friend_Requests")
                                .document(documentSnapshot.getString("id"))
                                .set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                //Add for notification data
                                FirebaseFirestore.getInstance()
                                        .collection("Notifications")
                                        .document(userid)
                                        .collection("Friend_Requests")
                                        .document(email)
                                        .set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Snackbar.make(view, "Friend request sent to " + name, Snackbar.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Error",e.getMessage());
                                    }
                                });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Error",e.getMessage());
                            }
                        });

                    }
                });

    }

}
