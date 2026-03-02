package com.drivingbuddy.data;

import android.util.Log;
import com.drivingbuddy.data.model.BucketedDataResponse;

public class DrivingDataCache {
    private static final String TAG = "DrivingDataCache";
    private static BucketedDataResponse cachedData = null;
    private static String cachedUserId = null;
    private static long cacheTimestamp = 0;
    private static final long CACHE_VALIDITY_MS = 5 * 60 * 1000;

    public static synchronized void setCachedData(BucketedDataResponse data, String userId) {
        Log.d(TAG, "Setting cached data");
        cachedData = data;
        cachedUserId = userId;
        cacheTimestamp = System.currentTimeMillis();
    }

    public static synchronized BucketedDataResponse getCachedData(String userId) {
        if (cachedData == null) {
            Log.d(TAG, "Cache is null");
            return null;
        }
        if (cachedUserId == null || userId == null || !cachedUserId.equals(userId)) {
            Log.d(TAG, "Cache user mismatch; clearing cache.");
            clearCache();
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
        cachedUserId = null;
        cacheTimestamp = 0;
    }

    public static synchronized boolean hasValidCache(String userId) {
        return getCachedData(userId) != null;
    }
}
