package gaugler.backitude.widget;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.ZLogger;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class RealtimeWidget extends AppWidgetProvider {


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		// implementation will follow

		final int N = appWidgetIds.length;
		// Perform this loop procedure for each App Widget that belongs to this provider
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];

			// Get the layout for the App Widget and attach an on-click listener to the button
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.realtime_widget_layout);

			Intent intent = new Intent(context, RealtimeWidget.class);
			intent.setAction(Constants.REALTIME_WIDGET_ACTION_WIDGET_CLICKED);

			PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.realtime_widget_button, actionPendingIntent);

			if(isRealtimeRunning(context))
			{
				views.setImageViewResource(R.id.realtime_widget_button, R.drawable.realtime_widget_run);
			}
			else
			{
				if(isRealtimeEnabled(context))
				{
					views.setImageViewResource(R.id.realtime_widget_button, R.drawable.realtime_widget_enabled);
				}
				else
				{
					views.setImageViewResource(R.id.realtime_widget_button, R.drawable.realtime_widget_disabled);
				}
			}
			// Tell the AppWidgetManager to perform an update on the current App Widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

	}

	private boolean isRealtimeEnabled(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		return settings.getBoolean(Prefs.KEY_realtime, false);
	}

	private boolean isRealtimeRunning(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		return settings.getBoolean(PersistedData.KEY_realtimeRunning, false);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName (), R.layout.realtime_widget_layout);
		
		int realtimestatus = Constants.REAL_TIME_WIDGET_DISABLED;
		if(isRealtimeRunning(context)){
			realtimestatus = Constants.REAL_TIME_WIDGET_RUNNING;
		}
		else
		{
			if(isRealtimeEnabled(context))
			{
				realtimestatus = Constants.REAL_TIME_WIDGET_ENABLED;
			}
		}
		
		if(intent!=null) {
			Bundle extras = intent.getExtras();
			if(extras!=null) {
				realtimestatus = extras.getInt(Constants.REALTIME_WIDGET_PARAM, realtimestatus);
				////ZLogger.log("Updated preferences say: Realtime widget: " + realtimestatus);
			}

			if (intent.getAction()!=null)
			{
				if(intent.getAction().equals(Constants.REALTIME_WIDGET_ACTION_UPDATE_WIDGET)) 
				{
					Intent buttonClickIntent = new Intent(context, RealtimeWidget.class);
					buttonClickIntent.setAction(Constants.REALTIME_WIDGET_ACTION_WIDGET_CLICKED);

					PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, buttonClickIntent, 0);
					remoteViews.setOnClickPendingIntent(R.id.realtime_widget_button, actionPendingIntent);
					
					switch(realtimestatus){
					case Constants.REAL_TIME_WIDGET_DISABLED: 
						remoteViews.setImageViewResource(R.id.realtime_widget_button, R.drawable.realtime_widget_disabled);
						break;
					case Constants.REAL_TIME_WIDGET_ENABLED:
						remoteViews.setImageViewResource(R.id.realtime_widget_button, R.drawable.realtime_widget_enabled);
						break;
					case Constants.REAL_TIME_WIDGET_RUNNING:
						remoteViews.setImageViewResource(R.id.realtime_widget_button, R.drawable.realtime_widget_run);
						break;
					default:
						remoteViews.setImageViewResource(R.id.realtime_widget_button, R.drawable.realtime_widget_disabled);
						break;
					}					
					ComponentName cn = new ComponentName(context, RealtimeWidget.class);
					AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);
				}
				else
				{
					if(intent.getAction().equals(Constants.REALTIME_WIDGET_ACTION_WIDGET_CLICKED))
					{
						toggleService(context);
					}
				}
			}
		}
	}

	private void toggleService(Context context) 
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());	
		boolean wasEnabled = settings.getBoolean(Prefs.KEY_realtime, false);
		ZLogger.log("Real-time toggleService: wasEnabled? " + wasEnabled);

		// Get the layout for the App Widget and attach an on-click listener to the button
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.realtime_widget_layout);
		if(wasEnabled)
		{			
			views.setImageViewResource(R.id.realtime_widget_button, R.drawable.realtime_widget_disabled);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(Prefs.KEY_realtime, false);
			editor.commit();

			if(isRealtimeRunning(context)){
				ServiceManager appStarter = new ServiceManager();
				appStarter.startAlarms(context.getApplicationContext());
			}
		}
		else
		{    			
			boolean isCharging = settings.getBoolean(PersistedData.KEY_isCharging, false);
			boolean isWiFiModeEnabled = settings.getBoolean(Prefs.KEY_wifi_mode, false);
			boolean isWiFiConnected = false;
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if(wifiNetInfo!=null)
			{
				ZLogger.log("ConnectivityListener onReceive: Wifi Connected = " + wifiNetInfo.isConnected());				
				isWiFiConnected = wifiNetInfo.isConnected();
			}
			boolean isWifiModeRunning = isWiFiModeEnabled && isWiFiConnected;
			
			if(isCharging && !isWifiModeRunning) 
			{
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean(Prefs.KEY_realtime, true);
				editor.putBoolean(Prefs.KEY_appEnabled, true);
				editor.commit();

				// Turn On OnOffWidget
				Intent uiIntent = new Intent(context, OnOffWidget.class);
				uiIntent.setAction(Constants.ON_OFF_WIDGET_ACTION_UPDATE_WIDGET);
				uiIntent.putExtra(Constants.ON_OFF_WIDGET_PARAM, true);
				context.sendBroadcast(uiIntent);

				ServiceManager appStarter = new ServiceManager();
				appStarter.startAlarms(context.getApplicationContext());
				
				views.setImageViewResource(R.id.realtime_widget_button, R.drawable.realtime_widget_run);
			}			
			else 
			{
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean(Prefs.KEY_realtime, true);
				editor.commit();
				views.setImageViewResource(R.id.realtime_widget_button, R.drawable.realtime_widget_enabled);
			}
		}

		Intent buttonClickIntent = new Intent(context, RealtimeWidget.class);
		buttonClickIntent.setAction(Constants.REALTIME_WIDGET_ACTION_WIDGET_CLICKED);

		PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, buttonClickIntent, 0);
		views.setOnClickPendingIntent(R.id.realtime_widget_button, actionPendingIntent);

		ComponentName cn = new ComponentName(context, RealtimeWidget.class);
		AppWidgetManager.getInstance(context).updateAppWidget(cn, views);
	}
}
