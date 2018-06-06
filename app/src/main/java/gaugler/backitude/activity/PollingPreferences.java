package gaugler.backitude.activity;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.ZLogger;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PollingPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	ListPreference poll_interval;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.polling_preferences);

		poll_interval = (ListPreference) findPreference(Prefs.KEY_interval);
		poll_interval.setOnPreferenceChangeListener(new
				Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				// Re-Sync intervals are only applicable when time interval is greater than 1 minute
				if(newValue!=null && !newValue.toString().equals("")) {
					int new_time_interval = 0;
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					boolean isStealsEnabled = prefs.getBoolean(Prefs.KEY_steals_enabled, false);
					boolean isRealTimeUpdatingEnabled = prefs.getBoolean(Prefs.KEY_realtime, false);
					int resync_value = Integer.parseInt(prefs.getString(Prefs.KEY_resync_interval, Prefs.DEFAULT_update_interval));  
					try{
						new_time_interval = Integer.parseInt(newValue.toString());	
					}
					catch(NumberFormatException ex) {}
							
					if(new_time_interval > Constants.ONE_MINUTE)
					{
						//  No interval polling, ReSync still possible
						if(new_time_interval==Constants.NO_POLLING_INTERVAL)
						{
							if(isStealsEnabled && resync_value > 0){
								Toast.makeText(getBaseContext(), getResources().getString(R.string.STEALS_ONLY_MESSAGE_1), Toast.LENGTH_LONG).show();
								Toast.makeText(getBaseContext(), getResources().getString(R.string.STEALS_ONLY_MESSAGE_3), Toast.LENGTH_LONG).show();
							}
							else if(isStealsEnabled){
								Toast.makeText(getBaseContext(), getResources().getString(R.string.STEALS_ONLY_MESSAGE_2), Toast.LENGTH_LONG).show();
								Toast.makeText(getBaseContext(), getResources().getString(R.string.STEALS_ONLY_MESSAGE_3), Toast.LENGTH_LONG).show();
							}
							else if(resync_value > 0)
							{
								Toast.makeText(getBaseContext(), getResources().getString(R.string.STEALS_ONLY_MESSAGE_4), Toast.LENGTH_LONG).show();
								Toast.makeText(getBaseContext(), getResources().getString(R.string.STEALS_ONLY_MESSAGE_5), Toast.LENGTH_LONG).show();
							}
							else if(isRealTimeUpdatingEnabled){
									Toast.makeText(getBaseContext(), getResources().getString(R.string.BAD_CONFIG), Toast.LENGTH_LONG).show();
							}
						}
					}
				}
				return true;
			}
		});

	}
	
	@Override
	public void onStart()
	{
		super.onStart();


	}

	@Override
	protected void onResume() 
	{
		ZLogger.log("PollingPreferences onResume");
		super.onResume();

		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	} //end onResume

	@Override
	protected void onPause()
	{
		ZLogger.log("PollingPreferences onPause");
		super.onPause();

		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	} //end onPause


	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//ZLogger.log("PollingPreferences onSharedPreferenceChanged: " + key);
		
		if(Prefs.KEY_interval.equals(key))
		{
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			boolean isEnabled = settings.getBoolean(Prefs.KEY_appEnabled, false);
			boolean isRealTimeRunning = settings.getBoolean(PersistedData.KEY_realtimeRunning, false);
			boolean isWifiModeRunning = settings.getBoolean(PersistedData.KEY_wifiModeRunning, false);
			// a change in time interval wouldn't affect a real-time updating system
			if(isEnabled && !isRealTimeRunning && !isWifiModeRunning){
				ZLogger.log("PollingPreferences onSharedPreferenceChanged: restart alarms");
				ServiceManager sm = new ServiceManager();
				sm.startAlarms(PollingPreferences.this);
			}
		}
	}


}