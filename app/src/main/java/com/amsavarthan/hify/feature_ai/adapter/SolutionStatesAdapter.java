package com.amsavarthan.hify.feature_ai.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.feature_ai.activities.ResultState;
import com.amsavarthan.hify.feature_ai.models.State;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SolutionStatesAdapter extends RecyclerView.Adapter<SolutionStatesAdapter.ViewHolder> {

    private List<State> states;
    private Context context;

    public SolutionStatesAdapter(List<State> states) {
        this.states = states;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_states,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.name.setText(states.get(holder.getAdapterPosition()).getName());

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResultState.startActivity(context,states.get(holder.getAdapterPosition()).getSolution_title(),states.get(holder.getAdapterPosition()).getName(),states.get(holder.getAdapterPosition()).getInput(),states.get(holder.getAdapterPosition()).getQuery());
            }
        });


    }

    @Override
    public int getItemCount() {
        return states.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;

        public ViewHolder(View itemView) {
            super(itemView);

            name=itemView.findViewById(R.id.textview);

        }
    }

}
