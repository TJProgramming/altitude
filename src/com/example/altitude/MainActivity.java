package com.example.altitude;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mPressure;

	TextView alt;

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		alt = (TextView) findViewById(R.id.altitudeText);
	}

	@Override
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public static float getAltitude(float p0, float p) {
		float localElevation = (float) (Math.pow(101325, (-1/5.25588))*(44330.8*Math.pow(101325,(1/5.25588))-(44330.8*Math.pow((1019*100), (1/5.25588)))));//(44330.8*Math.pow((p0*100), (1/5.25588)))));
		float currentElevation = (float) (Math.pow(101325, (-1/5.25588))*(44330.8*Math.pow(101325,(1/5.25588))-(44330.8*Math.pow((p*100), (1/5.25588)))));
		return currentElevation-localElevation;
	}

	@Override
	public final void onSensorChanged(SensorEvent event) {
		float millibars_of_pressure = event.values[0];
		float altitude = getAltitude(
				SensorManager.PRESSURE_STANDARD_ATMOSPHERE,						//Need to get this number to be the local number from the airport
				millibars_of_pressure);
		// Do something with this sensor data.
		alt.setText(Float.toString(altitude));
	}

	@Override
	protected void onResume() {
		// Register a listener for the sensor.
		super.onResume();
		mSensorManager.registerListener(this, mPressure,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		// Be sure to unregister the sensor when the activity pauses.
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
}
