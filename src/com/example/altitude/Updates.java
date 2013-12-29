package com.example.altitude;

import android.hardware.SensorEvent;
import android.location.Location;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class Updates {

	/**
	 * Updates polyline on GoogleMap
	 * 
	 * @param map
	 *            Map used
	 * @param latitude
	 *            Current latitude
	 * @param longitude
	 *            Current longitude
	 * @param tracking
	 *            Boolean
	 * @param location
	 *            Location to access coordinates
	 * @param polylineOption
	 *            PolylineOptions for drawing to GoogleMap
	 */
	public void updateMap(GoogleMap map, double latitude, double longitude,
			boolean tracking, Location location, PolylineOptions polylineOption) {
		if (!tracking && location != null) {
			double prevLat = latitude;
			double prevLongi = longitude;
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			map.addPolyline(polylineOption
					.add(new LatLng(prevLat, prevLongi),
							new LatLng(latitude, longitude)).color(0xFFFF0000)
					.width(5));
		}
	}

	/**
	 * Updates time used to calculate vertical speed
	 * 
	 * @param time
	 *            Time to maintain time interval
	 * @param event
	 *            SensorEvent input from sensors
	 * @param status
	 *            Boolean to keep track of time intervals
	 */
	public void updateTime(double time, SensorEvent event, boolean status) {
		// If statement: records time of sensor change and requires at least 1
		// second to go by before calculating current speed
		if (status == true) {
			time = event.timestamp * Math.pow(10, -9);
			status = false;
		} else {
			if (((System.nanoTime() * Math.pow(10, -9)) - time) >= 1) {
				time = ((System.nanoTime() * Math.pow(10, -9)) - time);
			}
		}
	}

}
