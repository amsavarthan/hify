package com.amsavarthan.hify.feature_ai.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.feature_ai.activities.Preview;
import com.amsavarthan.hify.feature_ai.models.Solution;
import com.amsavarthan.hify.feature_ai.models.State;
import com.amsavarthan.hify.feature_ai.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SolutionStateResultAdapter extends RecyclerView.Adapter<SolutionStateResultAdapter.SolutionViewHolder> {

    private List<Solution> solutions;
    private Context context;
    private int lastAnimatedPosition = -1;

    public SolutionStateResultAdapter(List<Solution> solutions) {
        this.solutions = solutions;
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
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public SolutionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        context = parent.getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.item_solution_state, parent, false);
        return new SolutionViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull final SolutionViewHolder holder, int position) {

        holder.title.setText(solutions.get(holder.getAdapterPosition()).getTitle());
        holder.description.setText(solutions.get(position).getDescription());

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.mipmap.placeholder))
                .load(solutions.get(holder.getAdapterPosition()).getSrc())
                .into(holder.src);

        holder.copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData;

                if (StringUtils.isNotEmpty(solutions.get(holder.getAdapterPosition()).getDescription())) {
                    clipData = ClipData.newPlainText("Professor Lv", solutions.get(holder.getAdapterPosition()).getDescription());
                    if (clipboardManager != null) {
                        clipboardManager.setPrimaryClip(clipData);
                        Toast.makeText(context, "Description copied to clipboard", Toast.LENGTH_LONG).show();
                    }
                } else if (StringUtils.isNotEmpty(solutions.get(holder.getAdapterPosition()).getSrc())) {
                    clipData = ClipData.newPlainText("Professor Lv", solutions.get(holder.getAdapterPosition()).getSrc());
                    if (clipboardManager != null) {
                        clipboardManager.setPrimaryClip(clipData);
                        Toast.makeText(context, "Image link copied to clipboard", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

        if (solutions.get(holder.getAdapterPosition()).isDefaultCard()) {

            holder.description.setVisibility(View.VISIBLE);
            holder.src.setVisibility(View.VISIBLE);

        }

        if (solutions.get(holder.getAdapterPosition()).isStateResult()) {

            if(solutions.get(holder.getAdapterPosition()).isStepbyStepSolution()){
                holder.description.setVisibility(View.GONE);
                holder.src.setVisibility(View.VISIBLE);
                holder.src.setMinimumHeight(solutions.get(holder.getAdapterPosition()).getHeight());


            }else{

                holder.description.setVisibility(View.VISIBLE);
                holder.src.setVisibility(View.VISIBLE);
                holder.src.setMinimumHeight(solutions.get(holder.getAdapterPosition()).getHeight());
                holder.src.setMaxHeight(solutions.get(holder.getAdapterPosition()).getHeight());

            }

        }else{

            if(StringUtils.isEmpty(holder.description.getText())){
                holder.description.setVisibility(View.GONE);
                holder.src.setVisibility(View.VISIBLE);
                holder.src.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(new Intent(context,Preview.class).putExtra("url",solutions.get(holder.getAdapterPosition()).getSrc()));
                    }
                });
            }else{
                holder.description.setVisibility(View.VISIBLE);
                holder.src.setVisibility(View.GONE);
            }

        }

        setAnimation(holder.mainView, holder.getAdapterPosition());

        holder.states=new ArrayList<>();
        SolutionStatesAdapter adapter=new SolutionStatesAdapter(holder.states);
        holder.recyclerView.setAdapter(adapter);

        for(int i=0;i<solutions.get(holder.getAdapterPosition()).getState_count();i++){
            State state=new State();
            state.setSolution_title(solutions.get(holder.getAdapterPosition()).getTitle());
            state.setQuery(solutions.get(holder.getAdapterPosition()).getInput());
            state.setInput(solutions.get(holder.getAdapterPosition()).state_input.get(i));
            state.setName(solutions.get(holder.getAdapterPosition()).state_name.get(i));
            holder.states.add(state);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public int getItemCount() {
        return solutions.size();
    }

    private void setAnimation(View viewToAnimate, int position) {

        viewToAnimate.clearAnimation();

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            viewToAnimate.setTranslationY(Utils.getScreenHeight(context));
            viewToAnimate.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(700)
                    .start();
        }

    }

    public static class SolutionViewHolder extends RecyclerView.ViewHolder {

        CardView mainView;
        TextView title;
        TextView description;
        TextView copy;
        ImageView src;
        RecyclerView recyclerView;
        List<State> states;

        SolutionViewHolder(View itemView,Context context) {
            super(itemView);
            mainView = itemView.findViewById(R.id.result_pod_view);
            title = itemView.findViewById(R.id.text_result_pod_title);
            copy = itemView.findViewById(R.id.copy);
            description = itemView.findViewById(R.id.text_result_pod_description);
            src = itemView.findViewById(R.id.image);

            recyclerView=itemView.findViewById(R.id.recyclerView);
            LinearLayoutManager layoutManager=new LinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
    }
}