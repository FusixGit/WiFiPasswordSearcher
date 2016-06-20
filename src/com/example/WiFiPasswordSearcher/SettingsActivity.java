package com.example.WiFiPasswordSearcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class SettingsActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    private ListView GeneralListView = null;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        GeneralListView = (ListView) findViewById(R.id.SettingsListView);

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

                case 1: // About
                    Intent AboutInfoIntent = new Intent(SettingsActivity.this, AboutActivity.class);
                    startActivity(AboutInfoIntent);
                    break;
                case 2: // Check updates
                    AppVersion Version = new AppVersion(getApplicationContext());
                    if(!Version.isActualyVersion())
                    {
                        Version.ShowUpdateDialog(SettingsActivity.this);
                    }
                    break;
            }
        }
    };
}
