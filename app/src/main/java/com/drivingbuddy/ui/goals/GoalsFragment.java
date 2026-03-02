package com.drivingbuddy.ui.goals;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.drivingbuddy.R;
import com.drivingbuddy.data.api.ApiClient;
import com.drivingbuddy.data.api.SensorDataApiService;
import com.drivingbuddy.data.DrivingDataCache;
import com.drivingbuddy.data.model.BucketedDataResponse;
import com.drivingbuddy.data.model.DriveDataResponse;
import com.drivingbuddy.data.model.Goal;
import com.drivingbuddy.ui.auth.AuthViewModel;
import com.drivingbuddy.utils.GoalProgressCalculator;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.FrameLayout;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalsFragment extends Fragment {

    private AuthViewModel authViewModel;
    private AutoCompleteTextView goalDropdown;
    private FrameLayout goalContainer;
    private RecyclerView goalRecyclerView;
    private GoalViewModel goalViewModel;
    private ArrayAdapter<String> adapter;
    private GoalAdapter goalAdapter;
    private SensorDataApiService apiService;

    private final List<Goal> goals = new ArrayList<>();
    private final List<String> allGoals = Arrays.asList(
            "Reduce sudden braking",
            "Reduce sharp turns",
            "Reduce inconsistent speeds",
            "Reduce lane deviation"
    );

    private final Map<String, Integer> goalProgress = new HashMap<>();
    private boolean hasEnoughData = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_goals, container, false);

        String userName = authViewModel.getUserName();

        TextView goals_title = root.findViewById(R.id.goals_title);
        if (userName != null && !userName.isEmpty()) {
            goals_title.setText(userName + "'s Goals");
        }

        // api service setup
        apiService = ApiClient.getClient().create(SensorDataApiService.class);

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

        // fetch data from sensor-data collection
        fetchDrivingDataAndUpdateProgress();

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
                    // set initial progress if available
                    Integer progress = goalProgress.get(goal.getTitle());
                    if (progress != null) {
                        goal.setProgress(progress);
                    }
                    goal.setHasEnoughData(hasEnoughData);
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
                    Integer progress = goalProgress.get(goal.getTitle());
                    if (progress != null) {
                        goal.setProgress(progress);
                    } else {
                    }
                    goal.setHasEnoughData(hasEnoughData);
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

    private void fetchDrivingDataAndUpdateProgress() {
        String userID = authViewModel.getUserId();
        if (userID == null || userID.isEmpty()) {
            Log.w("GoalsFragment", "Missing user ID; skipping driving data fetch.");
            return;
        }
        Call<BucketedDataResponse> call = apiService.getPersistentSummaryData(userID);

        call.enqueue(new Callback<BucketedDataResponse>() {
            @Override
            public void onResponse(Call<BucketedDataResponse> call, Response<BucketedDataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BucketedDataResponse data = response.body();
                    calculateProgressFromData(data);
                } else {
                    Log.e("GoalsFragment", "Failed to fetch driving data");
                }
            }

            @Override
            public void onFailure(Call<BucketedDataResponse> call, Throwable t) {
                Log.e("GoalsFragment", "Network error: " + t.getMessage());
            }
        });
    }

    private void calculateProgressFromData(BucketedDataResponse data) {
        if (data == null) {
            return;
        }

        goalProgress.clear();
        GoalProgressCalculator.Result result = GoalProgressCalculator.calculate(data.getDrives());
        goalProgress.putAll(result.getProgress());
        hasEnoughData = result.hasEnoughData();
        updateGoalsWithProgress();
    }

    private void updateGoalsWithProgress() {
        for (Goal goal : goals) {
            Integer progress = goalProgress.get(goal.getTitle());
            if (progress != null) {
                goal.setProgress(progress);
            }
            goal.setHasEnoughData(hasEnoughData);
        }

        // notify adapter to refresh the views
        if (goalAdapter != null) {
            goalAdapter.notifyDataSetChanged();
        }
    }

}
