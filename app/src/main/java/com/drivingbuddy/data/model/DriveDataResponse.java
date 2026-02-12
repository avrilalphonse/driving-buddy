package com.drivingbuddy.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class DriveDataResponse {

    @SerializedName("tripID")
    private String tripId;

    @SerializedName("date")
    private String date;

    @SerializedName("displayDate")
    private String displayDate;

    @SerializedName("incidents")
    private IncidentData incidents;

    @SerializedName("start_location")
    private double[] startLocation;

    @SerializedName("end_location")
    private double[] endLocation;

    @SerializedName("incident_details")
    private java.util.Map<String, List<Map<String, Object>>> incidentDetails;

    @SerializedName("duration_minutes")
    private Integer durationMinutes;

    @SerializedName("raw_data")
    private Object rawData;

    // Getters and setters
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Object getRawData() { return rawData; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getDisplayDate() { return displayDate; }
    public void setDisplayDate(String displayDate) { this.displayDate = displayDate; }

    public IncidentData getIncidents() { return incidents; }
    public void setIncidents(IncidentData incidents) { this.incidents = incidents; }

    public double[] getStartLocation() { return startLocation; }
    public double[] getEndLocation() { return endLocation; }
    public java.util.Map<String, List<java.util.Map<String, Object>>> getIncidentDetails() { return incidentDetails; }

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