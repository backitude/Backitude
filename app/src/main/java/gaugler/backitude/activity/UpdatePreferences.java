package gaugler.backitude.activity;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.constants.SyncOptionsEnum;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.DatabaseHelper;
import gaugler.backitude.util.ServiceHelper;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.OfflineLocationSyncService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class UpdatePreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	ListPreference resync_interval;
	CheckBoxPreference steals_enabled_pref;
	CheckBoxPreference wifiOnlyEnabled_pref;
	ListPreference max_steals_interval;
	ListPreference min_distance;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.update_preferences);

		resync_interval = (ListPreference) findPreference(Prefs.KEY_resync_interval);
		steals_enabled_pref = (CheckBoxPreference) findPreference(Prefs.KEY_steals_enabled);
		steals_enabled_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				max_steals_interval.setEnabled(!((CheckBoxPreference)preference).isChecked());
				return true;
			}			
		});		

		wifiOnlyEnabled_pref = (CheckBoxPreference) findPreference(Prefs.KEY_wifiOnly_enabled);
		wifiOnlyEnabled_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(((CheckBoxPreference)preference).isChecked())
				{
					// Un-checked Data Roam mode, I don't know? 
					if(DatabaseHelper.recordsExist(UpdatePreferences.this)){
						if(!ServiceHelper.isMyOfflineServiceRunning(UpdatePreferences.this)){

							ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
							NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
							NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
							if(mobNetInfo!=null)
							{
								ZLogger.log("OfflinePreferences syncPreferenceChange: Mobile Connected = " + mobNetInfo.isConnected());	
								if(mobNetInfo.isConnected())
								{
									SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
									int sync_option = Integer.parseInt(prefs.getString(Prefs.KEY_syncOptions, SyncOptionsEnum.ANY_DATA_NETWORK.getString()));
									if(sync_option == SyncOptionsEnum.ANY_DATA_NETWORK.getValue()){
										Intent serviceIntent = new Intent(UpdatePreferences.this, OfflineLocationSyncService.class);
										serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.OFFLINE_SYNC_FLAG);
										startService(serviceIntent);
										return true;
									}
								}
							}
							if(wifiNetInfo!=null)
							{
								ZLogger.log("OfflinePreferences syncPreferenceChange: Wifi Connected = " + wifiNetInfo.isConnected());				
								if(wifiNetInfo.isConnected())
								{
									Intent serviceIntent = new Intent(UpdatePreferences.this, OfflineLocationSyncService.class);
									serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.OFFLINE_SYNC_FLAG);
									startService(serviceIntent);
									return true;
								}
							}

						}
					}
				}
				return true;
			}			
		});

		max_steals_interval = (ListPreference) findPreference(Prefs.KEY_max_steals_interval);
		max_steals_interval.setOnPreferenceChangeListener(new
				Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				int intervalValue = Integer.parseInt(settings.getString(Prefs.KEY_interval, Prefs.DEFAULT_interval));  
				boolean realtimeRunning = settings.getBoolean(PersistedData.KEY_realtimeRunning, false);
				boolean isWifiModeRunning = settings.getBoolean(PersistedData.KEY_wifiModeRunning, false);
				if(realtimeRunning)
				{
					intervalValue = Integer.parseInt(settings.getString(Prefs.KEY_realtime_interval, Prefs.DEFAULT_realtime_interval));  
				}
				else if(isWifiModeRunning)
				{
					intervalValue = Integer.parseInt(settings.getString(Prefs.KEY_wifi_mode_interval, Prefs.DEFAULT_wifi_mode_interval));
				}
				int stealBuffer = Integer.parseInt(settings.getString(Prefs.KEY_max_steals_interval, Prefs.DEFAULT_max_steals_interval));  
				if(intervalValue <= stealBuffer){
					Toast.makeText(UpdatePreferences.this, getResources().getString(R.string.STEALS_BUFFER_RATE), Toast.LENGTH_LONG).show();
				}
				return true;
			}
		});

		min_distance = (ListPreference) findPreference(Prefs.KEY_min_distance);
		min_distance.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				resync_interval.setEnabled((Integer.parseInt(String.valueOf(newValue)) > 0));	
				return true;
			}
		});
	}


	@Override
	public void onStart()
	{
		super.onStart();

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		int INTERVAL = Integer.parseInt(settings.getString(Prefs.KEY_interval, Prefs.DEFAULT_interval)); 
		int MIN_DISTANCE = Integer.parseInt(settings.getString(Prefs.KEY_min_distance, Prefs.DEFAULT_min_distance)); 
		steals_enabled_pref.setEnabled(INTERVAL > Constants.ONE_MINUTE);
		max_steals_interval.setEnabled(INTERVAL > Constants.ONE_MINUTE && steals_enabled_pref.isChecked());
		resync_interval.setEnabled(MIN_DISTANCE > 0);	
	}

	@Override
	protected void onResume() 
	{
		ZLogger.log("UpdatePreferences onResume");
		super.onResume();

		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	} //end onResume

	@Override
	protected void onPause()
	{
		ZLogger.log("UpdatePreferences onPause");
		super.onPause();

		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	} //end onPause


	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//ZLogger.log("UpdatePreferences onSharedPreferenceChanged: " + key);
		if(Prefs.KEY_resync_interval.equals(key))
		{
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			boolean isEnabled = settings.getBoolean(Prefs.KEY_appEnabled, false);
			boolean isRealTimeRunning = settings.getBoolean(PersistedData.KEY_realtimeRunning, false);
			// a change in time interval wouldn't affect a real-time updating system
			if(isEnabled && !isRealTimeRunning){
				//Intent serviceIntent = new Intent(getBaseContext(), MyBackgroundService);
				//serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.SERVICE_STARTUP_FLAG);
				//startService(serviceIntent);
				ZLogger.log("UpdatePreferences onSharedPreferenceChanged: restart alarms");
				ServiceManager sm = new ServiceManager();
				sm.startAlarms(UpdatePreferences.this);
			}
		}
		else if(Prefs.KEY_steals_enabled.equals(key))
		{
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			boolean isEnabled = settings.getBoolean(Prefs.KEY_appEnabled, false);
			if(isEnabled){
				//Intent serviceIntent = new Intent(getBaseContext(), MyBackgroundService.class);
				//serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.SERVICE_STARTUP_FLAG);
				//startService(serviceIntent);
				ZLogger.log("UpdatePreferences onSharedPreferenceChanged: restart alarms");
				ServiceManager sm = new ServiceManager();
				sm.startAlarms(UpdatePreferences.this);
			}
		}
	}
}