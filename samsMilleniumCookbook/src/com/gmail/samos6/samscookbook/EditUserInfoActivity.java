package com.gmail.samos6.samscookbook;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class EditUserInfoActivity extends Activity{
	
	Button btnUpdateAccount;
	EditText txtNickName;
	EditText txtEmail;
	EditText txtFirstName;
	EditText txtLastName;
	EditText txtOldPassword;
	EditText txtNewPassword;
	EditText txtNewTestQuestion;
	EditText txtNewTestAnswer;
	TextView txtOldTestQuestion;
	EditText txtOldTestAnswer;
	
	// Progress Dialog
	private ProgressDialog pDialog;
	
	boolean successful = false;
	String message = "";
	
	//used to see if user canceled the AsyncTask
	Boolean bCancelled=false;
	
	//preference access
	SharedPreferences prefs;
	String userName="";
	String token="";
	
	String userId;
	String nickName;
	String email;
	String firstName;
	String lastName;
	String oldPassword;
	String newPassword;
	String testOldQuestion;
	String testOldAnswer;
	String testNewQuestion="";
	String testNewAnswer="";
	
	//used to set font
	Typeface typeFace; 
	
	//Creating the variable that will hold the url when it is pulled from resources
	String urlUpdateAccount;
	String urlGetUserInfo;
	
	
	// JSON parser class
	JSONParser jsonParser = new JSONParser();
	
	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PRODUCT = "product";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_NICKNAME = "nickName";
	private static final String TAG_EMAIL = "email";
	private static final String TAG_FIRSTNAME = "firstName";
	private static final String TAG_LASTNAME = "lastName";
	private static final String TAG_OLDPASSWORD = "oldPassword";
	private static final String TAG_NEWPASSWORD = "newPassword";
	private static final String TAG_TESTQUESTION = "testQuestion";
	private static final String TAG_TESTANSWER = "testAnswer";
	private static final String TAG_USERID = "userId";
	private static final String TAG_TOKEN = "token";
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_info_edit);
		
		//setting user name and password from preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		userName =prefs.getString("nickName", "guest");
		token =prefs.getString("token", "");
		
		//getting url from resources
		urlUpdateAccount = getResources().getString(R.string.urlUpdateAccount);
		urlGetUserInfo = getResources().getString(R.string.urlGetUserInfo);
		
		//setting the btn and txt
		btnUpdateAccount = (Button) findViewById(R.id.btnPreferenceUpdate);
		txtNickName = (EditText) findViewById(R.id.editUserNickName);
		txtEmail = (EditText) findViewById(R.id.editUserEmail);
		txtFirstName = (EditText) findViewById(R.id.editUserFirstName);
		txtLastName = (EditText) findViewById(R.id.editUserLastName);
		txtOldPassword = (EditText) findViewById(R.id.editUserOldPassword);
		txtNewPassword = (EditText) findViewById(R.id.editUserNewPassword);
		txtNewTestQuestion = (EditText) findViewById(R.id.editUserTestQuestion);
		txtNewTestAnswer = (EditText) findViewById(R.id.editUserTestQuestionAnswer);
		txtOldTestQuestion = (TextView) findViewById(R.id.editUserOldTestQuestion);
		txtOldTestAnswer = (EditText) findViewById(R.id.editUserOldTestAnswer);
		
		//setting the font type from assets		
		typeFace = Typeface.createFromAsset(getAssets(), "fonts/KELMSCOT.ttf");
		btnUpdateAccount.setTypeface(typeFace);
		txtNickName.setTypeface(typeFace);
		txtEmail.setTypeface(typeFace);
		txtFirstName.setTypeface(typeFace);
		txtLastName.setTypeface(typeFace);
		txtOldPassword.setTypeface(typeFace);
		txtNewPassword.setTypeface(typeFace);
		txtNewTestQuestion.setTypeface(typeFace);
		txtNewTestAnswer.setTypeface(typeFace);
		txtOldTestQuestion.setTypeface(typeFace);
		txtOldTestAnswer.setTypeface(typeFace);
		
		((TextView) findViewById(R.id.pref1)).setTypeface(typeFace);
		((TextView) findViewById(R.id.pref2)).setTypeface(typeFace);
		((TextView) findViewById(R.id.pref3)).setTypeface(typeFace);
		((TextView) findViewById(R.id.pref4)).setTypeface(typeFace);
		((TextView) findViewById(R.id.pref7)).setTypeface(typeFace);
		((TextView) findViewById(R.id.pref8)).setTypeface(typeFace);
		((TextView) findViewById(R.id.pref9)).setTypeface(typeFace);
		((TextView) findViewById(R.id.pref10)).setTypeface(typeFace);
		((TextView) findViewById(R.id.pref11)).setTypeface(typeFace);
		
		//getting user info from db
		new LoadUserInfo().execute();
		
		// Create Account click event
		btnUpdateAccount.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {

				
				//log.d(" inside create account=", "inside onclick");
				
				nickName =  txtNickName.getText().toString();
				email = txtEmail.getText().toString();
				firstName = txtFirstName.getText().toString();
				lastName = txtLastName.getText().toString();
				newPassword = txtNewPassword.getText().toString();
				oldPassword = txtOldPassword.getText().toString();
				testNewQuestion = txtNewTestQuestion.getText().toString();
				testNewAnswer = txtNewTestAnswer.getText().toString();
				testOldAnswer = txtOldTestAnswer.getText().toString();
				
				String msg = "";
				boolean incomplete=false;
				
				if(nickName.matches("")){
					msg = getString(R.string.needNickName);
					incomplete=true;
				}else if(!isValidEmailAddress(email)){
					msg = getString(R.string.pEnterValidEmail);
					incomplete=true;
				}else if(firstName.matches("")){
					msg = getString(R.string.pEnterYourFname);
					incomplete=true;
				}else if(lastName.matches("")){
					msg = getString(R.string.pEnterYourLname);
					incomplete=true;
				}else if(oldPassword.matches("")){
					msg = getString(R.string.pEnterYourPassToConfirm);
					incomplete=true;
				}else if(!testNewQuestion.matches("") && testNewAnswer.matches("")){
					msg = getString(R.string.pEnterTestQAns);
					incomplete=true;
				}else if(testOldAnswer.matches("")){
					msg = getString(R.string.pEnterTestQAnsToConfirm);
					incomplete=true;
				}else{
					new UpdateAccount().execute();
				}
				
				if(incomplete)
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();			
				
			}
		});
	}
	
	// Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
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
	 * Sets the user info into preferences (keeps him logged in)
	 */ 
	private void setPreferences(){

		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putString("nickName", nickName);
		editor.putString("email", email);
		editor.putString("firstName", firstName);
		editor.putString("lastName", lastName);
		editor.putString("token", token);
		editor.commit();
		
		finish();
		
	};
	
	
	private void addDetails(){
		
		txtNickName.setText(userName);
		txtEmail.setText(email);
		txtFirstName.setText(firstName);
		txtLastName.setText(lastName);
		txtOldTestQuestion.setText(testOldQuestion);
	}
	
	
	/**
	 * Background Async Task to Create new Account
	 * */
	class UpdateAccount extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(EditUserInfoActivity.this);
			pDialog.setMessage(getString(R.string.updatingAcc));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.setOnCancelListener(cancelListener);
			pDialog.show();
			
		}

		/**
		 * Creating account
		 * */
		@Override
		protected String doInBackground(String... args) {
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			//log.d("update Account=", "do in background");

			// Building Parameters
			params.add(new BasicNameValuePair(TAG_TOKEN, token));
			params.add(new BasicNameValuePair(TAG_USERID, userId));
			params.add(new BasicNameValuePair(TAG_NICKNAME, nickName));
			params.add(new BasicNameValuePair(TAG_EMAIL, email));
			params.add(new BasicNameValuePair(TAG_FIRSTNAME, firstName));
			params.add(new BasicNameValuePair(TAG_LASTNAME, lastName));
			params.add(new BasicNameValuePair(TAG_OLDPASSWORD, oldPassword));
			params.add(new BasicNameValuePair(TAG_NEWPASSWORD, newPassword));
			params.add(new BasicNameValuePair("testOldAnswer", testOldAnswer));
			params.add(new BasicNameValuePair("testNewQuestion", testNewQuestion));
			params.add(new BasicNameValuePair("testNewAnswer", testNewAnswer));

			//log.d("UpdateAccount params: ", params.toString());
			
			// getting JSON Object
			JSONObject json = jsonParser.makeHttpRequest(urlUpdateAccount, "POST", params);

			//reseting variable
			successful = false;
			
			//if asyncTask has Not been cancelled then continue
			if (!bCancelled) try {
				
				// check log cat for response
				//log.d("update account Response", json.toString());
				
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					
					successful=true;
					// successfully created Account
					//log.d("UpdateAccount_Background", "Success! account updated");
					// Getting Array of Products
					JSONArray products = json.getJSONArray(TAG_PRODUCT);				
					
					// get first ingredient object from JSON Array
					JSONObject product = products.getJSONObject(0);
					
					token= product.getString(TAG_TOKEN);
					
				} else {
					// failed to create Account
					 message = json.getString(TAG_MESSAGE);
					//log.d("UpdateAccount_Background", "oops! Failed to update Account "+message);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		@Override
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once done
			pDialog.dismiss();
			if(successful){
				setPreferences();
				Toast.makeText(getApplicationContext(), getString(R.string.accountUpdated), Toast.LENGTH_LONG).show();
			}
			else
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
		}

	}
	
	/**
	 * Checks to see if the email address provided is valid
	 * 
	 * @param email
	 * @return true if valid email
	 */
	public static boolean isValidEmailAddress(String email) {
		
		Pattern emailPattern = Pattern.compile("[a-zA-Z0-9[!#$%&'()*+,/-_.\"]]+@[a-zA-Z0-9[!#$%&'()*+,/-_\"]]+.[a-zA-Z0-9[!#$%&'()*+,/-_\".]]+");

		Matcher m = emailPattern.matcher(email); 
		
		return m.matches();

	}
	 
	 /**
		 * Background Async Task to Load comment info by making HTTP Request
		 * */
		class LoadUserInfo extends AsyncTask<String, String, String> {

			/**
			 * Before starting background thread Show Progress Dialog
			 * */
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				pDialog = new ProgressDialog(EditUserInfoActivity.this);
				pDialog.setMessage(getString(R.string.loadingUserInfo));
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(true);
				pDialog.setOnCancelListener(cancelListener);
				pDialog.show();
			}

			/**
			 * getting comment info from url
			 * */
			@Override
			protected String doInBackground(String... args) {
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>(); 	
				
				//including recipeName for the query
				params.add(new BasicNameValuePair(TAG_NICKNAME, userName));
				params.add(new BasicNameValuePair(TAG_TOKEN, token));
				
				
				// getting JSON string from URL
				JSONObject json = jsonParser.makeHttpRequest(urlGetUserInfo, "POST", params);
				
				
				//if AsyncTask was not cancelled then carry on
				if(!bCancelled) try {
					
					//log.d("EditComment: ", json.toString());
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
						userId= product.getString(TAG_USERID);
						email= product.getString(TAG_EMAIL);
						firstName= product.getString(TAG_FIRSTNAME);
						lastName= product.getString(TAG_LASTNAME);
						testOldQuestion= product.getString(TAG_TESTQUESTION);


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
			@Override
			protected void onPostExecute(String file_url) {
				// dismiss the dialog after getting comment		
				pDialog.dismiss();
				if(successful)
					addDetails();
				else
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();


			}
			

		}
		
		 @Override
		    public boolean onOptionsItemSelected(MenuItem item){
		 
		        switch (item.getItemId()){
		 
		        case R.id.menuHome:
		        	Intent i = new Intent(getApplicationContext(), MainScreenActivity.class);
					// Closing all previous activities
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
		            return true;
		 
		        default:
		            return super.onOptionsItemSelected(item);
		        }
		    } 

}