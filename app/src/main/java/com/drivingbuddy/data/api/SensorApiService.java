package com.drivingbuddy.data.api;

import com.drivingbuddy.data.model.SensorCounts;
import retrofit2.Call;
import retrofit2.http.*;

public interface SensorApiService {

    @GET("api/sensor-data/counts")
    Call<SensorCounts> getCounts();
}
