package com.drivingbuddy.data.api;

import com.drivingbuddy.data.model.Goal;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;
import java.util.Map;

public interface GoalApiService {

    @GET("api/goals")
    Call<List<Goal>> getGoals(@Header("Authorization") String token);

    @POST("api/goals")
    Call<Goal> createGoal(@Header("Authorization") String token, @Body Map<String, Object> goal);

    @DELETE("api/goals/{id}")
    Call<Void> deleteGoal(@Header("Authorization") String token, @Path("id") String id);

}
