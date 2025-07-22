package com.drivingbuddy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.lifecycle.ViewModelProvider;
import com.drivingbuddy.data.model.Goal;
import com.drivingbuddy.ui.goals.GoalViewModel;
import java.util.ArrayList;

public class GoalsFragment extends Fragment {

    private AutoCompleteTextView goalDropdown;
    private LinearLayout goalContainer;
    private GoalViewModel goalViewModel;
    private ArrayAdapter<String> adapter;

    private final List<String> allGoals = Arrays.asList(
            "Reduce sudden braking",
            "Reduce sharp turns",
            "Reduce inconsistent speeds",
            "Reduce lane deviation"
    );
    private final Set<String> addedGoals = new HashSet<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_goals, container, false);

        goalDropdown = root.findViewById(R.id.goal_dropdown);
        goalContainer = root.findViewById(R.id.goal_container);

        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>(allGoals));
        goalDropdown.setAdapter(adapter);

        goalViewModel = new ViewModelProvider(this).get(GoalViewModel.class);
        goalViewModel.getGoals().observe(getViewLifecycleOwner(), goals -> {
            goalContainer.removeAllViews();
            addedGoals.clear();
            adapter.clear();
            adapter.addAll(allGoals);
            
            if (goals != null) {
                for (Goal goal : goals) {
                    addGoalCard(inflater, goal);
                    addedGoals.add(goal.getTitle());
                    adapter.remove(goal.getTitle());
                }
                adapter.notifyDataSetChanged();
            }
        });

        goalDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGoal = adapter.getItem(position);

            goalViewModel.createGoal(selectedGoal).observe(getViewLifecycleOwner(), goal -> {
                if (goal != null) {
                    Toast.makeText(getContext(), "Goal added!", Toast.LENGTH_SHORT).show();
                    
                    addGoalCard(getLayoutInflater(), goal);
                    addedGoals.add(goal.getTitle());
                    adapter.remove(selectedGoal);
                    adapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(getContext(), "Failed to add goal", Toast.LENGTH_SHORT).show();
                }
            });

            goalDropdown.setText(""); // reset
        });

        return root;
    }

    private void toggleExpandableSection(View expandableSection) {
        if (expandableSection.getVisibility() == View.GONE) {
            expandableSection.setVisibility(View.VISIBLE);
            expandableSection.setAlpha(0f);
            expandableSection.setScaleY(0f);
            expandableSection.animate()
                    .alpha(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
        } else {
            expandableSection.animate()
                    .alpha(0f)
                    .scaleY(0f)
                    .setDuration(200)
                    .withEndAction(() -> expandableSection.setVisibility(View.GONE))
                    .start();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addGoalCard(LayoutInflater inflater, Goal goal) {
        if (addedGoals.contains(goal.getTitle())) {
            Toast.makeText(getContext(), "Goal already added", Toast.LENGTH_SHORT).show();
            return;
        }

        View goalCardRoot = inflater.inflate(R.layout.goal_card_template, goalContainer, false);
        CardView goalCard = goalCardRoot.findViewById(R.id.goal_card);
        LinearLayout expandableSection = goalCardRoot.findViewById(R.id.goal_expandable_section);
        ImageButton deleteButton = goalCardRoot.findViewById(R.id.delete_goal);
        TextView titleView = goalCardRoot.findViewById(R.id.goal_card_text);
        ProgressBar progressBar = goalCardRoot.findViewById(R.id.goal_progress);
        TextView progressPercent = goalCardRoot.findViewById(R.id.goal_progress_percent);

        int progress = goal.getProgress();
        titleView.setText(goal.getTitle());
        progressBar.setProgress(progress);
        progressPercent.setText("Progress: " + progress + "%");

        List<String> tips = goal.getTips();
        if (tips != null && tips.size() >= 3) {
            TextView tip1 = goalCardRoot.findViewById(R.id.goal_tip_1);
            TextView tip2 = goalCardRoot.findViewById(R.id.goal_tip_2);
            TextView tip3 = goalCardRoot.findViewById(R.id.goal_tip_3);

            tip1.setText("• " + tips.get(0));
            tip2.setText("• " + tips.get(1));
            tip3.setText("• " + tips.get(2));
        }

        final float[] startX = {0};
        final long[] startTime = {0};
        final boolean[] isSwiping = {false};

        goalCard.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX[0] = event.getX();
                    startTime[0] = System.currentTimeMillis();
                    isSwiping[0] = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaX = event.getX() - startX[0];
                    if (Math.abs(deltaX) > 20) {
                        isSwiping[0] = true;
                    }
                    if (deltaX < 0) {  // left swipe only
                        v.setTranslationX(deltaX);
                        deleteButton.setVisibility(View.VISIBLE);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float totalDelta = event.getX() - startX[0];
                    long duration = System.currentTimeMillis() - startTime[0];

                    if (!isSwiping[0] && Math.abs(totalDelta) < 20 && duration < 200) {
                        // tap
                        toggleExpandableSection(expandableSection);
                        return true;
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
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Goal")
                    .setMessage("Are you sure you want to delete this goal?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        goalViewModel.deleteGoal(goal.getId()).observe(getViewLifecycleOwner(), success -> {
                            if (Boolean.TRUE.equals(success)) {
                                Toast.makeText(getContext(), "Goal deleted", Toast.LENGTH_SHORT).show();
                                
                                 goalContainer.removeView(goalCardRoot);
                                addedGoals.remove(goal.getTitle());
                                adapter.add(goal.getTitle());
                                adapter.notifyDataSetChanged();
                                
                            } else {
                                Toast.makeText(getContext(), "Failed to delete goal", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        goalCard.animate().translationX(0).setDuration(200).start();
                        deleteButton.setVisibility(View.INVISIBLE);
                    })
                    .show();
        });

        goalContainer.addView(goalCardRoot);
    }

}