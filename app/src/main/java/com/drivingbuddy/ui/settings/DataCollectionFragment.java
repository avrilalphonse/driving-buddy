package com.drivingbuddy.ui.settings;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.widget.PopupMenu;
import android.view.MenuInflater;
import com.drivingbuddy.R;

public class DataCollectionFragment extends Fragment {

    public DataCollectionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_collection, container, false);

        TextView title = view.findViewById(R.id.settings_toolbar_title);
        title.setText(getString(R.string.privacy_title));

        ImageButton backBtn = view.findViewById(R.id.toolbar_back);
        ImageButton menuBtn = view.findViewById(R.id.toolbar_menu);

        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.settingsFragment));
        menuBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), menuBtn);
            MenuInflater menuInflater = popup.getMenuInflater();
            menuInflater.inflate(R.menu.settings_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_data_collection) {
                    return true;
                } else if (id == R.id.menu_notif) {
                    Navigation.findNavController(view).navigate(R.id.notificationsFragment);
                    return true;
                } else if (id == R.id.menu_perms) {
                    Navigation.findNavController(view).navigate(R.id.termsFragment);
                    return true;
                } else {
                    return false;
                }
            });
            popup.show();
        });

        TextView citesText = view.findViewById(R.id.cites_text);
        String htmlText =
                "To determine events like hard braking, sharp turning, and other driving behaviors, we use thresholds and logic informed by publicly available research and industry documentation, including:<br><br>" +
                "• <a href='https://www.researchgate.net/publication/319192847_Harsh_Braking_by_Truck_Drivers_A_Comparison_of_Thresholds_and_Driving_Contexts_Using_Naturalistic_Driving_Data'>Harsh Braking by Truck Drivers: A Comparison of Thresholds and Driving Contexts Using Naturalistic Driving Data (ResearchGate)</a><br>" +
                "• <a href='https://www.sciencedirect.com/science/article/pii/S2352146517303732'>Assessing Braking Behavior in Urban Driving (ScienceDirect)</a><br>" +
                "• <a href='https://link.springer.com/chapter/10.1007/978-3-030-20351-1_13'>Modelling Harsh Braking Using Telematics Data (Springer)</a><br>" +
                "• <a href='https://docs.sentiance.com/drive/events-and-scores/'>Sentiance Driving Events and Scores (Sentiance Docs)</a><br>" +
                "• <a href='https://arxiv.org/abs/1711.03938'>CARLA: An Open Urban Driving Simulator (arXiv)</a><br>" +
                "• <a href='https://www.diva-portal.org/smash/record.jsf?pid=diva2%3A1210211&dswid=-3606'>Driver Behavior Classification (DiVA Portal)</a><br><br>" +
                "We do not share raw driving data with third parties. These sources help us ensure our interpretations of driving events are based on sound, research-backed criteria.";
        citesText.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
        citesText.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }
}