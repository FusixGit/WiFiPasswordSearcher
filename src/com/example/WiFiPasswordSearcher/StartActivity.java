package com.example.WiFiPasswordSearcher;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by пк on 20.12.2015.
 */
public class StartActivity extends Activity {

    private Settings mSettings;
    private static Boolean VersionAlreadyChecked = false;
    private UserManager User;

    public String SERVER_URI = "";
    public EditText edtLogin = null;
    public EditText edtPassword = null;
    public Button btnGetKeys = null;
    public Button btnStart = null;
    public Button btnUserInfo = null;
    public LinearLayout llMenu = null;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);

        mSettings = new Settings(getApplicationContext());
        User = new UserManager(getApplicationContext());

        edtLogin = (EditText)findViewById(R.id.edtLogin);
        edtPassword = (EditText)findViewById(R.id.edtPassword);

        llMenu = (LinearLayout)findViewById(R.id.llStartMenu);
        btnGetKeys = (Button)findViewById(R.id.btnGetApiKeys);
        btnStart = (Button)findViewById(R.id.btnStart);
        btnUserInfo = (Button)findViewById(R.id.btnUserInfo);

        SERVER_URI = mSettings.AppSettings.getString(Settings.APP_SERVER_URI, "http://3wifi.stascorp.com");
        String API_READ_KEY = mSettings.AppSettings.getString(Settings.API_READ_KEY, "");
        String API_WRITE_KEY = mSettings.AppSettings.getString(Settings.API_WRITE_KEY, "");
        Boolean API_KEYS_VALID = mSettings.AppSettings.getBoolean(Settings.API_KEYS_VALID, false);
        String SavedLogin = mSettings.AppSettings.getString(Settings.APP_SERVER_LOGIN, "");
        String SavedPassword = mSettings.AppSettings.getString(Settings.APP_SERVER_PASSWORD, "");
        Boolean CHECK_UPDATES = mSettings.AppSettings.getBoolean(Settings.APP_CHECK_UPDATES, true);

        String LastUpdate = mSettings.AppSettings.getString(Settings.USER_LASTUPDATE, "");

        if(API_KEYS_VALID) {
            btnGetKeys.setVisibility(View.GONE);
            edtLogin.setEnabled(false);
            edtPassword.setEnabled(false);
            llMenu.setVisibility(View.VISIBLE);

            if(CHECK_UPDATES && !VersionAlreadyChecked)
            {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final AppVersion Version = new AppVersion(getApplicationContext());
                        Boolean Result = Version.isActualyVersion();
                        if(!Result)
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    Version.ShowUpdateDialog(StartActivity.this);
                                }
                            });
                        }
                        VersionAlreadyChecked = true;
                    }
                }).start();
            }

        }

        edtLogin.setText(SavedLogin);
        edtPassword.setText(SavedPassword);

        btnGetKeys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dProccess = new ProgressDialog(StartActivity.this);
                dProccess.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dProccess.setMessage("Logging...");
                dProccess.setCanceledOnTouchOutside(false);
                dProccess.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Boolean res = false;

                        try {
                            res = getApiKeys(edtLogin.getText().toString(), edtPassword.getText().toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (res)
                        {
                            User.getFromSite();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnGetKeys.setVisibility(View.GONE);
                                    llMenu.setVisibility(View.VISIBLE);
                                    edtLogin.setEnabled(false);
                                    edtPassword.setEnabled(false);
                                }});
                        }
                        dProccess.dismiss();
                    }
                }).start();
            }
        });

        btnUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent userActivity = new Intent(StartActivity.this, UserInfoActivity.class);
                startActivity(userActivity);
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivity = new Intent(StartActivity.this, MyActivity.class);
                startActivity(mainActivity);
                finish();
                return;
            }
        });
    }

    private boolean getApiKeys(String Login, String Password) throws IOException
    {
            String Args = "/api/ajax.php?Query=GetApiKeys";
            BufferedReader Reader = null;
            String ReadLine = "";
            String RawData = "";

            try {
                URL Uri = new URL(SERVER_URI + Args);

                HttpURLConnection Connection = (HttpURLConnection) Uri.openConnection();
                Connection.setRequestMethod("POST");
                Connection.setDoOutput(true);
                Connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStream os = Connection.getOutputStream();
                DataOutputStream  writer = new DataOutputStream (
                        Connection.getOutputStream());
                writer.writeBytes("Login=" + URLEncoder.encode(Login, "UTF-8") + "&Password=" + URLEncoder.encode(Password, "UTF-8"));

                Connection.setReadTimeout(10 * 1000);
                Connection.connect();

                Reader = new BufferedReader(new InputStreamReader(Connection.getInputStream()));

                while ((ReadLine = Reader.readLine()) != null) {
                    RawData += ReadLine;
                }

                try
                {
                    JSONObject Json = new JSONObject(RawData);
                    Boolean Successes = Json.getBoolean("Successes");
                    if(Successes)
                    {
                        String ReadApiKey = Json.getString("r");
                        String WriteApiKey = Json.getString("w");

                        mSettings.Editor.putString(Settings.APP_SERVER_LOGIN, Login);
                        mSettings.Editor.putString(Settings.APP_SERVER_PASSWORD, Password);
                        mSettings.Editor.putString(Settings.API_READ_KEY, ReadApiKey);
                        mSettings.Editor.putString(Settings.API_WRITE_KEY, WriteApiKey);
                        mSettings.Editor.putBoolean(Settings.API_KEYS_VALID, true);
                        mSettings.Editor.commit();

                        return true;
                    }else {
                        JSONObject error = Json.getJSONObject("Error");
                        if (error != null) {
                            final String errorDesc = error.getString("Desc");
                            Integer errorCode = error.getInt("Code");

                            if(errorDesc != null)
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast t = Toast.makeText(getApplicationContext(), errorDesc, Toast.LENGTH_SHORT);
                                        t.show();
                                    }
                                });

                            }
                        }


                    }

                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }finally
            {

            }
        return false;
    }
}
