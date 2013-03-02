package com.gmail.samos6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditCommentActivity extends Activity {

	// Progress Dialog
	private ProgressDialog pDialog;

	JSONParser jsonParser = new JSONParser();
	
	TextView txtRecipeName;
	EditText txtComment;
	Spinner spnrRating;
	Button btnPost;
	
	String recipeName;

	//preference access
	SharedPreferences prefs;
	String userName="";
	String password="";
	
	String rating;
	String comment;
	
	//these 2 variables are used to test the results and errors from the server
	Boolean successful =false;
	String message="";
	
	//used to see if user canceled the AsyncTask
	Boolean bCancelled=false;

	// url to create new product
	String urlCreateNewRating;
	String urlEditComment;
	
	
	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_PRODUCT = "product";
	private static final String TAG_RECIPENAME = "recipeName";
	private static final String TAG_COMMENT = "comment";
	private static final String TAG_RATING = "rating";
	private static final String TAG_AUTHORNAME = "author";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_comment);

		//setting user name and password from preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		userName =prefs.getString("nickName", "guest");
		password =prefs.getString("password", "");
		
		//getting url from resources
		urlCreateNewRating = getResources().getString(R.string.urlCreateNewRating);
		urlEditComment = getResources().getString(R.string.urlEditComment);
		
		// Edit Text
		txtRecipeName = (TextView) findViewById(R.id.txtCreateCommentRecipeName);
		txtComment = (EditText) findViewById(R.id.txtCreateCommentComment);
		spnrRating = (Spinner) findViewById(R.id.CreateRecipeRatingSpinner);
		
		// getting recipeName from intent
		Intent intent = getIntent();
		
		// getting data past from intent
		recipeName = intent.getStringExtra(TAG_RECIPENAME);
		
		txtRecipeName.setText(recipeName);
		
		//loading the comment
		new LoadComment().execute();
		
		// Create button
		btnPost = (Button) findViewById(R.id.btnCreateCommentPost);

		// button click event
		btnPost.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				// creating new Comment in background thread
				new CreateNewComment().execute();
			}
		});
	}
	
	
	private void addDetails(){
		
		txtComment.setText(comment);
		spnrRating.setSelection(Integer.parseInt(rating));
		
	}
	
	/**
	 * Enables user to cancel the AsychTask by hitting the back button
	 */
	OnCancelListener cancelListener=new OnCancelListener(){
	    @Override
	    public void onCancel(DialogInterface arg0){
	    	//used to see if user canceled the AsyncTask
	    	bCancelled=true;
	        finish();
	    }
	};

	/**
	 * Background Async Task to Create new Comment
	 * */
	class CreateNewComment extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(EditCommentActivity.this);
			pDialog.setMessage("Updating Comment...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.setOnCancelListener(cancelListener);
			pDialog.show();
		}

		/**
		 * Creating product
		 * */
		protected String doInBackground(String... args) {
			
			String comment = txtComment.getText().toString();
			String rating = spnrRating.getSelectedItem().toString();

			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			params.add(new BasicNameValuePair(TAG_RECIPENAME, recipeName));
			params.add(new BasicNameValuePair(TAG_COMMENT, comment));
			params.add(new BasicNameValuePair(TAG_RATING, rating));
			params.add(new BasicNameValuePair(TAG_AUTHORNAME, userName));

			// getting JSON Object
			JSONObject json = jsonParser.makeHttpRequest(urlCreateNewRating, "GET", params);
			
			//if asyncTask has not been cancelled then continue
			if(!bCancelled) try {
				
				// check log cat for response
				Log.d("Create Comment", json.toString());
				
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					
					successful=true;
					// successfully created Comment
					// notify previous activity by sending code 100
					Intent i = getIntent();
					// send result code 100 to notify that the mission was accomplished
					setResult(100, i);
					
					// closing this screen
					finish();
				} else {
					message = json.getString(TAG_MESSAGE);
					// failed to create Comment
					Log.d("Failed to create new comment", "failed");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once done
			pDialog.dismiss();
			
			if(successful)
				Toast.makeText(getApplicationContext(), "Comment Posted", Toast.LENGTH_LONG).show();
			else
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
		}

	}
	
	/**
	 * Background Async Task to Load comment info by making HTTP Request
	 * */
	class LoadComment extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(EditCommentActivity.this);
			pDialog.setMessage("Loading Comment...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.setOnCancelListener(cancelListener);
			pDialog.show();
		}

		/**
		 * getting comment info from url
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>(); 	
			
			//including recipeName for the query
			params.add(new BasicNameValuePair(TAG_RECIPENAME, recipeName));
			params.add(new BasicNameValuePair(TAG_AUTHORNAME, userName));
			
			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(urlEditComment, "GET", params);
			
			
			//if AsyncTask was not cancelled then carry on
			if(!bCancelled) try {
				
				Log.d("EditComment: ", json.toString());
				// Checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					
					successful=true;
					// products found
					// Getting Array of Products
					JSONArray products = json.getJSONArray(TAG_PRODUCT);				
					
					// get first ingredient object from JSON Array
					JSONObject product = products.getJSONObject(0);

					// Storing each json item in variable						
					rating = product.getString(TAG_RATING);
					comment = product.getString(TAG_COMMENT);

				} else {
					// no Comments found
					message = json.getString(TAG_MESSAGE);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting comment		
			pDialog.dismiss();
			if(successful){
				Toast.makeText(getApplicationContext(), "Ingredient Created", Toast.LENGTH_LONG).show();
				addDetails();
			}
			else
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();


		}
		

	}
}