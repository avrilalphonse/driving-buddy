package com.drivingbuddy.ui.home;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.drivingbuddy.data.api.ApiClient;
import com.drivingbuddy.data.api.SensorDataApiService;
import com.drivingbuddy.data.DrivingDataCache;
import com.drivingbuddy.data.model.BucketedDataResponse;
import com.drivingbuddy.data.model.DriveDataResponse;
import com.drivingbuddy.data.model.Goal;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 * xyz
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

    private SensorDataApiService apiService;
    private final List<DriveData> drives = new ArrayList<>();
    private LineChart insightChart;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    private List<Goal> currentGoals = new ArrayList<>();

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
        String userEmail = authViewModel.getUserEmail();

        // welcome banner
        TextView welcomeHeader = view.findViewById(R.id.welcome_header);
        if (userName != null && !userName.isEmpty()) {
            welcomeHeader.setText("Welcome, " + userName + "!");
        } else {
            welcomeHeader.setText("Welcome!");
        }

        insightChart = view.findViewById(R.id.long_term_insight_chart);
        progressBar = view.findViewById(R.id.chart_progress_bar);

        View chartContainer = view.findViewById(R.id.chart_container);
        chartContainer.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
            bottomNav.setSelectedItemId(R.id.insightsFragment);
        });

        // api service setup
        apiService = ApiClient.getClient().create(SensorDataApiService.class);
        if ("test@gmail.com".equals(userEmail)) {
            BucketedDataResponse cachedData = DrivingDataCache.getCachedData();
            if (cachedData != null) {
                // use cached data if available
                processDriveData(cachedData);
            } else {
                //show loading circle!
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                // fetch data from API
                fetchDriveData();
            }
        } else {
            // for demo users, show empty state!
            showEmptyChart();
        }

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
                currentGoals = goals;
                if ("test@gmail.com".equals(userEmail)) {
                    // Update progress if we have cached data
                    updateGoalProgress(goals);
                }
                homeGoalAdapter.setGoals(goals);
            }
        });

        return view;
    }

    private void updateGoalProgress(List<Goal> goals) {
        // Get cached data to calculate progress
        BucketedDataResponse cachedData = DrivingDataCache.getCachedData();
        if (cachedData == null || goals == null) {
            return;
        }

        // Calculate progress from cached data
        int totalDrives = 0;
        int goodBrakingDrives = 0;
        int goodSpeedDrives = 0;
        int goodLaneDeviationDrives = 0;

        for (DriveDataResponse drive : cachedData.getDrives()) {
            totalDrives++;

            if (drive.getIncidents().getSuddenBraking() == 0) {
                goodBrakingDrives++;
            }
            if (drive.getIncidents().getInconsistentSpeed() == 0) {
                goodSpeedDrives++;
            }
            if (drive.getIncidents().getLaneDeviation() == 0) {
                goodLaneDeviationDrives++;
            }
        }

        // Calculate progress percentages
        int brakingProgress = Math.min(100, goodBrakingDrives * 2);
        int speedProgress = Math.min(100, goodSpeedDrives * 2);
        int laneProgress = Math.min(100, goodLaneDeviationDrives * 2);

        // Update each goal with its progress
        for (Goal goal : goals) {
            switch (goal.getTitle()) {
                case "Reduce sudden braking":
                    goal.setProgress(brakingProgress);
                    break;
                case "Reduce inconsistent speeds":
                    goal.setProgress(speedProgress);
                    break;
                case "Reduce lane deviation":
                    goal.setProgress(laneProgress);
                    break;
                case "Reduce sharp turns":
                    goal.setProgress(0); // No data available
                    break;
            }
        }
    }

    private void fetchDriveData() {
        Call<BucketedDataResponse> call = apiService.getBucketedData(10);

        call.enqueue(new Callback<BucketedDataResponse>() {
            @Override
            public void onResponse(Call<BucketedDataResponse> call, Response<BucketedDataResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    BucketedDataResponse data = response.body();
                    DrivingDataCache.setCachedData(data);
                    processDriveData(data);
                } else {
                    showEmptyChart();
                }
            }

            @Override
            public void onFailure(Call<BucketedDataResponse> call, Throwable t) {
                // Hide progress bar
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                showEmptyChart();
            }
        });
    }

    private void processDriveData(BucketedDataResponse data) {
        drives.clear();

        // create DriveData objects
        List<DriveData> allDrives = new ArrayList<>();
        for (DriveDataResponse drive : data.getDrives()) {
            String displayDate = formatDateForDisplay(drive.getDate());

            // ignoring sharp turns for now
            DriveData driveData = new DriveData(
                    displayDate,
                    drive.getIncidents().getSuddenBraking(),
                    0,
                    drive.getIncidents().getInconsistentSpeed(),
                    drive.getIncidents().getLaneDeviation()
            );
            allDrives.add(driveData);
        }

        // API call returns newest first, but we want oldest first for charts
        Collections.reverse(allDrives);

        // show 5 most recent drives
        int startIndex = Math.max(0, allDrives.size() - 5);
        for (int i = startIndex; i < allDrives.size(); i++) {
            drives.add(allDrives.get(i));
        }

        updateChart();

        // update goals with progress now that we have data!
        if (!currentGoals.isEmpty()) {
            updateGoalProgress(currentGoals);
            homeGoalAdapter.setGoals(currentGoals);
        }
    }

    private void updateChart() {
        insightChart.setData(createCombinedInsightChart(drives));
        styleChart(insightChart);
        insightChart.invalidate();
    }

    private void showEmptyChart() {
        insightChart.setNoDataText("No driving data available yet");
        insightChart.setNoDataTextColor(Color.GRAY);
        insightChart.invalidate();
    }

    private String formatDateForDisplay(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d", Locale.US);
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private void styleChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.getLegend().setTextColor(Color.GRAY);
        chart.getAxisRight().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setClickable(true);
        chart.setFocusable(true);

        chart.setViewPortOffsets(40f, 20f, 40f, 50f);

        // show date range instead of individual dates
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.GRAY);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setTextSize(10f);
        xAxis.setLabelCount(2, true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (drives.isEmpty()) return "";
                if (value <= 0) {
                    return drives.get(0).date; // oldest
                } else if (value >= drives.size() - 1) {
                    return drives.get(drives.size() - 1).date; // newest
                }
                return "";
            }
        });

        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(Color.parseColor("#E0E0E0"));
        chart.getAxisLeft().setAxisLineColor(Color.GRAY);
        chart.getAxisLeft().setTextColor(Color.GRAY);
        chart.getAxisLeft().setGranularity(1f);
        chart.getAxisLeft().setAxisMinimum(0f);

        // little animation to make the graph :)
        chart.animateY(500);
    }

    private LineData createCombinedInsightChart(List<DriveData> drives) {
        List<Entry> suddenBrakingEntries = new ArrayList<>();
        List<Entry> inconsistentSpeedEntries = new ArrayList<>();
        List<Entry> laneDeviationEntries = new ArrayList<>();

        for (int i = 0; i < drives.size(); i++) {
            suddenBrakingEntries.add(new Entry(i, drives.get(i).sharpBraking));
            inconsistentSpeedEntries.add(new Entry(i, drives.get(i).inconsistentSpeeds));
            laneDeviationEntries.add(new Entry(i, drives.get(i).reducedLaneDeviation));
        }

        // Sudden Braking
        LineDataSet suddenBrakingSet = new LineDataSet(suddenBrakingEntries, "Sudden Braking");
        suddenBrakingSet.setColor(Color.parseColor("#E76F51"));
        suddenBrakingSet.setLineWidth(2.5f);
        suddenBrakingSet.setDrawCircles(false);
        suddenBrakingSet.setDrawValues(false);
        suddenBrakingSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        suddenBrakingSet.setCubicIntensity(0.2f);

        // Inconsistent Speed
        LineDataSet inconsistentSpeedSet = new LineDataSet(inconsistentSpeedEntries, "Inconsistent Speed");
        inconsistentSpeedSet.setColor(Color.parseColor("#2A9D8F"));
        inconsistentSpeedSet.setLineWidth(2.5f);
        inconsistentSpeedSet.setDrawCircles(false);
        inconsistentSpeedSet.setDrawValues(false);
        inconsistentSpeedSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        inconsistentSpeedSet.setCubicIntensity(0.2f);

        // Lane Deviation
        LineDataSet laneDeviationSet = new LineDataSet(laneDeviationEntries, "Lane Deviation");
        laneDeviationSet.setColor(Color.parseColor("#264653"));
        laneDeviationSet.setLineWidth(2.5f);
        laneDeviationSet.setDrawCircles(false);
        laneDeviationSet.setDrawValues(false);
        laneDeviationSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        laneDeviationSet.setCubicIntensity(0.2f);

        LineData lineData = new LineData();
        lineData.addDataSet(suddenBrakingSet);
        lineData.addDataSet(inconsistentSpeedSet);
        lineData.addDataSet(laneDeviationSet);

        return lineData;
    }
}