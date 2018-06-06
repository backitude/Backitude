package gaugler.backitude.activity;

import gaugler.backitude.R;
import gaugler.backitude.constants.AuthenticationOptionsEnum;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.util.ServiceHelper;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.MyWakefulService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ServerPreferences extends PreferenceActivity {

	Preference test_connection_prefs;
	
	ListPreference authentication_pref;
	
	EditTextPreference server_user_name;
	EditTextPreference server_password;
	EditTextPreference server_uid;
	
	EditTextPreference server_key_username;
	EditTextPreference server_key_password;
	EditTextPreference server_key_latitude;
	EditTextPreference server_key_longitude;
	EditTextPreference server_key_loc_timestamp;
	EditTextPreference server_key_req_timestamp;
	EditTextPreference server_key_accuracy;
	EditTextPreference server_key_speed;
	EditTextPreference server_key_altitude;
	EditTextPreference server_key_bearing;
	EditTextPreference server_key_uid;
	EditTextPreference server_key_account;

	private String usernameVal = Prefs.DEFAULT_server_key_username;
	private String passwordVal = Prefs.DEFAULT_server_key_password;
	private String latitudeVal = Prefs.DEFAULT_server_key_latitude;
	private String longitudeVal = Prefs.DEFAULT_server_key_longitude;
	private String timestampVal = Prefs.DEFAULT_server_key_loc_timestamp;
	private String req_timestampVal = Prefs.DEFAULT_server_key_req_timestamp;
	private String accuracyVal = "";
	private String speedVal = "";
	private String altitudeVal = "";
	private String bearingVal = "";
	private String uidVal = "";
	private String accountVal = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.server_preferences);

		server_user_name = (EditTextPreference) findPreference(Prefs.KEY_server_user_name);
		server_password = (EditTextPreference) findPreference(Prefs.KEY_server_password);
		
		server_uid = (EditTextPreference) findPreference(Prefs.KEY_server_uid);
		server_uid.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				server_key_uid.setEnabled(!newValue.toString().equals(""));
				if(server_key_uid.getText().length() == 0 && !newValue.toString().equals("")) {
					Toast.makeText(getBaseContext(), getResources().getString(R.string.NO_UID_KEY), Toast.LENGTH_SHORT).show();
				}
				return true;
			}

		});

		authentication_pref = (ListPreference) findPreference(Prefs.KEY_authentication);
		authentication_pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				// Re-Sync intervals are only applicable when time interval is greater than 1 minute
				if(newValue!=null && !newValue.toString().equals("")) {
					server_user_name.setEnabled(!newValue.toString().equals(AuthenticationOptionsEnum.NONE.getString()));
					server_password.setEnabled(!newValue.toString().equals(AuthenticationOptionsEnum.NONE.getString()));	
					server_key_username.setEnabled(!newValue.toString().equals(AuthenticationOptionsEnum.NONE.getString())
							&& !newValue.toString().equals(AuthenticationOptionsEnum.BASIC_AUTH.getString()));
					server_key_password.setEnabled(!newValue.toString().equals(AuthenticationOptionsEnum.NONE.getString())
							&& !newValue.toString().equals(AuthenticationOptionsEnum.BASIC_AUTH.getString()));
				}
				return true;
			}
		});


		server_key_username = (EditTextPreference) findPreference(Prefs.KEY_server_key_username);
		server_key_username.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				server_key_username.setSummary(String.valueOf(newValue));
				return true;
			}

		});


		server_key_password = (EditTextPreference) findPreference(Prefs.KEY_server_key_password);
		server_key_password.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				server_key_password.setSummary(String.valueOf(newValue));
				return true;
			}

		});

		server_key_latitude = (EditTextPreference) findPreference(Prefs.KEY_server_key_latitude);
		server_key_latitude.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				server_key_latitude.setSummary(String.valueOf(newValue));
				return true;
			}

		});

		server_key_longitude = (EditTextPreference) findPreference(Prefs.KEY_server_key_longitude);
		server_key_longitude.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				server_key_longitude.setSummary(String.valueOf(newValue));
				return true;
			}

		});
		
		server_key_loc_timestamp = (EditTextPreference) findPreference(Prefs.KEY_server_key_loc_timestamp);
		server_key_loc_timestamp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				server_key_loc_timestamp.setSummary(String.valueOf(newValue));
				return true;
			}

		});
		
		server_key_req_timestamp = (EditTextPreference) findPreference(Prefs.KEY_server_key_req_timestamp);
		server_key_req_timestamp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				server_key_req_timestamp.setSummary(String.valueOf(newValue));
				return true;
			}

		});

		server_key_accuracy = (EditTextPreference) findPreference(Prefs.KEY_server_key_accuracy);
		server_key_accuracy.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(String.valueOf(newValue).length()>0){
					server_key_accuracy.setSummary(String.valueOf(newValue));
				} else {
					server_key_accuracy.setSummary(getResources().getString(R.string.PARAMETER_DISABLED));
				}
				return true;
			}

		});

		server_key_speed = (EditTextPreference) findPreference(Prefs.KEY_server_key_speed);
		server_key_speed.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(String.valueOf(newValue).length()>0){
					server_key_speed.setSummary(String.valueOf(newValue));
				} else {
					server_key_speed.setSummary(getResources().getString(R.string.PARAMETER_DISABLED));
				}
				return true;
			}

		});

		server_key_altitude = (EditTextPreference) findPreference(Prefs.KEY_server_key_altitude);
		server_key_altitude.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(String.valueOf(newValue).length()>0){
					server_key_altitude.setSummary(String.valueOf(newValue));
				} else {
					server_key_altitude.setSummary(getResources().getString(R.string.PARAMETER_DISABLED));
				}
				return true;
			}

		});
		
		server_key_bearing = (EditTextPreference) findPreference(Prefs.KEY_server_key_bearing);
		server_key_bearing.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(String.valueOf(newValue).length()>0){
					server_key_bearing.setSummary(String.valueOf(newValue));
				} else {
					server_key_bearing.setSummary(getResources().getString(R.string.PARAMETER_DISABLED));
				}
				return true;
			}

		});

		server_key_uid = (EditTextPreference) findPreference(Prefs.KEY_server_key_uid);
		server_key_uid.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(String.valueOf(newValue).length()>0){
					server_key_uid.setSummary(String.valueOf(newValue));
				} else {
					server_key_uid.setSummary(getResources().getString(R.string.PARAMETER_DISABLED));
				}
				return true;
			}

		});
		
		server_key_account = (EditTextPreference) findPreference(Prefs.KEY_server_key_account);
		server_key_account.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(String.valueOf(newValue).length()>0){
					server_key_account.setSummary(String.valueOf(newValue));
				} else {
					server_key_account.setSummary(getResources().getString(R.string.PARAMETER_DISABLED));
				}
				return true;
			}

		});
		
		test_connection_prefs = (Preference) findPreference(Constants.RUN_NOW_LAUNCH);
		test_connection_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					
				if( // Fire if....
						(prefs.getBoolean(Prefs.KEY_offlineSync, false)) ||
						(prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false) && ServiceHelper.isConnectedToWifi(getBaseContext())) ||
						(!prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false) && ServiceHelper.isNetworkAvailable(getBaseContext()))
						)
				{
					if(!ServiceHelper.isMyWakefulServiceRunning(ServerPreferences.this)){
						Intent serviceIntent = new Intent(ServerPreferences.this, MyWakefulService.class);
						serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.FIRE_UPDATE_FLAG);
						startService(serviceIntent);
					}
					else
					{
						ZLogger.log("ServerPreferences Test Connection: " + getResources().getString(R.string.LOC_SERVICE_RUNNING));
						Toast.makeText(ServerPreferences.this, getResources().getString(R.string.LOC_SERVICE_RUNNING), Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
					if(prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false))
					{
						ZLogger.log("ServerPreferences Test Connection: Cannot Fire update..no wifi service at this time during wifiOnly mode");
						Toast.makeText(ServerPreferences.this, getResources().getString(R.string.NO_FIRE_NO_WIFI), Toast.LENGTH_LONG).show();
					}
					else
					{
						ZLogger.log("ServerPreferences Test Connection: Cannot Fire update..no network service at this time");
						Toast.makeText(ServerPreferences.this, getResources().getString(R.string.NO_FIRE_NO_NETWORK), Toast.LENGTH_SHORT).show();
					}
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
		
		server_user_name.setEnabled(!authentication_pref.getValue().equals(AuthenticationOptionsEnum.NONE.getString()));
		server_password.setEnabled(!authentication_pref.getValue().equals(AuthenticationOptionsEnum.NONE.getString()));	

		server_key_username.setEnabled(!authentication_pref.getValue().equals(AuthenticationOptionsEnum.NONE.getString()) 
				&& !authentication_pref.getValue().equals(AuthenticationOptionsEnum.BASIC_AUTH.getString()));
		server_key_password.setEnabled(!authentication_pref.getValue().equals(AuthenticationOptionsEnum.NONE.getString()) 
				&& !authentication_pref.getValue().equals(AuthenticationOptionsEnum.BASIC_AUTH.getString()));
		server_key_uid.setEnabled(server_uid.getText().length()>0);
		
		server_key_username.setSummary(usernameVal);
		server_key_password.setSummary(passwordVal);		
		server_key_latitude.setSummary(latitudeVal);
		server_key_longitude.setSummary(longitudeVal);
		server_key_loc_timestamp.setSummary(timestampVal);
		server_key_req_timestamp.setSummary(req_timestampVal);
		
		if(accuracyVal.length()>0){
			server_key_accuracy.setSummary(accuracyVal);
		} else {
			server_key_accuracy.setSummary(getResources().getString(R.string.PARAMETER_DISABLED));
		}
		if(speedVal.length()>0){
			server_key_speed.setSummary(speedVal);
		} else {
			server_key_speed.setSummary(getResources().getString(R.string.PARAMETER_DISABLED));
		}
		if(altitudeVal.length()>0){
			server_key_altitude.setSummary(altitudeVal);
		} else {
			server_key_altitude.setSummary(getResources().getString(R.string.PARAMETER_DISABLED));
		}
		if(bearingVal.length()>0){
			server_key_bearing.setSummary(bearingVal);
		} else {
			server_key_bearing.setSummary(getResources().getString(R.string.PARAMETER_DISABLED));
		}
		if(uidVal.length()>0){
			server_key_uid.setSummary(uidVal);
		} else {
			server_key_uid.setSummary(getResources().getString(R.string.PARAMETER_DISABLED));
		}
		if(accountVal.length()>0){
			server_key_account.setSummary(accountVal);
		} else {
			server_key_account.setSummary(getResources().getString(R.string.PARAMETER_DISABLED));
		}
	}

	private void getPrefs()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		usernameVal = settings.getString(Prefs.KEY_server_key_username,  Prefs.DEFAULT_server_key_username);
		passwordVal = settings.getString(Prefs.KEY_server_key_password,  Prefs.DEFAULT_server_key_password);
		latitudeVal = settings.getString(Prefs.KEY_server_key_latitude,  Prefs.DEFAULT_server_key_latitude);
		longitudeVal = settings.getString(Prefs.KEY_server_key_longitude,  Prefs.DEFAULT_server_key_longitude);
		timestampVal = settings.getString(Prefs.KEY_server_key_loc_timestamp,  Prefs.DEFAULT_server_key_loc_timestamp);
		req_timestampVal = settings.getString(Prefs.KEY_server_key_req_timestamp,  Prefs.DEFAULT_server_key_req_timestamp);
		accuracyVal = settings.getString(Prefs.KEY_server_key_accuracy,  "");
		speedVal = settings.getString(Prefs.KEY_server_key_speed,  "");
		altitudeVal = settings.getString(Prefs.KEY_server_key_altitude,  "");
		bearingVal = settings.getString(Prefs.KEY_server_key_bearing,  "");
		uidVal = settings.getString(Prefs.KEY_server_key_uid,  "");
		accountVal = settings.getString(Prefs.KEY_server_key_account,  "");

	}

}