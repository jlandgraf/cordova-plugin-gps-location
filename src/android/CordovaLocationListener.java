/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */
package com.coolprofs.cordova.gpslocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.cordova.CallbackContext;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

public class CordovaLocationListener implements LocationListener {
	public static int PERMISSION_DENIED = 1;
	public static int POSITION_UNAVAILABLE = 2;
	public static int TIMEOUT = 3;

	private static final long MIN_UPDATE_INTERVAL_IN_MS = 1 * 1000;
  private static final float MIN_UPDATE_DISTANCE_IN_M = 0;

	public HashMap<String, CallbackContext> watches = new HashMap<String, CallbackContext>();

	private CordovaGPSLocation mOwner;
	private List<CallbackContext> mCallbacks = new ArrayList<CallbackContext>();
	private Timer mTimer = null;
	private String TAG;

	public CordovaLocationListener(CordovaGPSLocation owner, String tag) {
		mOwner = owner;
		TAG = tag;
	}

	@Override
	public void onLocationChanged(Location location) {
		/* this is for some reason never called */
		/* IRIS - this is not called on my device, but apparantly it is called on the famoco-device */
		Log.d(TAG, "The location has been updated!");
		win(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		if (LocationManager.GPS_PROVIDER.equals(provider)) {
			fail(POSITION_UNAVAILABLE, "GPS provider has been disabled.");
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, "Provider " + provider + " status changed to " + status);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "Provider " + provider + " has been enabled.");
	}

	public int size() {
		return watches.size() + mCallbacks.size();
	}

	public void addWatch(String timerId, CallbackContext callbackContext) {
		watches.put(timerId, callbackContext);

		if (size() == 1) {
			start();
		}
	}

	/* we are trying to get a new location listner running */
	public void addCallback(CallbackContext callbackContext, int timeout) {
		if (mTimer == null) {
			mTimer = new Timer();
		}

		mTimer.schedule(new LocationTimeoutTask(callbackContext, this), timeout);
		mCallbacks.add(callbackContext);

		if (size() == 1) {
			start();
		}
	}

	public void clearWatch(String timerId) {
		if (watches.containsKey(timerId)) {
			watches.remove(timerId);
		}
		if (size() == 0) {
			stop();
		}
	}

	public void destroy() {
		stop();
	}

	protected void fail(int code, String message) {
		cancelTimer();

		for (CallbackContext callbackContext : mCallbacks) {
			mOwner.fail(code, message, callbackContext, false);
		}

		if (watches.size() == 0) {
			stop();
		}

		mCallbacks.clear();

		for (CallbackContext callbackContext : watches.values()) {
			mOwner.fail(code, message, callbackContext, true);
		}
	}

	protected void win(Location loc) {
		cancelTimer();

		for (CallbackContext callbackContext : mCallbacks) {
			mOwner.win(loc, callbackContext, false);
		}

		if (watches.size() == 0) {
			stop();
		}

		mCallbacks.clear();

		for (CallbackContext callbackContext : watches.values()) {
			mOwner.win(loc, callbackContext, true);
		}
	}

	/* start requesting for updates */
	private void start() {
		Criteria criteria = new Criteria();
    //criteria.setAccuracy(Criteria.ACCURACY_HIGH);
    //criteria.setPowerRequirement(Criteria.POWER_LOW);
    //criteria.setAltitudeRequired(false);
    //criteria.setBearingRequired(false);

    //if true then only enabled providers are included
    boolean enabledOnly = false;
    String provider = mOwner.getLocationManager().getBestProvider(criteria, enabledOnly);
    /* this should fire up the locationUpdate */
    /* IRIS - this is being called, so we should get updates */
		mOwner.getLocationManager().requestLocationUpdates(provider, MIN_UPDATE_INTERVAL_IN_MS, MIN_UPDATE_DISTANCE_IN_M, this, Looper.getMainLooper());
	}

	private void stop() {
		cancelTimer();
		mOwner.getLocationManager().removeUpdates(this);
	}

	private void cancelTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
	}

	private class LocationTimeoutTask extends TimerTask {

		private CallbackContext mCallbackContext = null;
		private CordovaLocationListener mListener = null;

		public LocationTimeoutTask(CallbackContext callbackContext,
				CordovaLocationListener listener) {
			mCallbackContext = callbackContext;
			mListener = listener;
		}

		@Override
		public void run() {
			for (CallbackContext callbackContext : mListener.mCallbacks) {
				if (mCallbackContext == callbackContext) {
					mListener.mCallbacks.remove(callbackContext);
					break;
				}
			}

			if (mListener.size() == 0) {
				mListener.stop();
			}
		}
	}
}
