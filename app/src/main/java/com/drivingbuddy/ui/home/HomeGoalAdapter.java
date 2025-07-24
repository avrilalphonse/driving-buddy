package com.drivingbuddy.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.drivingbuddy.R;
import com.drivingbuddy.data.model.Goal;
import java.util.List;

public class HomeGoalAdapter extends RecyclerView.Adapter<HomeGoalAdapter.GoalViewHolder> {

    public interface OnGoalClickListener {
        void onGoalClick(Goal goal);
    }

    private List<Goal> goals;
    private final OnGoalClickListener listener;

    public HomeGoalAdapter(List<Goal> goals, OnGoalClickListener listener) {
        this.goals = goals;
        this.listener = listener;
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_goal_card, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goals.get(position);
        holder.title.setText(goal.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onGoalClick(goal));
    }

    @Override
    public int getItemCount() {
        return goals == null ? 0 : goals.size();
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.home_goal_title);
        }
    }

}
