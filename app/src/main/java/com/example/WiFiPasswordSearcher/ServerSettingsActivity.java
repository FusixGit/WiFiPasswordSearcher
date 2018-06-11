package com.example.WiFiPasswordSearcher;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;

public class ServerSettingsActivity extends Activity
{

    private EditText txtServerLogin = null;
    private EditText txtServerPassword = null;
    private EditText txtServerUri = null;
    private Switch swFetchESS = null;
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
        swFetchESS = (Switch) findViewById(R.id.sw_fetch_ess);
        swCheckUpd = (Switch) findViewById(R.id.sw_check_upd);

        btnCancel = (Button) findViewById(R.id.btn_settitgs_3wifi_cancel);
        btnSave = (Button) findViewById(R.id.btn_settitgs_3wifi_save);

        mSettings = new Settings(getApplicationContext());
        mSettings.Reload();

        String SERVER_LOGIN = mSettings.AppSettings.getString(Settings.APP_SERVER_LOGIN, "");
        String SERVER_PASSWORD = mSettings.AppSettings.getString(Settings.APP_SERVER_PASSWORD, "");
        String SERVER_URI = mSettings.AppSettings.getString(Settings.APP_SERVER_URI, getResources().getString(R.string.SERVER_URI_DEFAULT));
        Boolean FETCH_ESS =  mSettings.AppSettings.getBoolean(Settings.APP_FETCH_ESS, false);
        Boolean CHECK_UPDATES =  mSettings.AppSettings.getBoolean(Settings.APP_CHECK_UPDATES, true);

        txtServerLogin.setText(SERVER_LOGIN);
        txtServerPassword.setText(SERVER_PASSWORD);
        txtServerUri.setText(SERVER_URI);
        swFetchESS.setChecked(FETCH_ESS);
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
            String Login = txtServerLogin.getText().toString();
            String Password = txtServerPassword.getText().toString();
            String Uri = txtServerUri.getText().toString();
            Boolean FetchESS = swFetchESS.isChecked();
            Boolean CheckUpdates = swCheckUpd.isChecked();

            // Save
            mSettings.Editor.putString(Settings.APP_SERVER_LOGIN, Login);
            mSettings.Editor.putString(Settings.APP_SERVER_PASSWORD, Password);
            mSettings.Editor.putString(Settings.APP_SERVER_URI, Uri);
            mSettings.Editor.putBoolean(Settings.APP_FETCH_ESS, FetchESS);
            mSettings.Editor.putBoolean(Settings.APP_CHECK_UPDATES, CheckUpdates);
            mSettings.Editor.commit();

            finish();
        }
    };

    public void cbUnmaskClick(View view)
    {
        int eType = txtServerPassword.getInputType();
        if (((CheckBox)view).isChecked()) {
            txtServerPassword.setInputType(eType & ~InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        else {
            txtServerPassword.setInputType(eType | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }
}