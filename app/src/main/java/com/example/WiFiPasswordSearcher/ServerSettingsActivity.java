package com.example.WiFiPasswordSearcher;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

public class ServerSettingsActivity extends Activity
{

    private EditText txtServerLogin = null;
    private EditText txtServerPassword = null;
    private EditText txtServerUri = null;
    private Switch swCheckUpd = null;

    Button btnCancel = null;
    Button btnSave = null;

    Settings mSettings;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_settings);

        txtServerLogin = (EditText) findViewById(R.id.txt_settings_3wifi_login);
        txtServerPassword = (EditText) findViewById(R.id.txt_settings_3wifi_password);
        txtServerUri = (EditText) findViewById(R.id.txt_settings_3wifi_server);
        swCheckUpd = (Switch) findViewById(R.id.sw_check_upd);

        btnCancel = (Button) findViewById(R.id.btn_settitgs_3wifi_cancel);
        btnSave = (Button) findViewById(R.id.btn_settitgs_3wifi_save);

        mSettings = new Settings(getApplicationContext());
        mSettings.Reload();

        String SERVER_LOGIN = mSettings.AppSettings.getString(Settings.APP_SERVER_LOGIN, "");
        String SERVER_PASSWORD = mSettings.AppSettings.getString(Settings.APP_SERVER_PASSWORD, "");
        String SERVER_URI = mSettings.AppSettings.getString(Settings.APP_SERVER_URI, getResources().getString(R.string.SERVER_URI_DEFAULT));
        Boolean CHECK_UPDATES =  mSettings.AppSettings.getBoolean(Settings.APP_CHECK_UPDATES, true);

        txtServerLogin.setText(SERVER_LOGIN);
        txtServerPassword.setText(SERVER_PASSWORD);
        txtServerUri.setText(SERVER_URI);
        swCheckUpd.setChecked(CHECK_UPDATES);

        btnCancel.setOnClickListener(btnCloseOnClick);
        btnSave.setOnClickListener(btnSaveOnClick);

    }
    private View.OnClickListener btnCloseOnClick = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            finish();
        }
    };
    private View.OnClickListener btnSaveOnClick = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            String Login = "";
            String Password = "";
            String Uri = "";
            Boolean CheckUpdates = true;

            Login = txtServerLogin.getText().toString();
            Password = txtServerPassword.getText().toString();
            Uri = txtServerUri.getText().toString();
            CheckUpdates = swCheckUpd.isChecked();

            // Save
            mSettings.Editor.putString(Settings.APP_SERVER_LOGIN, Login);
            mSettings.Editor.putString(Settings.APP_SERVER_PASSWORD, Password);
            mSettings.Editor.putString(Settings.APP_SERVER_URI, Uri);
            mSettings.Editor.putBoolean(Settings.APP_CHECK_UPDATES, CheckUpdates);
            mSettings.Editor.commit();

            finish();
        }
    };
}