package com.hershic.mapper_jb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

class RequestTask extends AsyncTask<String, String, String> {

	private MainActivity main_activity;
	private Context context;
	private String lat;
	private String lon;

	public void setContext(Context ctx) {
		context = ctx;
	}

	protected String doInBackground(String... uri) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		String responseString = null;
		try {
			response = httpclient.execute(new HttpGet(uri[0]));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (ClientProtocolException e) {
			// TODO Handle problems..
		} catch (IOException e) {
			// TODO Handle problems..
		}
		return responseString;
	}

	public void doSomethingToAct(MainActivity act) {
		main_activity = act;	
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		// Do anything with response..
		Log.v("GOT_DATA", result);
		String printresults = "Nothing Found";

		result = result.replaceAll("\n", "");
		// specify that we want to search for two groups in the string
		Pattern p = Pattern
				.compile("geometry.*location\" : \\{.* : ([-.\\d]+).*lng\" :.* ([-.\\d]+).*");
		Matcher m = p.matcher(result);

		String group1 = "0.0";
		String group2 = "0.0";

		// if our pattern matches the string, we can try to extract our groups
		if (m.find()) {
			// get the two groups we were looking for
			lat = m.group(1);
			lon = m.group(2);

			// print the groups, with a wee bit of formatting
			main_activity.setLatLong(lat, lon);
		}
		Toast.makeText(context, lat+"\n"+lon, Toast.LENGTH_LONG).show();
	}
}

public class MainActivity extends Activity {

	final Context context = this;
	String lat;
	String lon;
	final MainActivity main_activity = this;
	
	public void moveMapToLocation(String lat, String lon) {
		LatLng latlng = new LatLng(Double.parseDouble(lat),
				Double.parseDouble(lon));
		// Construct a CameraPosition focusing on Mountain View and animate the
		// camera to that position.
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(latlng) // Sets the center of the map to Mountain View
				.zoom(2) // Sets the zoom in dp (density-independent pixels)
				.bearing(0) // Sets the orientation of the camera to North
				.tilt(30) // Sets the tilt of the camera to 30 degrees
				.build(); // Creates a CameraPosition from the builder
		((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}
	
	public void setLatLong(String la, String lo) {
		lat = la;
		lon = lo;
		moveMapToLocation(lat,lon);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_search) {

			LayoutInflater li = LayoutInflater.from(this);
			View promptsView = li.inflate(R.layout.prompt_modal, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);

			// set prompts.xml to alertdialog builder
			alertDialogBuilder.setView(promptsView);

			final EditText userInput = (EditText) promptsView
					.findViewById(R.id.editTextDialogUserInput);

			// set dialog message
			alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// get user input and set it to result
									// edit text

									String input_escaped = userInput.getText()
											.toString().replaceAll(" ", "+");
									RequestTask task = new RequestTask();
									task.setContext(context);
									task.execute("https://maps.googleapis.com/maps/api/geocode/json?address="
											+ input_escaped
											+ "&key=AIzaSyABIsx_rPFKIqctKcGXZ7-0lWWKQQNNS8w");
									task.doSomethingToAct(main_activity);
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
		}
		return super.onOptionsItemSelected(item);
	}
}
