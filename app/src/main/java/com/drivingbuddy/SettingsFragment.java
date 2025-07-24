package com.drivingbuddy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import com.drivingbuddy.data.api.ApiClient;
import com.drivingbuddy.data.api.SensorApiService;
import com.drivingbuddy.data.model.SensorCounts;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

//        // testing if i can see sensor data in the settings fragment - it worked!
//        SensorApiService sensorApi = ApiClient.getSensorApiService();
//        sensorApi.getCounts().enqueue(new Callback<SensorCounts>() {
//            @Override
//            public void onResponse(Call<SensorCounts> call, Response<SensorCounts> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    SensorCounts counts = response.body();
//                    Log.d("SensorData", "Hard braking: " + counts.getHardBrakingCount());
//                    Log.d("SensorData", "Inconsistent speed: " + counts.getInconsistentSpeedCount());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<SensorCounts> call, Throwable t) {
//                Log.e("SensorData", "Failed to get counts: " + t.getMessage());
//            }
//        });

        // when logout is clicked, log the user out and return to the login screen
        View logout_btn = view.findViewById(R.id.logout_btn);
        logout_btn.setOnClickListener(v -> {
            ((MainActivity) getActivity()).logout();
        });
        return view;

//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}