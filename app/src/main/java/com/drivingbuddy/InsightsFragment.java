package com.drivingbuddy;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.drivingbuddy.data.api.ApiClient;
import com.drivingbuddy.data.api.SensorDataApiService;
import com.drivingbuddy.data.DrivingDataCache;
import com.drivingbuddy.data.model.BucketedDataResponse;
import com.drivingbuddy.data.model.DriveDataResponse;
import com.drivingbuddy.ui.auth.AuthViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InsightsFragment extends Fragment {

    private final List<DriveData> drives = new ArrayList<>();
    private LinearLayout drivesContainer;
    private TextView summaryText;
    private ProgressBar progressBar;
    private SensorDataApiService apiService;

    private AuthViewModel authViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insights, container, false);

        String userName = authViewModel.getUserName();
        TextView insights_title = view.findViewById(R.id.insights_title);

        String userEmail = authViewModel.getUserEmail();
        if (userName != null && !userName.isEmpty()) {
            insights_title.setText(userName + "'s Insights");
        }

        drivesContainer = view.findViewById(R.id.past_drives_container);
        summaryText = view.findViewById(R.id.summary_text);
        progressBar = view.findViewById(R.id.progress_bar);

        // api service setup
        apiService = ApiClient.getClient().create(SensorDataApiService.class);
        if ("test@gmail.com".equals(userEmail)) {
            BucketedDataResponse cachedData = DrivingDataCache.getCachedData();
            if (cachedData != null) {
                // use cached data if available
                processDriveData(cachedData, view, inflater);
            } else {
                // show loading circle!
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                // fetch data from API
                fetchDriveData(view, inflater);
            }
        } else {
            // for demo users, show empty state!
            showEmptyStateForDemoUser(view);
        }

        return view;
    }

    private void showEmptyStateForDemoUser(View view) {
        // no progress bar
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // clear data
        drives.clear();

        // blank summary
        TextView summaryText = view.findViewById(R.id.summary_text);
        summaryText.setText("\n\nWelcome! Start driving to see your insights and track your progress here.");

        // clear drive cards
        LinearLayout container = view.findViewById(R.id.past_drives_container);
        container.removeAllViews();

        // clear charts
        setChartNoData(view, R.id.lineChart1);
        setChartNoData(view, R.id.lineChart2);
        setChartNoData(view, R.id.lineChart3);
        setChartNoData(view, R.id.lineChart4);
    }

    private void setChartNoData(View parent, int chartId) {
        LineChart chart = parent.findViewById(chartId);
        chart.setNoDataText("No data available yet");
        chart.setNoDataTextColor(Color.GRAY);
        chart.setNoDataTextTypeface(Typeface.DEFAULT);
        chart.invalidate();
    }

    private void fetchDriveData(View view, LayoutInflater inflater) {
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
                    processDriveData(data, view, inflater);
                } else {
                    Log.e("InsightsFragment", "Failed to fetch data: " + response.code());
                    Toast.makeText(getContext(), "Failed to load drive data", Toast.LENGTH_SHORT).show();
                    updateUI(view, inflater);
                }
            }

            @Override
            public void onFailure(Call<BucketedDataResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Log.e("InsightsFragment", "Network error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateUI(view, inflater);
            }
        });
    }

    private void processDriveData(BucketedDataResponse data, View view, LayoutInflater inflater) {
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

        updateUI(view, inflater);
    }

    private void updateUI(View view, LayoutInflater inflater) {
        setupSummary(view);
        populateDriveCards(view, inflater);
        setupCharts(view);
    }

    private String formatDateForDisplay(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d", Locale.US);
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e("InsightsFragment", "Error parsing date: " + dateStr);
            return dateStr; // Return original if parsing fails
        }
    }

    private void setupSummary(View view) {
        TextView summaryText = view.findViewById(R.id.summary_text);
        summaryText.setText(generateSummary());
    }

    private void populateDriveCards(View view, LayoutInflater inflater) {
        LinearLayout container = view.findViewById(R.id.past_drives_container);
        container.removeAllViews(); // clear views

        // newest drives first
        for (int i = drives.size() - 1; i >= 0; i--) {
            DriveData drive = drives.get(i);
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

        LineChart sharpTurnsChart = view.findViewById(R.id.lineChart3);
        sharpTurnsChart.setNoDataText("No data available"); // for now!
        sharpTurnsChart.setNoDataTextColor(Color.GRAY);
        sharpTurnsChart.setNoDataTextTypeface(Typeface.DEFAULT);
        sharpTurnsChart.invalidate();

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
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawCircles(false); // Remove circles
        dataSet.setDrawValues(false); // Remove value labels
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#E8F5F4"));
        dataSet.setFillAlpha(50);

        chart.setData(new LineData(dataSet));
        styleChart(chart);
        chart.invalidate();
    }

    private void styleChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);

        chart.setViewPortOffsets(40f, 20f, 40f, 50f);

        // show date range instead of individual dates
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.GRAY);
        xAxis.setAxisLineWidth(1f);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setTextSize(10f);
        xAxis.setDrawLabels(true);
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

        chart.getAxisLeft().setEnabled(true);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(Color.parseColor("#E0E0E0"));
        chart.getAxisLeft().setGridLineWidth(0.5f);
        chart.getAxisLeft().setDrawAxisLine(true);
        chart.getAxisLeft().setAxisLineColor(Color.GRAY);
        chart.getAxisLeft().setAxisLineWidth(1f);
        chart.getAxisLeft().setTextColor(Color.GRAY);
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisLeft().setGranularity(1f);
        chart.getAxisLeft().setAxisMinimum(0f);

        chart.getAxisRight().setEnabled(false);

        // little animation to make the graph :)
        chart.animateY(500);
    }

    private String generateSummary() {
        if (drives.size() < 2) return "Drive more to start seeing your driving trends here!";

        // update: oldest is first (index 0) and latest is last (size-1)
        DriveData oldest = drives.get(0);
        DriveData latest = drives.get(drives.size() - 1);

        List<String> improved = new ArrayList<>();
        List<String> worsened = new ArrayList<>();

        compareMetric("lane discipline", oldest.reducedLaneDeviation, latest.reducedLaneDeviation, improved, worsened);
        compareMetric("sudden braking", oldest.sharpBraking, latest.sharpBraking, improved, worsened);
        compareMetric("inconsistent speeds", oldest.inconsistentSpeeds, latest.inconsistentSpeeds, improved, worsened);

        StringBuilder summary = new StringBuilder("\n\nOver the past " + drives.size() + " drives, ");

        if (!improved.isEmpty() && !worsened.isEmpty()) {
            summary.append("you've shown improvement in ").append(listToSentence(improved))
                    .append(", but ").append(listToSentence(worsened))
                    .append(worsened.size() == 1 ? " has" : " have")
                    .append(" slightly increased and may need attention.");
        } else if (!improved.isEmpty()) {
            summary.append("you've shown great improvement in ").append(listToSentence(improved)).append("!");
        } else if (!worsened.isEmpty()) {
            summary.append(listToSentence(worsened))
                    .append(worsened.size() == 1 ? " has" : " have")
                    .append(" increased and may need attention.");
        } else {
            summary.append("your driving has remained consistent.");
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

    // class for DriveData
    static class DriveData {
        String date;
        int sharpBraking;
        int sharpTurns;
        int inconsistentSpeeds;
        int reducedLaneDeviation;

        DriveData(String date, int sharpBraking, int sharpTurns, int inconsistentSpeeds, int reducedLaneDeviation) {
            this.date = date;
            this.sharpBraking = sharpBraking;
            this.sharpTurns = sharpTurns;
            this.inconsistentSpeeds = inconsistentSpeeds;
            this.reducedLaneDeviation = reducedLaneDeviation;
        }
    }
}