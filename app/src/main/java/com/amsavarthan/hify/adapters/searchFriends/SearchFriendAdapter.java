package com.amsavarthan.hify.adapters.searchFriends;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.Friends;
import com.amsavarthan.hify.ui.activities.friends.FriendProfile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class SearchFriendAdapter extends RecyclerView.Adapter<SearchFriendAdapter.ViewHolder> {

    private List<Friends> usersList;
    private Context context;
    private ViewHolder holderr;
    private View view;
    private HashMap<String, Object> userMap;
    private boolean exist;

    public SearchFriendAdapter(List<Friends> usersList, Context context, View view) {
        this.usersList = usersList;
        this.context = context;
        this.view = view;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,int position) {

        holderr = holder;

        checkIfReqSent(holder);

        holder.listenerText.setText("Add as friend");

        holder.name.setText(usersList.get(position).getName());
        holder.username.setText("@"+usersList.get(position).getUsername());


        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(usersList.get(position).getImage())
                .into(holder.image);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendProfile.startActivity(context,usersList.get(holder.getAdapterPosition()).userId);
            }
        });

        holder.exist_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Information");
                dialog.setMessage("This icon is shown to indicate that friend request to this user has been sent already.");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setIcon(R.drawable.ic_call_made_black_24dp).show();
            }
        });

        holder.friend_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Information");
                dialog.setMessage("This icon is shown to indicate that the user is already your friend.");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setIcon(R.drawable.ic_friend).show();
            }
        });

    }

    private void checkIfReqSent(final ViewHolder holder) {

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(usersList.get(holder.getAdapterPosition()).userId)
                .collection("Friends")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    holder.exist_icon.setVisibility(View.GONE);
                    holder.friend_icon.setVisibility(View.VISIBLE);
                } else {
                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(usersList.get(holder.getAdapterPosition()).userId)
                            .collection("Friend_Requests")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                holder.friend_icon.setVisibility(View.GONE);
                                holder.exist_icon.setVisibility(View.VISIBLE);
                            } else {
                                holder.exist_icon.setVisibility(View.GONE);
                                holder.friend_icon.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });

    }



    @Override
    public int getItemCount() {
        return usersList.size();
    }
	
	@Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void removeItem(final int position, final Snackbar snackbar, final int deletedIndex, final Friends deletedItem) {

        new BottomDialog.Builder(context)
                .setTitle("Add Friend")
                .setContent("Are you sure do you want to add " + usersList.get(position).getName() + " to your friend list?")
                .setPositiveText("Yes")
                .setNegativeText("No")
                .setPositiveBackgroundColorResource(R.color.colorAccentt)
                .setCancelable(false)
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog dialog) {
                        addUser(position, deletedIndex, deletedItem);
                        dialog.dismiss();
                    }
                }).onNegative(new BottomDialog.ButtonCallback() {
            @Override
            public void onClick(@NonNull BottomDialog dialog) {
                dialog.dismiss();
                notifyDataSetChanged();
            }
        }).show();


    }

    private void addUser(final int position, final int deletedIndex, final Friends deletedItem) {

        FirebaseFirestore.getInstance().collection("Users").document(usersList.get(position).userId)
                .collection("Friend_Requests").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {
                    executeFriendReq(deletedIndex, deletedItem);
                } else {
                    Snackbar.make(view, "Friend request has been sent already", Snackbar.LENGTH_LONG).show();
                    notifyDataSetChanged();
                }
            }
        });

    }

    private void executeFriendReq(final int deletedIndex, final Friends deletedItem) {

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
                                .document(deletedItem.userId)
                                .collection("Friend_Requests")
                                .document(documentSnapshot.getString("id"))
                                .set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                //Add for notification data
                                FirebaseFirestore.getInstance()
                                        .collection("Notifications")
                                        .document(deletedItem.userId)
                                        .collection("Friend_Requests")
                                        .document(email)
                                        .set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Snackbar.make(view, "Friend request sent to " + deletedItem.getName(), Snackbar.LENGTH_LONG).show();
                                        notifyDataSetChanged();

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



    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView image;
        View mView;
        TextView name, listenerText,username;
        RelativeLayout viewBackground, viewForeground;
        ImageView exist_icon, friend_icon;

        ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            image = (CircleImageView) mView.findViewById(R.id.image);
            name = (TextView) mView.findViewById(R.id.name);
            username=(TextView)mView.findViewById(R.id.username);
            viewBackground = (RelativeLayout) mView.findViewById(R.id.view_background);
            viewForeground = (RelativeLayout) mView.findViewById(R.id.view_foreground);
            listenerText = (TextView) mView.findViewById(R.id.view_foreground_text);
            exist_icon = (ImageView) mView.findViewById(R.id.exist_icon);
            friend_icon = (ImageView) mView.findViewById(R.id.friend_icon);

        }
    }
}
