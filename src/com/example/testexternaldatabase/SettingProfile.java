package com.example.testexternaldatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.RecoverySystem.ProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class SettingProfile extends Activity{
	
	// this activity displays the registration form
	// on "Add Photo" takes photo, displays photo and asks "save or not?", if "yes" saves it
	// on "save all" sends all data to the DB

		public static final int CAMERA_RESULT = 0;
		List<NameValuePair> nvps;
		Button takePhoto;
		Button saveAll;
		String imageFilePath; // not populated path to image on SD card
		File imageFile ; // object to hold the image
		String imageUrlServer;
		
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);	
			setContentView(R.layout.registration);

			takePhoto = (Button) findViewById(R.id.add_photo);
			saveAll = (Button) findViewById(R.id.save_all);
			Button useThisPhoto; 
		}
		
		public void buttonClicked (View clickedView){
			
			Log.v("MainActivity", "button was clicked");
			
			if(clickedView == takePhoto){

				// take email value from the form and assign it to the name of the photo
				EditText imageNameValue = (EditText) findViewById(R.id.email);
				
				// take photo intent will open the camera
				Intent takePictureIntent = new 	Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				// camera app will automatically will save the image on SD card
				// to have access to the captured image we need to wrap it into a File object 
				// give it a name - imageFile (we declared it earlier)	
				imageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + imageNameValue.getText().toString() + ".jpg");
				// and get a path to it
				imageFilePath = imageFile.getAbsolutePath();
				Uri imageFileUri = Uri.fromFile(imageFile);
				// put that path to the image into the intent to use on activity result
				takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,imageFileUri); 
				// start the photo taking activity
				startActivityForResult(takePictureIntent,CAMERA_RESULT); 			
			}	
			
			if(clickedView == saveAll){
				
				// task 1: upload image to the server with the same name it has on the SD card							
				// get its length - it's required for http upload

				long fileLength = imageFile.length();

				// instantiate a FilePoster object - a wrapper required to send image via POST

				FilePoster fp = new FilePoster();
				
				// call it's execute method
				// from here we jump to 
				fp.execute(); 
				
				// this happens after the image file has been uploaded to the server
				// put data from the form fields into the list
				
				
			}
		}
		
		public void saveToDatabase(){
			
			EditText nameValue = (EditText) findViewById(R.id.name);
			EditText emailValue = (EditText) findViewById(R.id.email);
			EditText urlValue = (EditText) findViewById(R.id.url);
			EditText skillsValue = (EditText) findViewById(R.id.skills);
	    	
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("name", nameValue.getText().toString()));
			nvps.add(new BasicNameValuePair("email",emailValue.getText().toString()));
			nvps.add(new BasicNameValuePair("url", urlValue.getText().toString()));
			nvps.add(new BasicNameValuePair("skills", skillsValue.getText().toString()));
			// this one is wrong right now, it will have to be the url I get from response
			nvps.add(new BasicNameValuePair("photo", imageUrlServer)); 
			
			// create PostJason
			PostJSON send = new PostJSON("http://theolya.com/flashcards/addMember.php", nvps);
			
			// call execute method
			send.execute();
		}
		
		class PostJSON extends AsyncTask<Void, Void, String>{
			private String url;
			private List<NameValuePair> nvps ;
			
			public PostJSON(String _url, List<NameValuePair> _nvps ) {
				url = _url;
				nvps = _nvps;
			}	

	@Override
			protected String doInBackground(Void... params) {
				String responseString = null;
				
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(url);
				try {				
					httpPost.setEntity(new UrlEncodedFormEntity(nvps));
					HttpResponse response = httpclient.execute(httpPost);
					HttpEntity responseEntity = response.getEntity();
					responseString = EntityUtils.toString(responseEntity);
				} catch (IOException e) {
					e.printStackTrace();
				}			
				return responseString;
			}

	@Override
			protected void onProgressUpdate(Void... values) {
			}
			
			protected void onPostExecute(String result) {
				//Log.v("RESULT", result);
			}
		}
		
	// is this what's exetecuted on send.execute();? 

	class FilePoster extends AsyncTask<Void, String, String> implements ProgressListener {
			
	// imageUrl variable will contain the URL to the image file 
	// after it has been uploaded to the server
			
			String imageUrl;
			
	// this method does the http uploading?
			
			@Override
			protected String doInBackground(Void... params) {

				HttpClient httpclient = new DefaultHttpClient();

	// this URL should go to the php script that does receiving of the image file on the server
				
				HttpPost httppost = new HttpPost("http://theolya.com/flashcards/upload_image.php");
				
	// MultipartEntity is an entity that can be sent or received with an HTTP message
				
				MultipartEntity multipartentity = new MultipartEntity();
				
	// why am I not added my image's name here so I can find it on the server?
	// or will the imageNameValue will be used the way it was on SD card?
				
				try {
					multipartentity.addPart("file", new FileBody(imageFile));

					httppost.setEntity(multipartentity);
					HttpResponse httpresponse = httpclient.execute(httppost);

					HttpEntity responseentity = httpresponse.getEntity();
					
	// InputStream will hold the response from the server
					
					if (responseentity != null) {
						
						InputStream inputstream = responseentity.getContent();

						// if the server is sending something back, you can read it here
						// lookup example reading from inputstream
						//imageUrl = inputstream.
						
						BufferedReader r = new BufferedReader(new InputStreamReader(inputstream));
						StringBuilder total = new StringBuilder();
						String line;
						while ((line = r.readLine()) != null) {
						    total.append(line);
						}
				
						inputstream.close();
						return total.toString(); // will be sent to onPostExecute
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public void onProgress(int arg0) {
				// TODO Auto-generated method stub
				
			}
			
			protected void onPostExecute(String result) {
				//Log.v("RESULT", result);
				imageUrlServer = result;
				saveToDatabase();
			}
	}		
		// on takePictureIntent result, our image is saved on SD already automatically
		// We want to get it from SD card and show it to the user

		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
					
			if (requestCode == CAMERA_RESULT){

				Log.v("Image File Path", imageFilePath); 

				// we got the path, so this is to open image as a bitmap
				BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options(); 
				bmpFactoryOptions.inSampleSize = 8; 
				Bitmap bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions);

				// this is where I show image to the user
				
				// add imageView1 to layout
				ImageView imageView = (ImageView) this.findViewById(R.id.imageView1); 
				imageView.setImageBitmap(bmp);

				// now the layout shows: name,email,url,skills,photo, buttons "Take Photo" and "Save All"
				// when "Take Photo" is clicked, we go back to takePictureIntent
				// when "Save All" is clicked, we 
				// 1) upload photo to the server
				// 2) get path to the image on the server
				// 3) send TextEdit values and the path to the photo into the DB on the server
				// So, we will write code for all of that in the buttonClicked method now
				// if clickedView == saveAll		
			}
	}
		
}
	
	
	
	