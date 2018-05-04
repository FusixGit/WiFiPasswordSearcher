package com.example.WiFiPasswordSearcher;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by пк on 20.12.2015.
 */
public class UserInfoActivity extends Activity {

    public TextView txtLogin = null;
    public TextView txtRegDate = null;
    public TextView txtGroup = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user);

        UserManager User = new UserManager(getApplicationContext());
        User.getFromSettings();

        txtLogin = (TextView) findViewById(R.id.txtLogin);
        txtRegDate = (TextView) findViewById(R.id.txtRegDate);
        txtGroup = (TextView) findViewById(R.id.txtGroup);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date date;
        try {
            date = format.parse(User.RegDate);
        } catch (Exception e) {
            date = new Date();
        }

        String Nick = User.NickName;
        String RegDate = DateFormat.getDateTimeInstance().format(date);
        String Group = User.GetGroup();

        txtLogin.setText(Nick);
        txtRegDate.setText(RegDate);
        txtGroup.setText(Group);
    }
}
