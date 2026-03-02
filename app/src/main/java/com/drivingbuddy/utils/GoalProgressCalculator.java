package com.drivingbuddy.utils;

import com.drivingbuddy.data.model.DriveDataResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GoalProgressCalculator {
    private static final int MIN_TOTAL_MINUTES = 30;
    private static final double BASELINE_MINUTES = 60.0;
    private static final double ROLLING_MINUTES = 30.0;

    private GoalProgressCalculator() {
    }

    public static Result calculate(List<DriveDataResponse> drives) {
        Map<String, Integer> progress = new HashMap<>();
        progress.put("Reduce sudden braking", 0);
        progress.put("Reduce sharp turns", 0);
        progress.put("Reduce inconsistent speeds", 0);
        progress.put("Reduce lane deviation", 0);

        if (drives == null || drives.isEmpty()) {
            return new Result(progress, false);
        }

        List<DriveDataResponse> orderedDrives = new java.util.ArrayList<>(drives);
        orderedDrives.sort((a, b) -> {
            java.util.Date aStart = a == null ? null : a.getStartTimestamp();
            java.util.Date bStart = b == null ? null : b.getStartTimestamp();
            if (aStart != null && bStart != null) {
                return aStart.compareTo(bStart);
            }
            String aId = a == null ? null : a.getTripId();
            String bId = b == null ? null : b.getTripId();
            if (aId == null && bId == null) {
                return 0;
            }
            if (aId == null) {
                return 1;
            }
            if (bId == null) {
                return -1;
            }
            return aId.compareTo(bId);
        });

        double totalMinutes = 0.0;

        for (DriveDataResponse drive : orderedDrives) {
            Integer durationMinutes = getDurationMinutes(drive);
            if (durationMinutes == null || durationMinutes <= 0) {
                continue;
            }
            totalMinutes += durationMinutes;
        }

        if (totalMinutes < MIN_TOTAL_MINUTES) {
            return new Result(progress, false);
        }

        WindowTotals baseline = accumulateForward(orderedDrives, BASELINE_MINUTES);
        WindowTotals rolling = accumulateBackward(orderedDrives, ROLLING_MINUTES);
        if (baseline.totalMinutes < MIN_TOTAL_MINUTES || rolling.totalMinutes < MIN_TOTAL_MINUTES) {
            return new Result(progress, false);
        }

        double baselineHours = baseline.totalMinutes / 60.0;
        double rollingHours = rolling.totalMinutes / 60.0;

        double brakingBaselineRate = baseline.brakingCount / baselineHours;
        double speedBaselineRate = baseline.speedCount / baselineHours;
        double laneBaselineRate = baseline.laneCount / baselineHours;

        double brakingRollingRate = rolling.brakingCount / rollingHours;
        double speedRollingRate = rolling.speedCount / rollingHours;
        double laneRollingRate = rolling.laneCount / rollingHours;

        progress.put("Reduce sudden braking", progressFromRates(brakingBaselineRate, brakingRollingRate));
        progress.put("Reduce inconsistent speeds", progressFromRates(speedBaselineRate, speedRollingRate));
        progress.put("Reduce lane deviation", progressFromRates(laneBaselineRate, laneRollingRate));

        return new Result(progress, true);
    }

    private static int progressFromRates(double baselineRate, double currentRate) {
        if (baselineRate <= 0.0) {
            return 0;
        }
        int progress = (int) Math.round(((baselineRate - currentRate) / baselineRate) * 100.0);
        if (progress < 0) {
            return 0;
        }
        if (progress > 100) {
            return 100;
        }
        return progress;
    }

    private static WindowTotals accumulateForward(List<DriveDataResponse> drives, double minutesLimit) {
        WindowTotals totals = new WindowTotals();
        double remaining = minutesLimit;
        for (DriveDataResponse drive : drives) {
            if (remaining <= 0.0) {
                break;
            }
            Integer durationMinutes = getDurationMinutes(drive);
            if (durationMinutes == null || durationMinutes <= 0) {
                continue;
            }
            double minutes = durationMinutes;
            DriveDataResponse.IncidentData incidents = drive.getIncidents();
            int braking = incidents == null ? 0 : incidents.getSuddenBraking();
            int speed = incidents == null ? 0 : incidents.getInconsistentSpeed();
            int lane = incidents == null ? 0 : incidents.getLaneDeviation();

            if (minutes <= remaining) {
                totals.add(minutes, braking, speed, lane);
                remaining -= minutes;
            } else {
                double ratio = remaining / minutes;
                totals.add(remaining, braking * ratio, speed * ratio, lane * ratio);
                remaining = 0.0;
            }
        }
        return totals;
    }

    private static WindowTotals accumulateBackward(List<DriveDataResponse> drives, double minutesLimit) {
        WindowTotals totals = new WindowTotals();
        double remaining = minutesLimit;
        for (int i = drives.size() - 1; i >= 0; i--) {
            if (remaining <= 0.0) {
                break;
            }
            DriveDataResponse drive = drives.get(i);
            Integer durationMinutes = getDurationMinutes(drive);
            if (durationMinutes == null || durationMinutes <= 0) {
                continue;
            }
            double minutes = durationMinutes;
            DriveDataResponse.IncidentData incidents = drive.getIncidents();
            int braking = incidents == null ? 0 : incidents.getSuddenBraking();
            int speed = incidents == null ? 0 : incidents.getInconsistentSpeed();
            int lane = incidents == null ? 0 : incidents.getLaneDeviation();

            if (minutes <= remaining) {
                totals.add(minutes, braking, speed, lane);
                remaining -= minutes;
            } else {
                double ratio = remaining / minutes;
                totals.add(remaining, braking * ratio, speed * ratio, lane * ratio);
                remaining = 0.0;
            }
        }
        return totals;
    }

    private static final class WindowTotals {
        double totalMinutes;
        double brakingCount;
        double speedCount;
        double laneCount;

        void add(double minutes, double braking, double speed, double lane) {
            totalMinutes += minutes;
            brakingCount += braking;
            speedCount += speed;
            laneCount += lane;
        }
    }

    private static Integer getDurationMinutes(DriveDataResponse drive) {
        if (drive == null) {
            return null;
        }
        Integer durationMinutes = drive.getDurationMinutes();
        if (durationMinutes != null && durationMinutes > 0) {
            return durationMinutes;
        }
        java.util.Date start = drive.getStartTimestamp();
        java.util.Date end = drive.getEndTimestamp();
        if (start == null || end == null) {
            start = drive.getStartOfTripTimestamp();
            end = drive.getEndOfTripTimestamp();
        }
        if (start == null || end == null) {
            return null;
        }
        long diffMs = end.getTime() - start.getTime();
        if (diffMs <= 0) {
            return null;
        }
        return (int) Math.round(diffMs / 60000.0);
    }

    public static final class Result {
        private final Map<String, Integer> progress;
        private final boolean hasEnoughData;

        Result(Map<String, Integer> progress, boolean hasEnoughData) {
            this.progress = progress;
            this.hasEnoughData = hasEnoughData;
        }

        public Map<String, Integer> getProgress() {
            return progress;
        }

        public boolean hasEnoughData() {
            return hasEnoughData;
        }
    }
}
