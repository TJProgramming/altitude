package com.example.altitude;

import java.text.DecimalFormat;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends Activity implements SensorEventListener,
		LocationListener {

	private Updates update;
	private GoogleMap map;
	private LocationManager mLocationManager;
	private Location mLocation;
	private SensorManager mSensorManager;
	private Sensor mPressure;
	private TextView altitudeText;
	private TextView currentPressureText;
	private TextView verticalSpeedText;
	private Button trackButton;
	private PolylineOptions po = new PolylineOptions();
	private double lat;
	private double longi;
	private float altitude = -100000;
	private float oldAltitude = 0;
	private static boolean status = true;
	private static boolean tracking = true;
	private static double time;

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		update = new Updates();
		// Instantiate the views
		trackButton = (Button) findViewById(R.id.trackButton);
		altitudeText = (TextView) findViewById(R.id.altitudeText);
		currentPressureText = (TextView) findViewById(R.id.currentPressureText);
		verticalSpeedText = (TextView) findViewById(R.id.verticalSpeedText);
		map = ((MapFragment) getFragmentManager()
				.findFragmentById(R.id.mapView)).getMap();
		map.setMapType(map.MAP_TYPE_SATELLITE);

		// Instantiate the components for the sensors
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		mLocationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		mLocation = mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		trackButton.setText("Tracking: Off");
		if (mLocation != null) {
			if (mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				lat = mLocation.getLatitude();
				longi = mLocation.getLongitude();
			}
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					mLocation.getLatitude(), mLocation.getLongitude()), 17));
			onLocationChanged(mLocation);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Register a listener for the sensor.
		mSensorManager.registerListener(this, mPressure,
				SensorManager.SENSOR_DELAY_NORMAL);
		if (!tracking) {
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, this);
		} else {
			if (!mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				alertMessageNoGps();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the sensor when the activity pauses
		mSensorManager.unregisterListener(this);
	}

	@Override
	public final void onSensorChanged(SensorEvent event) {
		float millibars_of_pressure = event.values[0];
		// If statement: records time of sensor change and requires at least 1
		// second to go by before calculating current speed
		update.updateTime(time, event, status);
		// Sets old altitude to keep track of change in altitude
		if (altitude != -100000) {
			oldAltitude = altitude;
		}
		altitude = mSensorManager.getAltitude(1022, millibars_of_pressure);
		altitudeText.setText("Current: " + Float.toString(altitude));
		currentPressureText.setText("Current Pressure (millibars): "
				+ Float.toString(millibars_of_pressure));
		verticalSpeedText.setText("Current Speed (mph): N/A");
		if (status == false
				&& ((System.nanoTime() * Math.pow(10, -9)) - time) >= 1
				&& (time < 78922) && oldAltitude >= altitude) {
			double verticalSpeed = Math.abs((oldAltitude - altitude) / time);
			// Must be moving at least 2 m/s or 4.47388 mph before verticalSpeed
			// shows up on the screen
			if (verticalSpeed > 2) {
				verticalSpeedText.setText("Current Speed (mph): "
						+ roundTwoDecimals(verticalSpeed * 2.23694));
				status = true;
			}
		}
	}

	@Override
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onLocationChanged(Location location) {
		if (!tracking) {
			update.updateMap(map, lat, longi, tracking, mLocation, po);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	/**
	 * Limits a double to only two decimal places
	 * 
	 * @param d
	 *            Double that will be limited to only two decimal places
	 * @return d to two decimal places
	 */
	public final double roundTwoDecimals(double d) {
		DecimalFormat numberFormat = new DecimalFormat(".##");
		return Double.valueOf(numberFormat.format(d));
	}

	/**
	 * Enable or disables the GPS tracking
	 */
	public void enableTracking(View view) {
		if (tracking) {
			trackButton.setText("Tracking: On");
			tracking = false;
		} else {
			trackButton.setText("Tracking: Off");
			tracking = true;
		}
	}

	/**
	 * Sends alert message if GPS is not enabled
	 */
	private void alertMessageNoGps() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
				.setMessage("GPS is disabled. Would you like to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent callGPSSettingIntent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(callGPSSettingIntent);
							}
						});
		alertDialogBuilder.setNegativeButton("No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}
}
