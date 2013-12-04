package com.example.altitude;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener,
		LocationListener {

	private LocationManager mLocationManager;
	private Location mLocation;
	private SensorManager mSensorManager;
	private Sensor mPressure;
	private TextView altitudeText;
	private TextView currentPressureText;
	private TextView localElevationText;
	private TextView currentElevationText;
	private TextView verticalSpeedText;
	private TextView latitudeText;
	private TextView longitudeText;
	private float altitude = -100000;
	private float oldAltitude = 0;
	private static boolean status = true;
	private static double t0;

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		altitudeText = (TextView) findViewById(R.id.altitudeText);
		currentPressureText = (TextView) findViewById(R.id.currentPressureText);
		localElevationText = (TextView) findViewById(R.id.localElevationText);
		currentElevationText = (TextView) findViewById(R.id.currentElevationText);
		verticalSpeedText = (TextView) findViewById(R.id.verticalSpeedText);
		latitudeText = (TextView) findViewById(R.id.latitudeText);
		longitudeText = (TextView) findViewById(R.id.longitudeText);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		mLocationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		mLocation = mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (mLocation != null) {
			onLocationChanged(mLocation);
		}
	}

	@Override
	protected void onResume() {
		// Register a listener for the sensor.
		super.onResume();
		mSensorManager.registerListener(this, mPressure,
				SensorManager.SENSOR_DELAY_NORMAL);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				0, 0, this);
	}

	@Override
	protected void onPause() {
		// Be sure to unregister the sensor when the activity pauses.
		super.onPause();
		mSensorManager.unregisterListener(this);
		mLocationManager.removeUpdates(this);
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
	 * Calculates the current altitude based on pressures p0 and p
	 * 
	 * @param p0
	 *            Local ground pressure (in millibars)
	 * @param p
	 *            Current pressure (in millibars)
	 * @return The height from the ground in meters
	 */
	public static float getAltitude(float p0, float p) {
		float localElevation = (float) (Math.pow(101325, (-1 / 5.25588)) * (44330.8 * Math
				.pow(101325, (1 / 5.25588)) - (44330.8 * Math.pow((p0 * 100),
				(1 / 5.25588)))));
		float currentElevation = (float) (Math.pow(101325, (-1 / 5.25588)) * (44330.8 * Math
				.pow(101325, (1 / 5.25588)) - (44330.8 * Math.pow((p * 100),
				(1 / 5.25588)))));
		return currentElevation - localElevation;
	}

	/**
	 * Calculates the local elevation relative to sea level based on pressure p0
	 * 
	 * @param p0
	 *            Local ground pressure (in millibars)
	 * 
	 * @return The local elevation relative to sea level in meters
	 */
	public static float getLocalElevation(float p0) {
		float localElevation = (float) (Math.pow(101325, (-1 / 5.25588)) * (44330.8 * Math
				.pow(101325, (1 / 5.25588)) - (44330.8 * Math.pow((p0 * 100),
				(1 / 5.25588)))));
		return localElevation;
	}

	/**
	 * Calculates the current elevation relative to sea level based on pressure
	 * p
	 * 
	 * @param p
	 *            Current ground pressure (in millibars)
	 * 
	 * @return The current elevation relative to sea level in meters
	 */
	public static float getCurrentElevation(float p) {
		float currentElevation = (float) (Math.pow(101325, (-1 / 5.25588)) * (44330.8 * Math
				.pow(101325, (1 / 5.25588)) - (44330.8 * Math.pow((p * 100),
				(1 / 5.25588)))));
		return currentElevation;
	}

	@Override
	public final void onSensorChanged(SensorEvent event) {
		float millibars_of_pressure = event.values[0];
		// If statement records time of sensor change and requires at least 1
		// second to go by before calculating current speed
		if (status == true) {
			t0 = event.timestamp * Math.pow(10, -9);
			status = false;
		} else {
			if (((System.nanoTime() * Math.pow(10, -9)) - t0) >= 1) {
				t0 = ((System.nanoTime() * Math.pow(10, -9)) - t0);
			}
		}
		// Sets old altitude to keep track of change in altitude
		if (altitude != -100000) {
			oldAltitude = altitude;
		}
		altitude = getAltitude(1002, millibars_of_pressure);
		altitudeText.setText("Current: " + Float.toString(altitude));
		currentPressureText.setText("Current Pressure (millibars): "
				+ Float.toString(millibars_of_pressure));
		localElevationText.setText("Local Elevation (meters): "
				+ getLocalElevation(1002));
		currentElevationText.setText("Current Elevation (meters): "
				+ getCurrentElevation(millibars_of_pressure));
		if (status == false
				&& ((System.nanoTime() * Math.pow(10, -9)) - t0) >= 1
				&& (t0 < 78922) && oldAltitude >= altitude) {
			double verticalSpeed = Math.abs((oldAltitude - altitude) / t0);
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
		double lat = location.getLatitude();
		double longi = location.getLongitude();
		latitudeText.setText("Latitude: " + Double.toString(lat));
		longitudeText.setText("Longitude: " + Double.toString(longi));
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
}
