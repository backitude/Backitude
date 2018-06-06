package gaugler.backitude.activity;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.util.ZLogger;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.Settings;

public class SystemPreferences extends PreferenceActivity {
    //implements OnSharedPreferenceChangeListener {
	
	Preference loc_system_prefs;
	Preference wifi_connect_prefs;
	
	private boolean isGpsEnabled = false;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.system_preferences);
		
		loc_system_prefs = (Preference) findPreference(Constants.SYS_LOC_SETTINGS_LAUNCH);
		loc_system_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				return true;
			}

		});

		wifi_connect_prefs = (Preference) findPreference(Constants.WIFI_SYS_PREF);
		wifi_connect_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				return true;
			}

		});
	}

	@Override
	public void onStart()
	{
		super.onStart();

		ContentResolver contentResolver = getContentResolver();
		isGpsEnabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER);
		boolean isNetworkLocEnabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER);

		if(isGpsEnabled && isNetworkLocEnabled){
			loc_system_prefs.setSummary(R.string.GPS_NetworkLoc_on);
		}
		else {
			if(isGpsEnabled){
				loc_system_prefs.setSummary(R.string.GPS_on);
			}else{
				if(isNetworkLocEnabled){
					loc_system_prefs.setSummary(R.string.NetworkLoc_on);
				}
				else
				{
					loc_system_prefs.setSummary(R.string.GPS_NetworkLoc_off);
				}
			}
		}

		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled()){
			wifi_connect_prefs.setSummary(R.string.Wifi_sys_on);
		}
		else
		{
			wifi_connect_prefs.setSummary(R.string.Wifi_sys_off);
		}
	}
	
	@Override
	protected void onResume() 
	{
		ZLogger.log("SystemPreferences onResume");
		super.onResume();

	}

	@Override
	protected void onPause()
	{
		ZLogger.log("SystemPreferences onPause");
		super.onPause();
		
	}
	
}