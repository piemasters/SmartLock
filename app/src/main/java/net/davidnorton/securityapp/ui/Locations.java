package net.davidnorton.securityapp.ui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.davidnorton.securityapp.R;

public class Locations extends Fragment {

    ImageView ivIcon;
    TextView tvItemName;

    public static final String IMAGE_RESOURCE_ID = "iconResourceID";
    public static final String ITEM_NAME = "itemName";

    public Locations() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_locations, container, false);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        ivIcon = (ImageView) view.findViewById(R.id.locations_icon);
        tvItemName = (TextView) view.findViewById(R.id.locations_title);

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
            cardView = (CardView) view.findViewById(R.id.card_4);
            cardView.setCardBackgroundColor(Color.rgb(40, 40, 40));
        }

        return view;
    }

}