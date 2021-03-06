package com.gmail.samos6.samscookbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

public class GetIngredientActivity extends ListActivity {
	
	
	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();

	SamsListAdapter adapter;
	Button btnDone;
	Button btnAdd;
    ListView lv;

	//used to set font
	Typeface typeFace;
	
	ArrayList<HashMap<String, String>> productsList;
	List<String> list = new ArrayList<String>();
	
	//Instantiating the SQLite database
	final DatabaseHandler db = new DatabaseHandler(this);
	

	String urlGetAllIngredients;
	
	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PRODUCTS = "products";
	private static final String TAG_INGREDIENTNAME = "ingredientName";
	private static final String TAG_CALORIES = "calories";
	private static final String TAG_PROTEIN = "protein";
	private static final String TAG_FAT = "fat";
	private static final String TAG_CARBS = "carbs";
	private static final String TAG_NOTES = "notes";
	private static final String TAG_ADDEDBY = "addedBy";
	private static final String TAG_TYPE = "type";
	private static final String TAG_DATECREATED = "dateCreated";
	private static final String TAG_DATEUPDATED = "dateUpdated";
	
	private static final String TAG_LISTINGREDIENT = "listIngredient";
	private static final String TAG_ADDINGREDIENT = "addIngredient";
	private static final String TAG_ORIGIN = "origin";
	
	//used to see if user canceled the AsyncTask
	Boolean bCancelled=false;
	
	// products JSONArray
	JSONArray products = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_ingredients_for_create_recipe);
		
		//getting url from resources
		urlGetAllIngredients = getResources().getString(R.string.urlGetAllIngredients);
	
		// Hashmap for ListView
		productsList = new ArrayList<HashMap<String, String>>();

		// Loading products in Background Thread
		new LoadAllIngredients().execute();

		//checks to see if any info was passed over (happens if this is accessed more than once)
		if( getIntent().getExtras() != null){
			Bundle extras= getIntent().getExtras();
			list = extras.getStringArrayList("IngredientList");
			//log.d("inside getExtra from intent: ", list.toString());
		}
		
		
		// Get listview
		lv = getListView(); 
		
		
		btnDone = (Button) findViewById(R.id.btnGetIngredientDone);
		btnAdd = (Button) findViewById(R.id.btnGetIngredientCreate);
		
		//setting the font type from assets		
		typeFace = Typeface.createFromAsset(getAssets(), "fonts/KELMSCOT.ttf");
		btnDone.setTypeface(typeFace);
		btnAdd.setTypeface(typeFace);
		
		// save selected ingredients click event
		btnDone.setOnClickListener(new View.OnClickListener() {
							
				@Override
				public void onClick(View view) {
					//log.d("GetIngredient: ", "in Done onClick");
								
					//list= new ArrayList<String>();
								
					list= adapter.getChecked();
								
					//log.d("inside Get ingredient btnDone: ", list.toString());					
								
					//Toast.makeText(getApplicationContext(), "Your Pantry has been Saved", Toast.LENGTH_SHORT).show();
								
					Intent intent= getIntent();
                    Bundle b = new Bundle();
                    b.putStringArrayList("IngredientList", (ArrayList<String>) list);
                    intent.putExtras(b);
                    setResult(100, intent);
                    finish();
								
								
				}
		});	
				
		// save selected ingredients click event
		btnAdd.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View view) {
						//log.d("GetIngredient: ", "in Add onClick");
						
						// getting values from selected ListItem
						String ingredientname = getString(R.string.newIngredient);
								
						// Starting new intent
						Intent intent = new Intent(getApplicationContext(), EditIngredientActivity.class);
						// sending ingredientName to next activity
						intent.putExtra(TAG_INGREDIENTNAME, ingredientname);
						intent.putExtra(TAG_ORIGIN, TAG_ADDINGREDIENT);	
						startActivityForResult(intent, 100);
					}
				});				
		
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				//log.d("GetIngredient inside listClick: ", "position = "+Integer.toString(position));
				if(((CheckBox) view.findViewById(R.id.ingredientCheckBox)).isChecked())
					((CheckBox) view.findViewById(R.id.ingredientCheckBox)).setChecked(false);
				else
					((CheckBox) view.findViewById(R.id.ingredientCheckBox)).setChecked(true);
				
				adapter.toogleSelection(position, view);
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
	 * Finds which letter was clicked and goes to the first ingredient 
	 * that begins with that letter in the ListView. 
	 * This method is called from inside the XML.
	 * @param view
	 */
	public void letterClicked(View view) {
		
		//log.d("letterClicked: ", "in onClick");
		int id = view.getId();
		
		String letter = ((Button) view.findViewById(id)).getText().toString();
		int position = adapter.getPositionOf(letter);
		
		if (position==-1){
			Toast.makeText(getApplicationContext(), getString(R.string.noIngreWithLetter)+" "+letter, Toast.LENGTH_SHORT).show();
		}else{
			lv.setSelectionFromTop(position, 0);
			Toast.makeText(getApplicationContext(), getString(R.string.movingToLetter)+" "+letter, Toast.LENGTH_SHORT).show();
		}			
	}
	
	/**
	 * Enables user to cancel the AsychTask by hitting the back button
	 */
	OnCancelListener cancelListener=new OnCancelListener(){
	    @Override
	    public void onCancel(DialogInterface arg0){
	        bCancelled=true;
	        finish();
	    }
	};
	
	// Response from Edit Product Activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// if result code 100
		if (resultCode == 100) {
			// if result code 100 is received 
			// means user edited/deleted ingredient
			//clears the adapter for new incoming items
			adapter.clear();
			new LoadAllIngredients().execute();
		}

	}

	/**
	 * Background Async Task to Load all product by making HTTP Request
	 * */
	class LoadAllIngredients extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(GetIngredientActivity.this);
			pDialog.setMessage(getString(R.string.loadingIngredients));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.setOnCancelListener(cancelListener);
			bCancelled=false;
			pDialog.show();
		}

		/**
		 * getting All products from url
		 * */
		@Override
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>(); 	

			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(urlGetAllIngredients, "POST", params);
			
			
			//if the asyncTask has not been cancelled then continue
			if(!bCancelled) try {
				//log.d("GetIngredient: ", json.toString());
				
				// Checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// Ingredient found
					// Getting Array of Ingredients
					products = json.getJSONArray(TAG_PRODUCTS);

					// looping through All Ingredients
					for (int i = 0; i < products.length(); i++) {
						JSONObject c = products.getJSONObject(i);
						
						String ingredientName = c.getString(TAG_INGREDIENTNAME);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						map.put(TAG_INGREDIENTNAME, ingredientName);

						// adding HashList to ArrayList
						productsList.add(map);
					}
				} else {
					// no ingredient found
					// Launch Add new Ingredient Activity
					Intent i = new Intent(getApplicationContext(), EditIngredientActivity.class);
					// Closing all previous activities
					//i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
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
			// dismiss the dialog after getting all Ingredients
			pDialog.dismiss();
			
			
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					/**
					 * Updating parsed JSON data into ListView
					 * */
					
					//list = db.getAllIngredients();
					adapter = new SamsListAdapter(GetIngredientActivity.this, productsList, list, "getIngredient", typeFace);
					
					
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