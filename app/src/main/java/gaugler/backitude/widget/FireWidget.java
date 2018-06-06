package gaugler.backitude.widget;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.util.PreferenceHelper;
import gaugler.backitude.util.ServiceHelper;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.MyWakefulService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.RemoteViews;
import android.widget.Toast;

public class FireWidget extends AppWidgetProvider {

	// This method initializes the widget and sets up the click event
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		final int N = appWidgetIds.length;
		// Perform this loop procedure for each App Widget that belongs to this provider
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];

			// Get the layout for the App Widget and attach an on-click listener to the button
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fire_widget_layout);

			Intent intent = new Intent(context, FireWidget.class);
			intent.setAction(Constants.FIRE_WIDGET_ACTION_WIDGET_CLICKED);

			PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.fire_widget_button, actionPendingIntent);

			if(ServiceHelper.isMyWakefulServiceRunning(context))
			{
				views.setImageViewResource(R.id.fire_widget_button, R.drawable.fire_widget_on);
			}
			else{
				views.setImageViewResource(R.id.fire_widget_button, R.drawable.fire_widget_off);
			}

			// Tell the AppWidgetManager to perform an update on the current App Widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

	}

	// onReceive comes from the application updating the state of the widget or someone clicking the widget
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName (), R.layout.fire_widget_layout);
		boolean isPolling = false;
		if(intent!=null) {
			Bundle extras = intent.getExtras();
			if(extras!=null) {
				isPolling = extras.getBoolean(Constants.FIRE_WIDGET_PARAM, false);
				////ZLogger.log("Fire Widget isPolling: " + isPolling);
			}
			if (intent.getAction()!=null)
			{
				if(intent.getAction().equals(Constants.FIRE_WIDGET_ACTION_UPDATE_WIDGET)) 
				{					
					// Activity is just updating the state of the widget
					Intent buttonClickIntent = new Intent(context, FireWidget.class);
					buttonClickIntent.setAction(Constants.FIRE_WIDGET_ACTION_WIDGET_CLICKED);

					PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, buttonClickIntent, 0);
					remoteViews.setOnClickPendingIntent(R.id.fire_widget_button, actionPendingIntent);

					if(isPolling)
					{
						remoteViews.setImageViewResource(R.id.fire_widget_button, R.drawable.fire_widget_on);
					}
					else{
						remoteViews.setImageViewResource(R.id.fire_widget_button, R.drawable.fire_widget_off);
					}
					ComponentName cn = new ComponentName(context, FireWidget.class);
					AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);
				}
				else
				{
					if(intent.getAction().equals(Constants.FIRE_WIDGET_ACTION_WIDGET_CLICKED))
					{
						if(isAbleToBeEnabled(context)){
							SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
							if( // Fire if....
									(prefs.getBoolean(Prefs.KEY_offlineSync, false)) ||
									(prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false) && ServiceHelper.isConnectedToWifi(context)) ||
									(!prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false) && ServiceHelper.isNetworkAvailable(context))
									)
							{
								if(!ServiceHelper.isMyWakefulServiceRunning(context)){
									remoteViews.setImageViewResource(R.id.fire_widget_button, R.drawable.fire_widget_on);
									ComponentName cn = new ComponentName(context, FireWidget.class);
									AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);

									Intent serviceIntent = new Intent(context, MyWakefulService.class);
									serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.FIRE_UPDATE_FLAG);
									context.startService(serviceIntent);
								}
								else
								{
									Toast.makeText(context, context.getResources().getString(R.string.LOC_SERVICE_RUNNING), Toast.LENGTH_SHORT).show();
								}
							}
							else
							{
								if(prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false))
								{
									ZLogger.log("Cannot Fire update..no wifi service at this time during wifiOnly mode");
									Toast.makeText(context, context.getResources().getString(R.string.NO_FIRE_NO_WIFI), Toast.LENGTH_LONG).show();
								}
								else
								{
									ZLogger.log("Cannot Fire update..no network service at this time");
									Toast.makeText(context, context.getResources().getString(R.string.NO_FIRE_NO_NETWORK), Toast.LENGTH_SHORT).show();
								}
							}
						}
						else
						{
							Toast.makeText(context, context.getResources().getString(R.string.NO_FIRE_NO_PROVIDER), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		}
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

		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isWiFiLocEnabled = (Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER)) &&
				(mWifi.isAvailable() || mWifi.isConnectedOrConnecting() || mWifi.isRoaming()) && wifiPollingAllowed;

		return (isGpsLocEnabled||isNetworkLocEnabled||isWiFiLocEnabled);
	}
}
