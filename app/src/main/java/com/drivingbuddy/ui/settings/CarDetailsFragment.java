package com.drivingbuddy.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.drivingbuddy.R;
import com.drivingbuddy.data.model.CarProfile;
import com.drivingbuddy.utils.TokenManager;
import com.drivingbuddy.ui.auth.AuthViewModel;
import com.drivingbuddy.data.model.AuthResponse;

public class CarDetailsFragment extends Fragment {

    private AuthViewModel authViewModel;

    public CarDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_details, container, false);

        Spinner makeSpinner = view.findViewById(R.id.car_make_spinner);
        Spinner modelSpinner = view.findViewById(R.id.car_model_spinner);
        Spinner colorSpinner = view.findViewById(R.id.car_color_spinner);
        Button saveButton = view.findViewById(R.id.save_car_details_button);
        ImageButton backButton = view.findViewById(R.id.car_details_back_button);

        ArrayAdapter<CharSequence> makeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.car_make_options,
                android.R.layout.simple_spinner_item
        );
        makeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        makeSpinner.setAdapter(makeAdapter);

        ArrayAdapter<CharSequence> modelAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.car_model_options,
                android.R.layout.simple_spinner_item
        );
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(modelAdapter);

        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.car_color_options,
                android.R.layout.simple_spinner_item
        );
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);

        CarProfile existing = authViewModel.getCarProfile();
        if (existing != null) {
            setSpinnerSelection(makeSpinner, existing.getMake());
            setSpinnerSelection(modelSpinner, existing.getModel());
            setSpinnerSelection(colorSpinner, existing.getColorName());
        }

        saveButton.setOnClickListener(v -> {
            String make = makeSpinner.getSelectedItem().toString();
            String model = modelSpinner.getSelectedItem().toString();
            String colorName = colorSpinner.getSelectedItem().toString();
            String colorHex = mapColorNameToHex(colorName);

            authViewModel.updateCarDetails(make, model, colorName, colorHex)
                .observe(getViewLifecycleOwner(), authResponse -> {
                    if (authResponse != null && authResponse.getUser() != null) {
                        Toast.makeText(requireContext(), R.string.car_details_saved, Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    } else {
                        Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show();
                    }
                });
        });

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void setSpinnerSelection(@NonNull Spinner spinner, @NonNull String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }

    private String mapColorNameToHex(String colorName) {
        switch (colorName) {
            case "Red":
                return "#D32F2F";
            case "Blue":
                return "#1976D2";
            case "White":
                return "#F5F5F5";
            case "Grey":
                return "#9E9E9E";
            case "Black":
                return "#212121";
            default:
                return "#CCCCCC";
        }
    }
}
