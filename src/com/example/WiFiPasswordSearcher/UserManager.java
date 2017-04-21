package com.example.WiFiPasswordSearcher;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by οκ on 20.12.2015.
 */
public class UserManager {
    Context context = null;
    private Settings mSettings = null;
    private String APP_VERSION = "";
    private String SERVER_URI = "";
    private String API_READ_KEY = "";

    public String Login = "";
    public String NickName = "";
    public String RegDate = "";
    public String InviterNickName = "";
    public Integer Level = -2;
    public Integer InvCount = 0;
    public Integer LastUpdate = 0;

    UserManager(Context context) {
        APP_VERSION = context.getResources().getString(R.string.app_version);
        mSettings = new Settings(context);
        SERVER_URI = mSettings.AppSettings.getString(Settings.APP_SERVER_URI, "http://3wifi.stascorp.com");
        API_READ_KEY = mSettings.AppSettings.getString(Settings.API_READ_KEY, "");
        Login = mSettings.AppSettings.getString(Settings.APP_SERVER_LOGIN, "");
    }

    public boolean isActualyData()
    {
        if(LastUpdate > 0) return true;
        return false;
    }

    public void getFromSettings()
    {
        RegDate = mSettings.AppSettings.getString(Settings.USER_REGDATE, "");
        Level = mSettings.AppSettings.getInt(Settings.USER_GROUP, -2);
        InvCount = mSettings.AppSettings.getInt(Settings.USER_INVCOUNT, 0);
        InviterNickName = mSettings.AppSettings.getString(Settings.USER_INVITER, "");
        LastUpdate = mSettings.AppSettings.getInt(Settings.USER_LASTUPDATE, 0);
    }

    public boolean getFromSite()
    {
        if(API_READ_KEY == "")
            API_READ_KEY = mSettings.AppSettings.getString(Settings.API_READ_KEY, "");
        String Args = "/api/ajax.php?Version="+APP_VERSION+"&Query=GetUserInfo&Key="+API_READ_KEY;
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
                if(Successes)
                {
                    NickName = Json.getString("Nickname");
                    RegDate = Json.getString("RegDate");
                    Level = Json.getInt("Level");
                    InvCount = Json.getInt("InvCount");
                    InviterNickName = Json.getString("Inviter");
                    LastUpdate = Json.getInt("LastUpdate");

                    mSettings.Editor.putString(Settings.USER_REGDATE, RegDate);
                    mSettings.Editor.putInt(Settings.USER_GROUP, Level);
                    mSettings.Editor.putInt(Settings.USER_INVCOUNT, InvCount);
                    mSettings.Editor.putString(Settings.USER_INVITER, InviterNickName);
                    mSettings.Editor.putInt(Settings.USER_LASTUPDATE, LastUpdate);
                    mSettings.Editor.commit();

                    return true;
                }

            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally
        {

        }
        return false;
    }

    public String GetGroup()
    {
        return GetTextGroup(Level);
    }

    public String GetTextGroup(Integer Level)
    {
        switch (Level)
        {
            case -2: return "Banned";
            case -1: return "No logged";
            case 0: return "Guest";
            case 1: return "User";
            case 2: return "Developer";
            case 3: return "Administrator";
        }
        return "";
    }

}
