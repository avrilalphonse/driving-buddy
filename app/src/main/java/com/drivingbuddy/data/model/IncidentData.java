package com.drivingbuddy.data.model;

import com.google.gson.annotations.SerializedName;

public class IncidentData {
    @SerializedName("sudden_braking")
    private int suddenBraking;

    @SerializedName("inconsistent_speed")
    private int inconsistentSpeed;

    @SerializedName("sharp_turning")
    private int sharpTurning;

    @SerializedName("lane_deviation")
    private int laneDeviation;

    public int getSuddenBraking() {
        return suddenBraking;
    }

    public void setSuddenBraking(int suddenBraking) {
        this.suddenBraking = suddenBraking;
    }

    public int getInconsistentSpeed() {
        return inconsistentSpeed;
    }

    public void setInconsistentSpeed(int inconsistentSpeed) {
        this.inconsistentSpeed = inconsistentSpeed;
    }

    public int getSharpTurning() {
        return sharpTurning;
    }

    public void setSharpTurning(int sharpTurning) {
        this.sharpTurning = sharpTurning;
    }

    public int getLaneDeviation() {
        return laneDeviation;
    }

    public void setLaneDeviation(int laneDeviation) {
        this.laneDeviation = laneDeviation;
    }
}
