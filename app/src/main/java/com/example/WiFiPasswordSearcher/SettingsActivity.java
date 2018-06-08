package com.example.WiFiPasswordSearcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;

import java.util.HashMap;

public class SettingsActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        ListView GeneralListView = (ListView) findViewById(R.id.SettingsListView);

        String[] strSettingsRows = getResources().getStringArray(R.array.strings_settings_rows);
        ArrayAdapter<String> adapterSettingsListView = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strSettingsRows);
        GeneralListView.setAdapter(adapterSettingsListView);

        GeneralListView.setOnItemClickListener(GeneralListOnClick);
    }

    private AdapterView.OnItemClickListener GeneralListOnClick = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> parent, View Item, int position, long id)
        {
            TextView txtItem = (TextView) Item;

            switch((int)id)
            {
                case 0: // 3WiFi settings
                    Intent ServerSettingsIntent = new Intent(SettingsActivity.this, ServerSettingsActivity.class);
                    startActivity(ServerSettingsIntent);
                    break;
                case 1: // Manage WPS PIN Companion
                    Intent userActivity = new Intent(SettingsActivity.this, UserInfoActivity.class);
                    userActivity.putExtra("showInfo", "wpspin");
                    startActivity(userActivity);
                    break;
                case 2: // Monitor a network
                    LinearLayout lay = new LinearLayout(SettingsActivity.this);
                    lay.setOrientation(LinearLayout.VERTICAL);

                    final EditText ebss = new EditText(SettingsActivity.this);
                    ebss.setHint("Enter BSSID (11:22:33:44:55:66)");
                    ebss.setInputType(InputType.TYPE_CLASS_TEXT);
                    lay.addView(ebss);

                    final EditText eess = new EditText(SettingsActivity.this);
                    eess.setHint("Enter ESSID");
                    eess.setInputType(InputType.TYPE_CLASS_TEXT);
                    lay.addView(eess);

                    AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
                    alert.setTitle("Enter network properties");
                    alert.setView(lay);

                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Intent detailsActivityIntent = new Intent(SettingsActivity.this, WifiDetails.class);
                            HashMap<String, String> WifiInfo = new HashMap<String, String>();
                            WifiInfo.put("BSSID", ebss.getText().toString().toLowerCase());
                            WifiInfo.put("SSID", eess.getText().toString());
                            WifiInfo.put("Freq", "0");
                            WifiInfo.put("Signal", "-100");

                            finish();
                            detailsActivityIntent.putExtra("WifiInfo", WifiInfo);
                            startActivity(detailsActivityIntent);
                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });

                    alert.show();
                    break;
                case 3: // About
                    Intent AboutInfoIntent = new Intent(SettingsActivity.this, AboutActivity.class);
                    startActivity(AboutInfoIntent);
                    break;
                case 4: // Check updates
                    AppVersion Version = new AppVersion(getApplicationContext());
                    if (!Version.isActualyVersion(getApplicationContext(), true))
                    {
                        Version.ShowUpdateDialog(SettingsActivity.this);
                    }
                    break;
                case 5: // Logout
                    Settings mSettings = new Settings(getApplicationContext());
                    mSettings.Reload();
                    mSettings.Editor.remove(Settings.APP_SERVER_LOGIN);
                    mSettings.Editor.remove(Settings.APP_SERVER_PASSWORD);
                    mSettings.Editor.remove(Settings.API_READ_KEY);
                    mSettings.Editor.remove(Settings.API_WRITE_KEY);
                    mSettings.Editor.remove(Settings.API_KEYS_VALID);
                    mSettings.Editor.commit();

                    Intent StartPage = new Intent(getApplicationContext(), StartActivity.class);
                    StartPage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(StartPage);
                    break;
            }
        }
    };
}
