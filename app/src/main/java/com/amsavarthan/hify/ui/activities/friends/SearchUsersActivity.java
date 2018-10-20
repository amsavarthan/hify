package com.amsavarthan.hify.ui.activities.friends;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.addFriends.RecyclerViewTouchHelper;
import com.amsavarthan.hify.adapters.searchFriends.SearchFriendAdapter;
import com.amsavarthan.hify.models.Friends;
import com.amsavarthan.hify.utils.AnimationUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SearchUsersActivity extends AppCompatActivity {

    public static FloatingActionButton fab;
    private EditText searchText;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private ArrayList<Friends> usersList;
    private SearchFriendAdapter usersAdapter;
    private ItemTouchHelper.SimpleCallback itemTouchHelperCallback;
    private ListenerRegistration mRegistration;
    private Query mQuery;
    private ProgressDialog mDialog;

    public static void startActivity(Activity activity, Context context, View view) {
        Intent intent = new Intent(context, SearchUsersActivity.class);

        if (Build.VERSION.SDK_INT >= 21) {
            String transitionName = "search";
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                            view,   // Starting view
                            transitionName    // The String
                    );
            ActivityCompat.startActivity(context, intent, options.toBundle());

        } else {
            context.startActivity(intent);
        }

    }

    public void stopListening() {

        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }

    }

    public void getUsers() {
        mDialog.show();
        usersList.clear();
        mQuery = mFirestore.collection("Users");
        startListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListening();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopListening();
        fab.hide();
    }

    private void startListening() {

        final String searchQuery = searchText.getText().toString();
        if (!TextUtils.isEmpty(searchQuery)) {

            View view = this.getCurrentFocus();
            if (view != null) {
                searchText.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            mQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        final String docId = doc.getDocument().getId();
                        if (!docId.equals(mAuth.getCurrentUser().getUid())) {

                            mFirestore.collection("Users").document(docId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    if (documentSnapshot.getString("name").toLowerCase().contains(searchQuery.toLowerCase())) {
                                        Friends friends = documentSnapshot.toObject(Friends.class).withId(docId);
                                        usersList.add(friends);
                                        usersAdapter.notifyDataSetChanged();
                                    }

                                }
                            });

                        }
                    }
                    mDialog.dismiss();
                }
            });

        } else {
            AnimationUtil.shakeView(searchText, this);
            mDialog.dismiss();
        }


    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/bold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#212121"));
        }

        mRecyclerView = findViewById(R.id.usersList);
        searchText = findViewById(R.id.searchText);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        fab = findViewById(R.id.search_button);
        usersList = new ArrayList<>();
        usersAdapter = new SearchFriendAdapter(usersList, this, findViewById(R.id.layout));

        itemTouchHelperCallback = new RecyclerViewTouchHelper(0, ItemTouchHelper.LEFT, new RecyclerViewTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                if (viewHolder instanceof SearchFriendAdapter.ViewHolder) {
                    // get the removed item name to display it in snack bar
                    String name = usersList.get(viewHolder.getAdapterPosition()).getName();

                    // backup of removed item for undo purpose
                    final Friends deletedItem = usersList.get(viewHolder.getAdapterPosition());
                    final int deletedIndex = viewHolder.getAdapterPosition();

                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.layout), "Friend request sent to " + name, Snackbar.LENGTH_LONG);

                    // remove the item from recycler view
                    usersAdapter.removeItem(viewHolder.getAdapterPosition(), snackbar, deletedIndex, deletedItem);

                }
            }
        });


        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        //new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(usersAdapter);

    }

    public void executeSearch(View view) {
        usersList.clear();
        getUsers();
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

    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

}
