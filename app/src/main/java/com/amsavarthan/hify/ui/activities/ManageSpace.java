package com.amsavarthan.hify.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.account.LoginActivity;
import com.amsavarthan.hify.utils.database.UserHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ManageSpace extends AppCompatActivity {

    private UserHelper userHelper;
    private FirebaseFirestore firestore;
    private String userId;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private FirebaseUser currentuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_space);

        mAuth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();
        userHelper=new UserHelper(this);
        currentuser=mAuth.getCurrentUser();
        userId=currentuser.getUid();
        storageReference = FirebaseStorage.getInstance().getReference().child("images").child(currentuser.getUid() + ".jpg");

    }

    public void performUploadTask(){

        if(isOnline()){
            Cursor rc =userHelper.getData(1);
            rc.moveToFirst();

            final String nam = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
            final String emai = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_EMAIL));
            final String imag = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));
            final String password = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_PASS));
            final String usernam = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_USERNAME));
            final String loc = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_LOCATION));
            final String bi = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_BIO));

            if(!rc.isClosed()){
                rc.close();
            }

            FirebaseFirestore.getInstance().collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    String name = documentSnapshot.getString("name");
                    String image = documentSnapshot.getString("image");
                    final String email = documentSnapshot.getString("email");
                    String bio = documentSnapshot.getString("bio");
                    String usrname = documentSnapshot.getString("username");
                    String location = documentSnapshot.getString("location");


                    if (!image.equals(imag)) {
                        storageReference.putFile(Uri.parse(imag)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    final String downloadUri = task.getResult().toString();

                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("image", downloadUri);

                                    FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            userHelper.updateContactImage(1, downloadUri);

                                        }

                                    });
                                }
                            }
                        });
                    }

                    if (!bio.equals(bi)) {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("bio", bi);

                        FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                userHelper.updateContactBio(1, bi);

                            }

                        });
                    }

                    if (!location.equals(loc)) {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("location", loc);

                        FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                userHelper.updateContactLocation(1, loc);

                            }

                        });
                    }

                    if (!name.equals(nam)) {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", nam);

                        FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                userHelper.updateContactName(1, nam);

                            }

                        });
                    }

                    if (!currentuser.getEmail().equals(emai)) {


                        AuthCredential credential = EmailAuthProvider
                                .getCredential(currentuser.getEmail(), password);

                        currentuser.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        currentuser.updateEmail(emai).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    if (!email.equals(emai)) {
                                                        Map<String, Object> userMap = new HashMap<>();
                                                        userMap.put("email", emai);

                                                        FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                userHelper.updateContactEmail(1, emai);
                                                            }

                                                        });
                                                    }

                                                } else {

                                                    Log.e("Update email error", task.getException().getMessage() + "..");

                                                }

                                            }
                                        });

                                    }
                                });
                    }
                }
            });

        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void onLogoutClick(View view) {
        performUploadTask();
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setMessage("Logging you out...");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        Map<String, Object> tokenRemove = new HashMap<>();
        tokenRemove.put("token_id", "");

        firestore.collection("Users").document(userId).update(tokenRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                userHelper.deleteContact(1);
                mAuth.signOut();
                try {
                    MainActivity.activity.finish();
                }catch (Exception e){
                    e.printStackTrace();
                }
                LoginActivity.startActivityy(ManageSpace.this);
                mDialog.dismiss();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Logout Error", e.getMessage());
            }
        });
    }

    public void onClearCache(View view) {
        try{
            File dir=this.getCacheDir();
            deleteDir(dir);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {

        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else
            return dir != null && dir.isFile() && dir.delete();

    }

}
