package gaugler.backitude.listener;

import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.ZLogger;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BatteryListener extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent arg1) {

		ZLogger.log("BatteryListener onReceive");
		boolean isCharging = false;
		boolean oldIsCharging = false;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		if (arg1.getAction().equals(Intent.ACTION_POWER_CONNECTED)){
			ZLogger.log("BatteryListener: Device is docked");
			isCharging =  true;
			// example, Intent.ACTION_BATTERY_CHANGED
			// Battery Level: (String.valueOf(arg1.getIntExtra("level", 0)) + "%");
			// Voltage: (String.valueOf((float)arg1.getIntExtra("voltage", 0)/1000) + "V");
			// Temperature: (String.valueOf((float)arg1.getIntExtra("temperature", 0)/10) + "c");
			// Technology: (arg1.getStringExtra("technology"));

			// get BatteryManager.BATTERY_STATUS_UNKNOWN
			//  Options:
			// BatteryManager.BATTERY_STATUS_CHARGING;
			// BatteryManager.BATTERY_STATUS_DISCHARGING)
			// BatteryManager.BATTERY_STATUS_NOT_CHARGING){
			// BatteryManager.BATTERY_STATUS_FULL){

			// Health = arg1.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN);
			//  Options:
			//      BatteryManager.BATTERY_HEALTH_GOOD
			//		BatteryManager.BATTERY_HEALTH_OVERHEAT
			//		BatteryManager.BATTERY_HEALTH_DEAD
			//		BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE
			//		BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE	
		}
		else {
			isCharging = false;
			ZLogger.log("BatteryListener: Device is UN-docked");
		}
		
		if(prefs!=null)
		{
			oldIsCharging = prefs.getBoolean(PersistedData.KEY_isCharging, false);
			if(isCharging!=oldIsCharging)
			{
				ZLogger.log("BatteryListener: Device docked status is different then before");
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(PersistedData.KEY_isCharging, isCharging);
				editor.commit();

				boolean realtimeEnabled = prefs.getBoolean(Prefs.KEY_realtime, false);
				boolean isEnabled = prefs.getBoolean(Prefs.KEY_appEnabled, false);
				boolean isWifiModeRunning = prefs.getBoolean(PersistedData.KEY_wifiModeRunning, false);
				/*
				* If RealTime updating is enabled and the application is enabled, 
				* and if we're in here, the charging status changed
				* So we need to restart the alarm service to change the alarm as applicable
				*/
				if(isEnabled && realtimeEnabled && !isWifiModeRunning){
					ZLogger.log("BatteryListener: docked status changed AND realtime updating is effected");
					ServiceManager appStarter = new ServiceManager();
					appStarter.startAlarmsWithDelay(context.getApplicationContext());
				}
				else
				{
					ZLogger.log("BatteryListener: Device status changed but dont matter- doesnt affect realtime");
				}
			}
			else
			{
				ZLogger.log("BatteryListener: Device status is the same as previous, do nothing");
			}
		}
	}
	
}