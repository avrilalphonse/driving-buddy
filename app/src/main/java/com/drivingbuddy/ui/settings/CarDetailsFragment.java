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

import com.drivingbuddy.R;
import com.drivingbuddy.data.model.CarProfile;
import com.drivingbuddy.utils.TokenManager;

public class CarDetailsFragment extends Fragment {

    public CarDetailsFragment() {
        // Required empty public constructor
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

        TokenManager tokenManager = new TokenManager(requireContext());
        CarProfile existing = tokenManager.getCarProfile();
        if (existing != null) {
            setSpinnerSelection(makeSpinner, existing.getMake());
            setSpinnerSelection(modelSpinner, existing.getModel());
            setSpinnerSelection(colorSpinner, existing.getColorName());
        }

        makeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                modelSpinner.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        saveButton.setOnClickListener(v -> {
            String make = makeSpinner.getSelectedItem().toString();
            String model = modelSpinner.getSelectedItem().toString();
            String colorName = colorSpinner.getSelectedItem().toString();
            String colorHex = mapColorNameToHex(colorName);
            tokenManager.saveCarProfile(make, model, colorName, colorHex);
            Toast.makeText(requireContext(), R.string.car_details_saved, Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
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