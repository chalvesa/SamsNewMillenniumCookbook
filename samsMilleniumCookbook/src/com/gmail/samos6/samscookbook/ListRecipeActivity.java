package com.gmail.samos6.samscookbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListRecipeActivity  extends ListActivity{
	
	
	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();
	
	//used to see if user canceled the AsyncTask
	Boolean bCancelled=false;
	Boolean successful=false;

	RecipeLazyAdapter adapter;
	Button btnSave;
	Button btnAdd;
	
	String searchAuthor;
	String searchFoodName;
	String searchRecipeType;
	String searchKeyWord;
	String searchCookTime;
	int position;

	//used to set font
	Typeface typeFace; 
	
	ArrayList<HashMap<String, String>> productsList;
	
	//Instantiating the SQLite database
	final DatabaseHandler db = new DatabaseHandler(this);
	
	String urlGetAllRecipesByAlpha;
	String urlGetAllRecipesByReview;
	String urlGetAllRecipesByRating;
	String urlSortFormat;
	String urlRoot;

	ListView lv;
	
	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PRODUCTS = "products";
	private static final String TAG_RECIPENAME = "recipeName";
	private static final String TAG_SUMMERY = "summery";
	private static final String TAG_RATING = "rating";
	private static final String TAG_NUMRATINGS = "numRatings";
	private static final String TAG_PREPTIME = "prepTime";
	private static final String TAG_TOTALTIME = "totalTime";
	private static final String TAG_COOKTIME = "cookTime";
	private static final String TAG_IMAGEURL = "imageUrl";
	
	private static final String TAG_AUTHOR = "author";
	private static final String TAG_FOODNAME = "foodName";
	private static final String TAG_RECIPETYPE = "recipeType";
	private static final String TAG_KEYWORD = "keyWord";
	private static final String TAG_ORIGIN = "origin";
	

	// products JSONArray
	JSONArray products = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_recipes);
	
		typeFace = Typeface.createFromAsset(getAssets(), "fonts/KELMSCOT.ttf");
		
		//getting url from resources
		urlGetAllRecipesByAlpha = getResources().getString(R.string.urlGetAllRecipesByAlpha);
		urlGetAllRecipesByReview = getResources().getString(R.string.urlGetAllRecipesByReview);
		urlGetAllRecipesByRating = getResources().getString(R.string.urlGetAllRecipesByRating);
		urlSortFormat = urlGetAllRecipesByAlpha;
		urlRoot = getResources().getString(R.string.urlRoot);
		
		// Hashmap for ListView
		productsList = new ArrayList<HashMap<String, String>>();
		
		// getting ingredient details from intent
		Intent intent = getIntent();
		// getting data past from intent
		searchAuthor = intent.getStringExtra(TAG_AUTHOR);
		searchFoodName = intent.getStringExtra(TAG_FOODNAME);
		searchRecipeType = intent.getStringExtra(TAG_RECIPETYPE);
		searchKeyWord = intent.getStringExtra(TAG_KEYWORD);
		searchCookTime = intent.getStringExtra(TAG_COOKTIME);
		
		//setting first tab (default) to cyan
		findViewById(R.id.tblSortByAlpha).setBackgroundColor(Color.CYAN);
		
		((TextView)findViewById(R.id.txtSortByAlpha)).setTypeface(typeFace);
		((TextView)findViewById(R.id.txtSortByRating)).setTypeface(typeFace);
		((TextView)findViewById(R.id.txtSortByReview)).setTypeface(typeFace);
		
		
		// Loading products in Background Thread
		new LoadAllRecipes().execute();

		// Get listview
		lv = getListView();
		
		// on selecting single recipe
		// launching recipe Screen
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						
				// getting values from selected ListItem
				String recipeName = ((TextView) view.findViewById(R.id.txtListRecipeRecipeName)).getText().toString();
				//log.d("ListRecipe: ", recipeName);
						
				// Starting new intent
				Intent intent = new Intent(getApplicationContext(), RecipeViewActivity.class);
				
				// sending recipeName to next activity
				intent.putExtra(TAG_RECIPENAME, recipeName);
				
				// starting new activity and expecting some response back
				startActivityForResult(intent, 100);
			}
		});

	}
	
	/**
	 * used as tabs to define which sort will be used
	 * @param view
	 */
	public void tableClicked(View view) {
		
		//log.d("tableClicked: ", "in onClick");
		
		findViewById(R.id.tblSortByReview).setBackgroundColor(0);
		findViewById(R.id.tblSortByRating).setBackgroundColor(0);
		findViewById(R.id.tblSortByAlpha).setBackgroundColor(0);
		
		String oldUrl=urlSortFormat;
		
		if(view.getId()==findViewById(R.id.tblSortByAlpha).getId()){
			view.setBackgroundColor(Color.CYAN);
			urlSortFormat = urlGetAllRecipesByAlpha;
			
		}else if (view.getId()==findViewById(R.id.tblSortByRating).getId()){
			view.setBackgroundColor(Color.CYAN);
			urlSortFormat = urlGetAllRecipesByRating;
			
		}else if (view.getId()==findViewById(R.id.tblSortByReview).getId()){
			view.setBackgroundColor(Color.CYAN);
			urlSortFormat = urlGetAllRecipesByReview;
		}
		
		//check to see if user actually clicked a diff button
		if(!oldUrl.equalsIgnoreCase(urlSortFormat)){
			adapter.clear();
			new LoadAllRecipes().execute();
		}
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();
	    
	    //log.d("list recipe inside", "on restart");
	    position= lv.getFirstVisiblePosition();
		//adapter.clear();
		//new LoadAllRecipes().execute();
		//lv.setSelectionFromTop(position, 0);
		//log.d("list recipe inside", "visible pos="+Integer.toString(position));
	}

	// Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }
    
	// Response from Edit Product Activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// if result code 100
		if (resultCode == 100) {
			// if result code 100 is received 
			// means user edited/deleted ingredient
			// reload this screen again
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}

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
	 * Background Async Task to Load all Recipe by making HTTP Request
	 * */
	class LoadAllRecipes extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ListRecipeActivity.this);
			pDialog.setMessage(getString(R.string.loadingRecipes));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.setOnCancelListener(cancelListener);
			pDialog.setIcon(R.drawable.icon_37_by_37);
			pDialog.show();
		}

		/**
		 * getting All products from url
		 * */
		@Override
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>(); 	
			
			params.add(new BasicNameValuePair(TAG_AUTHOR, searchAuthor));
			params.add(new BasicNameValuePair(TAG_FOODNAME, searchFoodName));
			params.add(new BasicNameValuePair(TAG_RECIPETYPE, searchRecipeType));
			params.add(new BasicNameValuePair(TAG_KEYWORD, searchKeyWord));
			params.add(new BasicNameValuePair(TAG_COOKTIME, searchCookTime));

			//log.d("SearchRecipes params: ", params.toString());
			
			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(urlSortFormat, "POST", params);
			
			//reseting variable
			successful=false;
			
			//if AsyncTask has Not been cancelled then continue
			if (!bCancelled) try {
				
				//log.d("All Recipes: ", json.toString());
				// Checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// products found
					// Getting Array of Products
					products = json.getJSONArray(TAG_PRODUCTS);

					successful=true;
					
					// looping through All Products
					for (int i = 0; i < products.length(); i++) {
						JSONObject c = products.getJSONObject(i);

						// Storing each json item in variable						
						String recipeName = c.getString(TAG_RECIPENAME);
						String summery = c.getString(TAG_SUMMERY);
						String rating = c.getString(TAG_RATING);
						String numRatings = c.getString(TAG_NUMRATINGS);
						String prepTime = c.getString(TAG_PREPTIME);
						String cookTime = c.getString(TAG_COOKTIME);
						String author = c.getString(TAG_AUTHOR);
						String imageUrl = urlRoot+c.getString(TAG_IMAGEURL); //adding the urlRoot to the url returned by php
						
						int cookT = Integer.parseInt(cookTime);
						int prepT = Integer.parseInt(prepTime);

						String totalTime = Integer.toString(cookT+prepT);
						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put(TAG_RECIPENAME, recipeName);
						map.put(TAG_SUMMERY, summery);
						map.put(TAG_RATING, rating);
						map.put(TAG_NUMRATINGS, numRatings);
						map.put(TAG_AUTHOR, author);
						map.put(TAG_TOTALTIME, totalTime);
						map.put(TAG_IMAGEURL, imageUrl);

						// adding HashList to ArrayList
						productsList.add(map);
					}
				} else {
					// no recipes found
					//Toast.makeText(getApplicationContext(), "Your no recipes found", Toast.LENGTH_SHORT).show();
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
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			
			if(!successful){
				Toast toast= Toast.makeText(getApplicationContext(), getString(R.string.noRecipesFound), Toast.LENGTH_LONG);  
						toast.setGravity(Gravity.TOP, 0, 125);
						toast.show();
			}
			
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					/**
					 * Updating parsed JSON data into ListView
					 * */
					
					
					adapter = new RecipeLazyAdapter(ListRecipeActivity.this, productsList, typeFace);
					
					setListAdapter(adapter);
				}
			});

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
