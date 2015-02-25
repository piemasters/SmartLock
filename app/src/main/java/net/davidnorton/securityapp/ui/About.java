package net.davidnorton.securityapp.ui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.davidnorton.securityapp.R;

/**
 * Fragment used to contain the About page.
 *
 * @author David Norton
 */
public class About extends Fragment {

    ImageView ivIcon;
    TextView tvItemName;

    public static final String IMAGE_RESOURCE_ID = "iconResourceID";
    public static final String ITEM_NAME = "itemName";

    /**
     * Creates the About fragment.
     *
     * @param inflater Instantiates a layout XML file into its corresponding View objects.
     * @param container Container used to the base for layouts and views containers.
     * @param savedInstanceState Saves current state of application to be referred back to.
     * @return The view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about, container, false);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        ivIcon = (ImageView) view.findViewById(R.id.about_icon);
        tvItemName = (TextView) view.findViewById(R.id.about_title);

        tvItemName.setText(getArguments().getString(ITEM_NAME));
        ivIcon.setImageDrawable(view.getResources().getDrawable(getArguments().getInt(IMAGE_RESOURCE_ID)));

        // Apply correct colour scheme.
        if (pref.getBoolean("dark_theme", false)) {
            View cardLayout = view.findViewById(R.id.card_1);
            cardLayout.setBackgroundColor(Color.rgb(40, 40, 40));
            cardLayout = view.findViewById(R.id.card_2);
            cardLayout.setBackgroundColor(Color.rgb(40, 40, 40));
        }

        return view;
    }

}