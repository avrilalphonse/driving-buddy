package com.drivingbuddy;

import android.content.Context;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GoalsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GoalsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Spinner goalSpinner;
    private Button addGoalButton;
    private List<Button> deleteGoalButtons;
    private List<TextView> goalCardTexts;
    private List<CardView> goalCards;
    private List<CardView> goalDetails;
    private List<String> currentGoals = new ArrayList<>();
    private List<Integer> goalProgress = new ArrayList<>();
    private final int MAX_GOALS = 3;

    private Context context;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GoalsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GoalsFragment newInstance(String param1, String param2) {
        GoalsFragment fragment = new GoalsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public GoalsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goals, container, false);

        context = requireContext();

        // UI elements
        goalSpinner = view.findViewById(R.id.goal_spinner);
        addGoalButton = view.findViewById(R.id.add_goal_button);

        goalCards = Arrays.asList(
                view.findViewById(R.id.goal_card_1),
                view.findViewById(R.id.goal_card_2),
                view.findViewById(R.id.goal_card_3)
        );

        goalDetails = Arrays.asList(
                view.findViewById(R.id.goal_card_detail_1),
                view.findViewById(R.id.goal_card_detail_2),
                view.findViewById(R.id.goal_card_detail_3)
        );

        goalCardTexts = Arrays.asList(
                view.findViewById(R.id.goal_card_text_1),
                view.findViewById(R.id.goal_card_text_2),
                view.findViewById(R.id.goal_card_text_3)
        );

        deleteGoalButtons = Arrays.asList(
                view.findViewById(R.id.delete_goal_1),
                view.findViewById(R.id.delete_goal_2),
                view.findViewById(R.id.delete_goal_3)
        );

        updateUI();

        // Set delete listeners
        for (int i = 0; i < deleteGoalButtons.size(); i++) {
            final int index = i;
            deleteGoalButtons.get(i).setOnClickListener(v -> {
                if (index < currentGoals.size()) {
                    currentGoals.remove(index);
                    goalProgress.remove(index);
                    updateUI();
                    TextView messageView = view.findViewById(R.id.message_view);
                    messageView.setText(""); // clear any error messages on delete
                }
            });
        }

        addGoalButton.setOnClickListener(v -> {
            String selectedGoal = goalSpinner.getSelectedItem().toString();
            TextView messageView = view.findViewById(R.id.message_view);
            if (currentGoals.size() < MAX_GOALS && !currentGoals.contains(selectedGoal)) {
                goalProgress.add(0); // initial progress
                currentGoals.add(selectedGoal);
                updateUI();
                // clear message
                messageView.setText("");
            }
            else {
                // Show error message
                messageView.setText("You can only add up to " + MAX_GOALS + " unique goals.");
            }
        });
        return view;
    }

    private void updateUI() {
        // Hide all main cards and details first
        for (CardView card : goalCards) card.setVisibility(View.GONE);
        for (CardView detail : goalDetails) detail.setVisibility(View.GONE);

        // Show based on currentGoals
        CardView detail = null;
        for (int i = 0; i < currentGoals.size(); i++) {
            CardView card = goalCards.get(i);
            detail = goalDetails.get(i);

            card.setVisibility(View.VISIBLE);
            detail.setVisibility(View.VISIBLE);

            // Set main card text
            TextView cardText = goalCardTexts.get(i);
            cardText.setText(currentGoals.get(i));

            // Set detail title and body
            TextView detailTitle = detail.findViewById(getResources().getIdentifier("goal_detail_title_" + (i + 1), "id", context.getPackageName()));
            TextView detailBody = detail.findViewById(getResources().getIdentifier("goal_detail_body_" + (i + 1), "id", context.getPackageName()));

            detailTitle.setText(currentGoals.get(i));
            detailBody.setText("Detailed summary for " + currentGoals.get(i));

            ProgressBar progressBar = detail.findViewById(
                    getResources().getIdentifier("goal_progress_" + (i + 1), "id", context.getPackageName())
            );
            progressBar.setProgress(goalProgress.get(i)); // You can make this dynamic based on goal completion

        }


        addGoalButton.setEnabled(currentGoals.size() < MAX_GOALS);
    }
}