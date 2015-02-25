package net.davidnorton.securityapp.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import net.davidnorton.securityapp.R;
import net.davidnorton.securityapp.profile.ProfileEditActivity;
import net.davidnorton.securityapp.profile.XmlParserPref;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Adds Profiles to Profile fragment.
 *
 * @author David Norton
 */
public class ProfileList extends ArrayAdapter<String> implements OnClickListener {

	List<String> list;
	Context context;
	String element;

	public ProfileList(Context cont, int textViewResourceId, List<String> objects) {
		super(cont, textViewResourceId, objects);
		list = objects;
		context = cont;
	}

    /**
     * Returns view with list filled with Profiles.
     *
     * @param position Position in list.
     * @param convertView New View.
     * @param parent Parent View.
     * @return convertView.
     */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Create profile list if it doesn't exist.
		if (convertView == null) {
			Context c = getContext();
			LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.fragment_profiles_list, null);
		}

        // Get correct edit button image.
		ImageButton buttonEdit = (ImageButton) convertView.findViewById(R.id.buttonEdit);

        // Change edit button colour when using dark theme.
        if (pref.getBoolean("dark_theme", true)) {
			buttonEdit.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_action_settings));
		}

		element = list.get(position);

		if (element != null) {

			// Adds profiles
            TextView v = (TextView) convertView.findViewById(R.id.textViewProfileName);
			v.setText(element);

			// Adds edit buttons
			buttonEdit.setFocusable(false);
			buttonEdit.setOnClickListener(this);
			buttonEdit.setTag(this.element);
		}
		return convertView;
	}

    /**
     * Open edit profile activity when edit button is clicked.
     *
     * @param v View.
     */
	@Override
	public void onClick(View v) {

        XmlParserPref xmlParserPref = new XmlParserPref(context, v.getTag().toString());

		try {
			xmlParserPref.initializeXmlParser(context.openFileInput(v.getTag() + "_profile.xml"));
		} catch (XmlPullParserException | IOException e) {
			e.printStackTrace();
		}

        Intent i = new Intent(context, ProfileEditActivity.class);
		context.startActivity(i);
	}
}