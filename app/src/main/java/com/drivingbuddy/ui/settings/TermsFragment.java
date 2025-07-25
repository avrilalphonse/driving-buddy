package com.drivingbuddy.ui.settings;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.navigation.Navigation;
import com.drivingbuddy.R;

public class TermsFragment extends androidx.fragment.app.Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terms, container, false);

        TextView title = view.findViewById(R.id.settings_toolbar_title);
        title.setText(getString(R.string.perms_title));

        // Set the terms content with HTML formatting and clickable links
        TextView termsContent = view.findViewById(R.id.terms_content_text);
        String htmlText =
                "<b>1. Introduction</b><br>" +
                "Welcome to Driving Buddy. By accessing or using our app and dashboard, you agree to be bound by these Terms and Conditions. Please read them carefully before using our services.<br><br>" +

                "<b>2. Use of the App</b><br>" +
                "You agree to:<br>" +
                "&bull; Use Driving Buddy for lawful purposes only<br>" +
                "&bull; Not misuse or tamper with the app or dashboard<br>" +
                "&bull; Ensure your account details are accurate and kept private<br><br>" +

                "<b>3. Sensors and Hardware Use</b><br>" +
                "To provide insights and feedback on your driving behavior, Driving Buddy uses:<br>" +
                "&bull; Gyroscope: For detecting braking, acceleration, and cornering<br>" +
                "&bull; Camera Module: For lane deviation and safety alerts (however, does not store any images)<br>" +
                "&bull; OBD-II Scanner: For accessing vehicle diagnostics like speed, RPM, and engine status<br>" +
                "&bull; GPS/Location: For route mapping, speed checks, and driving event context<br>" +
                "You agree to grant permission for these sensors when prompted.<br><br>" +

                "<b>4. Data Collection and Usage</b><br>" +
                "We collect driving data to:<br>" +
                "&bull; Generate safety scores and feedback<br>" +
                "&bull; Provide route and driving insights<br>" +
                "&bull; Improve app performance and features<br>" +
                "We do not sell your personal data. Data may be anonymized and used for research or development purposes.<br><br>" +

                "<b>5. User Responsibilities</b><br>" +
                "You are responsible for safe driving. Driving Buddy provides insights and estimations of real-time warnings, not driving instructions. Do not interact with the app while driving.<br>" +
                "Additionally, maintain any third-party hardware (e.g., OBD devices) safely and legally.<br><br>" +

                "<b>6. Intellectual Property</b><br>" +
                "All content, designs, and algorithms within Driving Buddy are owned by the developers of this product. You may not copy, distribute, or reverse-engineer any part of the service.<br><br>" +

                "<b>7. Termination</b><br>" +
                "We reserve the right to suspend or terminate your access to Driving Buddy for any misuse or breach of these Terms.<br><br>" +

                "<b>8. Limitation of Liability</b><br>" +
                "Driving Buddy provides driving feedback based on sensor data. We are not liable for:<br>" +
                "&bull; Accidents or damages resulting from your driving<br>" +
                "&bull; Inaccuracies in sensor readings or driving classifications<br>" +
                "&bull; Hardware malfunctions (e.g., OBD scanner or camera)<br><br>" +

                "<b>9. Changes to These Terms</b><br>" +
                "We may update these Terms periodically. Continued use of the app after changes means you accept the new terms.<br><br>" +

                "<b>10. Contact Us</b><br>" +
                "For any questions or concerns, please contact the Driving Buddy team. <br>";
        termsContent.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
        termsContent.setMovementMethod(LinkMovementMethod.getInstance());

        ImageButton backBtn = view.findViewById(R.id.toolbar_back);
        ImageButton menuBtn = view.findViewById(R.id.toolbar_menu);

        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.settingsFragment));
        menuBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), menuBtn);
            MenuInflater menuInflater = popup.getMenuInflater();
            menuInflater.inflate(R.menu.settings_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_perms) {
                    return true;
                } else if (id == R.id.menu_data_collection) {
                    Navigation.findNavController(view).navigate(R.id.dataCollectionFragment);
                    return true;
                } else if (id == R.id.menu_notif) {
                    Navigation.findNavController(view).navigate(R.id.notificationsFragment);
                    return true;
                } else if (id == R.id.menu_profile) {
                    Navigation.findNavController(view).navigate(R.id.profileFragment);
                    return true;
                } else {
                    return false;
                }
            });
            popup.show();
        });

        return view;
    }
} 