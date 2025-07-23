package com.drivingbuddy;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        View insights = view.findViewById(R.id.long_term_insight_chart);
        View.OnClickListener goToInsights = v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
            bottomNav.setSelectedItemId(R.id.insightsFragment);
        };
        insights.setOnClickListener(goToInsights);

        View goal1 = view.findViewById(R.id.goal1_container);
        View goal2 = view.findViewById(R.id.goal2_container);
        View goal3 = view.findViewById(R.id.goal3_container);
        View.OnClickListener goToGoals = v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
            bottomNav.setSelectedItemId(R.id.goalsFragment);
        };
        goal1.setOnClickListener(goToGoals);
        goal2.setOnClickListener(goToGoals);
        goal3.setOnClickListener(goToGoals);


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