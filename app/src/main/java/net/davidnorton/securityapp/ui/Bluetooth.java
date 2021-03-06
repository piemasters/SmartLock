package net.davidnorton.securityapp.ui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.davidnorton.securityapp.R;

public class Bluetooth extends Fragment {

    private ImageView ivIcon;
    private TextView tvItemName;

    private static final String IMAGE_RESOURCE_ID = "iconResourceID";
    private static final String ITEM_NAME = "itemName";

    public Bluetooth() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        ivIcon = (ImageView) view.findViewById(R.id.devices_icon);
        tvItemName = (TextView) view.findViewById(R.id.devices_title);

        tvItemName.setText(getArguments().getString(ITEM_NAME));
        ivIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), getArguments().getInt(IMAGE_RESOURCE_ID), null));

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