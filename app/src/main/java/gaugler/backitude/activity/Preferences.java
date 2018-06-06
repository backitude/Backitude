package gaugler.backitude.activity;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.util.DatabaseHelper;
import gaugler.backitude.util.StatusBarHelper;
import gaugler.backitude.util.ZLogger;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

public class Preferences extends PreferenceActivity {

	Preference polling_settings_pref;
	Preference realtime_settings_pref;
	Preference wifi_mode_settings_pref;

	Preference update_settings_pref;
	Preference server_settings_pref;
	Preference accuracy_settings_pref;
	Preference push_settings_pref;

	Preference offline_settings_pref;
	Preference account_pref;
	ListPreference statusBar_pref;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);

		update_settings_pref = (Preference) findPreference(Constants.UPDATE_SETTINGS_LAUNCH);
		update_settings_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent updatePrefsActivity = new Intent(Preferences.this, UpdatePreferences.class);
				startActivity(updatePrefsActivity);
				return true;
			}

		});
		
		server_settings_pref = (Preference) findPreference(Constants.SERVER_SETTINGS_LAUNCH);
		server_settings_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent settingsActivity = new Intent(Preferences.this, ServerPreferences.class);
				startActivity(settingsActivity);
				return true;
			}
			
		});
		
		accuracy_settings_pref = (Preference) findPreference(Constants.ACCURACY_SETTINGS_LAUNCH);
		accuracy_settings_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent accuracyActivity = new Intent(Preferences.this, AccuracyPreferences.class);
				startActivity(accuracyActivity);
				return true;
			}

		});

		polling_settings_pref = (Preference) findPreference(Constants.POLLING_SETTINGS_LAUNCH);
		polling_settings_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent pollingActivity = new Intent(Preferences.this, PollingPreferences.class);
				startActivity(pollingActivity);
				return true;
			}

		});

		realtime_settings_pref = (Preference) findPreference(Constants.REALTIME_SETTINGS_LAUNCH);
		realtime_settings_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent realtimeActivity = new Intent(Preferences.this, RealtimePreferences.class);
				startActivity(realtimeActivity);
				return true;
			}

		});
		
		wifi_mode_settings_pref = (Preference) findPreference(Constants.WIFI_MODE_SETTINGS_LAUNCH);
		wifi_mode_settings_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent wifi_modeActivity = new Intent(Preferences.this, WifiModePreferences.class);
				startActivity(wifi_modeActivity);
				return true;
			}

		});

		push_settings_pref = (Preference) findPreference(Constants.PUSH_SETTINGS_LAUNCH);
		push_settings_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent pushPrefsActivity = new Intent(Preferences.this, PushUpdatePreferences.class);
				startActivity(pushPrefsActivity);
				return true;
			}

		});
		
		offline_settings_pref = (Preference) findPreference(Constants.OFFLINE_SETTINGS_LAUNCH);
		offline_settings_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				// Create database if does not exist, create table if does not exist
				SQLiteDatabase sampleDB = null;
				try{
					sampleDB =  openOrCreateDatabase(Constants.OFFLINE_LOCATION_DB, MODE_PRIVATE, null);
					if(DatabaseHelper.isTableExists(Preferences.this, sampleDB, Constants.OFFLINE_LOCATION_TABLE)){
						sampleDB.execSQL("CREATE TABLE IF NOT EXISTS " +                        
								Constants.OFFLINE_LOCATION_TABLE +                       
								" (id INTEGER PRIMARY KEY, account TEXT, latitude REAL, longitude REAL, accuracy REAL," +                        
								" speed REAL, altitude REAL, bearing REAL, TimestampMs INTEGER, PollingTimestampMs INTEGER " +		
								" );");
					}
					sampleDB.close();
				}
				catch(Exception ex){
					ZLogger.logException("Preferences", new Exception(getResources().getString(R.string.DATABASE_ERROR)), Preferences.this);	
					if(sampleDB!=null) { sampleDB.close();}
				}
				Intent settingsActivity = new Intent(Preferences.this, OfflinePreferences.class);
				startActivity(settingsActivity);
				return true;
			}
		});

		account_pref = (Preference) findPreference(Prefs.KEY_accountName);
		account_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent accountActivity = new Intent(Preferences.this, AccountActivity.class);
				startActivity(accountActivity);
				return true;
			}
		});
		
		statusBar_pref = (ListPreference) findPreference(Prefs.KEY_statusBar);
		statusBar_pref.setOnPreferenceChangeListener(new
				Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				boolean isEnabled = settings.getBoolean(Prefs.KEY_appEnabled, false);
				boolean isRealTimeRunning = settings.getBoolean(PersistedData.KEY_realtimeRunning, false);
				// a change in refresh interval wouldn't affect a real-time updating system
				if(isEnabled)
				{
					if(!newValue.toString().equals("")) {
						int new_statusbar_preference = Integer.parseInt(newValue.toString());					
						if(new_statusbar_preference >= 0)
						{
							switch(new_statusbar_preference)
							{
							case 2: //StatusBarOptionsEnum.DISPLAY_POLLING.getValue()
								StatusBarHelper.cancelRealTimeEnabledNotfication(getBaseContext());
								StatusBarHelper.cancelAppEnabledNotfication(getBaseContext());
								break;
							case 3: //StatusBarOptionsEnum.DISPLAY_ENABLED.getValue()
								StatusBarHelper.cancelPollingNotfication(getBaseContext());
								StatusBarHelper.cancelRealTimeEnabledNotfication(getBaseContext());
								ZLogger.log("Preferences: app enabled notification bar");
								StatusBarHelper.createAppEnabledNotification(getBaseContext());
								break;
							case 4: //StatusBarOptionsEnum.DISPLAY_REALTIME.getValue()
								StatusBarHelper.cancelPollingNotfication(getBaseContext());
								StatusBarHelper.cancelAppEnabledNotfication(getBaseContext());
								if(isRealTimeRunning){
									StatusBarHelper.createRealTimeEnabledNotification(getBaseContext());
								}
								else
								{
									StatusBarHelper.cancelRealTimeEnabledNotfication(getBaseContext());
								}
								break;
							case 5: //StatusBarOptionsEnum.DISPLAY_POLLING_REALTIME.getValue()
								StatusBarHelper.cancelAppEnabledNotfication(getBaseContext());
								if(isRealTimeRunning){
									StatusBarHelper.createRealTimeEnabledNotification(getBaseContext());
								}
								else
								{
									StatusBarHelper.cancelRealTimeEnabledNotfication(getBaseContext());
								}
								break;
							default:
								StatusBarHelper.cancelPollingNotfication(getBaseContext());
								StatusBarHelper.cancelRealTimeEnabledNotfication(getBaseContext());
								StatusBarHelper.cancelAppEnabledNotfication(getBaseContext());
								break;
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
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String accountName = settings.getString(Prefs.KEY_accountName, Prefs.DEFAULT_accountName);
		if(accountName.length()>0){
			account_pref.setSummary(getResources().getString(R.string.CURRENT_ACCOUNT_SELECTED) + " " + accountName);
		}
		else
		{
			account_pref.setSummary(getResources().getString(R.string.NO_CURRENT_ACCOUNT_SELECTED));
		}

	}

	private static final int MENU_ADV_OPTIONS = 0;
	private static final int MENU_BACK = 1;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ADV_OPTIONS, 0, getResources().getString(R.string.preferences_menu_choice1));
		menu.add(0, MENU_BACK, 0, getResources().getString(R.string.preferences_menu_choice2));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADV_OPTIONS:
			Intent advancedOptionsActivity = new Intent(getBaseContext(), SystemPreferences.class);
			startActivity(advancedOptionsActivity);
			return true;
		case MENU_BACK:
			finish();
			return true;
		}
		return false;
	}
}