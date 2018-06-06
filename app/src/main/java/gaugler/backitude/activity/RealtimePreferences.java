package gaugler.backitude.activity;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.widget.RealtimeWidget;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class RealtimePreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	CheckBoxPreference realtime_pref;
	private boolean isRealtimeEnabled = false;
	private boolean isWifiModeRunning = false;
	private boolean isCharging = false;
	private boolean isAppEnabled = false;
	private boolean isRealTimeRunning = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.realtime_preferences);

		realtime_pref = (CheckBoxPreference) findPreference(Prefs.KEY_realtime);
		realtime_pref.setOnPreferenceChangeListener(new
				Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// Update the widgets accordingly
				getPrefs();
				
				if(!((CheckBoxPreference)preference).isChecked())
				{
					// Docked-Mode is now enabled
					if((isAppEnabled && isCharging && !isWifiModeRunning)|| isRealTimeRunning){
						ZLogger.log("Realtime Preference Click: change widget to running");
						updateRealtimeWidget(Constants.REAL_TIME_WIDGET_RUNNING);
					}
					else
					{
						ZLogger.log("Realtime Preference Click: change widget to enabled");
						updateRealtimeWidget(Constants.REAL_TIME_WIDGET_ENABLED);
					}
				}
				else
				{
					ZLogger.log("Realtime Preference Click: change widget to disabled");
					updateRealtimeWidget(Constants.REAL_TIME_WIDGET_DISABLED);
				}
				return true;
			}
		});
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();

		getPrefs();		
		realtime_pref.setChecked(isRealtimeEnabled);
	}
	
	@Override
	protected void onResume() 
	{
		ZLogger.log("Realtime Preferences onResume");
		super.onResume();

		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	} //end onResume

	@Override
	protected void onPause()
	{
		ZLogger.log("Realtime Preferences onPause");
		super.onPause();

		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	} //end onPause


	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//ZLogger.log("Realtime Preferences onSharedPreferenceChanged: " + key);
		if(Prefs.KEY_realtime.equals(key))
		{
			getPrefs();
			// Restart services no matter which value because it should be on or is already on and should be off....
			if((isAppEnabled && isCharging && !isWifiModeRunning) || isRealTimeRunning){
				ZLogger.log("Realtime Preferences onSharedPreferenceChanged: restart alarms");
				ServiceManager sm = new ServiceManager();
				sm.startAlarms(RealtimePreferences.this);
			}
		}
		else if(Prefs.KEY_realtime_interval.equals(key) || Prefs.KEY_realtime_timeout_interval.equals(key))
		{
			getPrefs();
			// If it should be on or is already on....
			if((isRealtimeEnabled && isAppEnabled && isCharging && !isWifiModeRunning) || isRealTimeRunning){
				ZLogger.log("Realtime Preferences onSharedPreferenceChanged: restart alarms");
				ServiceManager sm = new ServiceManager();
				sm.startAlarms(RealtimePreferences.this);
			}
		}
	}
	
	private void getPrefs() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		isWifiModeRunning = settings.getBoolean(PersistedData.KEY_wifiModeRunning, false);
		isRealtimeEnabled = settings.getBoolean(Prefs.KEY_realtime, false);
		isCharging = settings.getBoolean(PersistedData.KEY_isCharging, false);
		isAppEnabled = settings.getBoolean(Prefs.KEY_appEnabled, false);
		isRealTimeRunning = settings.getBoolean(PersistedData.KEY_realtimeRunning, false);
	}


	private void updateRealtimeWidget(int newValue) {

		ZLogger.log("Realtime Preferences: New value for realtime option:" + newValue);
		Intent uiIntent = new Intent(this, RealtimeWidget.class);
		uiIntent.setAction(Constants.REALTIME_WIDGET_ACTION_UPDATE_WIDGET);
		uiIntent.putExtra(Constants.REALTIME_WIDGET_PARAM, newValue);
		sendBroadcast(uiIntent);	
	}
}