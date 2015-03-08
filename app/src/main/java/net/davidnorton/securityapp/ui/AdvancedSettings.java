package net.davidnorton.securityapp.ui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.davidnorton.securityapp.R;

/**
 * Fragment used to contain the app advanced settings.
 *
 * @author David Norton
 */
public class AdvancedSettings extends Fragment {

    ImageView ivIcon;
    TextView tvItemName;


    public static final String IMAGE_RESOURCE_ID = "iconResourceID";
    public static final String ITEM_NAME = "itemName";

    /**
     * Creates the Advanced Settings fragment.
     *
     * @param inflater Instantiates a layout XML file into its corresponding View objects.
     * @param container Container used to the base for layouts and views containers.
     * @param savedInstanceState Saves current state of application to be referred back to.
     * @return The view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_advanced_settings, container, false);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        ivIcon = (ImageView) view.findViewById(R.id.advanced_settings_icon);
        tvItemName = (TextView) view.findViewById(R.id.advanced_settings_title);

        tvItemName.setText(getArguments().getString(ITEM_NAME));
        ivIcon.setImageDrawable(view.getResources().getDrawable(getArguments().getInt(IMAGE_RESOURCE_ID)));

        // Apply correct colour scheme.
        if (pref.getBoolean("dark_theme", false) ) {
            CardView cardView = (CardView) view.findViewById(R.id.card_1);
            cardView.setCardBackgroundColor(Color.rgb(40, 40, 40));
            cardView = (CardView) view.findViewById(R.id.card_2);
            cardView.setCardBackgroundColor(Color.rgb(40, 40, 40));
            cardView = (CardView) view.findViewById(R.id.card_3);
            cardView.setCardBackgroundColor(Color.rgb(40, 40, 40));
        }

        return view;
    }

    /**
     * Load the permanent notification preference option.
     */
    public static class PermanentNotificationFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_permanent_notification);
        }
    }

    /**
     * Load the dark theme preference option.
     */
    public static class DarkThemeFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_dark_theme);
        }
    }

}