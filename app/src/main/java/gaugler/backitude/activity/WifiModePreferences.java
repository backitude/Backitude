package gaugler.backitude.activity;

import gaugler.backitude.R;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.ZLogger;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class WifiModePreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.wifi_mode_preferences);

	}
	
	@Override
	protected void onResume() 
	{
		ZLogger.log("Wi-Fi Mode Preferences onResume");
		super.onResume();

		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	} //end onResume

	@Override
	protected void onPause()
	{
		ZLogger.log("Wi-Fi Mode Preferences onPause");
		super.onPause();

		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	} //end onPause


	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//ZLogger.log("Wi-Fi Mode Preferences onSharedPreferenceChanged: " + key);
		if(Prefs.KEY_wifi_mode.equals(key))
		{
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			boolean isAppEnabled = settings.getBoolean(Prefs.KEY_appEnabled, false);
			boolean isWifiModeRunning = settings.getBoolean(PersistedData.KEY_wifiModeRunning, false);
			
			boolean isWifiConnected = false;
			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if(wifiNetInfo!=null)
			{				
				isWifiConnected = wifiNetInfo.isConnected();
			}
				
			if(isWifiModeRunning || (isAppEnabled && isWifiConnected)){
				ZLogger.log("Wi-Fi Mode Preferences onSharedPreferenceChanged: restart alarms");
				ServiceManager sm = new ServiceManager();
				sm.startAlarms(WifiModePreferences.this);
			}
		}
		else if(Prefs.KEY_wifi_mode_interval.equals(key) || Prefs.KEY_wifi_mode_timeout_interval.equals(key))
		{
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			boolean isWifiModeEnabled = settings.getBoolean(Prefs.KEY_wifi_mode, false);
			boolean isAppEnabled = settings.getBoolean(Prefs.KEY_appEnabled, false);
			boolean isWifiModeRunning = settings.getBoolean(PersistedData.KEY_wifiModeRunning, false);

			boolean isWifiConnected = false;
			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if(wifiNetInfo!=null)
			{				
				isWifiConnected = wifiNetInfo.isConnected();
			}
			
			// If it should be on or is already on....
			if((isWifiModeEnabled && isAppEnabled && isWifiConnected) || isWifiModeRunning){
				ZLogger.log("Wi-Fi Mode Preferences onSharedPreferenceChanged: restart alarms");
				ServiceManager sm = new ServiceManager();
				sm.startAlarms(WifiModePreferences.this);
			}
		}
	}
}