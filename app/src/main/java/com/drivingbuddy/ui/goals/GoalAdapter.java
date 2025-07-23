package com.drivingbuddy.ui.goals;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.drivingbuddy.R;
import com.drivingbuddy.data.model.Goal;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    public interface GoalActionListener {
        void onDeleteGoal(Goal goal);
    }

    private final List<Goal> goals;
    private final GoalActionListener listener;
    private final Context context;

    public GoalAdapter(Context context, List<Goal> goals, GoalActionListener listener) {
        this.context = context;
        this.goals = goals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_card_template, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        holder.goalCard.setTranslationX(0);
        holder.deleteButton.setVisibility(View.INVISIBLE);

        holder.bind(goals.get(position), listener, context);
    }

    @Override
    public int getItemCount() {
        return goals == null ? 0 : goals.size();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        CardView goalCard;
        LinearLayout expandableSection;
        ImageButton deleteButton;
        TextView titleView, progressPercent, tip1, tip2, tip3;
        ProgressBar progressBar;

        float startX;
        boolean isSwiping;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            goalCard = itemView.findViewById(R.id.goal_card);
            expandableSection = itemView.findViewById(R.id.goal_expandable_section);
            deleteButton = itemView.findViewById(R.id.delete_goal);
            titleView = itemView.findViewById(R.id.goal_card_text);
            progressBar = itemView.findViewById(R.id.goal_progress);
            progressPercent = itemView.findViewById(R.id.goal_progress_percent);
            tip1 = itemView.findViewById(R.id.goal_tip_1);
            tip2 = itemView.findViewById(R.id.goal_tip_2);
            tip3 = itemView.findViewById(R.id.goal_tip_3);
        }

        public void bind(Goal goal, GoalActionListener listener, Context context) {
            titleView.setText(goal.getTitle());
            int progress = goal.getProgress();
            progressBar.setProgress(progress);
            List<String> tips = goal.getTips();
            if (tips != null && tips.size() >= 3) {
                tip1.setText("• " + tips.get(0));
                tip2.setText("• " + tips.get(1));
                tip3.setText("• " + tips.get(2));
            }

            expandableSection.setVisibility(View.GONE);

            goalCard.setOnClickListener(v -> {
                if (expandableSection.getVisibility() == View.GONE) {
                    expandableSection.setVisibility(View.VISIBLE);
                    expandableSection.setAlpha(0f);
                    expandableSection.setScaleY(0f);
                    expandableSection.animate()
                            .alpha(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                    progressPercent.setText("Progress: " + progress + "%");
                } else {
                    expandableSection.animate()
                            .alpha(0f)
                            .scaleY(0f)
                            .setDuration(200)
                            .withEndAction(() -> expandableSection.setVisibility(View.GONE))
                            .start();
                }
            });

            goalCard.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        isSwiping = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getX() - startX;
                        if (Math.abs(deltaX) > 20) {
                            isSwiping = true;
                        }
                        if (deltaX < 0) {
                            v.setTranslationX(deltaX);
                            deleteButton.setVisibility(View.VISIBLE);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        float totalDelta = event.getX() - startX;
                        if (!isSwiping && Math.abs(totalDelta) < 20) {
                            v.performClick();
                        }
                        if (Math.abs(totalDelta) > v.getWidth() * 0.3f) {
                            v.animate().translationX(-deleteButton.getWidth()).setDuration(200).start();
                            deleteButton.setVisibility(View.VISIBLE);
                        } else {
                            v.animate().translationX(0).setDuration(200).start();
                            deleteButton.setVisibility(View.INVISIBLE);
                        }
                        return true;
                }
                return false;
            });

            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Goal")
                        .setMessage("Are you sure you want to delete this goal?")
                        .setPositiveButton("Delete", (dialog, which) -> listener.onDeleteGoal(goal))
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            goalCard.animate().translationX(0).setDuration(200).start();
                            deleteButton.setVisibility(View.INVISIBLE);
                        })
                        .show();
            });
        }
    }
}