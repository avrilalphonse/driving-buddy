package com.drivingbuddy;

public class DriveData {
    public String date;
    public int sharpBraking;
    public int sharpTurns;
    public int inconsistentSpeeds;
    public int reducedLaneDeviation;

    public DriveData(String date, int sharpBraking, int sharpTurns, int inconsistentSpeeds, int reducedLaneDeviation) {
        this.date = date;
        this.sharpBraking = sharpBraking;
        this.sharpTurns = sharpTurns;
        this.inconsistentSpeeds = inconsistentSpeeds;
        this.reducedLaneDeviation = reducedLaneDeviation;
    }
}
