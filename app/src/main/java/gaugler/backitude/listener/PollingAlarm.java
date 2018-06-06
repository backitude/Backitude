package gaugler.backitude.listener;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.constants.StatusBarOptionsEnum;
import gaugler.backitude.util.PreferenceHelper;
import gaugler.backitude.util.ServiceHelper;
import gaugler.backitude.util.StatusBarHelper;
import gaugler.backitude.util.UpdateWidgetHelper;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.MyWakefulService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PollingAlarm extends BroadcastReceiver {

	// Application Preferences
	private boolean isApplicationEnabled = false;
	private boolean realtimeEnabled = false;
	private boolean isCharging = false;
	private boolean isWiFiModeEnabled = false;
	private boolean isWiFiConnected = false;
	private int PERIOD = Prefs.DEFAULT_VALUE_interval;
	private int RESYNC_PERIOD = Prefs.DEFAULT_VALUE_update_interval;
	private boolean isStealsEnabled = false;
	private int showStatusBar = StatusBarOptionsEnum.DISPLAY_ENABLED.getValue();

	// this constructor is called by the alarm manager.     
	public PollingAlarm(){ } 


	@Override
	public void onReceive(Context context, Intent intent) {

		if(intent!=null &&
				intent.getAction()!=null &&
				intent.getAction().equals(Constants.POLL_BUFFER_PARAM))
		{		
			ZLogger.log("PollingAlarm onReceive: method start for delayed start (waited for battery status)");
			start(context);
		}
		else
		{
			ZLogger.log("PollingAlarm onReceive: method start for appEnabled " + isApplicationEnabled);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isApplicationEnabled = prefs.getBoolean(Prefs.KEY_appEnabled, false);
			boolean showMessages = prefs.getBoolean(Prefs.KEY_update_toast, true);
			if(isApplicationEnabled){
				if( // Poll if....
						(prefs.getBoolean(Prefs.KEY_offlineSync, false)) ||
						(prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false) && ServiceHelper.isConnectedToWifi(context)) ||
						(!prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false) && ServiceHelper.isNetworkAvailable(context))
				)
				{
					if(!ServiceHelper.isMyWakefulServiceRunning(context)){
						ZLogger.log("PollingAlarm onReceive: Starting up wakeful service with param: " + Constants.POLL_TIMER_FLAG);
						// Start the wakeful service that will poll for new location value
						Intent serviceIntent = new Intent(context, MyWakefulService.class);
						serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.POLL_TIMER_FLAG);
						context.startService(serviceIntent);
					}
					else
					{
						//Application is in the middle of polling already,
						// or in the middle of sending a request to Latitude via ReSync (which would suck)
						ZLogger.log("PollingAlarm onReceive: Wakeful service already running");
					}
				}
				else
				{
					ZLogger.log("PollingAlarm onReceive: Cannot start wakeful service, No network data signal/Data saver enabled");
					if(showMessages){
						if(prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false))
						{
							Toast.makeText(context, context.getResources().getString(R.string.NO_POLL_NO_WIFI), Toast.LENGTH_LONG).show();
						}
						else
						{
							Toast.makeText(context, context.getResources().getString(R.string.NO_POLL_NO_NETWORK), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
			else
			{
				ZLogger.log("PollingAlarm onReceive: Application is not enabled, turn off alarms");
				stop(context);
			}
		}
	}

	public void start(Context context){

		if(context==null)
		{
			ZLogger.log("PollingAlarm start: context is null");
			return;
		}

		loadPreferences(context);

		ZLogger.log("PollingAlarm start: is application enabled? " + isApplicationEnabled);
		if(isApplicationEnabled){

			/* Check Battery Status
			 *  - There is a receiver for battery-charging status changes so that if RealTime updating
			 *  is enabled, and the battery charging changes, the alarm service is restarted and alarm changed
			 *  However...if the user is already charging battery before the app is enabled,
			 * 	the receiver will never receive so we still need to check status here anyways. 
			 * - Same is probably true if they undock to start a new alarm but the battery status is full (unintended results?) 
			 * - Removed Status Full check because it could probably be full without being docked and cause all problems
			 */
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = prefs.edit();
			Intent lastBatteryStatus = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			int status = lastBatteryStatus.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
			if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL){
				ZLogger.log("PollingAlarm start: Detects that the device is charging");
				isCharging = true;
				editor.putBoolean(PersistedData.KEY_isCharging, isCharging);
				editor.commit();
			}
			else
			{
				ZLogger.log("PollingAlarm: Detects that the device is not charging");
				isCharging = false;
				editor.putBoolean(PersistedData.KEY_isCharging, isCharging);
				editor.commit();
			}

			// Don't start the alarm for "steals-only" mode unless we're in Docked-Mode or Wi-Fi Connected Mode
			if(PERIOD != Constants.NO_POLLING_INTERVAL || 
					(realtimeEnabled && isCharging) ||
					(isWiFiModeEnabled && isWiFiConnected)) {
				startRepeatingAlarm(context, 0);
			}
			else
			{
				// "Steals-only" mode
				editor.putBoolean(PersistedData.KEY_realtimeRunning, false);
				editor.putBoolean(PersistedData.KEY_wifiModeRunning, false);
				editor.commit();
				ZLogger.log("PollingAlarm start: turn off Real Time widget: " + prefs.getBoolean(PersistedData.KEY_realtimeRunning, true));

				UpdateWidgetHelper.updateRealtimeWidget(context);

				// No alarm needed, just start steal service and resync
				boolean nothingIsRunning = true;
				if(isStealsEnabled){
					//ServiceStealHelper.start(context);
					PassiveLocationListener stealStarter = new PassiveLocationListener();
					stealStarter.start(context);
					nothingIsRunning = false;
				}
				else
				{
					ZLogger.log("ServiceAlarmHelper start: Could not start steals- not enabled in config");
				}
				if(PreferenceHelper.hasLastPolledLocation(context) && 
						(RESYNC_PERIOD > Constants.ON_LOC_POLL_ONLY_VALUE))
				{
					ZLogger.log("PollingAlarm start: Starting Re-Sync for steals only mode");
					ReSyncAlarm resyncStarter = new ReSyncAlarm();
					resyncStarter.start(context);
					nothingIsRunning = false;
				}
				else
				{
					ZLogger.log("PollingAlarm start: Couldn't start Re-Sync (null location or minimum distance = 0 or resync interval < interval)");
				}
				if(nothingIsRunning)
				{
					Toast.makeText(context, context.getResources().getString(R.string.BAD_CONFIG), Toast.LENGTH_SHORT).show();
				}
			}
			if(showStatusBar == StatusBarOptionsEnum.DISPLAY_ENABLED.getValue()){

				ZLogger.log("Polling Alarm: app enabled notification bar");
				StatusBarHelper.createAppEnabledNotification(context);
			}
			else {
				if((realtimeEnabled && isCharging) && 
						!(isWiFiModeEnabled && isWiFiConnected) &&
						(showStatusBar == StatusBarOptionsEnum.DISPLAY_REALTIME.getValue() || 
								showStatusBar == StatusBarOptionsEnum.DISPLAY_POLLING_REALTIME.getValue())){

					ZLogger.log("Polling Alarm: realtime notification bar");
					StatusBarHelper.createRealTimeEnabledNotification(context);
				}
			}
		}
		else
		{
			ZLogger.log("PollingAlarm start: Application is not enabled");
		}
	}

	public void delayedStart(Context context) {
		//This method is essentially called for the BatteryListener class
		// A device can become connected status, but not yet "charging status"
		// It may take a second or two until the device updates its status accordingly
		AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, PollingAlarm.class);
		i.setAction(Constants.POLL_BUFFER_PARAM);
		PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
		long delay = (long)(SystemClock.elapsedRealtime() + Constants.TWO_SECONDS);

		// Inexact repeating which is easier on battery (system can phase events and not wake at exact times)
		ZLogger.log("PollingAlarm delayedStart: Creating an alarm to delay start the creation of the alarm");
		mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
				delay,
				pi);

	}

	public void restartRepeatingAlarm(Context context, int delay)
	{
		loadPreferences(context);
		startRepeatingAlarm(context, delay);
	}

	private void startRepeatingAlarm(Context context, int extraDelay) {

		try
		{
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = settings.edit();

			AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, PollingAlarm.class);
			PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);

			int interval = PERIOD;
			if(isWiFiModeEnabled && isWiFiConnected)
			{
				editor.putBoolean(PersistedData.KEY_realtimeRunning, false);
				editor.putBoolean(PersistedData.KEY_wifiModeRunning, true);
				interval = Integer.parseInt(settings.getString(Prefs.KEY_wifi_mode_interval, Prefs.DEFAULT_wifi_mode_interval)); 
			}
			else
			{
				if(realtimeEnabled && isCharging)
				{
					editor.putBoolean(PersistedData.KEY_wifiModeRunning, false);
					editor.putBoolean(PersistedData.KEY_realtimeRunning, true);
					interval = Integer.parseInt(settings.getString(Prefs.KEY_realtime_interval, Prefs.DEFAULT_realtime_interval));   		
				}
				else
				{
					editor.putBoolean(PersistedData.KEY_wifiModeRunning, false);
					editor.putBoolean(PersistedData.KEY_realtimeRunning, false);
				}
			}
			editor.putBoolean(PersistedData.KEY_isAlarmRunning, true);
			editor.commit();

			ZLogger.log("PollingAlarm startRepeatingAlarm: update Real Time widget: " + settings.getBoolean(PersistedData.KEY_realtimeRunning, true));
			UpdateWidgetHelper.updateRealtimeWidget(context);

			long delay = (long)(SystemClock.elapsedRealtime() + Constants.TWO_SECONDS + extraDelay);

			// Inexact repeating which is easier on battery (system can phase events and not wake at exact times)
			ZLogger.log("PollingAlarm startRepeatingAlarm: Creating an alarm with the interval of: " + interval);
			mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
					delay,
					interval, 
					pi);

		}
		catch(Exception ex)
		{
			ZLogger.logException("PollingAlarm startRepeatingAlarm", ex, context);
			stop(context);
		}
	}

	public void stop(Context context) {

		AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, PollingAlarm.class);
		PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);

		if(context!=null){
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(PersistedData.KEY_isAlarmRunning, false);
			editor.putBoolean(PersistedData.KEY_realtimeRunning, false);
			editor.commit();
			ZLogger.log("PollingAlarm stop: turn off Real Time widget: " + settings.getBoolean(PersistedData.KEY_realtimeRunning, true));
			UpdateWidgetHelper.updateRealtimeWidget(context);
		}
		try{
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
			ZLogger.logException("PollingAlarm", ex, context);
		}

		ZLogger.log("PollingAlarm stop: cancel both top notifications");
		StatusBarHelper.cancelAppEnabledNotfication(context);
		StatusBarHelper.cancelRealTimeEnabledNotfication(context);
	}

	private void loadPreferences(Context context) {
		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		isApplicationEnabled = prefs.getBoolean(Prefs.KEY_appEnabled, false);
		PERIOD = Integer.parseInt(prefs.getString(Prefs.KEY_interval, Prefs.DEFAULT_interval));      
		realtimeEnabled = prefs.getBoolean(Prefs.KEY_realtime, false);
		isCharging = prefs.getBoolean(PersistedData.KEY_isCharging, false);
		showStatusBar = Integer.parseInt(prefs.getString(Prefs.KEY_statusBar, StatusBarOptionsEnum.DISPLAY_POLLING.getString()));
		isStealsEnabled = prefs.getBoolean(Prefs.KEY_steals_enabled, false);
		RESYNC_PERIOD = Integer.parseInt(prefs.getString(Prefs.KEY_resync_interval, Prefs.DEFAULT_update_interval));  

		isWiFiModeEnabled = prefs.getBoolean(Prefs.KEY_wifi_mode, false);
		isWiFiConnected = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if(wifiNetInfo!=null)
		{
			ZLogger.log("ConnectivityListener onReceive: Wifi Connected = " + wifiNetInfo.isConnected());				
			isWiFiConnected = wifiNetInfo.isConnected();
		}
	}
}