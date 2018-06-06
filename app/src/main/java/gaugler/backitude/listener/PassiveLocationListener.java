package gaugler.backitude.listener;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;

import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.util.DistanceCalculator;
import gaugler.backitude.util.PreferenceHelper;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.MyWakefulService;

public class PassiveLocationListener extends BroadcastReceiver {

	// this constructor is called by the alarm manager.     
	public PassiveLocationListener(){ } 

	@Override
	public void onReceive(Context context, Intent intent) {
		String key = LocationManager.KEY_LOCATION_CHANGED;
		ZLogger.log("PassiveLocationListener onReceive: method start");

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isApplicationEnabled = prefs.getBoolean(Prefs.KEY_appEnabled, false);
		if(isApplicationEnabled){
			try
			{
				if (intent!=null && intent.hasExtra(key)) 
				{
					// This update came from Passive provider, so we can extract the location
					// directly.
					onLocationReceived(context, (Location)intent.getExtras().get(key)); 
				}
				else if(intent!=null &&
						intent.getAction()!=null &&
						intent.getAction().equals(Constants.STEAL_BUFFER_PARAM))
				{		
					delayOver(context);
				}
				else
				{
					ZLogger.log("PassiveLocationListener onReceive: Intent toString = " + intent.toString());
				}
			}
			catch(Exception ex)
			{
				ZLogger.logException("PassiveLocationListener", ex, context);
				stop(context);
			}
		}
		else
		{
			ZLogger.log("PassiveLocationListener onReceive: app is not enabled");
			stop(context);
		}

	}

	public void start(Context context){

		ContentResolver contentResolver = context.getContentResolver();
		boolean isGPSenabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER);

		if(isGPSenabled){
			ZLogger.log("PassiveLocationListener start: GPS enabled, delay 15 seconds");
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
			int stealBufferDelay = Integer.parseInt(settings.getString(Prefs.KEY_max_steals_interval, Prefs.DEFAULT_max_steals_interval));
			startHoldTimer(context, stealBufferDelay);
		}
		else
		{
			ZLogger.log("PassiveLocationListener start: GPS not enabled, delay 1 minute");
			startHoldTimer(context, Constants.FIVE_MINUTES);
		}

		setIsStealAllowed(context, false);

		startStealListener(context);
	}

	// Resume can instantly start the steal listener and allow for updates immediately
	// Such as, when a steal update fails.
	public void resume(Context context){

		setIsStealAllowed(context, true);

		startStealListener(context);
	}

	public void stop(Context context)
	{
		//ZLogger.log("PassiveLocationListener stop: method start");
		stopStealListener(context);
		stopHoldTimer(context);
	}

	private void startStealListener(Context context)
	{
		try
		{
			PreferenceHelper.clearCachedSteal(context);

			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			Intent passiveIntent = new Intent(context, PassiveLocationListener.class);
			PendingIntent locListen = PendingIntent.getBroadcast(context, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, locListen); 
			ZLogger.log("PassiveLocationListener startStealListener: Started passive location listener as a pending intent");
		}
		catch (Exception ex)
		{
			ZLogger.logException("PassiveLocationListener startStealListener", ex, context);
			stop(context);
		}
	}

	private void stopStealListener(Context context){
		try 
		{
			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			Intent passiveIntent = new Intent(context, PassiveLocationListener.class);
			PendingIntent locListen = PendingIntent.getBroadcast(context, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			lm.removeUpdates(locListen);
			ZLogger.log("PassiveLocationListener stopStealListener: Stopped passive location listener");
		}
		catch (Exception ex) {
			ZLogger.logException("ServiceStealHelper", ex, context);
		}
	}

	private void startHoldTimer(Context context, int delay) 
	{
		try
		{
			AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, PassiveLocationListener.class);
			i.setAction(Constants.STEAL_BUFFER_PARAM);
			PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
			mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
					(long)(SystemClock.elapsedRealtime() + delay),
					pi);
			ZLogger.log("PassiveLocationListener setHoldTimer: Created a one-time alarm for: " + (delay/1000));
		}
		catch (Exception ex)
		{
			ZLogger.logException("PassiveLocationListener setHolderTimer", ex, context);
			stop(context);
		}
	}

	private void stopHoldTimer(Context context)
	{
		try
		{
			AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, PassiveLocationListener.class);
			PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
			mgr.cancel(pi);
			ZLogger.log("PassiveLocationListener stopHoldTimer: Stopped steal buffer timer");
		}
		catch (Exception ex) 
		{
			ZLogger.logException("PassiveLocationListener stopHolderTimer", ex, context);
		}
	}

	private void onLocationReceived(Context context, Location location)
	{
		try
		{
			if (location != null) {
				ZLogger.log("PassiveLocationListener onLocationReceived: Stolen Location Accuracy " + location.getAccuracy());
				if(meetsStealsRequirements(context, location)) 
				{
					if(isStealAllowed(context))
					{
						// Stolen Location is good to go, stop passive location listener
						//stopHoldTimer(context); // Shouldn't be necessary if we reached here
						stopStealListener(context);

						setIsStealAllowed(context, false);

						// About to perform steal update, stop the ReSync alarm so they don't overlap
						//ServiceRefreshAlarmHelper.stop(_service);
						ReSyncAlarm resyncStopper = new ReSyncAlarm();
						resyncStopper.stop(context);

						saveSteal(context, location);

						Intent serviceIntent = new Intent(context, MyWakefulService.class);
						serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.STEAL_UPDATE_FLAG);
						context.startService(serviceIntent);
					}
					else
					{
						ZLogger.log("PassiveLocationListener onLocationReceived: Steals buffer time has not been reached yet");
						saveSteal(context, location);
					}
				}
				else
				{
					ZLogger.log("PassiveLocationListener onLocationReceived: Stolen location does not meet accuracy requirements: " + location.getAccuracy());
				}
			}
			else
			{
				ZLogger.log("PassiveLocationListener onLocationReceived: location is null");
			}
		}
		catch (Exception ex) 
		{
			ZLogger.logException("PassiveLocationListener onLocationReceived", ex, context);
			stop(context);
		}
	}

	// Delay Timer to prevent consecutive updates from occurring on location changes
	private void delayOver(Context context)
	{
		try
		{
			ZLogger.log("PassiveLocationListener delayOver: buffer time reached.");
			setIsStealAllowed(context, true);
			stopHoldTimer(context);

			Location _lastLocation = PreferenceHelper.getCachedSteal(context);
			if(_lastLocation!=null && _lastLocation.hasAccuracy())
			{
				if(meetsStealsRequirements(context, _lastLocation)) 
				{
					// We had a steal cached during buffer delay and it meets the criteria
					stopStealListener(context);

					// About to perform a steal update, stop the resync alarm so they do not clash
					//ServiceRefreshAlarmHelper.stop(_service);
					ReSyncAlarm resyncStopper = new ReSyncAlarm();
					resyncStopper.stop(context);

					Intent serviceIntent = new Intent(context, MyWakefulService.class);
					serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.STEAL_UPDATE_FLAG);
					context.startService(serviceIntent);
				}
				else
				{
					ZLogger.log("PassiveLocationListener delayOver: Cached steal does not meet requirements: " + _lastLocation.getAccuracy());
				}
			}
			else
			{
				ZLogger.log("PassiveLocationListener delayOver: No steals were cached during Steal delay time period.  Keep waiting for steal");
			}

		}
		catch(Exception ex)
		{
			ZLogger.logException("ServiceStealHelper", ex, context);
			stop(context);
		}
	}

	private boolean meetsStealsRequirements(Context context, Location location) {
		return (meetsMinimumAccuracy(context, location) && 
				isDifferentLocation(context, location) && 
				DistanceCalculator.isBeyondDistance(location, context, getMinDistance(context, location)));
	}

	private boolean meetsMinimumAccuracy(Context context, Location location)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		float minGpsAccuracy = Float.parseFloat(settings.getString(Prefs.KEY_minGpsAccuracy, Prefs.DEFAULT_minGpsAccuracy));    
		float minWifiAccuracy = Float.parseFloat(settings.getString(Prefs.KEY_minWifiAccuracy, Prefs.DEFAULT_minWifiAccuracy)); 
		if(PreferenceHelper.isNetworkPollingAllowed(settings)){
			ZLogger.log("PassiveLocationListener meetsMinimumAccuracy: is more accurate than minimum WiFi accuracy requirement? " + (location.getAccuracy() <= minWifiAccuracy));
			return (location.getAccuracy() <= minWifiAccuracy);
		}
		else
		{
			ZLogger.log("PassiveLocationListener meetsMinimumAccuracy: is more accurate than minimum GPS accuracy requirement? " + (location.getAccuracy() <= minGpsAccuracy));
			return (location.getAccuracy() <= minGpsAccuracy);
		}
	}

	private boolean isDifferentLocation(Context context, Location location) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		float lastLatitude = prefs.getFloat(PersistedData.KEY_savedLocation_lat, 0);
		float lastLongitude = prefs.getFloat(PersistedData.KEY_savedLocation_long, 0);
		float lastAccuracy = prefs.getFloat(PersistedData.KEY_savedLocation_accur, 0);
		if((float)location.getLatitude()==lastLatitude &&
				(float)location.getLongitude()==lastLongitude &&
				(float)location.getAccuracy()==lastAccuracy
		)
		{
			ZLogger.log("PassiveLocationListener isDifferentLocation: location is the same as the last updated location.");
			return false;
		}

		ZLogger.log("PassiveLocationListener isDifferentLocation: location is not the same as last updated location.");
		return true;
	}

	private float getMinDistance(Context context, Location location)
	{
		float minDist = 0;

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		float minDistance = Float.parseFloat(prefs.getString(Prefs.KEY_min_distance, Prefs.DEFAULT_min_distance));
		if(minDistance==Constants.VARIABLE_MIN_DIST)
		{
			minDist = Math.max((location.getAccuracy() * 2), Constants.MIN_MIN_DISTANACE);
		}
		else
		{
			minDist = minDistance;
		}
		ZLogger.log("PassiveLocationListener getMinDistance: Get Minimum change in distance " + minDist);

		return minDist;
	}

	private void saveSteal(Context context, Location location) {
		if(location!=null && context!=null){
			ZLogger.log("PassiveLocationListener saveSteal for update");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putFloat(PersistedData.KEY_stolenLocation_lat, (float)location.getLatitude());
			editor.putFloat(PersistedData.KEY_stolenLocation_long, (float)location.getLongitude());
			editor.putLong(PersistedData.KEY_stolenLocation_date, location.getTime());
			if(location.hasAccuracy()){
				editor.putFloat(PersistedData.KEY_stolenLocation_accur, location.getAccuracy());
			} else {
				editor.remove(PersistedData.KEY_stolenLocation_accur);
			}
			if(location.hasSpeed()){
				editor.putFloat(PersistedData.KEY_stolenLocation_speed, location.getSpeed());
			} else {
				editor.remove(PersistedData.KEY_stolenLocation_speed);
			}
			if(location.hasAltitude()){
				editor.putFloat(PersistedData.KEY_stolenLocation_altitude, (float)location.getAltitude());
			} else {
				editor.remove(PersistedData.KEY_stolenLocation_altitude);
			}
			if(location.hasBearing()){
				editor.putFloat(PersistedData.KEY_stolenLocation_bearing, location.getBearing());
			} else {
				editor.remove(PersistedData.KEY_stolenLocation_bearing);
			}
			editor.commit();
		}	
	}
	
	private boolean isStealAllowed(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(PersistedData.KEY_stealsAllowed, false);
	}

	private void setIsStealAllowed(Context context, boolean isStealAllowed){
		if(isStealAllowed)
			ZLogger.log("PassiveLocationListener setIsStealAllowed: flipping the bit to true");
		else
			ZLogger.log("PassiveLocationListener setIsStealAllowed: flipping the bit to false");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(PersistedData.KEY_stealsAllowed, isStealAllowed);
		editor.commit();
	}
}