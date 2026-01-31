package com.drivingbuddy.data.api;

import com.drivingbuddy.data.model.BucketedDataResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SensorDataApiService {

    @GET("api/sensor-data/get-bucketed-data")
    Call<BucketedDataResponse> getBucketedData(@Query("windowSeconds") int windowSeconds);

    @GET("api/sensor-data/get-persistent-summary")
    Call<BucketedDataResponse> getPersistentSummaryData();

}
