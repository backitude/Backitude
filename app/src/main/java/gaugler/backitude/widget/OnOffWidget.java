package gaugler.backitude.widget;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.PreferenceHelper;
import gaugler.backitude.util.ZLogger;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.RemoteViews;
import android.widget.Toast;

public class OnOffWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		// implementation will follow

		final int N = appWidgetIds.length;
		// Perform this loop procedure for each App Widget that belongs to this provider
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];

			// Get the layout for the App Widget and attach an on-click listener to the button
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.on_off_widget_layout);

			Intent intent = new Intent(context, OnOffWidget.class);
			intent.setAction(Constants.ON_OFF_WIDGET_ACTION_WIDGET_CLICKED);

			PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.on_off_widget_button, actionPendingIntent);

			if(isAppEnabled(context))
			{
				views.setImageViewResource(R.id.on_off_widget_button, R.drawable.on_off_widget_on);
			}
			else{
				views.setImageViewResource(R.id.on_off_widget_button, R.drawable.on_off_widget_off);
			}

			// Tell the AppWidgetManager to perform an update on the current App Widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

	}

	private boolean isAppEnabled(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		return settings.getBoolean(Prefs.KEY_appEnabled, false);
	}

	private boolean isAbleToBeEnabled(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		boolean gpsPollingAllowed = PreferenceHelper.isGpsPollingAllowed(settings);
		boolean networkPollingAllowed = PreferenceHelper.isNetworkPollingAllowed(settings);
		boolean wifiPollingAllowed = PreferenceHelper.isWiFiPollingAllowed(settings);

		ContentResolver contentResolver = context.getContentResolver();
		boolean isGpsLocEnabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER) && gpsPollingAllowed;

		boolean isNetworkLocEnabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER) && networkPollingAllowed;

		WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		boolean isWiFiLocEnabled = (Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER)) &&

				wifi.isWifiEnabled() && wifiPollingAllowed;	

		return (isGpsLocEnabled||isNetworkLocEnabled||isWiFiLocEnabled);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		RemoteViews remoteViews = new RemoteViews(context.getPackageName (), R.layout.on_off_widget_layout);
		boolean isEnabled = false;
		if(intent!=null) {
			Bundle extras = intent.getExtras();
			if(extras!=null) {
				isEnabled = extras.getBoolean(Constants.ON_OFF_WIDGET_PARAM, false);
				////ZLogger.log("On / Off Widget extra: " + isEnabled);
			}

			if (intent.getAction()!=null)
			{
				if(intent.getAction().equals(Constants.ON_OFF_WIDGET_ACTION_UPDATE_WIDGET)) {
					////ZLogger.log("App is telling widget- set it to: " + isEnabled);

					Intent buttonClickIntent = new Intent(context, OnOffWidget.class);
					buttonClickIntent.setAction(Constants.ON_OFF_WIDGET_ACTION_WIDGET_CLICKED);

					PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, buttonClickIntent, 0);
					remoteViews.setOnClickPendingIntent(R.id.on_off_widget_button, actionPendingIntent);

					if(isEnabled)
					{
						remoteViews.setImageViewResource(R.id.on_off_widget_button, R.drawable.on_off_widget_on);
					}
					else{
						remoteViews.setImageViewResource(R.id.on_off_widget_button, R.drawable.on_off_widget_off);
					}
					ComponentName cn = new ComponentName(context, OnOffWidget.class);
					AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);
				}
				else
				{
					if(intent.getAction().equals(Constants.ON_OFF_WIDGET_ACTION_WIDGET_CLICKED))
					{
						remoteViews.setImageViewResource(R.id.on_off_widget_button, R.drawable.on_off_widget_keypress);
						ComponentName cn = new ComponentName(context, OnOffWidget.class);
						AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);
						toggleService(context);
					}
				}
			}
		}
	}

	private void toggleService(Context context) {

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		boolean wasEnabled = settings.getBoolean(Prefs.KEY_appEnabled, false);
		SharedPreferences.Editor editor = settings.edit();

		ZLogger.log("OnOffWidget toggleService: wasEnabled? " + wasEnabled);
		// Get the layout for the App Widget and attach an on-click listener to the button
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.on_off_widget_layout);
		if(!wasEnabled && isAbleToBeEnabled(context))
		{
			editor.putBoolean(Prefs.KEY_appEnabled, true);
			editor.commit();
			views.setImageViewResource(R.id.on_off_widget_button, R.drawable.on_off_widget_on);

			// Create an Intent to launch ExampleActivity
			//Intent serviceIntent = new Intent(context, MyBackgroundService.class);
			//serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.SERVICE_STARTUP_FLAG);
			//getBaseContext().startService(serviceIntent);
			ServiceManager appStarter = new ServiceManager();
			appStarter.enableApp(context.getApplicationContext());

		}
		else
		{
			views.setImageViewResource(R.id.on_off_widget_button, R.drawable.on_off_widget_off);
			if(wasEnabled){
				editor.putBoolean(Prefs.KEY_appEnabled, false);
				editor.commit();

				// Create an Intent to launch ExampleActivity
				//Intent serviceIntent = new Intent(context, MyBackgroundService.class);
				//context.stopService(serviceIntent);
				ServiceManager appStopper = new ServiceManager();
				appStopper.disableApp(context);
			}
			else
			{
				Toast.makeText(context, context.getResources().getString(R.string.NO_ENABLE_NO_PROVIDER), Toast.LENGTH_SHORT).show();
			}
		}

		Intent buttonClickIntent = new Intent(context, OnOffWidget.class);
		buttonClickIntent.setAction(Constants.ON_OFF_WIDGET_ACTION_WIDGET_CLICKED);

		PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, buttonClickIntent, 0);
		views.setOnClickPendingIntent(R.id.on_off_widget_button, actionPendingIntent);

		ComponentName cn = new ComponentName(context, OnOffWidget.class);
		AppWidgetManager.getInstance(context).updateAppWidget(cn, views);
	}

}
