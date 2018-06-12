package com.example.WiFiPasswordSearcher;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.webkit.WebView;

import java.io.InputStream;


public class AboutActivity extends Activity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        WebView about = (WebView) findViewById(R.id.aboutWeb);
        String filename = "about.html";
        String html;
        try {
            InputStream in = getAssets().open(filename);
            int size = in.available();
            byte[] data = new byte[size];
            int read = in.read(data);
            in.close();
            html = (read > 0 ? new String(data, "UTF-8") : "");
        }
        catch (Exception e) {
            html = "";
        }

        int backColor = getThemeColor(android.R.attr.colorBackground);
        int textColor = getResources().getColor(isColorDark(backColor) ? android.R.color.secondary_text_dark : android.R.color.secondary_text_light);
        html = html.replace("#000;", colorToCSS(backColor));
        html = html.replace("#fff;", colorToCSS(textColor));
        about.loadData(html, "text/html", "UTF-8");
    }
    private boolean isColorDark(int color)
    {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }
    private String colorToCSS(int color)
    {
        String str = String.format("%06x", color & 0xFFFFFF);
        return '#' + str + ";";
    }
    private int getThemeColor(int prop)
    {
        int color = 0;
        TypedValue v = new TypedValue();
        getTheme().resolveAttribute(prop, v, true);
        if (v.type >= TypedValue.TYPE_FIRST_COLOR_INT && v.type <= TypedValue.TYPE_LAST_COLOR_INT)
        {
            color = v.data;
        }
        return color;
    }
}
