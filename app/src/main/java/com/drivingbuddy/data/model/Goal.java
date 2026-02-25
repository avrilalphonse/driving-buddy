package com.drivingbuddy.data.model;

import java.util.List;

public class Goal {
    private String _id;
    private String title;
    private int progress;
    private List<String> tips;
    private boolean hasEnoughData = true;

    public String getId() {
        return _id;
    }

    public String getTitle() {
        return title;
    }

    public int getProgress() {
        return progress;
    }

    public List<String> getTips() {
        return tips;
    }

    public boolean hasEnoughData() {
        return hasEnoughData;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
    }

    public void setHasEnoughData(boolean hasEnoughData) {
        this.hasEnoughData = hasEnoughData;
    }
}
