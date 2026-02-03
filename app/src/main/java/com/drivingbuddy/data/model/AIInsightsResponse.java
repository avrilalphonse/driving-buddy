package com.drivingbuddy.data.model;

public class AIInsightsResponse {
    private String summary;
    private String generated_at;
    private boolean cached;

    public String getSummary() { return summary; }
    public String getGenerated_at() { return generated_at; }
    public boolean isCached() { return cached; }
}
