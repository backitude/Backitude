package gaugler.backitude.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.constants.SyncOptionsEnum;
import gaugler.backitude.listener.PassiveLocationListener;
import gaugler.backitude.listener.PollingAlarm;
import gaugler.backitude.listener.ReSyncAlarm;
import gaugler.backitude.util.DatabaseHelper;
import gaugler.backitude.util.PreferenceHelper;
import gaugler.backitude.util.ServiceHelper;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.MyWakefulService;
import gaugler.backitude.wakeful.OfflineLocationSyncService;
import gaugler.backitude.wakeful.ReSyncUpdateService;

public class ServiceManager {

	private static int intervalValue = Prefs.DEFAULT_VALUE_interval;
	private static boolean isEnabled = false;
	private static boolean realtimeRunning = false;
	private static boolean isWifiModeRunning = false;
	private static int realtimeInterval = Prefs.DEFAULT_VALUE_realtime_interval;
	private static boolean isStealsEnabled = false;

	public ServiceManager(){}

	public void enableApp(Context context){
		ZLogger.log("ServiceManager enableApp: start the application enable process");

		syncOfflineRecordsIfExists(context);
		startAlarms(context);

	}

	public void startAlarms(Context context){
		ZLogger.log("ServiceManager startAlarms: stop everything, then start the polling alarm");

		stopAll(context);
		
		PollingAlarm alarm = new PollingAlarm();
		alarm.start(context);
	}
	
	public void startAlarmsWithDelay(Context context){
		ZLogger.log("ServiceManager startAlarmsWithDelay: stop everything, then start the polling alarm with a delay");

		stopAll(context);

		PollingAlarm alarm = new PollingAlarm();
		alarm.delayedStart(context);
	}
	
	public void disableApp(Context context){

		ZLogger.log("ServiceManager disableApp: stopping everything");
		Intent stopMyWakefulService = new Intent(context, OfflineLocationSyncService.class);
		context.stopService(stopMyWakefulService);

		stopAll(context);
	}

	public void stopAll(Context context)
	{
		ZLogger.log("ServiceManager stopAll: stopping all location polling services");

		Intent stopWakefulIntent = new Intent(context, MyWakefulService.class);
		context.stopService(stopWakefulIntent);

		//TokenRefreshTask tokenRefreshStopper = new TokenRefreshTask();
		//tokenRefreshStopper.stop();

		PollingAlarm alarmStopper = new PollingAlarm();
		alarmStopper.stop(context);

		PassiveLocationListener stealStopper = new PassiveLocationListener();
		stealStopper.stop(context);

		ServiceLocationHelper.stop();	

		ReSyncAlarm resyncStopper = new ReSyncAlarm();
		resyncStopper.stop(context);

		ZLogger.log("ServiceManager stopAll: everything is stopped");
	}

	public void updateOver(Context context, int updateType)
	{
		ZLogger.log("ServiceManager updateOver: " + updateType);
		try
		{
			getPrefs(context);

			//TokenRefreshTask tokenRefreshStopper = new TokenRefreshTask();
			//tokenRefreshStopper.stop();

			ZLogger.log("ServiceManager updateOver: Is polling timer running? " + PreferenceHelper.isPollingAlarmRunning(context));

			if(updateType == Constants.POLL_UPDATE_OVER_TRUE_FLAG || 
					updateType == Constants.POLL_UPDATE_OVER_FALSE_FLAG ||
					updateType == Constants.FIRE_UPDATE_OVER_TRUE_FLAG ||
					updateType == Constants.FIRE_UPDATE_OVER_FALSE_FLAG)
				//Update from a Poll whether that be from Location timer service or Fire in the Hole
			{
				ZLogger.log("ServiceManager updateOver: Update is the result of a Poll or Fire Update");
				if(isEnabled && (
						(!realtimeRunning && intervalValue > Constants.ONE_MINUTE) ||
						(realtimeRunning && realtimeInterval > Constants.ONE_MINUTE) ||
						isWifiModeRunning))
				{
					if((updateType == Constants.FIRE_UPDATE_OVER_TRUE_FLAG) && PreferenceHelper.isPollingAlarmRunning(context))
					{
						ZLogger.log("ServiceManager updateOver: Re-start location polling timer after a successful Fire Update");
						//ServiceAlarmHelper.restartAlarm(this, intervalValue);
						PollingAlarm alarmStarter = new PollingAlarm();
						alarmStarter.restartRepeatingAlarm(context, intervalValue);
					}
					ZLogger.log("ServiceManager updateOver: App is enabled and interval is greater than 1minute, and real-time updating is not running");
					if(isStealsEnabled){
						// Start up the steals... if timer interval is every 1min interval or larger and not in Real-Time update mode
						ZLogger.log("ServiceManager updateOver: re-Starting steal service after location poll.");
						PassiveLocationListener stealStarter = new PassiveLocationListener();
						stealStarter.start(context);
					}
					else
					{
						ZLogger.log("ServiceManager updateOver: Steals service will not be restarted. (disabled or buffer greater than polling interval)");
					}
				}
				else
				{
					// Fire in the Hole or really fast time interval/Real-time Update mode, don't restart anything
					ZLogger.log("ServiceManager updateOver: steals not permitted for configuration, do not start steals)");
				}
			}
			else
			{
				ZLogger.log("ServiceManager updateOver: Update was result of steal or resync.");
				// Update was result of Steals or Refresh
				if(updateType == Constants.STEAL_UPDATE_OVER_TRUE_FLAG || updateType == Constants.STEAL_UPDATE_OVER_FALSE_FLAG)
				{
					ZLogger.log("ServiceManager updateOver: Update is a result of a steal.");
					if(PreferenceHelper.isPollingAlarmRunning(context))
					{
						ZLogger.log("ServiceManager updateOver: Polling alarm is running.");
						if(updateType == Constants.STEAL_UPDATE_OVER_TRUE_FLAG){
							ZLogger.log("ServiceManager updateOver: Re-start location polling timer after a successful steal");
							//ServiceAlarmHelper.restartAlarm(this, intervalValue);
							PollingAlarm alarmStarter = new PollingAlarm();
							alarmStarter.restartRepeatingAlarm(context, intervalValue);
						}
						else
						{
							ZLogger.log("ServiceManager updateOver: Steal was not successful, leave Loc Poll Alarm running");
						}
					}
					else
					{
						ZLogger.log("ServiceManager updateOver: Alarm was not running before steals update (steals only mode?)...Successful Update: " + (updateType == Constants.STEAL_UPDATE_OVER_TRUE_FLAG));
					}
					if(isStealsEnabled){
						// Came from Steals, because Steals stopped Refresh
						PassiveLocationListener stealStarter = new PassiveLocationListener();
						if(updateType == Constants.STEAL_UPDATE_OVER_TRUE_FLAG){
							ZLogger.log("ServiceManager updateOver: Start steal listener with delay after a successful steal");
							stealStarter.start(context);
						}
						else{
							ZLogger.log("ServiceManager updateOver: Resume steal listener after a failed steal update");
							stealStarter.resume(context);
						}
					}
					else
					{
						ZLogger.log("ServiceManager updateOver: Steals service will not be restarted. (disabled or buffer greater than polling interval)");
					}
				}
			}
			if(!PreferenceHelper.isReSyncAlarmRunning(context))
			{
				if(PreferenceHelper.hasLastPolledLocation(context))
				{
					ZLogger.log("ServiceManager updateOver: re-starting resync service if enabled");
					ReSyncAlarm resyncStarter = new ReSyncAlarm();
					resyncStarter.restart(context);
				}
				else
				{
					ZLogger.log("ServiceManager updateOver: Did not restart refresh service, no location in history to repeat");
				}
			}
			else
			{
				// Came from a Refresh
				ZLogger.log("ServiceManager updateOver: resync is already running");
			}

			if(updateType == Constants.RESYNC_UPDATE_OVER_TRUE_FLAG || updateType == Constants.RESYNC_UPDATE_OVER_FALSE_FLAG)
			{
				ZLogger.log("ServiceManager updateOver: Stop ReSync Update service");
				Intent stopReSyncUpdateService = new Intent(context, ReSyncUpdateService.class);
				context.stopService(stopReSyncUpdateService);
			}
			else
			{
				ZLogger.log("ServiceManager updateOver: Stop Generic Wakeful Service");
				Intent stopMyWakefulService = new Intent(context, MyWakefulService.class);
				context.stopService(stopMyWakefulService);
			}
		}
		catch(Exception ex)
		{
			ZLogger.logException("ServiceManager updateOver: fail", ex, context);
		}
	}

	private void syncOfflineRecordsIfExists(Context context) {
		ZLogger.log("ServiceManager syncOfflineRecordsIfExists: method start");
		try{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			int syncOption = Integer.parseInt(prefs.getString(Prefs.KEY_syncOptions, SyncOptionsEnum.ANY_DATA_NETWORK.getString()));
			if(!ServiceHelper.isMyOfflineServiceRunning(context)){
				if(DatabaseHelper.recordsExist(context)){
					ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
					NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					if(mobNetInfo!=null)
					{
						ZLogger.log("ServiceManager syncOfflineRecordsIfExists: Mobile Connected = " + mobNetInfo.isConnected());	
						if(mobNetInfo.isConnected() && !prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false))
						{
							if(syncOption == SyncOptionsEnum.ANY_DATA_NETWORK.getValue())
							{
								Intent serviceIntent = new Intent(context, OfflineLocationSyncService.class);
								serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.OFFLINE_SYNC_FLAG);
								context.startService(serviceIntent);
								return;
							}
						}
					}
					if(wifiNetInfo!=null)
					{
						ZLogger.log("ServiceManager syncOfflineRecordsIfExists: Wifi Connected = " + wifiNetInfo.isConnected());				
						if(wifiNetInfo.isConnected())
						{
							if(syncOption == SyncOptionsEnum.ANY_DATA_NETWORK.getValue() || syncOption == SyncOptionsEnum.WIFI_ONLY.getValue())
							{
								Intent serviceIntent = new Intent(context, OfflineLocationSyncService.class);
								serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.OFFLINE_SYNC_FLAG);
								context.startService(serviceIntent);
								return;
							}
						}
					}
				}
				else {
					ZLogger.log("ServiceManager syncOfflineRecordsIfExists: method exit (no records exist)");
				}
			}
			else
			{
				ZLogger.log("ServiceManager syncOfflineRecordsIfExists: method exit (offline sync already running)");
			}
		}
		catch(Exception ex){
			ZLogger.log("ServiceManager syncOfflineRecordsIfExists: " + ex.toString());
		}
	}

	private void getPrefs(Context context){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		intervalValue = Integer.parseInt(settings.getString(Prefs.KEY_interval, Prefs.DEFAULT_interval));  
		isEnabled = settings.getBoolean(Prefs.KEY_appEnabled, false);

		realtimeRunning = settings.getBoolean(PersistedData.KEY_realtimeRunning, false);
		realtimeInterval = Integer.parseInt(settings.getString(Prefs.KEY_realtime_interval, Prefs.DEFAULT_realtime_interval));  
		isWifiModeRunning = settings.getBoolean(PersistedData.KEY_wifiModeRunning, false);
		
		int currentInterval = intervalValue;
		if(realtimeRunning)
		{
			currentInterval = realtimeInterval;
		}
		else if(isWifiModeRunning)
		{
			currentInterval = Integer.parseInt(settings.getString(Prefs.KEY_wifi_mode_interval, Prefs.DEFAULT_wifi_mode_interval));
		}
		int stealBuffer = Integer.parseInt(settings.getString(Prefs.KEY_max_steals_interval, Prefs.DEFAULT_max_steals_interval));  
		isStealsEnabled = settings.getBoolean(Prefs.KEY_steals_enabled, false) && (currentInterval > stealBuffer);
	}

}