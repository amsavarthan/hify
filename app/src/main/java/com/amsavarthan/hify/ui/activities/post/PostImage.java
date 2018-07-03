package com.amsavarthan.hify.ui.activities.post;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.PostPhotosAdapter;
import com.amsavarthan.hify.models.MultipleImage;
import com.amsavarthan.hify.utils.AnimationUtil;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.rd.PageIndicatorView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PostImage extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private EditText mEditText;
    private RelativeLayout pager_layout,indicator_holder;
    private ViewPager pager;
    private PageIndicatorView indicator;
    private AdView mAdView;
    private ArrayList<MultipleImage> images=new ArrayList<>();
    private Map<String, Object> postMap;
    private ArrayList<String> uploadedImagesUrl=new ArrayList<>();
    private ProgressDialog mDialog;
    private ArrayList<Image> imagesList;
    private Compressor compressor;
    private boolean mState=false;
    private LinearLayout empty_holder;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, PostImage.class);
        context.startActivity(intent);
    }

    @NonNull
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_image);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("New Image Post");

        try {
            getSupportActionBar().setTitle("New Image Post");
        } catch (Exception e) {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        postMap = new HashMap<>();

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        pager=findViewById(R.id.pager);
        pager_layout=findViewById(R.id.pager_layout);
        indicator=findViewById(R.id.indicator);
        indicator_holder=findViewById(R.id.indicator_holder);
        mEditText = findViewById(R.id.text);
        empty_holder=findViewById(R.id.empty_holder);

        compressor=new Compressor(this)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.PNG)
                .setMaxHeight(350);

        pager_layout.setVisibility(View.GONE);

        mAdView = findViewById(R.id.adView);
        showAd(false);

        initAd();
        mDialog = new ProgressDialog(this);


    }


    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Config.RC_PICK_IMAGES ) {
            if (resultCode == RESULT_OK && data != null) {
                imagesList = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);

                Toast.makeText(this, "Please wait, Image will be loaded!", Toast.LENGTH_LONG).show();
                if(!imagesList.isEmpty()){

                    empty_holder.setVisibility(View.GONE);

                    showAd(true);
                    PostPhotosAdapter postPhotosAdapter;

                    if(imagesList.size()==1){
                        postPhotosAdapter = new PostPhotosAdapter(this,this, images, true);
                        pager.setAdapter(postPhotosAdapter);
                        indicator_holder.setVisibility(View.VISIBLE);
                        indicator.setViewPager(pager);

                        pager_layout.setVisibility(View.VISIBLE);
                        pager_layout.setAlpha(0.0f);

                        pager_layout.animate()
                                .setDuration(300)
                                .alpha(1.0f)
                                .start();


                        for (Image image : imagesList) {
                            MultipleImage multipleImage = new MultipleImage(image.getPath(), Uri.parse(new File(image.getPath()).toString()));
                            images.add(multipleImage);
                            postPhotosAdapter.notifyDataSetChanged();
                        }


                    }else {
                        postPhotosAdapter = new PostPhotosAdapter(this, this, images, true);
                        pager.setAdapter(postPhotosAdapter);
                        indicator_holder.setVisibility(View.VISIBLE);
                        indicator.setViewPager(pager);

                        pager_layout.setVisibility(View.VISIBLE);
                        pager_layout.setAlpha(0.0f);

                        pager_layout.animate()
                                .setDuration(300)
                                .alpha(1.0f)
                                .start();


                        for (Image image : imagesList) {
                            MultipleImage multipleImage = new MultipleImage(image.getPath(), Uri.parse(new File(image.getPath()).toString()));
                            images.add(multipleImage);
                            postPhotosAdapter.notifyDataSetChanged();
                        }


                    }

                }

            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_image_post, menu);

        /*MenuItem edit=menu.findItem(R.id.action_edit);
        MenuItem send=menu.findItem(R.id.action_post);

        if(mState){
            send.setVisible(true);
            edit.setVisible(true);
        }else{
            send.setVisible(false);
            edit.setVisible(false);
        }*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_edit:

                new MaterialDialog.Builder(this)
                        .title("Add Image(s)")
                        .content("How do you want to add image(s)?")
                        .positiveText("Camera")
                        .negativeText("Gallery")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                startPickImage(false);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                startPickImage(true);
                            }
                        })
                        .show();


                return true;

            case R.id.action_post:

                if (images.isEmpty()) {
                    new MaterialDialog.Builder(this)
                            .title("No image(s) selected")
                            .content("It seems that you haven't selected image(s) for posting, How do you want to insert image(s)?")
                            .positiveText("Camera")
                            .negativeText("Gallery")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    startPickImage(false);
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    startPickImage(true);
                                }
                            })
                            .show();
                    return true;
                }

                if (TextUtils.isEmpty(mEditText.getText().toString()) && !images.isEmpty())
                    AnimationUtil.shakeView(mEditText, PostImage.this);
                else
                    new uploadImages().execute();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //region Post


    private void startPickImage(boolean gallery) {

        if(gallery) {

            ImagePicker.with(this)
                    .setToolbarColor("#FFFFFF")
                    .setStatusBarColor("#CCCCCC")
                    .setToolbarTextColor("#212121")
                    .setToolbarIconColor("#212121")
                    .setProgressBarColor("#5093FF")
                    .setBackgroundColor("#FFFFFF")
                    .setCameraOnly(false)
                    .setMultipleMode(true)
                    .setFolderMode(true)
                    .setShowCamera(false)
                    .setFolderTitle("Albums")
                    .setImageTitle("Photos")
                    .setDoneTitle("Done")
                    .setLimitMessage("You have reached selection limit")
                    .setMaxSize(7)
                    .setSavePath("Hify")
                    .setAlwaysShowDoneButton(true)
                    .setKeepScreenOn(true)
                    .start();

        }else{

            ImagePicker.with(this)
                    .setToolbarColor("#FFFFFF")
                    .setStatusBarColor("#CCCCCC")
                    .setToolbarTextColor("#212121")
                    .setToolbarIconColor("#212121")
                    .setProgressBarColor("#5093FF")
                    .setBackgroundColor("#FFFFFF")
                    .setCameraOnly(true)
                    .setMultipleMode(true)
                    .setDoneTitle("Done")
                    .setLimitMessage("You have reached capture limit")
                    .setMaxSize(7)
                    .setSavePath("Hify")
                    .setKeepScreenOn(true)
                    .start();

        }

    }

    private void uploadPost() {


        mDialog=new ProgressDialog(this);
        mDialog.setMessage("Posting...");
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);

        if(!uploadedImagesUrl.isEmpty() && uploadedImagesUrl.size()==imagesList.size()){

            mDialog.show();

            mFirestore.collection("Users").document(mCurrentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    postMap.put("userId", documentSnapshot.getString("id"));
                    postMap.put("username", documentSnapshot.getString("username"));
                    postMap.put("name", documentSnapshot.getString("name"));
                    postMap.put("userimage", documentSnapshot.getString("image"));
                    postMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
                    postMap.put("image_count", uploadedImagesUrl.size());
                    try {
                        postMap.put("image_url_0", uploadedImagesUrl.get(0));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try {
                        postMap.put("image_url_1", uploadedImagesUrl.get(1));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try {
                        postMap.put("image_url_2", uploadedImagesUrl.get(2));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try {
                        postMap.put("image_url_3", uploadedImagesUrl.get(3));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try {
                        postMap.put("image_url_4", uploadedImagesUrl.get(4));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try {
                        postMap.put("image_url_5", uploadedImagesUrl.get(5));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try {
                        postMap.put("image_url_6", uploadedImagesUrl.get(6));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    postMap.put("likes", "0");
                    postMap.put("favourites", "0");
                    postMap.put("description", mEditText.getText().toString());
                    postMap.put("color", "0");

                    mFirestore.collection("Posts")
                            .add(postMap)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    mDialog.dismiss();
                                    showAd();
                                    Toast.makeText(PostImage.this, "Post sent", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mDialog.dismiss();
                                    showAd();
                                    Log.e("Error sending post", e.getMessage());
                                }
                            });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    showAd();
                    Log.e("Error getting user", e.getMessage());
                }
            });

        }else{
            mDialog.dismiss();
            Toast.makeText(this, "Yet to upload image(s), Press 'Post' again", Toast.LENGTH_SHORT).show();
            new uploadImages().execute();
        }


    }

    public void onSelectImage(View view) {
        startPickImage(true);
    }

    public void onSelectImageCamera(View view) {
        startPickImage(false);
    }

    private class uploadImages extends AsyncTask<Void,Void,Void>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mDialog.setMessage("Uploading...");
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

        }

        @Override
        protected Void doInBackground(Void... voids) {

            for (int i=0;i<images.size();i++) {
                uploadImage(images.get(i));
            }

            return null;
        }

        private void uploadImage(MultipleImage image) {

            File compressedfile;
            try {
                compressedfile=compressor.compressToFile(new File(image.getLocal_path()));

                final StorageReference post_images = FirebaseStorage.getInstance().getReference().child("post_images").child(random() + ".jpg");
                post_images.putFile(Uri.fromFile(compressedfile)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isComplete()&&task.getResult().toString()!=null){

                            post_images.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    uploadedImagesUrl.add(uri.toString());
                                    Log.i("upload","uploaded image");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Error",e.getLocalizedMessage());
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error",e.getLocalizedMessage());
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mDialog.dismiss();
            uploadPost();
        }

    }

    //endregion

    //region Ads

    InterstitialAd interstitialAd;

    private void showAd(boolean state) {

        if(!state){
            mAdView.setVisibility(View.GONE);
            return;
        }

        mAdView.setVisibility(View.VISIBLE);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                mAdView.setVisibility(View.GONE);
            }
        });
    }

    public void initAd(){
        interstitialAd=new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_2));

        AdRequest adRequest=new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
    }

    public void showAd() {
        if(interstitialAd.isLoaded()){
            interstitialAd.show();
        }
    }

    //endregion


}
