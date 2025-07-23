package com.drivingbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.drivingbuddy.data.model.Goal;
import com.drivingbuddy.ui.goals.GoalViewModel;
import com.drivingbuddy.ui.goals.GoalAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.FrameLayout;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class GoalsFragment extends Fragment {

    private AutoCompleteTextView goalDropdown;
    private FrameLayout goalContainer;
    private RecyclerView goalRecyclerView;
    private GoalViewModel goalViewModel;
    private ArrayAdapter<String> adapter;
    private GoalAdapter goalAdapter;

    private final List<Goal> goals = new ArrayList<>();
    private final List<String> allGoals = Arrays.asList(
            "Reduce sudden braking",
            "Reduce sharp turns",
            "Reduce inconsistent speeds",
            "Reduce lane deviation"
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_goals, container, false);

        goalDropdown = root.findViewById(R.id.goal_dropdown);
        goalContainer = root.findViewById(R.id.goal_container);

        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>(allGoals));
        goalDropdown.setAdapter(adapter);

        goalRecyclerView = new RecyclerView(requireContext());
        goalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        goalAdapter = new GoalAdapter(getContext(), goals, goal -> {
            int pos = findGoalPosition(goal.getTitle());
            if (pos != -1) {
                goals.remove(pos);
                goalAdapter.notifyItemRemoved(pos);
            }

            goalViewModel.deleteGoal(goal.getId()).observe(getViewLifecycleOwner(), success -> {
                if (Boolean.TRUE.equals(success)) {
                    Toast.makeText(getContext(), "Goal deleted", Toast.LENGTH_SHORT).show();
                    reloadGoalsFromBackend();
                } else {
                    Toast.makeText(getContext(), "Failed to delete goal", Toast.LENGTH_SHORT).show();
                    reloadGoalsFromBackend();
                }
            });
        });
        goalRecyclerView.setAdapter(goalAdapter);

        goalContainer.removeAllViews();
        goalContainer.addView(goalRecyclerView);

        goalViewModel = new ViewModelProvider(this).get(GoalViewModel.class);

        reloadGoalsFromBackend();

        goalDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGoal = adapter.getItem(position);
            if (findGoalPosition(selectedGoal) != -1) {
                Toast.makeText(getContext(), "Goal already added", Toast.LENGTH_SHORT).show();
                goalDropdown.setText("");
                return;
            }
            goalDropdown.setEnabled(false);

            goalViewModel.createGoal(selectedGoal).observe(getViewLifecycleOwner(), goal -> {
                goalDropdown.setEnabled(true);

                if (goal != null) {
                    goals.add(goal);
                    goalAdapter.notifyItemInserted(goals.size() - 1);
                    Toast.makeText(getContext(), "Goal added!", Toast.LENGTH_SHORT).show();
                    reloadGoalsFromBackend();
                } else {
                    Toast.makeText(getContext(), "Failed to add goal", Toast.LENGTH_SHORT).show();
                    reloadGoalsFromBackend();
                }
            });
            goalDropdown.setText(""); // reset

        });

        return root;
    }

    private void reloadGoalsFromBackend() {
        goalViewModel.getGoals().observe(getViewLifecycleOwner(), backendGoals -> {
            goals.clear();
            if (backendGoals != null) {
                for (Goal goal : backendGoals) {
                    goals.add(goal);
                }
            }
            goalAdapter.notifyDataSetChanged();
        });
    }

    private int findGoalPosition(String title) {
        for (int i = 0; i < goals.size(); i++) {
            Goal currentGoal = goals.get(i);
            String currentTitle = currentGoal.getTitle();

            if (currentTitle.equals(title)) {
                return i;
            }        }
        return -1;
    }
}