package gaugler.backitude.activity;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.PreferenceHelper;
import gaugler.backitude.util.UpdateWidgetHelper;
import gaugler.backitude.util.ZLogger;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	private boolean isEnabled = false;
	private boolean isNetworkLocEnabled = false;
	private boolean isGpsLocEnabled = false;
	private boolean isWiFiLocEnabled = false;
	private boolean gpsPollingAllowed = true;
	private boolean wifiPollingAllowed = true;
	private boolean networkPollingAllowed = true;

	CheckBoxPreference app_enabled;
	Preference viewExtras_prefs;
	Preference settings_prefs;
	
	Preference sys_settings_prefs;
	Preference donateTo_prefs;
	Preference faq_prefs;
	Preference about_prefs;

	static final int DIALOG_ABOUT = 1;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.main);

		// SETUP ONCLICK EVENT HANDLERS
		app_enabled = (CheckBoxPreference) findPreference(Prefs.KEY_appEnabled);
		app_enabled.setOnPreferenceChangeListener(new
				Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference oldPreference, Object newValue) {
				if(((CheckBoxPreference)oldPreference).isChecked())
				{
					Toast.makeText(MainActivity.this, getResources().getString(R.string.APP_STOPPED), Toast.LENGTH_SHORT).show();
					app_enabled.setSummary(R.string.appEnabled_summary);
				}
				else
				{
					app_enabled.setSummary(R.string.appEnabled_summary_enabled);
				}
				ZLogger.log("MainActivity onPreferenceChange: App Enabled toggled - Update On/Off Widget: " + !((CheckBoxPreference)oldPreference).isChecked());
				UpdateWidgetHelper.updateOnOffWidget(!((CheckBoxPreference)oldPreference).isChecked(), MainActivity.this);
				return true;
			}
		});

		settings_prefs = (Preference) findPreference(Constants.EXTRAS_LAUNCH);
		settings_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent extrasActivity = new Intent(MainActivity.this, ExtrasActivity.class);
				startActivity(extrasActivity);
				return true;
			}

		});

		settings_prefs = (Preference) findPreference(Constants.SETTINGS_LAUNCH);
		settings_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent settingsActivity = new Intent(MainActivity.this, Preferences.class);
				startActivity(settingsActivity);
				return true;
			}
		});

		sys_settings_prefs = (Preference) findPreference(Constants.ADVANCED_SETTINGS_LAUNCH);
		sys_settings_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent sysSettingsActivity = new Intent(MainActivity.this, SystemPreferences.class);
				startActivity(sysSettingsActivity);
				return true;
			}
		});

		faq_prefs = (Preference) findPreference(Constants.FAQ_LAUNCH);
		faq_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FAQ_URL));
				startActivity(browserIntent);
				return true;
			}

		});

		donateTo_prefs = (Preference) findPreference(Constants.DONATE_TO_LAUNCH);
		donateTo_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.donateTo_link)));
				startActivity(browserIntent);
				return true;
			}

		});

		about_prefs = (Preference) findPreference(Constants.ABOUT_LAUNCH);
		about_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				showDialog(DIALOG_ABOUT);
				return true;
			}

		});

	}

	@Override
	protected void onStart()
	{
		super.onStart();

		getPrefs();

		if(isGpsLocEnabled||isNetworkLocEnabled||isWiFiLocEnabled){
			ZLogger.log("MainActivity onStart: app enabled: " + isEnabled);
			app_enabled.setEnabled(true);
			app_enabled.setChecked(isEnabled);
			if(app_enabled.isChecked())
			{
				app_enabled.setSummary(R.string.appEnabled_summary_enabled);
			}
			else
			{
				app_enabled.setSummary(R.string.appEnabled_summary);
			}
		}
		else
		{
			if(app_enabled.isChecked())
			{      
				ZLogger.log("MainActivity onStart: app is enabled but it can't function so we'll turn it off.");
				ServiceManager sm = new ServiceManager();
				sm.stopAll(this);

				UpdateWidgetHelper.updateOnOffWidget(false, this);
			}
			isEnabled = false;
			app_enabled.setSummary(R.string.appEnabled_summary);
			app_enabled.setEnabled(false);
			app_enabled.setChecked(false);
		}		
		
		settings_prefs.setEnabled(isGpsLocEnabled||isNetworkLocEnabled||isWiFiLocEnabled);

	}

	@Override
	public void onRestart()
	{
		ZLogger.log("MainActivity onRestart");
		super.onRestart();
		
	}

	@Override
	protected void onResume() 
	{
		ZLogger.log("MainActivity onResume");
	    super.onResume();

	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	} 

	@Override
	protected void onPause()
	{
		ZLogger.log("MainActivity onPause");
	    super.onPause();

	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

	}
	
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		//ZLogger.log("MainActivity onSharedPreferenceChanged: " + key);
		if(Prefs.KEY_appEnabled.equals(key))
		{
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			isEnabled = settings.getBoolean(Prefs.KEY_appEnabled, false);
			ServiceManager sm = new ServiceManager();
			if(isEnabled){
				ZLogger.log("MainActivity onSharedPreferenceChanged: restart alarms");
				sm.enableApp(MainActivity.this);
			}
			else
			{ 
				ZLogger.log("MainActivity onSharedPreferenceChanged: stop alarms");
				sm.disableApp(MainActivity.this);
			}
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_choice1:
			Intent accountActivity = new Intent(MainActivity.this, AccountActivity.class);
			startActivity(accountActivity);
			return true;
		case R.id.exit:
			moveTaskToBack(true);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_ABOUT:
			return launchAboutDialog();
		default:
			return null;
		}
	}

	private void getPrefs()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		isEnabled = settings.getBoolean(Prefs.KEY_appEnabled, false);
		
		gpsPollingAllowed = PreferenceHelper.isGpsPollingAllowed(settings);
		wifiPollingAllowed = PreferenceHelper.isWiFiPollingAllowed(settings);
		networkPollingAllowed = PreferenceHelper.isNetworkPollingAllowed(settings);

		ContentResolver contentResolver = getContentResolver();
		isGpsLocEnabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER) && gpsPollingAllowed;

		isNetworkLocEnabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER) && networkPollingAllowed;

		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		isWiFiLocEnabled = (Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER)) &&
				wifi.isWifiEnabled() && wifiPollingAllowed;	
	}

	private Dialog launchAboutDialog()
	{
		Dialog dialog = new Dialog(MainActivity.this);

		dialog.setContentView(R.layout.about);
		dialog.setTitle(R.string.about_summary);
		dialog.setCancelable(true);

		return dialog;
	}
}