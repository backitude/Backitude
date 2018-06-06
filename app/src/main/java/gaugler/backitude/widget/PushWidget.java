package gaugler.backitude.widget;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.util.ZLogger;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class PushWidget extends AppWidgetProvider {
	private static final String PREF_NAME_PREFIX_KEY = "ContactName_prefix_";
	private static final String PREF_NUM_PREFIX_KEY = "ContactNumber_prefix_";
	// This method initializes the widget and sets up the click event
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		ZLogger.log("PushWidget onUpdate");
		final int N = appWidgetIds.length;
		// Perform this loop procedure for each App Widget that belongs to this provider
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];

			// Get the layout for the App Widget and attach an on-click listener to the button
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.push_widget_layout);

			Intent intent = new Intent(context, PushWidget.class);
			intent.setAction(Constants.PUSH_WIDGET_ACTION_WIDGET_CLICKED);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			ZLogger.log("PushWidget onUpdate: widget id = " + appWidgetId);

			PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.push_widget_button_id, actionPendingIntent);
			//ImageButton object only
			//views.setImageViewResource(R.id.push_widget_button_id, R.drawable.push_update);
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
			String Name = settings.getString(PREF_NAME_PREFIX_KEY + appWidgetId, "(default)");

			views.setTextViewText(R.id.push_widget_button_id, Name);

			// Tell the AppWidgetManager to perform an update on the current App Widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
			ZLogger.log("PushWidget onUpdate: updateAppWidget");
		}

	}

	// onReceive comes from the application updating the state of the widget or someone clicking the widget
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		ZLogger.log("PushWidget onReceive");
		//RemoteViews remoteViews = new RemoteViews(context.getPackageName (), R.layout.push_widget_layout);
		if(intent!=null) {
			if (intent.getAction()!=null)
			{
				if(intent.getAction().equals(Constants.PUSH_WIDGET_ACTION_WIDGET_CLICKED))
				{
					ZLogger.log("PushWidget onReceive: PUSH_WIDGET_ACTION_WIDGET_CLICKED");

					try{
						Bundle extras = intent.getExtras();
						
						int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
						if(extras!=null){
							mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
						}
						else
						{
							ZLogger.log("PushWidget onReceive: extras is null");
						}

						//If they gave us an intent without a valid widget Id, just bail.
						if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
						{
							ZLogger.log("PushWidget onReceive: mAppWidgetId is ok " + mAppWidgetId);
						}
						else
						{
							ZLogger.log("PushWidget onReceive: mAppWidgetId is not available " + mAppWidgetId);
						}
						SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
						//String name1 = settings.getString(PREF_NAME_PREFIX_KEY + mAppWidgetId, "");

						////ImageButton object only
						////remoteViews.setImageViewResource(R.id.push_widget_button_id, R.drawable.push_update);
						//remoteViews.setTextViewText(R.id.push_widget_select_button_id, name1);
						//ComponentName cn = new ComponentName(context, PushWidget.class);
						//AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);

						String Number = settings.getString(PREF_NUM_PREFIX_KEY + mAppWidgetId, "");
						Number = Number.replace("-","");
						String uri= "smsto:" + Number;
						ZLogger.log("PushWidget onReceive: sms uri: " + uri);
						Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
						sendIntent.putExtra("sms_body", context.getResources().getString(R.string.FORCE_BACKITUDE_UPDATE));
						sendIntent.putExtra("compose_mode", true);
						sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.getApplicationContext().startActivity(sendIntent);
					}
					catch(Exception ex){
						ZLogger.logException("PushWidget", ex, context);
					}
				}
			}
		}
		ZLogger.log("onReceive end");
	}

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId, String titlePrefix) {
		ZLogger.log("PushWidget updateAppWidget: start id = " + appWidgetId);
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.push_widget_layout);

		Intent intent = new Intent(context, PushWidget.class);
		intent.setAction(Constants.PUSH_WIDGET_ACTION_WIDGET_CLICKED);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		
		PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.push_widget_button_id, actionPendingIntent);
		views.setTextViewText(R.id.push_widget_button_id, titlePrefix);
		//ImageButton property
		//views.setImageViewResource(R.id.push_widget_button_id, R.drawable.push_update);

		// Tell the AppWidgetManager to perform an update on the current App Widget
		appWidgetManager.updateAppWidget(appWidgetId, views);
		ZLogger.log("PushWidget updateAppWidget: end");
	}

	@Override          
	public void onDeleted(Context context, int[] appWidgetIds) {               
		super.onDeleted(context, appWidgetIds);          
		final int N = appWidgetIds.length;          
        
		try{                  
			for (int i=0; i<N; i++) {                          
				ZLogger.log("PushWidget onDeleted: " + appWidgetIds[i]);     
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
				SharedPreferences.Editor edit = settings.edit();
				edit.remove(PREF_NAME_PREFIX_KEY + appWidgetIds[i]);
				edit.remove(PREF_NUM_PREFIX_KEY + appWidgetIds[i]);
				edit.commit();
			}          
		}
		catch(Exception e)
		{   
			ZLogger.logException("PushWidget", e, context);
		}
		finally
		{                 
        
		}        
	}  
}
