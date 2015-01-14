package net.davidnorton.securityapp.ui;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;

/**
 * Transparent(defined in manifest) activity used to show the pop-up list
 * dialog.
 *
 * @author Florian Schrofner & Dominik Koeltringer
 *
 */
public class ListDialogActivity extends Activity {

    List<String> profileList = new ArrayList<String>();

    /**
     * Refreshes the profile list before the pop-up will be shown.
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // refreshes the profile list
        profileList.clear();

        String[] fileList = getFilesDir().list();
        StringBuffer sb = new StringBuffer();
        for (String file : fileList) {
            sb.append(file);
            sb.delete(sb.length() - 4, sb.length());
            profileList.add(sb.toString());
            sb.delete(0, sb.length());
        }

        // sorts the list alphabetically
        Collections.sort(profileList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                if (lhs.toLowerCase().compareTo(rhs.toLowerCase()) > 0)
                    return 1;
                if (lhs.toLowerCase().compareTo(rhs.toLowerCase()) < 0)
                    return -1;
                return 0;
            }

        });

        // creates a new fragment and hands over the profiles to show the dialog
        DialogFragment newFragment = new ListDialog();
        Bundle bundle = new Bundle();
        bundle.putCharSequenceArray("ProfileList",
                profileList.toArray(new String[profileList.size()]));
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "ProfileListDialog");
    }

}