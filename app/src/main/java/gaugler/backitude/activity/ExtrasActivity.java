package gaugler.backitude.activity;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.util.DatabaseHelper;
import gaugler.backitude.util.PreferenceHelper;
import gaugler.backitude.util.ServiceHelper;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.MyWakefulService;
import gaugler.backitude.wakeful.OfflineLocationSyncService;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

public class ExtrasActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	Preference fire_prefs;
	Preference send_fire_prefs;
	Preference send_last_update_prefs;

	Preference manualSync_pref;
	Preference clearHistory_pref;

	Preference lastUpdate_prefs;
	Preference lastError_prefs;
	Preference lastPush_prefs;

	private boolean isOfflineSyncEnabled = false;
	private boolean isNetworkLocEnabled = false;
	private boolean isGpsLocEnabled = false;
	private boolean isWiFiLocEnabled = false;
	private boolean gpsPollingAllowed = true;
	private boolean wifiPollingAllowed = true;
	private boolean networkPollingAllowed = true;

	private ProgressDialog pd;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.extras);

		fire_prefs = (Preference) findPreference(Constants.RUN_NOW_LAUNCH);
		fire_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					
				if( // Fire if....
						(prefs.getBoolean(Prefs.KEY_offlineSync, false)) ||
						(prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false) && ServiceHelper.isConnectedToWifi(getBaseContext())) ||
						(!prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false) && ServiceHelper.isNetworkAvailable(getBaseContext()))
						)
				{
					if(!ServiceHelper.isMyWakefulServiceRunning(ExtrasActivity.this)){
						Intent serviceIntent = new Intent(ExtrasActivity.this, MyWakefulService.class);
						serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.FIRE_UPDATE_FLAG);
						startService(serviceIntent);
					}
					else
					{
						ZLogger.log("ExtrasActivity onCreate: " + getResources().getString(R.string.LOC_SERVICE_RUNNING));
						Toast.makeText(ExtrasActivity.this, getResources().getString(R.string.LOC_SERVICE_RUNNING), Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
					if(prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false))
					{
						ZLogger.log("ExtrasActivity onCreate: Cannot Fire update..no wifi service at this time during wifiOnly mode");
						Toast.makeText(ExtrasActivity.this, getResources().getString(R.string.NO_FIRE_NO_WIFI), Toast.LENGTH_LONG).show();
					}
					else
					{
						ZLogger.log("ExtrasActivity onCreate: Cannot Fire update..no network service at this time");
						Toast.makeText(ExtrasActivity.this, getResources().getString(R.string.NO_FIRE_NO_NETWORK), Toast.LENGTH_SHORT).show();
					}
				}
				return true;
			}
		});

		send_fire_prefs = (Preference) findPreference(Constants.SEND_FIRE_REQUEST);
		send_fire_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				try{
					//Intent sendIntent = new Intent(Intent.ACTION_VIEW);
					//sendIntent.setData(Uri.parse("sms"));
					//sendIntent.putExtra("sms_body", getResources().getString(R.string.FORCE_BACKITUDE_UPDATE)); 
					//sendIntent.setType("vnd.android-dir/mms-sms");
					String uri= "smsto:";
		            Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
		            sendIntent.putExtra("sms_body", getResources().getString(R.string.FORCE_BACKITUDE_UPDATE));
		            sendIntent.putExtra("compose_mode", true);

					startActivity(sendIntent);
				}
				catch(Exception ex){
					ZLogger.logException("ExtrasActivity", new Exception(getResources().getString(R.string.SMS_REQUIRED)), ExtrasActivity.this);
				}
				return true;
			}
		});
		
		send_last_update_prefs = (Preference) findPreference(Constants.SEND_LAST_UPDATE);
		send_last_update_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				try{       
					if(PreferenceHelper.hasLastPolledLocation(ExtrasActivity.this))
					{
		                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ExtrasActivity.this); 
		        		double lastPolledLocationLatitude = ((double)settings.getFloat(PersistedData.KEY_polledLocation_lat, 0));
		        		double lastPolledLocationLongitude = ((double)settings.getFloat(PersistedData.KEY_polledLocation_long, 0));
		                Intent i = new Intent(Intent.ACTION_SEND);
		                i.setType("text/plain");
		                i.putExtra(Intent.EXTRA_SUBJECT, settings.getString("message_subject", getString(R.string.SHARE_LOCATION_SUBJECT)));
		                i.putExtra(Intent.EXTRA_TEXT, settings.getString("message_body", getString(R.string.SHARE_LOCATION_BODY)) 
		                		+ " http://maps.google.com/maps?q=loc:"
		                        + lastPolledLocationLatitude + "," + lastPolledLocationLongitude
		                        + " " + getString(R.string.SHARE_LOCATION_BODY_2));
		                try {
		                    startActivity(Intent.createChooser(i, getString(R.string.SHARE_TITLE)));
		                } catch (android.content.ActivityNotFoundException ex) {
		                    Toast.makeText(ExtrasActivity.this, getString(R.string.NO_WAY_TO_SHARE), Toast.LENGTH_SHORT).show();
		                }
					}
					else
					{
						Toast.makeText(ExtrasActivity.this, getString(R.string.NO_LOCATION_TO_SHARE), Toast.LENGTH_SHORT).show();
					}
	            }
				catch(Exception ex){
					ZLogger.logException("ExtrasActivity", new Exception(getResources().getString(R.string.SMS_REQUIRED)), ExtrasActivity.this);
				}
				return true;
			}
		});

		manualSync_pref = (Preference) findPreference(Constants.MANUAL_OFFLINE_SYNC);
		manualSync_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				if(ServiceHelper.isNetworkAvailable(ExtrasActivity.this)){
					if(!ServiceHelper.isMyOfflineServiceRunning(ExtrasActivity.this)){
						if(DatabaseHelper.recordsExist(ExtrasActivity.this)){
							pd = ProgressDialog.show(ExtrasActivity.this, getResources().getString(R.string.PERFORMING_SYNC_MSG), getResources().getString(R.string.SYNC_DIALOG_MSG), true, true);
							Intent serviceIntent = new Intent(ExtrasActivity.this, OfflineLocationSyncService.class);
							serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.OFFLINE_SYNC_FLAG);
							startService(serviceIntent);
						}
						else
						{
							updatePreferenceLabels(false);
						}
					}
					else
					{
						ZLogger.log("ExtrasActivity onCreate: " + getResources().getString(R.string.LOC_SERVICE_RUNNING));
						Toast.makeText(ExtrasActivity.this, getResources().getString(R.string.LOC_SERVICE_RUNNING), Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
					ZLogger.log("ExtrasActivity onCreate: Cannot Fire update..no network service at this time");
					Toast.makeText(ExtrasActivity.this, getResources().getString(R.string.NO_UPDATE_NO_NETWORK), Toast.LENGTH_SHORT).show();
				}
				return true;
			}

		});

		clearHistory_pref = (Preference) findPreference(Constants.CLEAR_OFFLINE_STORAGE);
		clearHistory_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {

				AlertDialog.Builder builder = new AlertDialog.Builder(ExtrasActivity.this);
				builder.setMessage(getResources().getString(R.string.ARE_YOU_SURE))
				       .setCancelable(false)
				       .setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
								SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
								String accountName = settings.getString(Prefs.KEY_accountName, "");
								SQLiteDatabase sampleDB = null;
								try{
									sampleDB =  openOrCreateDatabase(Constants.OFFLINE_LOCATION_DB, MODE_PRIVATE, null);
									if(DatabaseHelper.isTableExists(ExtrasActivity.this, sampleDB, Constants.OFFLINE_LOCATION_TABLE)){
										sampleDB.execSQL("DELETE FROM " + Constants.OFFLINE_LOCATION_TABLE + " WHERE account = '" + accountName + "'");
									}
									sampleDB.close(); 
								}
								catch(Exception ex){
									ZLogger.logException("ExtrasActivity", new Exception(getResources().getString(R.string.DATABASE_ERROR)), ExtrasActivity.this);	
									if(sampleDB!=null) { sampleDB.close();}
								}
								updatePreferenceLabels(DatabaseHelper.recordsExist(ExtrasActivity.this));  
				           }
				       })
				       .setNegativeButton(getResources().getString(R.string.NO), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}

		});

		lastUpdate_prefs = (Preference) findPreference(Constants.LAST_UPDATE_LAUNCH);
		lastUpdate_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent lastUpdateActivity = new Intent(ExtrasActivity.this, LastUpdateActivity.class);
				startActivity(lastUpdateActivity);
				return true;
			}

		});

		lastError_prefs = (Preference) findPreference(Constants.LAST_ERROR_LAUNCH);
		lastError_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent lastErrorActivity = new Intent(ExtrasActivity.this, LastErrorActivity.class);
				startActivity(lastErrorActivity);
				return true;
			}

		});

		lastPush_prefs = (Preference) findPreference(Constants.LAST_PUSH_LAUNCH);
		lastPush_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent lastPushActivity = new Intent(ExtrasActivity.this, LastPushActivity.class);
				startActivity(lastPushActivity);
				return true;
			}

		});
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getPrefs();
		fire_prefs.setEnabled(isGpsLocEnabled||isNetworkLocEnabled||isWiFiLocEnabled);
		updatePreferenceLabels(DatabaseHelper.recordsExist(ExtrasActivity.this));
	}

	@Override
	protected void onResume() 
	{
		ZLogger.log("ExtrasActivity onResume: method start");
		super.onResume();

		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
		if(ServiceHelper.isMyOfflineServiceRunning(ExtrasActivity.this))
		{
			pd = ProgressDialog.show(ExtrasActivity.this, getResources().getString(R.string.PERFORMING_SYNC_MSG), getResources().getString(R.string.SYNC_DIALOG_MSG), true, true);
		}
		
	} //end onResume

	@Override
	protected void onPause()
	{
		ZLogger.log("ExtrasActivity onPause: method start");
		super.onPause();

		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		
		if(pd!=null)
		{
			pd.dismiss();
		}

	} //end onPause


	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//ZLogger.log("ExtrasActivity onSharedPreferenceChanged: " + key);
		if(Prefs.KEY_offlineSync_flag.equals(key))
		{
			if(pd!=null)
			{
				pd.dismiss();
			}
			updatePreferenceLabels(DatabaseHelper.recordsExist(ExtrasActivity.this));
		}
	}

	private void updatePreferenceLabels(boolean _recordsExist) {

		ZLogger.log("ExtrasActivity updatePreferenceLabels: records exist? " + _recordsExist);
		
		manualSync_pref.setEnabled(_recordsExist);
		clearHistory_pref.setEnabled(_recordsExist);
		if(_recordsExist)
		{
			manualSync_pref.setSummary(R.string.offlineSync_sync_summary1);
		}
		else
		{
			if(isOfflineSyncEnabled)
			{
				manualSync_pref.setSummary(R.string.offlineSync_sync_summary2);
			}
			else
			{
				manualSync_pref.setSummary(R.string.offlineSync_sync_summary);
			}
		}
	}

	private void getPrefs()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		isOfflineSyncEnabled = settings.getBoolean(Prefs.KEY_offlineSync, false);

		gpsPollingAllowed = PreferenceHelper.isGpsPollingAllowed(settings);
		wifiPollingAllowed = PreferenceHelper.isWiFiPollingAllowed(settings);
		networkPollingAllowed = PreferenceHelper.isNetworkPollingAllowed(settings);

		ContentResolver contentResolver = getContentResolver();
		isGpsLocEnabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER) && gpsPollingAllowed;

		isNetworkLocEnabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER) && networkPollingAllowed;

		//WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		isWiFiLocEnabled = (Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER)) &&
		(mWifi.isAvailable() || mWifi.isConnectedOrConnecting() || mWifi.isRoaming()) && wifiPollingAllowed;
		//		wifi.isWifiEnabled() && wifiPollingAllowed;
	}
}