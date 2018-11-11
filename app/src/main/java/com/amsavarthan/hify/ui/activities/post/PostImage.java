package com.amsavarthan.hify.ui.activities.post;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.UploadListAdapter;
import com.amsavarthan.hify.models.MultipleImage;
import com.amsavarthan.hify.utils.AnimationUtil;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.rd.PageIndicatorView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.amsavarthan.hify.adapters.UploadListAdapter.uploadedImagesUrl;

public class PostImage extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private EditText mEditText;
    private Map<String, Object> postMap;
    private ProgressDialog mDialog;
    private ArrayList<Image> imagesList;
    private Compressor compressor;
    private LinearLayout empty_holder;
    private List<String> fileNameList;
    private List<String> fileUriList;
    private List<String> fileDoneList;
    public static boolean canUpload=false;

    private UploadListAdapter uploadListAdapter;
    private StorageReference mStorage;
    private RecyclerView mUploadList;
    private RelativeLayout pager_layout;


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
                .setDefaultFontPath("fonts/bold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        postMap = new HashMap<>();

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        pager_layout=findViewById(R.id.pager_layout);

        fileNameList = new ArrayList<>();
        fileDoneList = new ArrayList<>();
        fileUriList = new ArrayList<>();

        uploadListAdapter = new UploadListAdapter(fileUriList,fileNameList, fileDoneList);

        //RecyclerView
        mUploadList=findViewById(R.id.recyclerView);
        mUploadList.setItemAnimator(new DefaultItemAnimator());
        mUploadList.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mUploadList.setLayoutManager(new LinearLayoutManager(this));
        mUploadList.setHasFixedSize(true);
        mUploadList.setAdapter(uploadListAdapter);

        mEditText = findViewById(R.id.text);
        empty_holder=findViewById(R.id.empty_holder);

        compressor=new Compressor(this)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.PNG)
                .setMaxHeight(350);

        pager_layout.setVisibility(View.GONE);
        empty_holder.setVisibility(View.VISIBLE);


        mDialog = new ProgressDialog(this);
        mStorage=FirebaseStorage.getInstance().getReference();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Config.RC_PICK_IMAGES ) {
            if (resultCode == RESULT_OK && data != null) {
                imagesList = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
                if(!imagesList.isEmpty()){

                    empty_holder.setVisibility(View.GONE);

                    pager_layout.setVisibility(View.VISIBLE);
                    pager_layout.setAlpha(0.0f);

                    pager_layout.animate()
                            .setDuration(300)
                            .alpha(1.0f)
                            .start();


                    for(int i=0;i<imagesList.size();i++) {

                        Uri fileUri = Uri.fromFile(new File(imagesList.get(i).getPath()));
                        String fileName = imagesList.get(i).getName();

                        fileNameList.add(fileName);
                        fileUriList.add(fileUri.toString());
                        fileDoneList.add("uploading");
                        uploadListAdapter.notifyDataSetChanged();

                        final StorageReference fileToUpload = mStorage.child("post_images").child(random() + ".png");

                        final int finalI = i;
                        fileToUpload.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                               fileToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                   @Override
                                   public void onSuccess(Uri uri) {
                                       fileDoneList.remove(finalI);
                                       fileDoneList.add(finalI,"done");
                                       uploadedImagesUrl.add(uri.toString());
                                       uploadListAdapter.notifyDataSetChanged();
                                   }
                               });

                            }
                        });
                    }

                }

            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_image_post, menu);

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

                if (imagesList.isEmpty()) {
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

                if (TextUtils.isEmpty(mEditText.getText().toString()) && !imagesList.isEmpty())
                    AnimationUtil.shakeView(mEditText, PostImage.this);
                else
                    uploadPost();

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

        if(canUpload) {
            if (!uploadedImagesUrl.isEmpty()) {

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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_1", uploadedImagesUrl.get(1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_2", uploadedImagesUrl.get(2));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_3", uploadedImagesUrl.get(3));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_4", uploadedImagesUrl.get(4));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_5", uploadedImagesUrl.get(5));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_6", uploadedImagesUrl.get(6));
                        } catch (Exception e) {
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
                                        Toast.makeText(PostImage.this, "Post sent", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mDialog.dismiss();
                                        Log.e("Error sending post", e.getMessage());
                                    }
                                });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                        Log.e("Error getting user", e.getMessage());
                    }
                });

            } else {
                mDialog.dismiss();
                Toast.makeText(this, "No image has been uploaded, Please wait or try again", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Please wait, images are uploading...", Toast.LENGTH_SHORT).show();
        }

    }

    public void onSelectImage(View view) {
        startPickImage(true);
    }

    public void onSelectImageCamera(View view) {
        startPickImage(false);
    }

    //endregion

}
