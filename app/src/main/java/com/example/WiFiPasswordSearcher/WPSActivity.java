package com.example.WiFiPasswordSearcher;

import android.app.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.json.*;


public class WPSActivity extends Activity
{
	private WebView mWebView;

	ArrayList<ItemWps> data = new ArrayList<ItemWps>();
	String[] pinlist;
	ProgressDialog pd=null;
	private Settings mSettings;
    public static String SERVER_URI = "";
    public static String API_READ_KEY = "";

	ArrayList<String> wpsPin = new ArrayList<String>();
	ArrayList<String> wpsMet = new ArrayList<String>();
	ArrayList<String> wpsScore = new ArrayList<String>();
	ArrayList<String> wpsDb = new ArrayList<String>();

	private DatabaseHelper mDBHelper;
	private SQLiteDatabase mDb;


    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.wps);
		findViewById(R.id.baseButton).getBackground().setColorFilter(Color.parseColor("#1cd000"), PorterDuff.Mode.MULTIPLY);

		mDBHelper = new DatabaseHelper(this);
		try
		{
            mDBHelper.updateDataBase();
        }
		catch (IOException mIOException)
		{
            throw new Error("UnableToUpdateDatabase");
        }

        try
		{
            mDb = mDBHelper.getWritableDatabase();
        }
		catch (SQLException mSQLException)
		{
            throw mSQLException;
        }




		if (android.os.Build.VERSION.SDK_INT > 9)
		{
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        mSettings = new Settings(getApplicationContext());

        SERVER_URI = mSettings.AppSettings.getString(Settings.APP_SERVER_URI, "http://3wifi.stascorp.com");
        API_READ_KEY = mSettings.AppSettings.getString(Settings.API_READ_KEY, "");

		ActionBar actionBar = getActionBar(); actionBar.hide();

		TextView ESSDWpsText = (TextView)findViewById(R.id.ESSDWpsTextView);
		String ESSDWps = getIntent().getExtras().getString("variable");
		ESSDWpsText.setText(ESSDWps); // ESSID
		TextView BSSDWpsText = (TextView)findViewById(R.id.BSSDWpsTextView);
		final String BSSDWps = getIntent().getExtras().getString("variable1");
		BSSDWpsText.setText(BSSDWps); // BSSID

		new GetPinsFromBase().execute(BSSDWps);
		
		mWebView = (WebView) findViewById(R.id.webView);
		mWebView.addJavascriptInterface(new myJavascriptInterface(), "HtmlHandler");
		mWebView.setWebViewClient(new WebViewClient(){
				@Override
				public void onPageFinished(WebView view, String url)
				{
					super.onPageFinished(view, url);
					final String BSSDWps = getIntent().getExtras().getString("variable1");
					mWebView.loadUrl("javascript:document.getElementById('bssids').innerHTML = '" + BSSDWps + "';pinSuggest()");
					mWebView.loadUrl("javascript:window.HtmlHandler.handleHtml" +
									 "(document.getElementById('result').value);");//js возвращает значение

				}
			});
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadUrl("file:///android_asset/wpspin.html");

    }
	private class GetPinsFromBase extends AsyncTask <String, Void, String>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pd = ProgressDialog.show(WPSActivity.this, "Please wait...", "Getting pins...");
		}


		protected String doInBackground(String[] BSSDWps)
		{
			String BSSID = BSSDWps[0];
			String response = "";
			String response2 = "";
			data.clear();
			wpsScore.clear();
			wpsDb.clear();
			wpsPin.clear();
			wpsMet.clear();
			DefaultHttpClient hc = new DefaultHttpClient();
			DefaultHttpClient hc2 = new DefaultHttpClient();
			ResponseHandler res = new BasicResponseHandler();
			ResponseHandler res2 = new BasicResponseHandler();

			HttpPost http2 = new HttpPost("http://wpsfinder.com/ethernet-wifi-brand-lookup/MAC:" + BSSID);
			try
			{
				response2 = hc2.execute(http2, res2);
				response2 = response2.substring(response2.indexOf("muted'><center>") + 15, response2.indexOf("<center></h4><h6"));
			}
			catch (Exception e)
			{}

			HttpGet http = new HttpGet(SERVER_URI + "/api/apiwps?key=" + API_READ_KEY + "&bssid=" + BSSID);
			try
			{
				response = hc.execute(http, res);
			}
			catch (Exception e)
			{}
			try
			{
				JSONObject jObject = new JSONObject(response);
				jObject = jObject.getJSONObject("data");
				jObject = jObject.getJSONObject(BSSID);

				JSONArray array =jObject.optJSONArray("scores");
				for (int i = 0; i < array.length(); i++)
				{
					jObject = array.getJSONObject(i);
					wpsPin.add(jObject.getString("value"));
					wpsMet.add(jObject.getString("name"));
					wpsScore.add(jObject.getString("score"));
					if (jObject.getBoolean("fromdb"))
					{
						wpsDb.add("✔");
					}
					else
					{wpsDb.add("");}
					Integer score = Math.round(Float.parseFloat(wpsScore.get(i)) * 100);
					wpsScore.set(i, Integer.toString(score) + "%");

					data.add(new ItemWps(wpsPin.get(i), wpsMet.get(i), wpsScore.get(i), wpsDb.get(i)));
				}
			}
			catch (JSONException e)
			{
			    e.printStackTrace();
			}

			return response2;
		}

		@Override
		protected void onPostExecute(String response2)
		{

			pd.dismiss();
			ListView wpslist = (ListView)findViewById(R.id.WPSlist);
			if (data.isEmpty())
			{
				data.add(new ItemWps(null, "   not found", null, null));
				wpslist.setEnabled(false);
			}

			wpslist.setAdapter(new MyAdapterWps(WPSActivity.this, data));
			TextView VendorWpsText = (TextView)findViewById(R.id.VendorWpsTextView);
			if (response2.length() > 50)
			{response2 = "unknown vendor";}
			VendorWpsText.setText(response2);

			wpslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id)
					{
						String pin = wpsPin.get(position);
						Toast.makeText(getApplicationContext(), "Pin " + pin + " copied", Toast.LENGTH_SHORT).show();
						try
						{
							ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
							ClipData dataClip = ClipData.newPlainText("text", pin);
							clipboard.setPrimaryClip(dataClip);
						}
						catch (Exception e)
						{}
					}
				});
		}
    }
	public void btnwpsbaseclick(View view)
	{ //пины из базы
		findViewById(R.id.baseButton).getBackground().setColorFilter(Color.parseColor("#1cd000"), PorterDuff.Mode.MULTIPLY);
		findViewById(R.id.wpsButton1).getBackground().clearColorFilter();
		findViewById(R.id.wpsButton2).getBackground().clearColorFilter();
		String BSSDWps = getIntent().getExtras().getString("variable1");
		new GetPinsFromBase().execute(BSSDWps);
	}

	
private class myJavascriptInterface {
		
		@JavascriptInterface
		public void handleHtml(String html) {
			pinlist = html.split("\n");
			
		}
}


	public void btnGenerate(View view)
	{ //генераторpppppp
		findViewById(R.id.wpsButton1).getBackground().setColorFilter(Color.parseColor("#1cd000"), PorterDuff.Mode.MULTIPLY);
		findViewById(R.id.baseButton).getBackground().clearColorFilter();
		findViewById(R.id.wpsButton2).getBackground().clearColorFilter();
		ListView wpslist = (ListView) findViewById(R.id.WPSlist);
		wpslist.setAdapter(null);
		wpsPin.clear();
		wpsMet.clear();
		data.clear();
		String p1 = null,p2=null;
		String sug="";

		for (int i=0; i<pinlist.length; i++){
		
			if (pinlist[i].length()>7){

				p1=pinlist[i].substring(0,8);
				p2=pinlist[i].substring(10);
				if (!wpsPin.contains(p1)){
				if (p1.equals("Suggeste"))sug="✔";
				if (p1.equals("Generate"))sug="";
				if (!p1.equals("Generate") & !p1.equals("Suggeste")){
			wpsPin.add(p1);
			wpsMet.add(p2);
				data.add(new ItemWps(p1, p2, "---", sug));}}
			}
		
			
		
		}
		wpslist.setAdapter(new MyAdapterWps(WPSActivity.this, data));

	}

	public void btnLocalClick(View view)
	{ //локальная база
		findViewById(R.id.wpsButton2).getBackground().setColorFilter(Color.parseColor("#1cd000"), PorterDuff.Mode.MULTIPLY);
		findViewById(R.id.wpsButton1).getBackground().clearColorFilter();
		findViewById(R.id.baseButton).getBackground().clearColorFilter();
		ListView wpslist = (ListView) findViewById(R.id.WPSlist);
		wpslist.setAdapter(null);
		final String BSSDWps = getIntent().getExtras().getString("variable1");

		try
		{
			data.clear();
			wpsPin.clear();
			Cursor cursor = mDb.rawQuery("SELECT * FROM pins WHERE mac='" + BSSDWps.substring(0, 8) + "'", null);
			cursor.moveToFirst();
			do {
				data.add(new ItemWps(cursor.getString(0), "---", "---", "---"));
				wpsPin.add(cursor.getString(0));
			}
			while(cursor.moveToNext());
			cursor.close();
		}
		catch (Exception e)
		{data.add(new ItemWps(null, "   not found", null, null));}
		wpslist.setAdapter(new MyAdapterWps(WPSActivity.this, data));

		wpslist.setEnabled(true);
	}

	//Toast
	public void toastMessage(String text)
	{
		Toast toast = Toast.makeText(getApplicationContext(),
									 text, Toast.LENGTH_LONG);
		toast.show();
	}
}
