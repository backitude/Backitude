package gaugler.backitude.util;

import gaugler.backitude.R;
import gaugler.backitude.activity.MainActivity;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StatusBarHelper {

	public static void createAppEnabledNotification(Context context)
	{
		ZLogger.log("NotificationManager: createAppEnabledNotification");
		if(context!=null){
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			if(mNotificationManager!=null){

				int icon = R.drawable.mainlogo;
				CharSequence tickerText = context.getResources().getString(R.string.STATUS_BAR_TITLE);
				long when = System.currentTimeMillis();

				Notification notification = new Notification(icon, tickerText, when);
				notification.flags |= Notification.FLAG_NO_CLEAR;
				notification.flags |= Notification.FLAG_ONGOING_EVENT;

				CharSequence contentTitle = context.getResources().getString(R.string.APP_NAME);

				Intent notificationIntent = new Intent(context, MainActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

				notification.setLatestEventInfo(context, contentTitle, getTickerText(context.getResources().getString(R.string.RUNNING_NOTIFICATION), context), contentIntent);

				mNotificationManager.notify(Constants.ENABLED_STATUS_ID, notification);
			}
		}
	}

	public static void cancelAppEnabledNotfication(Context context)
	{
		ZLogger.log("NotificationManager: cancelAppEnabledNotfication");
		if(context!=null){
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			if(mNotificationManager!=null){
				mNotificationManager.cancel(Constants.ENABLED_STATUS_ID);
			}
		}
	}

	public static void createRealTimeEnabledNotification(Context context)
	{
		ZLogger.log("NotificationManager: createRealTimeEnabledNotification");
		if(context!=null){
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			if(mNotificationManager!=null){
				int icon = R.drawable.statusbaricon;
				CharSequence tickerText = context.getResources().getString(R.string.STATUS_BAR_TITLE);
				long when = System.currentTimeMillis();

				Notification notification = new Notification(icon, tickerText, when);
				notification.flags |= Notification.FLAG_NO_CLEAR;
				notification.flags |= Notification.FLAG_ONGOING_EVENT;

				CharSequence contentTitle = context.getResources().getString(R.string.APP_NAME);
				Intent notificationIntent = new Intent(context, MainActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

				notification.setLatestEventInfo(context, contentTitle, getTickerText(context.getResources().getString(R.string.REALTIME_NOTIFICATION), context), contentIntent);

				mNotificationManager.notify(Constants.REALTIME_STATUS_ID, notification);
			}
		}
	}

	public static void cancelRealTimeEnabledNotfication(Context context)
	{
		ZLogger.log("NotificationManager: cancelRealTimeEnabledNotfication");
		if(context!=null){
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			if(mNotificationManager!=null){
				mNotificationManager.cancel(Constants.REALTIME_STATUS_ID);
			}
		}
	}

	public static void createPollingNotification(Context context)
	{
		ZLogger.log("NotificationManager: createPollingNotification");
		if(context!=null){
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			if(mNotificationManager!=null)
			{
				int icon = R.drawable.statusbaricon;
				CharSequence tickerText = "Backituding";
				long when = System.currentTimeMillis();

				Notification notification = new Notification(icon, tickerText, when);

				CharSequence contentTitle = context.getResources().getString(R.string.APP_NAME);
				Intent notificationIntent = new Intent(context, MainActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

				notification.setLatestEventInfo(context, contentTitle, getTickerText(context.getResources().getString(R.string.POLLING_NOTIFICATION), context), contentIntent);

				mNotificationManager.notify(Constants.POLLING_STATUS_ID, notification);
			}

		}
	}

	public static void cancelPollingNotfication(Context context) {
		ZLogger.log("NotificationManager: cancelPollingNotfication");
		if(context!=null){
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			if(mNotificationManager!=null){
				mNotificationManager.cancel(Constants.POLLING_STATUS_ID);
			}
		}

	}

	private static CharSequence getTickerText(CharSequence default_text, Context context){

		CharSequence contentText = default_text;
		try{
			if(context!=null){
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				boolean wifiMode = prefs.getBoolean(PersistedData.KEY_wifiModeRunning, false);
				boolean dockedMode = prefs.getBoolean(PersistedData.KEY_realtimeRunning, false);
				String mode = "";
				int PERIOD = 0;
				if(wifiMode){
					mode = context.getResources().getString(R.string.STATUS_BAR_MODE2);
					PERIOD = Integer.parseInt(prefs.getString(Prefs.KEY_wifi_mode_interval, Prefs.DEFAULT_interval));
				} else if (dockedMode) {
					mode = context.getResources().getString(R.string.STATUS_BAR_MODE3);
					PERIOD = Integer.parseInt(prefs.getString(Prefs.KEY_realtime_interval, Prefs.DEFAULT_interval));
				} else {
					mode = context.getResources().getString(R.string.STATUS_BAR_MODE1);
					PERIOD = Integer.parseInt(prefs.getString(Prefs.KEY_interval, Prefs.DEFAULT_interval));
				}

				int SECONDS = PERIOD / 1000;
				int MINUTES = 0;
				int HOURS = 0;

				if (SECONDS > 59){
					MINUTES = SECONDS / 60;
					SECONDS = 0;
				}

				if (MINUTES > 59){
					HOURS = MINUTES / 60;
					MINUTES = 0;
				}

				if (PERIOD == Constants.NO_POLLING_INTERVAL)
				{
					SECONDS = 0;
					MINUTES = 0;
					HOURS = 0;
				}

				if(SECONDS > 0){
					contentText = mode + ": " + SECONDS + " " + context.getResources().getString(R.string.seconds);
				} else if (MINUTES > 1) {
					contentText = mode + ": " + MINUTES + " " + context.getResources().getString(R.string.minutes);
				} else if (MINUTES > 0) {
					contentText = mode + ": " + MINUTES + " " + context.getResources().getString(R.string.minute);
				} else if (HOURS > 1) {
					contentText = mode + ": " + HOURS + " " + context.getResources().getString(R.string.hours);
				} else if (HOURS > 0) {
					contentText = mode + ": " + HOURS + " " + context.getResources().getString(R.string.hour);
				} else {
					contentText = mode + ": " + context.getResources().getString(R.string.none);
				}
			}
		}
		catch(Exception e)
		{
			ZLogger.logException("NotificationManager createAppEnabledNotification", e, context);
		}
		return contentText;
	}

}
