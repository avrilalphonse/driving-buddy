package com.drivingbuddy.ui.goals;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.drivingbuddy.data.model.Goal;
import com.drivingbuddy.data.repository.GoalRepository;
import java.util.List;

public class GoalViewModel extends AndroidViewModel {
    private final GoalRepository goalRepository;

    public GoalViewModel(@NonNull Application application) {
        super(application);
        goalRepository = new GoalRepository(application.getApplicationContext());
    }

    public LiveData<List<Goal>> getGoals() {
        return goalRepository.getGoals();
    }

    public LiveData<Goal> createGoal(String title) {
        return goalRepository.createGoal(title);
    }

    public LiveData<Boolean> deleteGoal(String id) {
        return goalRepository.deleteGoal(id);
    }

}