package com.drivingbuddy;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class InsightsFragment extends Fragment {

    private final List<DriveData> drives = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insights, container, false);

        populateDummyData();
        Collections.reverse(drives); // Ensure chronological order

        setupSummary(view);
        populateDriveCards(view, inflater);
        setupCharts(view);

        return view;
    }

    private void populateDummyData() {
        drives.clear();
        drives.add(new DriveData("Jul 11", 5, 3, 7, 2));
        drives.add(new DriveData("Jul 10", 2, 6, 1, 3));
        drives.add(new DriveData("Jul 8", 0, 4, 2, 1));
    }

    private void setupSummary(View view) {
        TextView summaryText = view.findViewById(R.id.summary_text);
        summaryText.setText(generateSummary());
    }

    private void populateDriveCards(View view, LayoutInflater inflater) {
        LinearLayout container = view.findViewById(R.id.past_drives_container);

        for (DriveData drive : drives) {
            View card = inflater.inflate(R.layout.insights_past_drive_template, container, false);

            ((TextView) card.findViewById(R.id.drive_date)).setText("Drive on " + drive.date);
            ((TextView) card.findViewById(R.id.sharp_braking_count)).setText(String.valueOf(drive.sharpBraking));
            ((TextView) card.findViewById(R.id.sharp_turns_count)).setText(String.valueOf(drive.sharpTurns));
            ((TextView) card.findViewById(R.id.inconsistent_speeds_count)).setText(String.valueOf(drive.inconsistentSpeeds));
            ((TextView) card.findViewById(R.id.reduced_lane_deviation_count)).setText(String.valueOf(drive.reducedLaneDeviation));

            container.addView(card);
        }
    }

    private void setupCharts(View view) {
        generateChart(view, R.id.lineChart1, drive -> drive.reducedLaneDeviation, "Bad Lane Changing");
        generateChart(view, R.id.lineChart2, drive -> drive.sharpBraking, "Sudden Braking");
        generateChart(view, R.id.lineChart3, drive -> drive.sharpTurns, "Sharp Turns");
        generateChart(view, R.id.lineChart4, drive -> drive.inconsistentSpeeds, "Inconsistent Speeds");
    }

    private void generateChart(View parent, int chartId, Function<DriveData, Integer> extractor, String label) {
        LineChart chart = parent.findViewById(chartId);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < drives.size(); i++) {
            entries.add(new Entry(i, extractor.apply(drives.get(i))));
        }

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(Color.parseColor("#115655"));
        dataSet.setCircleColor(Color.parseColor("#115655"));
        dataSet.setValueTextColor(Color.BLACK);

        chart.setData(new LineData(dataSet));
        styleChart(chart);
        chart.invalidate();
    }

    private void styleChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setViewPortOffsets(0f, 0f, 0f, 0f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return index >= 0 && index < drives.size() ? drives.get(index).date : "";
            }
        });

        chart.getAxisLeft().setDrawGridLines(false);
    }

    private String generateSummary() {
        if (drives.size() < 2) return "Drive more to start seeing your driving trends here!";

        DriveData oldest = drives.get(0);
        DriveData latest = drives.get(drives.size() - 1);

        List<String> improved = new ArrayList<>();
        List<String> worsened = new ArrayList<>();

        compareMetric("lane discipline", oldest.reducedLaneDeviation, latest.reducedLaneDeviation, improved, worsened);
        compareMetric("sharp braking incidents", oldest.sharpBraking, latest.sharpBraking, improved, worsened);
        compareMetric("sharp turns", oldest.sharpTurns, latest.sharpTurns, improved, worsened);
        compareMetric("inconsistent speeds", oldest.inconsistentSpeeds, latest.inconsistentSpeeds, improved, worsened);

        StringBuilder summary = new StringBuilder("\n\nOver the past " + drives.size() + " drives, ");

        if (!improved.isEmpty()) {
            summary.append("you've shown improvement in ").append(listToSentence(improved)).append(". ");
        }

        if (!worsened.isEmpty()) {
            summary.append("However, ").append(listToSentence(worsened))
                    .append(worsened.size() == 1 ? " has" : " have")
                    .append(" slightly increased and may need attention. ");
        }

        summary.append("\n\nKeep practicing for a safer driving experience.");
        return summary.toString();
    }

    private void compareMetric(String label, int oldVal, int newVal, List<String> improved, List<String> worsened) {
        if (newVal < oldVal) improved.add(label);
        else if (newVal > oldVal) worsened.add(label);
    }

    private String listToSentence(List<String> items) {
        if (items.size() == 1) return items.get(0);
        if (items.size() == 2) return items.get(0) + " and " + items.get(1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i));
            if (i < items.size() - 2) sb.append(", ");
            else if (i == items.size() - 2) sb.append(", and ");
        }
        return sb.toString();
    }
}
