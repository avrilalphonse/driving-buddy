package com.drivingbuddy.ui.home;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.drivingbuddy.ui.auth.AuthViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drivingbuddy.DriveData;
import com.drivingbuddy.R;
import com.drivingbuddy.ui.goals.GoalViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private GoalViewModel goalViewModel;
    private HomeGoalAdapter homeGoalAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private AuthViewModel authViewModel;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        String userName = authViewModel.getUserName();

        TextView home_title = view.findViewById(R.id.home_title);
        if (userName != null && !userName.isEmpty()) {
            home_title.setText(userName + "'s Statistics");
        }

        View insights = view.findViewById(R.id.long_term_insight_chart);
        View.OnClickListener goToInsights = v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
            bottomNav.setSelectedItemId(R.id.insightsFragment);
        };
        insights.setOnClickListener(goToInsights);

        List<DriveData> drives = new ArrayList<>();
        drives.add(new DriveData("Jul 8", 0, 4, 2, 5));
        drives.add(new DriveData("Jul 10", 2, 6, 1, 3));
        drives.add(new DriveData("Jul 11", 5, 3, 7, 2));

        LineChart insightChart = view.findViewById(R.id.long_term_insight_chart);

        insightChart.setData(createCombinedInsightChart(drives));
        insightChart.getDescription().setEnabled(false);
        insightChart.getLegend().setEnabled(true);
        insightChart.getAxisRight().setEnabled(false);

        XAxis xAxis = insightChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return index >= 0 && index < drives.size() ? drives.get(index).date : "";
            }
        });

        insightChart.getAxisLeft().setDrawGridLines(false);
        insightChart.invalidate();

        // music
        View musicButton = view.findViewById(R.id.btn_start_drive_music);
        musicButton.setOnClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
            builder.setTitle("Choose Music Service");
            String[] services = {"Spotify", "Apple Music", "SoundCloud"};
            builder.setItems(services, (dialog, which) -> {
                String url = null;
                switch (which) {
                    case 0: // spotify
                        url = "https://open.spotify.com/search/driving%20music"; // driving music search
                        break;
                    case 1: // apple music
                        url = "https://music.apple.com/us/search?term=driving%20music"; 
                        break;
                    case 2: // soundcloud
                        url = "https://soundcloud.com/search/sets?q=driving%20music";
                        break;
                }
                if (url != null) {
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
                    startActivity(intent);
                }
            });
            builder.show();
        });

        // Goals Summary

        RecyclerView goalRecycler = view.findViewById(R.id.home_goal_recycler);
        TextView noGoalsMessage = view.findViewById(R.id.no_goals_message);

        homeGoalAdapter = new HomeGoalAdapter(new ArrayList<>(), goal -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
            bottomNav.setSelectedItemId(R.id.goalsFragment);
        });

        goalRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        goalRecycler.setAdapter(homeGoalAdapter);

        goalViewModel = new ViewModelProvider(this).get(GoalViewModel.class);
        goalViewModel.getGoals().observe(getViewLifecycleOwner(), goals -> {
            if (goals == null || goals.isEmpty()) {
                noGoalsMessage.setVisibility(View.VISIBLE);
                homeGoalAdapter.setGoals(new ArrayList<>());
            } else {
                noGoalsMessage.setVisibility(View.GONE);
                homeGoalAdapter.setGoals(goals);
            }
        });
        return view;

        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    private LineData createCombinedInsightChart(List<DriveData> drives) {
        List<Entry> suddenBrakingEntries = new ArrayList<>();
        List<Entry> inconsistentSpeedEntries = new ArrayList<>();
        List<Entry> sharpTurningEntries = new ArrayList<>();
        List<Entry> laneDeviationEntries = new ArrayList<>();

        for (int i = 0; i < drives.size(); i++) {
            suddenBrakingEntries.add(new Entry(i, drives.get(i).sharpBraking));
            inconsistentSpeedEntries.add(new Entry(i, drives.get(i).inconsistentSpeeds));
            sharpTurningEntries.add(new Entry(i, drives.get(i).sharpTurns));
            laneDeviationEntries.add(new Entry(i, drives.get(i).reducedLaneDeviation));
        }

        LineDataSet suddenBrakingSet = new LineDataSet(suddenBrakingEntries, "Sudden Braking");
        suddenBrakingSet.setColor(Color.parseColor("#E76F51"));
        suddenBrakingSet.setCircleColor(Color.parseColor("#E76F51"));

        LineDataSet heavyAccelSet = new LineDataSet(inconsistentSpeedEntries, "Inconsistent Speed");
        heavyAccelSet.setColor(Color.parseColor("#2A9D8F"));
        heavyAccelSet.setCircleColor(Color.parseColor("#2A9D8F"));

        LineDataSet sharpTurningSet = new LineDataSet(sharpTurningEntries, "Sharp Turning");
        sharpTurningSet.setColor(Color.parseColor("#E9C46A"));
        sharpTurningSet.setCircleColor(Color.parseColor("#E9C46A"));

        LineDataSet laneDeviationSet = new LineDataSet(laneDeviationEntries, "Lane Deviation");
        laneDeviationSet.setColor(Color.parseColor("#264653"));
        laneDeviationSet.setCircleColor(Color.parseColor("#264653"));

        LineData lineData = new LineData();
        lineData.addDataSet(suddenBrakingSet);
        lineData.addDataSet(heavyAccelSet);
        lineData.addDataSet(sharpTurningSet);
        lineData.addDataSet(laneDeviationSet);

        return lineData;
    }

}