package io.github.golimpio.hellonfc.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        Log.d(TAG, "Creating NFC adapter (v0.3)...");
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {


        Log.d(TAG, "onPostCreate called");

        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: re-enabling foreground NFC dispatch...");
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
//
//        Intent intent = getIntent();
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
//            Toast.makeText(this, "NDEF discovered", Toast.LENGTH_SHORT).show();
//        }
//        else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
//            Toast.makeText(this, "TECH discovered", Toast.LENGTH_SHORT).show();
//        }
//        else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
//            Toast.makeText(this, "TAG discovered", Toast.LENGTH_SHORT).show();
//            String text = getTagInfo(intent);
//
//            TextView log = (TextView) findViewById(R.id.log);
//            log.append(text + '\n');
//
//        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: disabling foreground NFC dispatch...");
        nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.d(TAG, "NFC tag: [" + (tag == null ? "<null>" : tag.toString()) + "]");


        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Toast.makeText(this, "NDEF discovered", Toast.LENGTH_SHORT).show();
        }
        else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Toast.makeText(this, "TECH discovered", Toast.LENGTH_SHORT).show();
        }
        else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Toast.makeText(this, "TAG discovered", Toast.LENGTH_SHORT).show();

            String text = getTagInfo(intent);

            TextView log = (TextView) findViewById(R.id.log);
            log.append(text + '\n');

            Log.i(TAG, text);
        }
        else {
            Toast.makeText(this, "No action discovered", Toast.LENGTH_SHORT).show();
        }

        super.onNewIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks
        // on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }

    private String getTagInfo(Intent intent)
    {
        Tag extraTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);	//required
        Parcelable[] extraNdefMsg = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);	//optional
        byte[] extraID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);	//optional

        Log.d(TAG, "Getting tag information...");

        extraTag.getId();

        String text = "";	//TODO: make this StringBuilder
        //text = extraTag.toString();

        String[] techList = extraTag.getTechList();
        if (techList.length > 0 ) {
            text += "TechList: ";
            for (String s: techList) {
                text += s + ", ";
            }
            //for now, just choose the first tech in the list
            String tech = techList[0];

            BasicTagTechnologyWrapper mTagTech;
            try {
                mTagTech = new BasicTagTechnologyWrapper(extraTag, tech);
            } catch (NoSuchMethodException | IllegalArgumentException | ClassNotFoundException
                        | IllegalAccessException | InvocationTargetException e) {
                mTagTech = null;
                Log.e(TAG, "Unsupported tag type: " + e.toString(), e);
            }

            if (mTagTech != null) {
                Log.i(TAG, "Tech list: " + Arrays.toString(mTagTech.getTag().getTechList()));
            }
        }

        text += "\nNDEF Messages: ";
        if (extraNdefMsg != null) {
            NdefMessage[] msgs = new NdefMessage[extraNdefMsg.length];
            for (int i = 0; i < extraNdefMsg.length; i++) {
                msgs[i] = (NdefMessage) extraNdefMsg[i];
                text += msgs[i].toString() + ", ";
            }
        }
        else
            text += "null";

        text += "\nExtra ID: ";
        if (extraID != null) {
            text += TextHelper.byteArrayToHexString(extraID);
        }
        else
            text += "null";

        text += "\nUID: " + TextHelper.byteArrayToHexString(extraTag.getId()) + "\n";

        return text;
    }
}
