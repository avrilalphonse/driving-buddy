package com.drivingbuddy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class GoalsFragment extends Fragment {

    private AutoCompleteTextView goalDropdown;
    private LinearLayout goalContainer;

    private final List<String> allGoals = Arrays.asList(
            "Reduce sudden braking",
            "Reduce sharp turns",
            "Reduce inconsistent speeds",
            "Reduce lane deviation"
    );

    private final Map<String, Integer> dummyProgressData = new HashMap<>();
    private final Map<String, List<String>> dummyTipsData = new HashMap<>();
    private final Set<String> addedGoals = new HashSet<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_goals, container, false);

        goalDropdown = root.findViewById(R.id.goal_dropdown);
        goalContainer = root.findViewById(R.id.goal_container);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, allGoals);
        goalDropdown.setAdapter(adapter);

        goalDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGoal = adapter.getItem(position);
            addGoal(inflater, selectedGoal);
            goalDropdown.setText(""); // reset
        });

        generateDummyData();

        return root;
    }

    private void generateDummyData() {
        dummyProgressData.put("Reduce sudden braking", 45);
        dummyProgressData.put("Reduce sharp turns", 70);
        dummyProgressData.put("Reduce inconsistent speeds", 30);
        dummyProgressData.put("Reduce lane deviation", 60);

        dummyTipsData.put("Reduce sudden braking", Arrays.asList(
                "Ease into the brake",
                "Brake in advance",
                "Maintain safe distance"
        ));
        dummyTipsData.put("Reduce sharp turns", Arrays.asList(
                "Slow down before turning",
                "Keep both hands on wheel",
                "Avoid jerky steering movements"
        ));
        dummyTipsData.put("Reduce inconsistent speeds", Arrays.asList(
                "Use cruise control where possible",
                "Anticipate traffic flow",
                "Keep steady pressure on gas pedal"
        ));
        dummyTipsData.put("Reduce lane deviation", Arrays.asList(
                "Focus on lane markings",
                "Avoid distractions",
                "Use gentle steering corrections"
        ));
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
    private void addGoal(LayoutInflater inflater, String goalTitle) {
        if (addedGoals.contains(goalTitle)) {
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

        int progress = dummyProgressData.getOrDefault(goalTitle, 0);
        titleView.setText(goalTitle);
        progressBar.setProgress(progress);
        progressPercent.setText("Progress: " + progress + "%");

        List<String> tips = dummyTipsData.get(goalTitle);
        if (tips != null && tips.size() >= 3) {
            TextView tip1 = goalCardRoot.findViewById(R.id.goal_tip_1);
            TextView tip2 = goalCardRoot.findViewById(R.id.goal_tip_2);
            TextView tip3 = goalCardRoot.findViewById(R.id.goal_tip_3);

            tip1.setText("• " + tips.get(0));
            tip2.setText("• " + tips.get(1));
            tip3.setText("• " + tips.get(2));
        }

        // Expandable on click
        goalCard.setOnClickListener(v -> toggleExpandableSection(expandableSection));

        // Swipe-to-delete logic
        final float[] startX = {0};
        final boolean[] isSwiping = {false};

        goalCard.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX[0] = event.getX();
                    isSwiping[0] = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaX = event.getX() - startX[0];
                    if (Math.abs(deltaX) > 20) {
                        isSwiping[0] = true;
                    }
                    if (isSwiping[0]) {
                        v.setTranslationX(deltaX);
                        float alpha = 1 - Math.min(1f, Math.abs(deltaX) / v.getWidth());
                        v.setAlpha(alpha);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float totalDelta = event.getX() - startX[0];
                    if (isSwiping[0] && Math.abs(totalDelta) > v.getWidth() * 0.4f) {
                        v.animate()
                                .translationX(totalDelta > 0 ? v.getWidth() : -v.getWidth())
                                .alpha(0f)
                                .setDuration(200)
                                .withEndAction(() -> {
                                    goalContainer.removeView(goalCardRoot);
                                    addedGoals.remove(goalTitle);
                                }).start();
                    } else {
                        // Reset position
                        v.animate().translationX(0).alpha(1f).setDuration(200).start();

                        // Not swiped — treat like tap
                        if (!isSwiping[0]) {
                            toggleExpandableSection(expandableSection);
                        }
                    }
                    return true;
            }
            return false;
        });

        // Fallback delete button
        deleteButton.setOnClickListener(v -> {
            goalContainer.removeView(goalCardRoot);
            addedGoals.remove(goalTitle);
        });

        goalContainer.addView(goalCardRoot);
        addedGoals.add(goalTitle);
    }



}
