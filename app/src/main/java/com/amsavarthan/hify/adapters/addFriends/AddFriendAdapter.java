package com.amsavarthan.hify.adapters.addFriends;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.ViewHolder> {

    private List<Friends> usersList;
    private Context context;
    private View view;
    private HashMap<String, Object> userMap;
    private ViewHolder holderr;

    public AddFriendAdapter(List<Friends> usersList, Context context, View view) {
        this.usersList = usersList;
        this.context = context;
        this.view = view;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.friend_item_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holderr=holder;
        checkIfReqSent(holder);
        holder.name.setText(usersList.get(position).getName());
        if(holder.username.getText().equals("null")){
            holder.username.setText("loading...");
        }

        holder.username.setText("@"+usersList.get(position).getUsername());

        holder.listenerText.setText("Add as friend");

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
                dialog.setMessage("This icons shows to indicate that friend request to this user has been sent already.");
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
                dialog.setMessage("This icons shows to indicate that the user is already your friend.");
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
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    holder.exist_icon.setVisibility(View.GONE);
                    holder.friend_icon.setVisibility(View.VISIBLE);
                    holder.friend_icon.setAlpha(0.0f);

                    holder.friend_icon.animate()
                            .setDuration(200)
                            .alpha(1.0f)
                            .start();
                } else {
                    try {
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
                                    holder.exist_icon.setAlpha(0.0f);

                                    holder.exist_icon.animate()
                                            .alpha(1.0f)
                                            .start();
                                } else {
                                    holder.exist_icon.setVisibility(View.GONE);
                                    holder.friend_icon.setVisibility(View.GONE);
                                }
                            }
                        });
                    }catch(Exception e){
                        e.printStackTrace();
                    }
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

    private void addUser( final int position, final int deletedIndex, final Friends deletedItem) {

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(usersList.get(position).userId)
                .collection("Friends")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (!documentSnapshot.exists()) {

                            FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(usersList.get(position).userId)
                                    .collection("Friend_Requests")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                                            if(holderr.friend_icon.getVisibility()!=View.VISIBLE) {

                                                if (!documentSnapshot.exists()) {
                                                    executeFriendReq(deletedItem,holderr);
                                                } else {
                                                    Snackbar.make(view, "Friend request has been sent already", Snackbar.LENGTH_LONG).show();
                                                    notifyDataSetChanged();
                                                }

                                            }else{
                                                Snackbar.make(view, usersList.get(position).getName()+" is already your friend", Snackbar.LENGTH_LONG).show();
                                                notifyDataSetChanged();
                                            }

                                        }
                                    });

                        } else {
                            usersList.remove(position);
                            notifyDataSetChanged();
                        }

                    }
                });


    }

    private void executeFriendReq(final Friends deletedItem, final ViewHolder holder) {

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
                        .set(userMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //Add for notification data
                        FirebaseFirestore.getInstance()
                                .collection("Notifications")
                                .document(deletedItem.userId)
                                .collection("Friend_Requests")
                                .document(email)
                                .set(userMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                holder.friend_icon.setVisibility(View.GONE);
                                holder.exist_icon.setVisibility(View.VISIBLE);
                                holder.exist_icon.setAlpha(0.0f);

                                holder.exist_icon.animate()
                                        .setDuration(200)
                                        .alpha(1.0f)
                                        .start();
                                Snackbar.make(view, "Friend request sent to " + deletedItem.getName(), Snackbar.LENGTH_LONG).show();
                                notifyDataSetChanged();
                                notifyItemChanged(holder.getAdapterPosition());

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

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView image;
        View mView;
        TextView name, listenerText,username;
        RelativeLayout viewBackground, viewForeground;
        ImageView exist_icon,friend_icon;

        ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image=(CircleImageView)mView.findViewById(R.id.image);
            name=(TextView)mView.findViewById(R.id.name);
            username=(TextView)mView.findViewById(R.id.username);
            viewBackground = (RelativeLayout) mView.findViewById(R.id.view_background);
            viewForeground = (RelativeLayout) mView.findViewById(R.id.view_foreground);
            listenerText = (TextView) mView.findViewById(R.id.view_foreground_text);
            exist_icon = (ImageView) mView.findViewById(R.id.exist_icon);
            friend_icon = (ImageView) mView.findViewById(R.id.friend_icon);


        }
    }
}
