package com.example.WiFiPasswordSearcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppVersion
{
    private Context context;
    private String SERVER_URI;
    private String ActualyVersion;
    private String WhatNews;
    private Boolean LoadSuccesses = false;

    public AppVersion(Context _context)
    {
        context = _context;
        ActualyVersion = "";
        WhatNews = "";

        Settings mSettings = new Settings(context);
        SERVER_URI = mSettings.AppSettings.getString(mSettings.APP_SERVER_URI, context.getResources().getString(R.string.SERVER_URI_DEFAULT));
    }

    public void ShowUpdateDialog(Activity activity)
    {
        AlertDialog.Builder ad = new AlertDialog.Builder(activity);

        ad.setTitle("New version " + ActualyVersion + " available!");
        ad.setMessage(WhatNews);
        ad.setCancelable(false);
        ad.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getResources().getString(R.string.SERVER_URI_DEFAULT) + "/api/app.latest.apk"));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(browserIntent);
            }
        });

        ad.setNegativeButton("Ask later", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });

        ad.show();
    }

    private void GetActualyVersion() throws IOException
    {
        String Args = "/api/ajax.php?Query=AppVersion";
        BufferedReader Reader = null;
        String ReadLine = "";
        String RawData = "";

        try {
            URL Uri = new URL(SERVER_URI + Args);

            HttpURLConnection Connection = (HttpURLConnection) Uri.openConnection();
            Connection.setRequestMethod("GET");
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
                LoadSuccesses = Successes;
                if(LoadSuccesses)
                {
                    ActualyVersion = Json.getString("ActualyVersion");
                    WhatNews = Json.getString("WhatNews");
                    return;
                }

            } catch (JSONException e)
            {
                e.printStackTrace();
            }

        }
        finally
        {

        }
    }

    public Boolean isActualyVersion()
    {
        if(!LoadSuccesses) try {
            GetActualyVersion();
        } catch (IOException e) {
            return true;
        }
        if(!LoadSuccesses) return true;
        if(context.getResources().getString(R.string.app_version).equals(ActualyVersion)) return true;
        return false;
    }
}
