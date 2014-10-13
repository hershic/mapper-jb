package com.hershic.mapper_jb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
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

public class MainActivity extends Activity {

	public void doPost(String url) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(new HttpGet(url));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				String responseString = out.toString();
				// ..more logic
				out.close();
				setLatLon(responseString);
			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (Exception e) {
			Log.v("FUCKERS", "Done killed all the stuff");
		}
	}

	final Context context = this;
	static String lat;
	static String lon;
	private FragmentManager fragmgr;

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
		Log.v("CAMERA", "Lat: " + lat);
		Log.v("CAMERA", "Long: " + lon);
		((MapFragment) fragmgr.findFragmentById(R.id.map)).getMap()
				.animateCamera(
						CameraUpdateFactory.newCameraPosition(cameraPosition));
	}

	public void setLatLon(String result) {
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
			System.out.format("'%s', '%s'\n", group1, group2);
			printresults = group1 + " " + group2;
		}
		Toast.makeText(context, printresults, Toast.LENGTH_LONG).show();
		moveMapToLocation(lat, lon);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		fragmgr = getFragmentManager();
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
									doPost("https://maps.googleapis.com/maps/api/geocode/json?address="
											+ input_escaped
											+ "&key=AIzaSyABIsx_rPFKIqctKcGXZ7-0lWWKQQNNS8w");
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
