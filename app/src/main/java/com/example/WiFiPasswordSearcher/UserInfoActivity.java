package com.example.WiFiPasswordSearcher;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by пк on 20.12.2015.
 */
public class UserInfoActivity extends Activity {

    private Settings mSettings = null;
    private UserManager User = null;

    public TextView txtLogin = null;
    public TextView txtRegDate = null;
    public TextView txtGroup = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user);

        mSettings = new Settings(getApplicationContext());

        User = new UserManager(getApplicationContext());
        User.getFromSettings();

        txtLogin = (TextView) findViewById(R.id.txtLogin);
        txtRegDate = (TextView) findViewById(R.id.txtRegDate);
        txtGroup = (TextView) findViewById(R.id.txtGroup);

        String Nick = User.NickName;
        String RegDate = User.RegDate;
        String Group = User.GetGroup();

        txtLogin.setText(Nick);
        txtRegDate.setText(RegDate);
        txtGroup.setText(Group);
    }
}
