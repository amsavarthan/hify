package com.amsavarthan.hify.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.Message;
import com.amsavarthan.hify.ui.activities.notification.NotificationActivity;
import com.amsavarthan.hify.ui.activities.notification.NotificationImage;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class MessageImageAdapter extends RecyclerView.Adapter<MessageImageAdapter.ViewHolder> {

    private List<Message> messageList;
    private Context context;
    private FirebaseFirestore mFirestore;

    public MessageImageAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_text_item,parent,false);
        mFirestore=FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageImageAdapter.ViewHolder holder, int position) {

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(messageList.get(position).getUserimage())
                .into(holder.image);

        holder.name.setText(messageList.get(position).getUsername());

        mFirestore.collection("Users")
                .document(messageList.get(position).getFrom())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        Glide.with(context)
                                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                .load(documentSnapshot.getString("image"))
                                .into(holder.image);

                        holder.name.setText(documentSnapshot.getString("name"));

                    }
                });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, NotificationImage.class);
                intent.putExtra("from_id", messageList.get(holder.getAdapterPosition()).getFrom());
                intent.putExtra("message", messageList.get(holder.getAdapterPosition()).getMessage());
                intent.putExtra("image", messageList.get(holder.getAdapterPosition()).getImage());
                context.startActivity(intent);
            }
        });

        String timeAgo = TimeAgo.using(Long.parseLong(messageList.get(holder.getAdapterPosition()).getTimestamp()));
        holder.time.setText(timeAgo);

        holder.message.setText(String.format(Locale.ENGLISH,"Sent you an image with message: %s", messageList.get(position).getMessage()));

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private CircleImageView image;
        private TextView message,name,time;

        public ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image = mView.findViewById(R.id.image);
            name = mView.findViewById(R.id.name);
            message = mView.findViewById(R.id.message);
            time = mView.findViewById(R.id.time);

        }
    }
}
