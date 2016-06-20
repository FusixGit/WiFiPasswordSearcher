package com.example.WiFiPasswordSearcher;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Window;
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
    private HashMap<String, String> StartWifiInfo;
    private ScanResult WiFiInfo;
    private Thread ScanThread;
    private Thread DetectorThread;
    private SoundPool mSoundPool;
    private WifiManager WifiMgr;

    private String NetworkBSSID;
    private TextView txtBSSID;
    private TextView txtESSID;
    private TextView txtFreq;
    private TextView txtSignal;
    private TextView txtChannel;
    private CheckBox chkbUseDetector;
    private LinearLayout llGrphView;

    private boolean ScanThreadActive;
    private boolean UseWifiDetector;
    private int LastSignal;
    private int LastFreq;
    private String LastBSSID;
    private String LastESSID;


    private LineGraphSeries<DataPoint> graphSeries;
    private GraphView graphView;
    private int iGraphPointCount;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.wifi_details);


        HashMap<String, String> WifiInfoIntent;

        UseWifiDetector = false;
        StartWifiInfo = (HashMap<String, String>)(getIntent().getSerializableExtra("WifiInfo"));
        NetworkBSSID = StartWifiInfo.get("BSSID");

        txtBSSID = (TextView)this.findViewById(R.id.txtDetailsBSSID);
        txtESSID = (TextView)this.findViewById(R.id.txtDetailsESSID);
        txtFreq = (TextView)this.findViewById(R.id.txtDetailsFreq);
        txtSignal = (TextView)this.findViewById(R.id.txtDetailsSignal);
        txtChannel = (TextView)this.findViewById(R.id.txtDetailsChannel);
        chkbUseDetector = (CheckBox)this.findViewById(R.id.chkbUseDetector);
        llGrphView = (LinearLayout)this.findViewById(R.id.llGrphView);

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

        setBSSID(StartWifiInfo.get("BSSID"));
        setESSID(StartWifiInfo.get("SSID"));
        setFreq(StartWifiInfo.get("Freq"));
        setSignal(StartWifiInfo.get("Signal"));

        WifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        ScanThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ScanWorker();
            }
        });
        ScanThreadActive = true;
        ScanThread.start();
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
        int SleepTime = 0;

        while(UseWifiDetector)
        {
            mSoundPool.play(PickSoundId, 1, 1, 100, 0, 1);
            SleepTime = 2000-(2000/85)*LastSignal;
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

            for (ScanResult result : results) {
                if (result.BSSID.equals(NetworkBSSID)) {
                    WiFiInfo = result;
                    Update();
                    Founded = true;
                    break;
                }
            }
            if(!Founded) setSignal("0");
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
        if(BSSID == LastBSSID) return;

        String text = "BSSID: " + BSSID;

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
        if(ESSID == LastESSID) return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtESSID.setText(ESSID);
            }
        });
        LastESSID = ESSID;
    }

    private void setFreq(String Freq)
    {
        String sDiap = "";
        int Channel = 0;

        int iFreq = Integer.parseInt(Freq);
        if(iFreq == LastFreq) return;

        if(iFreq >= 2401 && iFreq <= 2483)
        {
            sDiap = "2.4 GHz";
            Channel = (int)((iFreq-2412)/5)+1;
        }
        else if(iFreq >= 5150 && iFreq <= 5250)
        {
            sDiap = "UNII 1";
            Channel = (5000+iFreq)/5;
        }
        else if(iFreq >= 5250 && iFreq <= 5350)
        {
            sDiap = "UNII 2";
            Channel = (5000+iFreq)/5;
        }
        else if(iFreq >= 5470 && iFreq <= 5725)
        {
            sDiap = "UNII 2 Extended";
            Channel = (int)(iFreq/5)+1;
        }
        else if(iFreq >= 5725  && iFreq <= 5825)
        {
            sDiap = "UNII 3";
            Channel = (5000+iFreq)/5;
        }
        else
        {
            sDiap = "";
        }

        String sText = "Freq: " + Freq + " MHz " + "( " + sDiap + " )";

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
        int iSignal = 100 + Integer.parseInt(Signal);
        LastSignal = iSignal;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                graphSeries.appendData(new DataPoint(iGraphPointCount, iSignal), true, 25);
                iGraphPointCount++;
                txtChannel.setText("Signal: " + iSignal + "%");
            }
        });
    }
    private void setChannel(int Channel)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtSignal.setText("Channel: " + Channel);
            }
        });
    }

}