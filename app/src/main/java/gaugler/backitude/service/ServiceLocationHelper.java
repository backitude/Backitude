package gaugler.backitude.service;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.FallbackOptionsEnum;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.constants.StatusBarOptionsEnum;
import gaugler.backitude.http.CustomServerPostTask;
import gaugler.backitude.listener.PassiveLocationListener;
import gaugler.backitude.listener.ReSyncAlarm;
import gaugler.backitude.util.DistanceCalculator;
import gaugler.backitude.util.PreferenceHelper;
import gaugler.backitude.util.StatusBarHelper;
import gaugler.backitude.util.UpdateWidgetHelper;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.MyWakefulService;
import gaugler.backitude.widget.FireWidget;
import gaugler.backitude.widget.OnOffWidget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

public class ServiceLocationHelper  {

	private static Service _service;
	private static String _phoneNumber;

	// Handler will trigger based on timeout value and destroy the whole service
	private static Handler handler;
	private static Handler delayed_GPS_handler;
	private static boolean isGpsEnabledAndAllowed = false;
	private static boolean isGpsDelayedForWifi = false;
	private static boolean isWiFiEnabled = false;
	private static boolean wifiPollingAllowed = true;
	private static boolean isNetworkLocEnabled = false;
	private static boolean networkPollingAllowed = true;
	private static boolean wifiOverride = false;

	private static int timeoutValue = Prefs.DEFAULT_VALUE_timeout_interval;
	private static int realtimeTimeoutValue = Prefs.DEFAULT_VALUE_realtime_timeout_interval;
	private static int wifiModeTimeoutValue = Prefs.DEFAULT_VALUE_wifi_mode_timeout_interval;
	private static boolean previousWithinNewCircleCheck = true;
	private static int fallbackOption = FallbackOptionsEnum.MOST_ACCURATE_OR_REPEAT_PREVIOUS.getValue();
	private static Location _lastLocation = null;
	private static Location _lastWifiLocation = null;
	private static Location _lastGPSLocation = null;
	private static LocationManager lm;
	private static LocationListener locListen;
	private static LocationListener wifiListen;

	private static float minGpsAccuracy = Prefs.DEFAULT_VALUE_minGpsAccuracy;
	private static float minWifiAccuracy = Prefs.DEFAULT_VALUE_minWifiAccuracy;
	private static float minDistance = Prefs.DEFAULT_VALUE_min_distance;
	private static int showStatusBar = StatusBarOptionsEnum.DISPLAY_POLLING.getValue();
	private static boolean realtimeRunning = false;
	private static boolean wifiModeRunning = false;

	private static boolean isFireUpdate = false;
	private static boolean isPushUpdate = false;

	public static void start(Service service, boolean fireUpdate) {
		start(service);
		isFireUpdate = true;
		isPushUpdate = false;
	}

	public static void start(Service service, boolean fireUpdate, String phoneNumber) {
		start(service);
		isFireUpdate = false;
		isPushUpdate = true;
		_phoneNumber = phoneNumber;
	}

	public static void start(Service service) {

		ZLogger.log("ServiceLocationHelper start: method call");
		if(service==null)
		{
			ZLogger.log("ServiceLocationHelper start: service object is null - not sure how we got here?");
			return;
		}
		_service = service;

		// Alarm fires, stop Steals Listener because we'll listen here now
		PassiveLocationListener stealStopper = new PassiveLocationListener();
		stealStopper.stop(_service);

		if(lm!=null)
		{
			ZLogger.log("ServiceLocationHelper start: Alarm has fired but previous location polling is still running - shouldn't have ever gotten here.");
			fail(_service);
			return;
		}

		initialize();

		getPrefs();

		// Check to make sure Docked Mode running status is what it should be
		if(restartForRealTimeAdjustment())
		{
			return;
		}

		lm = (LocationManager) _service.getSystemService(Context.LOCATION_SERVICE);
		if(lm!=null && lm.getProviders(true).size() > 0){
			ZLogger.log("Location provider exists");
			if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && isGpsDelayedForWifi && isWiFiEnabled && isGpsEnabledAndAllowed){
				ZLogger.log("Wifi and GPS are enabled and allowed, but GPS must wait for Wifi to finish.");
				delayed_GPS_handler.postDelayed(delayed_GPS_runnable, Constants.TEN_SECONDS);
				ZLogger.log("WiFi Polling timeout: " + (Constants.TEN_SECONDS/1000));
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, wifiListen);
			}
			else if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && isGpsEnabledAndAllowed){
				ZLogger.log("GPS is enabled and is allowed.");
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListen);
				if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && isNetworkLocEnabled && (networkPollingAllowed || (isWiFiEnabled && wifiPollingAllowed)))
				{
					ZLogger.log("Network locating is also enabled and is allowed.");
					lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, wifiListen);
				}
				if(realtimeRunning){
					ZLogger.log("Docked Polling timeout: " + (realtimeTimeoutValue/1000));
					handler.postDelayed(r, realtimeTimeoutValue);
				}
				else if (wifiModeRunning)
				{
					ZLogger.log("Wifi Connected Mode Polling timeout: " + (wifiModeTimeoutValue/1000));
					handler.postDelayed(r, wifiModeTimeoutValue);
				}
				else
				{
					ZLogger.log("Standard Polling timeout: " + (timeoutValue/1000));
					handler.postDelayed(r, timeoutValue);
				}
			}
			else if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && isNetworkLocEnabled && (isNetworkLocEnabled || (isWiFiEnabled && wifiPollingAllowed)))
			{
				ZLogger.log("GPS polling is not available, but Network locating is enabled and is allowed.");
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, wifiListen);
				ZLogger.log("No GPS Polling timeout: " + (Constants.TEN_SECONDS/1000));
				handler.postDelayed(r, Constants.TEN_SECONDS);
			}
			else
			{
				// in case they had a wifi location or whatever from previous update, and then turned off their wifi.  just keep updating it i guess.
				if(PreferenceHelper.hasLastPolledLocation(_service) && minDistance==Constants.NO_MIN_CHANGE_DISTANCE)
				{
					performUpdate();
					return;
				}
				else
				{
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_service.getBaseContext());
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean(Prefs.KEY_appEnabled, false);
					editor.commit();

					ZLogger.log("Can't enable anything: no location providers enabled");
					Toast.makeText(_service, _service.getResources().getString(R.string.NO_LOCATION_PROVIDERS_ENABLED), Toast.LENGTH_SHORT).show();

					ServiceManager sm = new ServiceManager();
					sm.stopAll(_service);

					updateOnOffWidget(false);

					ZLogger.logException("ServiceLocationHelper", new Exception(_service.getResources().getString(R.string.NO_LOCATION_PROVIDERS_DESC)), _service.getBaseContext());
					return;
				}
			}

			if((showStatusBar == StatusBarOptionsEnum.DISPLAY_POLLING.getValue() || showStatusBar == StatusBarOptionsEnum.DISPLAY_POLLING_REALTIME.getValue())
					&& !realtimeRunning)
			{
				StatusBarHelper.createPollingNotification(_service);
			}

			fireWidgetOn();

			ZLogger.log("LocationServices initialized");
		}
		else
		{
			ZLogger.log("No location providers enabled");
			fail(_service);
		}

	}

	public static void stop()
	{
		if(lm!=null)
		{
			ZLogger.log("ServiceLocationHelper stop: stop location listeners");
			try{
				if(locListen!=null){
					lm.removeUpdates(locListen);
				}
			}
			catch (Exception ex){
				ZLogger.logException("ServiceLocationHelper", ex, _service.getBaseContext());
			}
			try{
				if(wifiListen!=null){
					lm.removeUpdates(wifiListen);
				}
			}
			catch (Exception ex){
				ZLogger.logException("ServiceLocationHelper", ex, _service.getBaseContext());
			}
		}
		else{
			ZLogger.log("ServiceLocationHelper stop: location manager is null, location listeners should not be running");
		}

		lm = null;

		fireWidgetOff();

		if(handler!=null) {
			try {
				handler.removeCallbacks(r);
			}
			catch (Exception ex) {
				ZLogger.logException("ServiceLocationHelper", ex, _service.getBaseContext());
			}
		}
		handler=null;

		if(delayed_GPS_handler!=null) {
			try {
				delayed_GPS_handler.removeCallbacks(delayed_GPS_runnable);
			}
			catch (Exception ex) {
				ZLogger.logException("ServiceLocationHelper", ex, _service.getBaseContext());
			}
		}
		delayed_GPS_handler=null;

		locListen = null;
		wifiListen = null;

		StatusBarHelper.cancelPollingNotfication(_service);
	}

	private static void fail(Service service)
	{
		stop();

		ZLogger.log("Stop Location Service");
		//_service.updateOver(false);
		if(service!=null){
			int poll_type_fail = Constants.POLL_UPDATE_OVER_FALSE_FLAG;
			if(isFireUpdate){
				poll_type_fail = Constants.FIRE_UPDATE_OVER_FALSE_FLAG;
			} else if (isPushUpdate) {
				poll_type_fail = Constants.PUSH_UPDATE_OVER_FALSE_FLAG;
			}
			//Intent serviceIntent = new Intent(service, MyBackgroundService.class);
			//serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, poll_type_fail);
			//service.getBaseContext().startService(serviceIntent);
			ServiceManager sm = new ServiceManager();
			sm.updateOver(_service, poll_type_fail);
		}
	}

	private static void initialize() {
		isGpsEnabledAndAllowed = false;
		isGpsDelayedForWifi = false;
		isWiFiEnabled = false;
		wifiPollingAllowed = true;
		isNetworkLocEnabled = false;
		networkPollingAllowed = true;
		wifiOverride = false;
		timeoutValue = Prefs.DEFAULT_VALUE_timeout_interval;
		realtimeTimeoutValue = Prefs.DEFAULT_VALUE_realtime_timeout_interval;
		wifiModeTimeoutValue = Prefs.DEFAULT_VALUE_wifi_mode_timeout_interval;
		minGpsAccuracy = Prefs.DEFAULT_VALUE_minGpsAccuracy;
		minWifiAccuracy = Prefs.DEFAULT_VALUE_minWifiAccuracy;
		_lastLocation = null;
		_lastWifiLocation = null;
		_lastGPSLocation = null;

		locListen = new MyLocationListener();
		wifiListen = new MyWifiLocationListener();

		handler = new Handler();
		delayed_GPS_handler = new Handler();

		isFireUpdate = false;
		isPushUpdate = false;
	}

	private static boolean restartForRealTimeAdjustment() {
		// In case app never should've been real-time updating but Status Battery FULL tricked the app
		boolean isCharging = false;
		boolean isRealTimeEnabled = false;
		Intent lastBatteryStatus = _service.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int status = lastBatteryStatus.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
		ZLogger.log("ServiceLocationHelper: validate battery charging status data for: " + status);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_service.getBaseContext());
		isRealTimeEnabled = prefs.getBoolean(Prefs.KEY_realtime, false);
		SharedPreferences.Editor editor = prefs.edit();
		if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL){
			ZLogger.log("ServiceLocationHelper: battery charging status: charging");
			isCharging = true;
		}
		else
		{
			ZLogger.log("ServiceLocationHelper: battery charging status: NOT charging");
			isCharging = false;
		}
		editor.putBoolean(PersistedData.KEY_isCharging, isCharging);
		editor.commit();

		if(realtimeRunning)
		{
			if(isRealTimeEnabled){
				if(!isCharging){
					ZLogger.log("ServiceLocationHelper: realtime should not be running (not charging)");
					realtimeRunning = false;
					editor.putBoolean(PersistedData.KEY_realtimeRunning, false);
					editor.commit();
				}
			}
			else {
				ZLogger.log("ServiceLocationHelper: realtime should not be running (Real-time not enabled)");
				realtimeRunning = false;
				editor.putBoolean(PersistedData.KEY_realtimeRunning, false);
				editor.commit();
			}

			if(!realtimeRunning ){
				ZLogger.log("ServiceLocationHelper: turn Real Time widget off");
				UpdateWidgetHelper.updateRealtimeWidget(_service);

				if(showStatusBar == StatusBarOptionsEnum.DISPLAY_REALTIME.getValue() || showStatusBar == StatusBarOptionsEnum.DISPLAY_POLLING_REALTIME.getValue())
				{
					ZLogger.log("ServiceLocationHelper: cancel realtime notification bar");
					StatusBarHelper.cancelRealTimeEnabledNotfication(_service);
				}

				ZLogger.log("ServiceLocationHelper: restart service (real-time updating should be off)");
				ServiceManager sm = new ServiceManager();
				sm.startAlarms(_service);
				return true;
			}
		}
		else
		{
			boolean isEnabled = prefs.getBoolean(Prefs.KEY_appEnabled, true);
			if(isRealTimeEnabled && isCharging && isEnabled && !wifiModeRunning)
			{
				ZLogger.log("ServiceLocationHelper: restart service (real-time updating should be on)");
				ServiceManager sm = new ServiceManager();
				sm.startAlarms(_service);
				return true;
			}
		}
		return false;
	}

	private static class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			try{
				if (location != null) {
					ZLogger.log("onLocationChanged GPS: " + location.getAccuracy());

					if(_lastGPSLocation==null || location.getAccuracy() <= _lastGPSLocation.getAccuracy()){
						_lastGPSLocation = location;
						if(_lastLocation==null || _lastGPSLocation.getAccuracy() <= _lastLocation.getAccuracy()){
							_lastLocation = _lastGPSLocation;
						}
						if( _lastGPSLocation.getAccuracy() <= minGpsAccuracy){
							_lastLocation = _lastGPSLocation;
							performUpdate();
						}
					}
				}
			}
			catch(Exception ex)
			{
				ZLogger.logException("ServiceLocationHelper", ex, _service.getBaseContext());
				fail(_service);
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
			switch(status){
			case LocationProvider.AVAILABLE:
				return;
			case LocationProvider.OUT_OF_SERVICE:
				return;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				return;
			default:

			}

		}
	}

	private static class MyWifiLocationListener implements LocationListener {
		private int counter = 0;

		@Override
		public void onLocationChanged(Location location) {
			try{
				if (location != null) {
					ZLogger.log("onLocationChanged Wifi: " + location.getAccuracy());

					if(_lastWifiLocation==null || location.getAccuracy() <= _lastWifiLocation.getAccuracy())
					{
						_lastWifiLocation = location;
					}
					else
					{
						ZLogger.log("disregard wifi same/less accurate: " + location.getAccuracy() + " Count: " + counter);
					}
					if(_lastLocation==null || _lastWifiLocation.getAccuracy() <= _lastLocation.getAccuracy()){
						_lastLocation = _lastWifiLocation;
					}
					counter = counter + 1;

					if(!isGpsEnabledAndAllowed) 
					{
						if(_lastWifiLocation.getAccuracy() <= minWifiAccuracy || counter > 1)
						{
							_lastLocation = _lastWifiLocation;
							performUpdate();
						}
						// else just keep polling
					}
					else // check for wifi override
					{
						ZLogger.log("If wifiOverride in place: " + (wifiOverride && _lastWifiLocation.getAccuracy() <= minWifiAccuracy));
						if(wifiOverride && _lastWifiLocation.getAccuracy() <= minWifiAccuracy)
						{
							_lastLocation = _lastWifiLocation;
							performUpdate();
						}
					}
				}
			}
			catch(Exception ex)
			{
				ZLogger.logException("ServiceLocationHelper", ex, _service.getBaseContext());
				fail(_service);
			}
		}
		@Override
		public void onProviderDisabled(String provider) {
			ZLogger.log("LocationProvider disabled: " + provider);
		}
		@Override
		public void onProviderEnabled(String provider) {
			ZLogger.log("LocationProvider enabled: " + provider);
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			switch(status){
			case LocationProvider.AVAILABLE:
				ZLogger.log("LocationProvider enabled: " + provider);
				return;
			case LocationProvider.OUT_OF_SERVICE:
				ZLogger.log("LocationProvider disabled: " + provider);
				return;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				ZLogger.log("LocationProvider unavailable: " + provider);
				return;
			default:

			}
		}
	}

	private static final Runnable delayed_GPS_runnable = new Runnable()
	{
		public void run()
		{
			try{
				delayed_GPS_handler.removeCallbacks(this);
				ZLogger.log("GPS delayed start is now over.  GPS polling can begin");

				if(lm!=null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && isGpsEnabledAndAllowed){
					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListen);
					if(realtimeRunning){
						ZLogger.log("Docked Polling timeout: " + (realtimeTimeoutValue/1000));
						handler.postDelayed(r, realtimeTimeoutValue);
					}
					else if (wifiModeRunning)
					{
						ZLogger.log("Wifi Connected Mode Polling timeout: " + (wifiModeTimeoutValue/1000));
						handler.postDelayed(r, wifiModeTimeoutValue);
					}
					else
					{
						ZLogger.log("Standard Polling timeout: " + (timeoutValue/1000));
						handler.postDelayed(r, timeoutValue);
					}
				}
				else {
					ZLogger.log("GPS delayed start is now over, but GPS is not available");
					performUpdate();
				}
			}
			catch(Exception ex)
			{
				ZLogger.logException("ServiceLocationHelper", ex, _service.getBaseContext());
				fail(_service);
			}
		}
	};

	private static final Runnable r = new Runnable()
	{
		public void run()
		{
			try{
				handler.removeCallbacks(this);

				if(_lastLocation==null)
				{
					ZLogger.log("Timeout! before getting accurate location..Repeating last update");
				}
				else
				{
					ZLogger.log("Timeout! before getting accurate location.  Using Accuracy: " + _lastLocation.getAccuracy());
				}
				performUpdate();

			}
			catch(Exception ex)
			{
				ZLogger.logException("ServiceLocationHelper", ex, _service.getBaseContext());
				fail(_service);
			}
		}
	};

	private static void performUpdate()
	{
		boolean skipThisUpdate = false;

		try {

			ZLogger.log("ServiceLocationHelper performUpdate: start");
			stop();

			if(_lastLocation!=null)
			{
				ZLogger.log("ServiceLocationHelper performUpdate: Accuracy Validation logic and Fallback options");
				if(PreferenceHelper.isWiFiPollingAllowed(PreferenceManager.getDefaultSharedPreferences(_service.getBaseContext())))
				{	// Any Priority option except for GPS ONLY
					if(_lastLocation.getAccuracy() > minWifiAccuracy)
					{	// New location is not accurate enough, check the fallback options
						if(fallbackOption == FallbackOptionsEnum.DO_NOT_UPDATE.getValue() ||
								fallbackOption == FallbackOptionsEnum.REPEAT_PREVIOUS.getValue())
						{
							ZLogger.log("ServiceLocationHelper performUpdate: New location is not accurate enough according to min accuracy requirements");
							_lastLocation = null;
						}
					}
				}
				else
				{
					// Priority is set to GPS ONLY
					if(_lastLocation.getAccuracy() > minGpsAccuracy)
					{
						if(fallbackOption == FallbackOptionsEnum.DO_NOT_UPDATE.getValue() ||
								fallbackOption == FallbackOptionsEnum.REPEAT_PREVIOUS.getValue())
						{
							ZLogger.log("ServiceLocationHelper performUpdate: New location is not accurate enough according to min accuracy requirements");
							_lastLocation = null;
						}
					}
				}
			}

			// Null out Location value if Cell Towers triangulation is not allowed 
			//   and the location value does not meet minimum requirements
			if(_lastLocation!=null){
				ZLogger.log("ServiceLocationHelper performUpdate: Check if cell tower triangulation is permitted and verify accuracy of location: "  + _lastLocation.getAccuracy());
				if(!networkPollingAllowed && _lastLocation.getAccuracy() > minWifiAccuracy)
				{
					ZLogger.log("ServiceLocationHelper performUpdate: Set _lastlocation = null, location does not meet minimum wifi requirements and cell tower triangulation is not allowed");
					_lastLocation = null;
				}
			}

			if(_lastLocation!=null)
			{	
				ZLogger.log("ServiceLocationHelper performUpdate: validate against min distance location filter next");
				// if there's a minimum distance then we'll never repeat the previous location update like could happen with Enforce Accuracy
				if(!(isFireUpdate||isPushUpdate) && minDistance > Constants.NO_MIN_CHANGE_DISTANCE)
				{
					ZLogger.log("ServiceLocationHelper performUpdate: Compute minimum change in distance");
					// Minimum distance filter: did it change enough since last update?
					if(!DistanceCalculator.isBeyondDistance(_lastLocation, _service, getMinDistance())){
						ZLogger.log("ServiceLocationHelper performUpdate: Change in distance is not great enough");
						_lastLocation = null;
					}
					else
					{
						ZLogger.log("ServiceLocationHelper performUpdate: New location value is beyond min distance required");
					}
				}
				else{
					// Location is accurate enough to pass tests
					// There is no minimum distance requirement, so check a generic time change requirement.
					if(!(isFireUpdate||isPushUpdate)){

						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_service);
						String lastUpdateTime = prefs.getString(PersistedData.KEY_savedLocation_UpdateTime, "");

						float min_dist = 0;
						try{
							min_dist = Float.parseFloat("0.0001");
						}
						catch(Exception ex)	{
							ZLogger.logException("ServiceLocationHelper", ex, _service.getBaseContext());
						}

						// If new location is same as previous AND time elapsed since previous is under 15 min, suck it
						if(!DistanceCalculator.isBeyondDistance(_lastLocation, _service, min_dist) &&
								!sufficientTimeElapsed(lastUpdateTime))
						{
							ZLogger.log("ServiceLocationHelper performUpdate: Location value skipped - too soon, too close");
							_lastLocation = null;
							skipThisUpdate = true;
						}
					}

					// Accuracy Filter: was the previous location more accurate and inside the current location					
					ZLogger.log("ServiceLocationHelper performUpdate: Do not perform min distance logic.");
					if(_lastLocation!=null && previousWithinNewCircleCheck)
					{
						ZLogger.log("ServiceLocationHelper performUpdate: Accuracy Filter is enabled.");
						_lastLocation = performLocationLogic();
					}					
				}
			}

			// If new location is found and passes all tests, update using that.  
			// If it didn't find a location or it didn't pass the tests,
			//   but the previous location can be repeated, then pass in a null and it will be repeated	
			if(_lastLocation!=null || 
					(PreferenceHelper.hasLastPolledLocation(_service) && 
							!skipThisUpdate &&
							minDistance==Constants.NO_MIN_CHANGE_DISTANCE &&
							(fallbackOption==FallbackOptionsEnum.MOST_ACCURATE_OR_REPEAT_PREVIOUS.getValue() 
							|| fallbackOption==FallbackOptionsEnum.REPEAT_PREVIOUS.getValue())
							)
					)
			{
				if(_service!=null){
					// Stop the Latitude location refresh timer from updating with old value
					// while we're about to update with a new location
					ReSyncAlarm resyncStopper = new ReSyncAlarm();
					resyncStopper.stop(_service);

					//if location polling did not result in new location, repeat previous if minDistance = 0
					if(isFireUpdate)
					{
						ZLogger.log("ServiceLocationHelper performUpdate: start retry timer for Fire before attempt on CustomServerPostTask");
						Intent serviceIntent = new Intent(_service, MyWakefulService.class);
						serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.START_FIRE_RETRY_TIMER);
						_service.getBaseContext().startService(serviceIntent);
						new CustomServerPostTask(_service, _lastLocation, _service.getResources().getString(R.string.UPDATE_TYPE_FIRE)).execute();
					} 
					else if(isPushUpdate)
					{
						ZLogger.log("ServiceLocationHelper performUpdate: start retry timer for Push Update before attempt on CustomServerPostTask");
						Intent serviceIntent = new Intent(_service, MyWakefulService.class);
						serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.START_PUSH_RETRY_TIMER);
						serviceIntent.putExtra(Constants.SERVICE_PHONE_PARAM, _phoneNumber);
						_service.getBaseContext().startService(serviceIntent);
						new CustomServerPostTask(_service, _lastLocation, _service.getResources().getString(R.string.UPDATE_TYPE_PUSH), _phoneNumber).execute();
					}
					else
					{
						ZLogger.log("ServiceLocationHelper performUpdate: start retry timer for Poll before attempt on CustomServerPostTask");
						Intent serviceIntent = new Intent(_service, MyWakefulService.class);
						serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.START_POLL_RETRY_TIMER);
						_service.getBaseContext().startService(serviceIntent);
						new CustomServerPostTask(_service, _lastLocation, _service.getResources().getString(R.string.UPDATE_TYPE_POLL)).execute();
					}
				}
			}	
			else{
				// No location found or, Location update is being skipped because it does not meet minimum change in distance requirements
				ZLogger.log("ServiceLocationHelper performUpdate: Cannot update, location does not meet requirements necesary to send location to Google.");
				fail(_service);
			}

		}
		catch(Exception ex)
		{
			ZLogger.logException("ServiceLocationHelper", ex, _service.getBaseContext());
			fail(_service);
		}
	}

	private static boolean sufficientTimeElapsed(String lastUpdateTime) {

		try 
		{  			
			Calendar cal = Calendar.getInstance(); // creates calendar
			cal.setTime(new Date()); // sets calendar time/date
			cal.add(Calendar.MINUTE, -15); // subtract 15 minutes
			Date now = cal.getTime(); //

			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
			Date then = (Date)sdf.parse(lastUpdateTime);  

			return now.after(then);

		} 
		catch (java.text.ParseException e){
			ZLogger.logException("ServiceLocationHelper sufficientTimeElapsed:", e, _service.getBaseContext());
		}  
		return true;

	}

	private static Location performLocationLogic() 
	{
		if(PreferenceHelper.hasLastPolledLocation(_service))
		{
			return DistanceCalculator.isWithinCircle(_lastLocation,_service);
		}
		else
		{
			return _lastLocation;
		}
	}

	private static float getMinDistance()
	{
		float minDist = 0;
		if(_service!=null){
			if(minDistance==Constants.VARIABLE_MIN_DIST)
			{
				minDist = Math.max(_lastLocation.getAccuracy(), Constants.MIN_MIN_DISTANACE);
			}
			else
			{
				minDist = minDistance;
			}
			ZLogger.log("Stolen Location: Get Minimum change in distancefrom prefs " + minDist);
		}
		return minDist;
	}

	private static void getPrefs(){
		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_service.getBaseContext());
		timeoutValue = Integer.parseInt(prefs.getString(Prefs.KEY_timeout_interval, Prefs.DEFAULT_timeout_interval));
		realtimeTimeoutValue = Integer.parseInt(prefs.getString(Prefs.KEY_realtime_timeout_interval, Prefs.DEFAULT_realtime_timeout_interval));
		wifiModeTimeoutValue = Integer.parseInt(prefs.getString(Prefs.KEY_wifi_mode_timeout_interval, Prefs.DEFAULT_wifi_mode_timeout_interval));
		showStatusBar = Integer.parseInt(prefs.getString(Prefs.KEY_statusBar, StatusBarOptionsEnum.DISPLAY_POLLING.getString()));
		previousWithinNewCircleCheck = prefs.getBoolean(Prefs.KEY_min_accuracy, true);
		fallbackOption = Integer.parseInt(prefs.getString(Prefs.KEY_fallbackOptions, FallbackOptionsEnum.MOST_ACCURATE_OR_REPEAT_PREVIOUS.getString()));
		minDistance = Float.parseFloat(prefs.getString(Prefs.KEY_min_distance, Prefs.DEFAULT_min_distance));    
		minGpsAccuracy = Float.parseFloat(prefs.getString(Prefs.KEY_minGpsAccuracy, Prefs.DEFAULT_minGpsAccuracy));    
		minWifiAccuracy = Float.parseFloat(prefs.getString(Prefs.KEY_minWifiAccuracy, Prefs.DEFAULT_minWifiAccuracy));    
		realtimeRunning = prefs.getBoolean(PersistedData.KEY_realtimeRunning, false); 
		wifiModeRunning = prefs.getBoolean(PersistedData.KEY_wifiModeRunning, false);

		ContentResolver contentResolver = _service.getBaseContext().getContentResolver();
		isGpsEnabledAndAllowed = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER) && PreferenceHelper.isGpsPollingAllowed(prefs);

		isNetworkLocEnabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER);
		networkPollingAllowed = PreferenceHelper.isNetworkPollingAllowed(prefs);

		wifiPollingAllowed = PreferenceHelper.isWiFiPollingAllowed(prefs);
		ConnectivityManager connManager = (ConnectivityManager) _service.getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		isWiFiEnabled = (mWifi.isAvailable() || mWifi.isConnectedOrConnecting() || mWifi.isRoaming());

		wifiOverride = PreferenceHelper.isWifiOverrideEnabled(prefs);

		isGpsDelayedForWifi =  PreferenceHelper.isGpsDelayedForWiFi(prefs);
	}

	private static void fireWidgetOn()
	{
		if(_service!=null){
			Intent uiIntent = new Intent(_service, FireWidget.class);
			uiIntent.setAction(Constants.FIRE_WIDGET_ACTION_UPDATE_WIDGET);
			uiIntent.putExtra(Constants.FIRE_WIDGET_PARAM, true);
			_service.sendBroadcast(uiIntent);	
		}
	}

	private static void fireWidgetOff() {
		if(_service!=null){
			Intent uiIntent = new Intent(_service, FireWidget.class);
			uiIntent.setAction(Constants.FIRE_WIDGET_ACTION_UPDATE_WIDGET);
			uiIntent.putExtra(Constants.FIRE_WIDGET_PARAM, false);
			_service.sendBroadcast(uiIntent);
		}
	}


	private static void updateOnOffWidget(boolean appEnabled) {
		if(_service!=null){	
			Intent uiIntent = new Intent(_service, OnOffWidget.class);
			uiIntent.setAction(Constants.ON_OFF_WIDGET_ACTION_UPDATE_WIDGET);
			uiIntent.putExtra(Constants.ON_OFF_WIDGET_PARAM, appEnabled);
			_service.sendBroadcast(uiIntent);		
		}
	}

}