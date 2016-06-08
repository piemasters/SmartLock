package net.davidnorton.securityapp.lockscreen;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import net.davidnorton.securityapp.R;
import net.davidnorton.securityapp.ui.MainActivity;

/**
 * Activity used to start NFC discovery and send
 * read tag ID to the Lockscreens fragment.
 *
 * @author David Norton
 *
 */
public class NFCReader extends Activity {

    private NfcAdapter nfcAdapter;

    // Preference Settings.
    private ColorFilter filter;

    // Char array for bytes to hex string method
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // Sets the Theme
        changeTheme(pref);
    }

    /**
     * Toggle app colour theme.
     *
     * @param pref Container for stored shared preferences.
     */
    private void changeTheme(SharedPreferences pref) {

        Context context = getApplicationContext();

        // Set dark theme if selected
        if (pref.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppThemeDark);
            // Set primary colour for icons.
            filter = new LightingColorFilter( ContextCompat.getColor(context, R.color.dark_primary_dark), ContextCompat.getColor(context, R.color.dark_primary_dark));
            // Display image.
            setContentView(R.layout.activity_nfc_reader_dark);

            // Set light theme if selected
        } else {
            setTheme(R.style.AppThemeLight);
            // Set primary colour for icons.
            filter = new LightingColorFilter( ContextCompat.getColor(context, R.color.primary_dark), ContextCompat.getColor(context, R.color.primary_dark));
            // Display image.
            setContentView(R.layout.activity_nfc_reader);
        }
    }

    /**
     * Display image and start listening for NFC tags.
     */
     @Override
    protected void onResume() {
         super.onResume();

        // Create PendingIntent for enableForegroundDispatch for NFC tag discovery.
        PendingIntent NFCIntent = PendingIntent.getActivity(this, 0, new Intent
                (this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] filters = new IntentFilter[] { tagDetected };
        nfcAdapter.enableForegroundDispatch(this, NFCIntent, filters, null);
    }

    /**
     * When a NFC tag is discovered, send ID to Lockscreens fragment.
     *
     * @param intent NFC discovered.
     */
    @Override
    protected void onNewIntent(Intent intent) {

        String action = intent.getAction();

        // If intent is a discovered NFC tag.
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {

            // Get tag ID and turn into String.
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] tagIDbytes = tag.getId();
            String tagID = bytesToHex(tagIDbytes);

            // Set shared preference to state tag has been scanned.
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("TagScanned", true);
            editor.apply();

            // Start MainActivity with tagID to parse to Lockscreens fragment.
            Intent newintent = new Intent(this, MainActivity.class);
            newintent.putExtra("tagID", tagID);
            startActivity(newintent);
        }
    }

    /**
     * Bytes to hex string method
     *
     * @param bytes NFC tag ID.
     *
     * @return NFC tag ID as hex.
     */
    private static String bytesToHex(byte[] bytes) {

        char[] hexChars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int x = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[x >>> 4];
            hexChars[i * 2+1] = hexArray[x & 0x0F];
        }

        return new String(hexChars);
    }
}
