package com.example.WiFiPasswordSearcher;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.HashMap;
import java.util.List;

public class WifiDetails extends Activity
{
    private ScanResult WiFiInfo;
    private Thread DetectorThread;
    private SoundPool mSoundPool;
    private WifiManager WifiMgr;

    private String NetworkBSSID;
    private String NetworkESSID;
    private TextView txtBSSID;
    private TextView txtESSID;
    private TextView txtFreq;
    private TextView txtSignal;
    private TextView txtChannel;

    private boolean ScanThreadActive;
    private boolean UseWifiDetector;
    private int LastSignal;
    private int LastFreq = -1;
    private String LastBSSID;
    private String LastESSID;
    private Settings mSettings;


    private LineGraphSeries<DataPoint> graphSeries;
    private GraphView graphView;
    private int iGraphPointCount;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_details);
        this.onConfigurationChanged(getResources().getConfiguration());

        UseWifiDetector = false;
        HashMap<String, String> StartWifiInfo = (HashMap<String, String>)(getIntent().getSerializableExtra("WifiInfo"));
        NetworkBSSID = StartWifiInfo.get("BSSID");
        NetworkESSID = StartWifiInfo.get("SSID");

        txtBSSID = (TextView)this.findViewById(R.id.txtDetailsBSSID);
        txtESSID = (TextView)this.findViewById(R.id.txtDetailsESSID);
        txtFreq = (TextView)this.findViewById(R.id.txtDetailsFreq);
        txtSignal = (TextView)this.findViewById(R.id.txtDetailsSignal);
        txtChannel = (TextView)this.findViewById(R.id.txtDetailsChannel);
        CheckBox chkbUseDetector = (CheckBox)this.findViewById(R.id.chkbUseDetector);
        LinearLayout llGrphView = (LinearLayout)this.findViewById(R.id.llGrphView);

        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        // Graph init
        iGraphPointCount = 0;
        graphSeries = new LineGraphSeries<>();
        graphView = new GraphView(this);
        graphView.getGridLabelRenderer().setNumVerticalLabels(2);
        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(100);
        graphView.getViewport().setYAxisBoundsStatus(Viewport.AxisBoundsStatus.FIX);
        graphView.getViewport().setYAxisBoundsManual(true);

        graphView.setTitle("Signal graph");
        graphView.addSeries(graphSeries);
        llGrphView.addView(graphView);

        chkbUseDetector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UseWifiDetector = isChecked;

                mSettings.Editor.putBoolean(Settings.WIFI_SIGNAL, UseWifiDetector);
                mSettings.Editor.commit();

                if (UseWifiDetector) {
                    DetectorThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DetectorWorker();
                        }
                    });
                    DetectorThread.start();
                }
            }
        });

        mSettings = new Settings(getApplicationContext());
        mSettings.Reload();

        Boolean wifiSignal = mSettings.AppSettings.getBoolean(Settings.WIFI_SIGNAL, false);
        chkbUseDetector.setChecked(wifiSignal);

        setBSSID(StartWifiInfo.get("BSSID"));
        setESSID(StartWifiInfo.get("SSID"));
        setFreq(StartWifiInfo.get("Freq"));
        setSignal(StartWifiInfo.get("Signal"));

        WifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        Thread ScanThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ScanWorker();
            }
        });
        ScanThreadActive = true;
        ScanThread.start();
    }

    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        LinearLayout LR = (LinearLayout)findViewById(R.id.rootLayout);
        LinearLayout LI = (LinearLayout)findViewById(R.id.layoutInfo);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            LR.setOrientation(LinearLayout.VERTICAL);
            ViewGroup.LayoutParams layoutParams = LI.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            LI.setLayoutParams(layoutParams);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            LR.setOrientation(LinearLayout.HORIZONTAL);
            ViewGroup.LayoutParams layoutParams = LI.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            LI.setLayoutParams(layoutParams);
        }
    }

    protected void onDestroy()
    {
        super.onDestroy();
        UseWifiDetector = false;
        ScanThreadActive = false;
        graphView.removeAllSeries();
        graphSeries = null;
    }
    private void Update()
    {
        setBSSID(WiFiInfo.BSSID);
        setESSID(WiFiInfo.SSID);
        setFreq(Integer.toString(WiFiInfo.frequency));
        setSignal(Integer.toString(WiFiInfo.level));
    }

    private void DetectorWorker()
    {
        int PickSoundId = mSoundPool.load(getApplicationContext(), R.raw.pick, 1);

        while (UseWifiDetector)
        {
            if (LastSignal > 0)
                mSoundPool.play(PickSoundId, 1, 1, 100, 0, 1);
            int SleepTime = 2100-(2000/100)*LastSignal;
            try {
                Thread.sleep((long)SleepTime, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void ScanWorker() // THREADED!
    {
        while(ScanThreadActive)
        {
            List<ScanResult> results;

            Boolean Founded = false;
            WifiMgr.startScan();
            results = WifiMgr.getScanResults();
            boolean match;

            for (ScanResult result : results) {
                if (!NetworkBSSID.isEmpty() && !NetworkESSID.isEmpty())
                {
                    match = (result.BSSID.equals(NetworkBSSID) && result.SSID.equals(NetworkESSID));
                }
                else if (NetworkBSSID.isEmpty() && !NetworkESSID.isEmpty())
                {
                    match = result.SSID.equals(NetworkESSID);
                }
                else if (!NetworkBSSID.isEmpty() && NetworkESSID.isEmpty())
                {
                    match = result.BSSID.equals(NetworkBSSID);
                }
                else
                {
                    match = true;
                }
                if (match) {
                    if (NetworkBSSID.isEmpty()) NetworkBSSID = result.BSSID;
                    if (NetworkESSID.isEmpty()) NetworkESSID = result.SSID;

                    WiFiInfo = result;
                    Update();
                    Founded = true;
                    break;
                }
            }
            if (!Founded) setSignal("-100");
            try {
                Thread.sleep((long)1000, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void setBSSID(String BSSID)
    {
        BSSID = BSSID.toUpperCase();
        if (BSSID.equals(LastBSSID)) return;

        final String text = "BSSID: " + (BSSID.isEmpty() ? "Unknown" : BSSID);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtBSSID.setText(text);
            }
        });
        LastBSSID = BSSID;
    }

    private void setESSID(String ESSID)
    {
        if (ESSID.equals(LastESSID)) return;
        final String text = (ESSID.isEmpty() ? "<unknown>" : ESSID);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtESSID.setText(text);
            }
        });
        LastESSID = ESSID;
    }

    private void setFreq(String Freq)
    {
        String sDiap = "";
        int Channel = 0;

        int iFreq = Integer.parseInt(Freq);
        if (iFreq == LastFreq) return;

        if (iFreq > 0)
        {
            if (iFreq >= 2401 && iFreq <= 2483)
            {
                sDiap = "2.4 GHz";
                Channel = ((iFreq - 2412) / 5) + 1;
            }
            else if (iFreq >= 5150 && iFreq <= 5250)
            {
                sDiap = "UNII 1";
                Channel = (5000 + iFreq) / 5;
            }
            else if (iFreq >= 5250 && iFreq <= 5350)
            {
                sDiap = "UNII 2";
                Channel = (5000 + iFreq) / 5;
            }
            else if (iFreq >= 5470 && iFreq <= 5725)
            {
                sDiap = "UNII 2 Extended";
                Channel = (iFreq / 5) + 1;
            }
            else if (iFreq >= 5725  && iFreq <= 5825)
            {
                sDiap = "UNII 3";
                Channel = (5000 + iFreq) / 5;
            }
        }

        final String sText = "Freq: " + (iFreq <= 0 ? "Unknown" : Freq + " MHz " + "(" + sDiap + ")");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtFreq.setText(sText);
            }
        });
        setChannel(Channel);

        LastFreq = iFreq;
    }

    private void setSignal(String Signal)
    {
        int iSignal = Integer.parseInt(Signal);
        iSignal = (100 + iSignal) * 2;
        iSignal = Math.min(Math.max(iSignal, 0), 100);
        final int fSignal = iSignal;
        LastSignal = iSignal;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                graphSeries.appendData(new DataPoint(iGraphPointCount, fSignal), true, 25);
                iGraphPointCount++;
                txtSignal.setText("Signal: " + fSignal + "%");
            }
        });
    }
    private void setChannel(final int Channel)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtChannel.setText("Channel: " + (Channel <= 0 ? "N/A" : Channel));
            }
        });
    }

}