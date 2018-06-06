package gaugler.backitude.listener;

import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.FallbackOptionsEnum;
import gaugler.backitude.constants.GpsOptionsEnum;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.constants.SyncOptionsEnum;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.PreferenceHelper;
import gaugler.backitude.util.UpdateWidgetHelper;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.MyWakefulService;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class PreferenceChangeListener extends BroadcastReceiver {

	// onReceive comes from the application updating the state of the widget or someone clicking the widget
	@Override
	public void onReceive(Context context, Intent intent) {

		ZLogger.log("Preference Change - Send Intent Action received");

		if(intent!=null) 
		{
			if (intent.getAction()!=null)
			{
				if(intent.getAction().equals(Constants.CHANGE_SETTING_BY_INTENT)) 
				{	
					Bundle extras = intent.getExtras();
					if(extras!=null) 
					{
						if (extras.containsKey(Constants.KEY_parameter_latitude) && extras.containsKey(Constants.KEY_parameter_longitude)){
							float latitudeValue = extras.getFloat(Constants.KEY_parameter_latitude);
							float longitudeValue = extras.getFloat(Constants.KEY_parameter_longitude);

							if(latitudeValue != 0 && longitudeValue != 0)
							{
								ZLogger.log("PreferenceChangeListener save manual location for update");
								SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
								SharedPreferences.Editor editor = settings.edit();
								editor.putFloat(PersistedData.KEY_stolenLocation_lat, latitudeValue);
								editor.putFloat(PersistedData.KEY_stolenLocation_long, longitudeValue);
								editor.putLong(PersistedData.KEY_stolenLocation_date, (new Date()).getTime());
								editor.remove(PersistedData.KEY_stolenLocation_accur);
								editor.remove(PersistedData.KEY_stolenLocation_speed);
								editor.remove(PersistedData.KEY_stolenLocation_altitude);
								editor.remove(PersistedData.KEY_stolenLocation_bearing);
								editor.commit();

								Intent serviceIntent = new Intent(context, MyWakefulService.class);
								serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.MANUAL_UPDATE_FLAG);
								context.startService(serviceIntent);
							}
						}
						else 
						{	
							SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
							boolean isAppEnabled = prefs.getBoolean(Prefs.KEY_appEnabled, false);
							boolean preferenceChanged = false;

							if(extras.containsKey(Constants.KEY_parameter_interval)){
								int currentInterval = Integer.parseInt(prefs.getString(Prefs.KEY_interval, Prefs.DEFAULT_interval));  
								int pollingInterval =  currentInterval;   

								pollingInterval = extras.getInt(Constants.KEY_parameter_interval, currentInterval);
								ZLogger.log("Standard Polling Interval change request to: " + pollingInterval);

								if(pollingInterval >= 15000 || pollingInterval == 0){

									if(pollingInterval == 0)
									{
										pollingInterval = Constants.NO_POLLING_INTERVAL;
									}
									if(pollingInterval!=currentInterval){
										preferenceChanged = true;
										SharedPreferences.Editor editor = prefs.edit();
										editor.putString(Prefs.KEY_interval, String.valueOf(pollingInterval));
										editor.commit();
									}
								}
							}
							
							if(extras.containsKey(Constants.KEY_parameter_priority)){

								int currentPriority = Integer.parseInt(prefs.getString(Prefs.KEY_gpsOption, GpsOptionsEnum.GPS_ALL.getString()));
								int gpsPriorityValue = currentPriority;

								gpsPriorityValue = extras.getInt(Constants.KEY_parameter_priority, currentPriority);
								ZLogger.log("GPS Priority change request to: " + gpsPriorityValue);

								if((gpsPriorityValue > 0 && gpsPriorityValue < 8) && gpsPriorityValue!=currentPriority){
									preferenceChanged = true;
									SharedPreferences.Editor editor = prefs.edit();
									editor.putString(Prefs.KEY_gpsOption, String.valueOf(gpsPriorityValue));
									editor.commit();
								}
							}

							if(extras.containsKey(Constants.KEY_parameter_enabled)){

								int appEnabledDefault = 0;
								if(isAppEnabled) appEnabledDefault = 1;

								int enableApp = extras.getInt(Constants.KEY_parameter_enabled, appEnabledDefault);

								if(enableApp!=appEnabledDefault)
								{
									if(enableApp==1 && PreferenceHelper.isAbleToBeEnabled(context)){
										isAppEnabled = true;
										preferenceChanged = true;

										SharedPreferences.Editor editor = prefs.edit();
										editor.putBoolean(Prefs.KEY_appEnabled, true);
										editor.commit();

										UpdateWidgetHelper.updateOnOffWidget(true, context);
									}
									else{
										isAppEnabled = false;

										SharedPreferences.Editor editor = prefs.edit();
										editor.putBoolean(Prefs.KEY_appEnabled, false);
										editor.commit();

										ServiceManager sm = new ServiceManager();
										sm.stopAll(context);
										UpdateWidgetHelper.updateOnOffWidget(false, context);
									}
								}

							}

							if(extras.containsKey(Constants.KEY_parameter_docked)){

								boolean isDocked = prefs.getBoolean(Prefs.KEY_realtime, false);
								int isDockedDefault = 0;
								if(isDocked) isDockedDefault = 1;

								int enableDocked = extras.getInt(Constants.KEY_parameter_docked, isDockedDefault);

								if(enableDocked!=isDockedDefault)
								{
									preferenceChanged = true;

									SharedPreferences.Editor editor = prefs.edit();
									editor.putBoolean(Prefs.KEY_realtime, (enableDocked==1));
									editor.commit();

									UpdateWidgetHelper.updateRealtimeWidget(context);
								}
							}

							if(extras.containsKey(Constants.KEY_parameter_dockedInterval)){
								int currentInterval = Integer.parseInt(prefs.getString(Prefs.KEY_realtime_interval, Prefs.DEFAULT_realtime_interval));  
								int pollingInterval =  currentInterval;   

								pollingInterval = extras.getInt(Constants.KEY_parameter_dockedInterval, currentInterval);
								ZLogger.log("Docked Mode Polling Interval change request to: " + pollingInterval);

								if(pollingInterval >= 15000){

									if(pollingInterval!=currentInterval){
										preferenceChanged = true;
										SharedPreferences.Editor editor = prefs.edit();
										editor.putString(Prefs.KEY_realtime_interval, String.valueOf(pollingInterval));
										editor.commit();
									}
								}
							}
							
							if(extras.containsKey(Constants.KEY_parameter_sync)){

								int currentSyncValue = Integer.parseInt(prefs.getString(Prefs.KEY_syncOptions, SyncOptionsEnum.ANY_DATA_NETWORK.getString()));
								int newSyncValue = currentSyncValue;

								newSyncValue = extras.getInt(Constants.KEY_parameter_sync, currentSyncValue);
								ZLogger.log("Offline Sync type change to: " + newSyncValue);

								if((newSyncValue > 0 && newSyncValue < 4) && newSyncValue!=currentSyncValue){
									preferenceChanged = true;
									SharedPreferences.Editor editor = prefs.edit();
									editor.putString(Prefs.KEY_syncOptions, String.valueOf(newSyncValue));
									editor.commit();
								}
							}
							
							if(extras.containsKey(Constants.KEY_parameter_fallback)){

								int currentFallbackValue = Integer.parseInt(prefs.getString(Prefs.KEY_fallbackOptions, FallbackOptionsEnum.MOST_ACCURATE_OR_REPEAT_PREVIOUS.getString()));
								int newFallbackValue = currentFallbackValue;

								newFallbackValue = extras.getInt(Constants.KEY_parameter_fallback, currentFallbackValue);
								ZLogger.log("Offline Sync type change to: " + newFallbackValue);

								if((newFallbackValue > 0 && newFallbackValue < 5) && newFallbackValue!=currentFallbackValue){
									preferenceChanged = true;
									SharedPreferences.Editor editor = prefs.edit();
									editor.putString(Prefs.KEY_fallbackOptions, String.valueOf(newFallbackValue));
									editor.commit();
								}
							}
							
							if(isAppEnabled && preferenceChanged){
								ZLogger.log("PreferenceChangeListener onReceive: restart alarms");
								ServiceManager sm = new ServiceManager();
								sm.startAlarms(context);
							}

						}
					}
				}
			}
		}
	}	
}
