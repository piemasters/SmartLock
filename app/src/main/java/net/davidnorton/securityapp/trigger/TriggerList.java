package net.davidnorton.securityapp.trigger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import net.davidnorton.securityapp.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Adds Triggers to Trigger fragment.
 *
 * @author David Norton
 */
public class TriggerList extends ArrayAdapter<String> implements OnClickListener {

	List<String> list;
	Context context;
	String element;

	public TriggerList(Context cont, int textViewResourceId, List<String> objects) {
		super(cont, textViewResourceId, objects);
		list = objects;
		context = cont;
	}

    /**
     * Returns view with list filled with Triggers.
     *
     * @param position Position in list.
     * @param convertView New View.
     * @param parent Parent View.
     * @return convertView.
     */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Create trigger list if it doesn't exist.
        if (convertView == null) {
			Context c = getContext();
			LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.fragment_triggers_list, null);
		}

        // Get correct edit button image.
		ImageButton buttonEdit = (ImageButton) convertView.findViewById(R.id.buttonEdit);

        // Change edit button colour when using dark theme.
        if (pref.getBoolean("dark_theme", false)) {
			buttonEdit.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_action_settings));
		}

		element = list.get(position);

		if (element != null) {

            // Adds triggers
			TextView v = (TextView) convertView.findViewById(R.id.textViewTriggerName);
			StringBuilder sb = new StringBuilder();
			sb.append(element);
			sb.delete(sb.length() - 12, sb.length());
			v.setText(sb.toString());

            // Adds edit buttons
			buttonEdit.setFocusable(false);
			buttonEdit.setOnClickListener(this);
			buttonEdit.setTag(sb.toString());

            // Disable all triggers marked as disabled
			if (element.contains("_tri_dis")) {
				v.setTextColor(Color.GRAY);
				buttonEdit.setEnabled(false);
				buttonEdit.setImageDrawable(null);
			}
		}
		return convertView;
	}

    /**
     * Open edit trigger activity when edit button is clicked.
     *
     * @param v View.
     */
	@Override
	public void onClick(View v) {

		XmlParserPrefTrigger xmlParserPrefTrigger = new XmlParserPrefTrigger(context, v.getTag().toString());

		try {
			xmlParserPrefTrigger.initializeXmlParser(context.openFileInput(v.getTag() + "_trigger.xml"));
		} catch (XmlPullParserException | IOException e) {
			e.printStackTrace();
		}

        Intent i = new Intent(context, TriggerEditActivity.class);
		context.startActivity(i);
	}
}