package com.amsavarthan.hify.feature_ai.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amsavarthan.hify.R;
import com.amsavarthan.hify.feature_ai.activities.ResultActivity;
import com.amsavarthan.hify.feature_ai.models.Recents;
import com.amsavarthan.hify.feature_ai.utils.RecentsDatabase;

import java.util.List;

public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.ViewHolder> {

    private List<Recents> recents;
    private Context context;
    private int lastAnimatedPosition = -1;

    public RecentsAdapter(List<Recents> recents) {
        this.recents = recents;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recents_card,parent,false));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.query.setText(recents.get(position).getQuery());

        holder.search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(context)
                        .title("Search")
                        .content("Do you want to search the following?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                ResultActivity.startActivity(context,recents.get(holder.getAdapterPosition()).getQuery());
                            }
                        }).show();
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(context)
                        .title("Delete")
                        .content("Do you want to delete this query?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                RecentsDatabase database=new RecentsDatabase(context);
                                database.deleteQuery(holder.getAdapterPosition());
                                recents.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                                notifyDataSetChanged();
                            }
                        }).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return recents.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView query;
        ImageView search,delete;

        ViewHolder(View itemView) {
            super(itemView);
            search = itemView.findViewById(R.id.search);
            delete = itemView.findViewById(R.id.remove);
            query = itemView.findViewById(R.id.query);
        }
    }
}