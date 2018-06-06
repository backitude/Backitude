package gaugler.backitude.activity;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.constants.SyncOptionsEnum;
import gaugler.backitude.util.DatabaseHelper;
import gaugler.backitude.util.ServiceHelper;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.OfflineLocationSyncService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class OfflinePreferences extends PreferenceActivity {

	CheckBoxPreference offlineEnabled_pref;
	ListPreference syncOptions_pref;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.offline_preferences);
		
		offlineEnabled_pref = (CheckBoxPreference) findPreference(Prefs.KEY_offlineSync);
		offlineEnabled_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				if(!((CheckBoxPreference)preference).isChecked())
				{
					// Offline Storage is Enabled
					SQLiteDatabase sampleDB = null;
					try{
						// Create database if does not exist, create table if does not exist
						sampleDB =  openOrCreateDatabase(Constants.OFFLINE_LOCATION_DB, MODE_PRIVATE, null);
						sampleDB.execSQL("CREATE TABLE IF NOT EXISTS " +                        
								Constants.OFFLINE_LOCATION_TABLE +                       
								" (id INTEGER PRIMARY KEY, account TEXT, latitude REAL, longitude REAL, accuracy REAL," +                        
								" speed REAL, altitude REAL, bearing REAL, TimestampMs INTEGER, PollingTimestampMs INTEGER " +		
						" );");
						sampleDB.close();
					}
					catch(Exception ex){
						ZLogger.logException("OfflinePreferences", new Exception(getResources().getString(R.string.DATABASE_ERROR)), OfflinePreferences.this);	
						if(sampleDB!=null) { sampleDB.close();}
					}
				}
				return true;
			}
		});

		syncOptions_pref = (ListPreference) findPreference(Prefs.KEY_syncOptions);
		syncOptions_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				ZLogger.log("OfflinePreferences syncPreferenceChange: start");
				if(newValue!=null && !newValue.toString().equals("")) {
					int new_sync_option = 0;  
					try{
						new_sync_option = Integer.parseInt(newValue.toString());	
					}
					catch(NumberFormatException ex) {}
					
					if(new_sync_option == SyncOptionsEnum.ANY_DATA_NETWORK.getValue() ||
							new_sync_option == SyncOptionsEnum.WIFI_ONLY.getValue()	)
					{
						if(!ServiceHelper.isMyOfflineServiceRunning(OfflinePreferences.this)){
							if(DatabaseHelper.recordsExist(OfflinePreferences.this))
							{
								ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
								NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
								NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
								if(mobNetInfo!=null)
								{
									ZLogger.log("OfflinePreferences syncPreferenceChange: Mobile Connected = " + mobNetInfo.isConnected());	
									if(mobNetInfo.isConnected())
									{
										SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(OfflinePreferences.this);
										boolean isWiFiOnlyUpdates = prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false);
										if(new_sync_option == SyncOptionsEnum.ANY_DATA_NETWORK.getValue() && !isWiFiOnlyUpdates){
											Intent serviceIntent = new Intent(OfflinePreferences.this, OfflineLocationSyncService.class);
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
										Intent serviceIntent = new Intent(OfflinePreferences.this, OfflineLocationSyncService.class);
										serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.OFFLINE_SYNC_FLAG);
										startService(serviceIntent);
										return true;
									}
								}
							}
						}
					}
				}
				return true;
			}
		});
	}
}