package gaugler.backitude.listener;

import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.util.ServiceHelper;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.ReSyncUpdateService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class ReSyncAlarm extends BroadcastReceiver {
	
	// Application Preferences
	private boolean isApplicationEnabled = false;
	private int PERIOD = Prefs.DEFAULT_VALUE_interval;
	private int RESYNC_PERIOD = Prefs.DEFAULT_VALUE_update_interval;
	private int REALTIME_PERIOD = Prefs.DEFAULT_VALUE_realtime_interval;
	private int WIFI_PERIOD = Prefs.DEFAULT_VALUE_wifi_mode_interval;
	private float minDistance = Prefs.DEFAULT_VALUE_min_distance;
	private boolean realtimeEnabled = false;
	private boolean isCharging = false;
	private boolean isRealtimeRunning = false;
	private boolean isWifiModeRunning = false;
	private boolean restartWithDelay = false;
	
	// this constructor is called by the alarm manager.     
	public ReSyncAlarm(){ } 

	@Override
	public void onReceive(Context context, Intent intent) {
		ZLogger.log("ReSyncAlarm onReceive: start");
				
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isApplicationEnabled = prefs.getBoolean(Prefs.KEY_appEnabled, false);
		ZLogger.log("ReSyncAlarm onReceive: appEnabled " + isApplicationEnabled);
		if(isApplicationEnabled){
			if(ServiceHelper.isNetworkAvailable(context) || prefs.getBoolean(Prefs.KEY_offlineSync, false)){
				if(!ServiceHelper.isReSyncUpdateServiceRunning(context)){
					ZLogger.log("ReSyncAlarm onReceive: Starting up wakeful service with param: " + Constants.RESYNC_ALARM_FLAG);
					//Wakeful re-sync
					Intent serviceIntent = new Intent(context, ReSyncUpdateService.class);
					serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.RESYNC_ALARM_FLAG);
					context.startService(serviceIntent);
				}
				else
				{
					//Application is in the middle of polling already,
					// or in the middle of sending a request to Latitude via ReSync (which would suck)
					ZLogger.log("ReSyncAlarm onReceive: Wakeful service already running");
				}
			}
			else
			{
				ZLogger.log("ReSyncAlarm onReceive: Cannot start wakeful service, No network data signal");
			}
		}
		else
		{
			ZLogger.log("ReSyncAlarm onReceive: Application is not enabled, turn off alarms");
			stop(context);
		}
	}
	
	public void restart(Context context)
	{
		ZLogger.log("ReSyncAlarm: restart");
		restartWithDelay = true;
		start(context);
	}
	
	public void start(Context context)
	{
		ZLogger.log("ReSyncAlarm: start");
		if(context==null)
		{
			ZLogger.log("ReSyncAlarm: context is null");
			return;
		}
		
		loadPreferences(context);

		if(isApplicationEnabled && 
				RESYNC_PERIOD > Constants.ON_LOC_POLL_ONLY_VALUE && 
				(
					(PERIOD > RESYNC_PERIOD && !isRealtimeRunning && !isWifiModeRunning) || 
					(REALTIME_PERIOD > RESYNC_PERIOD && isRealtimeRunning) || 
					(WIFI_PERIOD > RESYNC_PERIOD && isWifiModeRunning) ||
					minDistance != Constants.NO_MIN_CHANGE_DISTANCE
				)
			)
		{		
			startRepeatingAlarm(context);			
		}
		else
		{
			ZLogger.log("ReSyncAlarm failed to start: " + isApplicationEnabled + " resync period = " + RESYNC_PERIOD + " is not fast enough");
		}
	}
	
	private void startRepeatingAlarm(Context context) {

		try
		{
			ZLogger.log("ReSyncAlarm start: " + (RESYNC_PERIOD/1000));
					
			AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, ReSyncAlarm.class);
			PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
			
			// Start ReSyncAlarm with a delayed start so it doesn't fire immediately
			long delay = (long)(SystemClock.elapsedRealtime() + RESYNC_PERIOD);

			// For "ReSync Only" mode, start Resync without a delay unless we're real-time updating in which case its not really resync only mode
			if(PERIOD == Constants.NO_POLLING_INTERVAL && !isRealtimeRunning && !isWifiModeRunning && !restartWithDelay) {
				delay = (long)(SystemClock.elapsedRealtime() + Constants.THREE_SECONDS);
			}
			
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(PersistedData.KEY_isReSyncRunning, true);
			editor.commit();
			
			mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
					delay,
					RESYNC_PERIOD, 
					pi);	
		}
		catch(Exception ex)
		{
			ZLogger.logException("ReSyncAlarm startRepeatingAlarm", ex, context);
			stop(context);
		}
	}

	public void stop(Context context) {
		ZLogger.log("ReSyncAlarm: stop");
		try
		{
			Intent serviceIntent = new Intent(context, ReSyncUpdateService.class);
			context.stopService(serviceIntent);
			
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(PersistedData.KEY_isReSyncRunning, false);
			editor.commit();
			
			AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, ReSyncAlarm.class);
			PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
			
			if(mgr!=null)
			{
				if(pi!=null){
					mgr.cancel(pi);
					pi.cancel();
					pi = null;
				}
				mgr = null;
			}
		}
		catch(Exception ex)
		{
			ZLogger.logException("ReSyncAlarm stop", ex, context);
		}
	}

	private void loadPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		isApplicationEnabled = prefs.getBoolean(Prefs.KEY_appEnabled, false);
		RESYNC_PERIOD = Integer.parseInt(prefs.getString(Prefs.KEY_resync_interval, Prefs.DEFAULT_update_interval));
		minDistance = Float.parseFloat(prefs.getString(Prefs.KEY_min_distance, Prefs.DEFAULT_min_distance));    
		PERIOD = Integer.parseInt(prefs.getString(Prefs.KEY_interval, Prefs.DEFAULT_interval));
		REALTIME_PERIOD = Integer.parseInt(prefs.getString(Prefs.KEY_realtime_interval, Prefs.DEFAULT_realtime_interval));
		WIFI_PERIOD = Integer.parseInt(prefs.getString(Prefs.KEY_wifi_mode_interval, Prefs.DEFAULT_wifi_mode_interval));
		
		realtimeEnabled = prefs.getBoolean(Prefs.KEY_realtime, false);
		isCharging = prefs.getBoolean(PersistedData.KEY_isCharging, false);
		
		boolean isWiFiModeEnabled = prefs.getBoolean(Prefs.KEY_wifi_mode, false);
		boolean isWiFiConnected = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(wifiNetInfo!=null)
		{
			ZLogger.log("ConnectivityListener onReceive: Wifi Connected = " + wifiNetInfo.isConnected());				
			isWiFiConnected = wifiNetInfo.isConnected();
		}
		isWifiModeRunning = isWiFiModeEnabled && isWiFiConnected;
		isRealtimeRunning = realtimeEnabled && isCharging && !isWifiModeRunning;
		
	}
}