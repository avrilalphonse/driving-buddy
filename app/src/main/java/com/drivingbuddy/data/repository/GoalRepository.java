package com.drivingbuddy.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.drivingbuddy.data.api.ApiClient;
import com.drivingbuddy.data.api.GoalApiService;
import com.drivingbuddy.data.model.Goal;
import com.drivingbuddy.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class GoalRepository {
    private final GoalApiService apiService;
    private final TokenManager tokenManager;

    public GoalRepository(Context context) {
        this.apiService = ApiClient.getClient().create(GoalApiService.class);
        this.tokenManager = new TokenManager(context);
    }

    public LiveData<List<Goal>> getGoals() {
        MutableLiveData<List<Goal>> liveData = new MutableLiveData<>();
        String token = "Bearer " + tokenManager.getToken();

        apiService.getGoals(token).enqueue(new Callback<List<Goal>>() {
            @Override
            public void onResponse(Call<List<Goal>> call, Response<List<Goal>> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<List<Goal>> call, Throwable t) {
                liveData.setValue(Collections.emptyList());
            }
        });

        return liveData;
    }

    public LiveData<Goal> createGoal(String title) {
        MutableLiveData<Goal> liveData = new MutableLiveData<>();
        String token = "Bearer " + tokenManager.getToken();
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);

        apiService.createGoal(token, body).enqueue(new Callback<Goal>() {
            @Override
            public void onResponse(Call<Goal> call, Response<Goal> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<Goal> call, Throwable t) {
                liveData.setValue(null);
            }
        });

        return liveData;
    }

    public LiveData<Boolean> deleteGoal(String id) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        String token = "Bearer " + tokenManager.getToken();

        apiService.deleteGoal(token, id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                liveData.setValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                liveData.setValue(false);
            }
        });

        return liveData;
    }

}