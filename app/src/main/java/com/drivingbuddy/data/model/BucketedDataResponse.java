package com.drivingbuddy.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BucketedDataResponse {

    @SerializedName("drives")
    private List<DriveDataResponse> drives;

    @SerializedName("summary")
    private SummaryData summary;

    public List<DriveDataResponse> getDrives() {
        return drives;
    }

    public void setDrives(List<DriveDataResponse> drives) {
        this.drives = drives;
    }

    public SummaryData getSummary() {
        return summary;
    }

    public void setSummary(SummaryData summary) {
        this.summary = summary;
    }

    public static class SummaryData {
        @SerializedName("total_drives")
        private int totalDrives;

        @SerializedName("sudden_braking")
        private int suddenBraking;

        @SerializedName("inconsistent_speed")
        private int inconsistentSpeed;

        @SerializedName("lane_deviation")
        private int laneDeviation;

        @SerializedName("total")
        private int total;

        // Getters and setters
        public int getTotalDrives() { return totalDrives; }
        public void setTotalDrives(int totalDrives) { this.totalDrives = totalDrives; }

        public int getSuddenBraking() { return suddenBraking; }
        public void setSuddenBraking(int suddenBraking) { this.suddenBraking = suddenBraking; }

        public int getInconsistentSpeed() { return inconsistentSpeed; }
        public void setInconsistentSpeed(int inconsistentSpeed) { this.inconsistentSpeed = inconsistentSpeed; }

        public int getLaneDeviation() { return laneDeviation; }
        public void setLaneDeviation(int laneDeviation) { this.laneDeviation = laneDeviation; }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
    }
}