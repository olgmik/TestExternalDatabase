package com.example.testexternaldatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	// displays the entire profile
	// having pulled data from the DB
	TextView resultView; 
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 1) display registration form
		setContentView(R.layout.activity_main);
		
		StrictMode.enableDefaults(); 
		resultView = (TextView) findViewById(R.id.result);
		getData();
	}
	
	public void getData(){
		String result = "";
		InputStream isr = null;
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://theolya.com/flashcards/getAllMembers.php");
			// get Json formatted string as a response
			HttpResponse response = httpclient.execute(httppost);
			// store that Json string in the "entity" var
			HttpEntity entity = response.getEntity();
			isr = entity.getContent();
		}
		catch(Exception e){
			Log.e("log_tag", "Error in http connection " + e.toString());
			resultView.setText("Couldn't connect to database");
		}
		// convert response to string, it's always the same code
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(isr, "iso-8859-1"), 8); 
			StringBuilder sb = new StringBuilder();
			String line = null;
			while((line = reader.readLine()) != null){
				sb.append(line + "\n"); 
			}
			isr.close();
			result = sb.toString();
		}
		catch (Exception e){
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		// parse json data
		try {
			String s = "";
			JSONArray jArray = new JSONArray(result);
			
			for(int i=0; i<jArray.length(); i++){
				JSONObject json = jArray.getJSONObject(i);
				s=s+ 
						"Name : " + json.getString("name")+"\n"+
						"Email: " + json.getString("email")+"\n"+
						"URL: " + json.getString("url")+"\n"+
						"Skills: " + json.getString("skills")+"\n\n";
			}
			resultView.setText(s);
		} catch (Exception e){
			Log.e("log_tag", "Error Parsing Data" + e.toString());
		}
	}
}
