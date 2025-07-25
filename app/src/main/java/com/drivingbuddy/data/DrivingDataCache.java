package com.drivingbuddy.data;

import android.util.Log;
import com.drivingbuddy.data.model.BucketedDataResponse;

public class DrivingDataCache {
    private static final String TAG = "DrivingDataCache";
    private static BucketedDataResponse cachedData = null;
    private static long cacheTimestamp = 0;
    private static final long CACHE_VALIDITY_MS = 5 * 60 * 1000;

    public static synchronized void setCachedData(BucketedDataResponse data) {
        Log.d(TAG, "Setting cached data");
        cachedData = data;
        cacheTimestamp = System.currentTimeMillis();
    }

    public static synchronized BucketedDataResponse getCachedData() {
        if (cachedData == null) {
            Log.d(TAG, "Cache is null");
            return null;
        }

        long age = System.currentTimeMillis() - cacheTimestamp;
        if (age > CACHE_VALIDITY_MS) {
            Log.d(TAG, "Cache expired (age: " + age + "ms)");
            cachedData = null;
            return null;
        }

        Log.d(TAG, "Returning cached data (age: " + age + "ms)");
        return cachedData;
    }

    public static synchronized void clearCache() {
        Log.d(TAG, "Clearing cache");
        cachedData = null;
        cacheTimestamp = 0;
    }

    public static synchronized boolean hasValidCache() {
        return getCachedData() != null;
    }
}