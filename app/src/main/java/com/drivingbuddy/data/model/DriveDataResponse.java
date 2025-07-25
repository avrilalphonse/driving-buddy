package com.drivingbuddy.data.model;

import com.google.gson.annotations.SerializedName;

public class DriveDataResponse {

    @SerializedName("tripID")
    private String tripId;

    @SerializedName("date")
    private String date;

    @SerializedName("displayDate")
    private String displayDate;

    @SerializedName("incidents")
    private IncidentData incidents;

    @SerializedName("raw_data")

    // Getters and setters
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDisplayDate() { return displayDate; }
    public void setDisplayDate(String displayDate) { this.displayDate = displayDate; }

    public IncidentData getIncidents() { return incidents; }
    public void setIncidents(IncidentData incidents) { this.incidents = incidents; }

    public static class IncidentData {
        @SerializedName("sudden_braking")
        private int suddenBraking;

        @SerializedName("inconsistent_speed")
        private int inconsistentSpeed;

        @SerializedName("lane_deviation")
        private int laneDeviation;

        // Getters and setters
        public int getSuddenBraking() { return suddenBraking; }
        public void setSuddenBraking(int suddenBraking) { this.suddenBraking = suddenBraking; }

        public int getInconsistentSpeed() { return inconsistentSpeed; }
        public void setInconsistentSpeed(int inconsistentSpeed) { this.inconsistentSpeed = inconsistentSpeed; }

        public int getLaneDeviation() { return laneDeviation; }
        public void setLaneDeviation(int laneDeviation) { this.laneDeviation = laneDeviation; }
    }
}