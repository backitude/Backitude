package gaugler.backitude.util;

import gaugler.backitude.R;
import gaugler.backitude.activity.AccountActivity;
import gaugler.backitude.activity.LastErrorActivity;
import gaugler.backitude.activity.LastPushActivity;
import gaugler.backitude.constants.Constants;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class WarningNotification {

	public static void createStatusBarErrorNotification(Context _context, CharSequence tickerText)
	{
		NotificationManager mNotificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = R.drawable.warning;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		//notification.defaults |= Notification.DEFAULT_SOUND;
		//notification.defaults |= Notification.DEFAULT_VIBRATE;
		//notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Context context = _context;
		CharSequence contentTitle = context.getResources().getString(R.string.APP_NAME);
		CharSequence contentText = context.getResources().getString(R.string.WARNING_NOTIFICATION);
		Intent notificationIntent = new Intent(context, LastErrorActivity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(Constants.AUTH_ERROR_ID, notification);
	}
	
	public static void createStatusBarPushNotification(Context _context, CharSequence tickerText)
	{
		NotificationManager mNotificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = R.drawable.push_update_notification_icon;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		//notification.defaults |= Notification.DEFAULT_SOUND;
		//notification.defaults |= Notification.DEFAULT_VIBRATE;
		//notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Context context = _context;
		CharSequence contentTitle = context.getResources().getString(R.string.APP_NAME) + " - " + context.getResources().getString(R.string.PUSH_NOTIF);
		CharSequence contentText = tickerText.toString();
		Intent notificationIntent = new Intent(context, LastPushActivity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(Constants.UPDATE_REQUEST_ID, notification);
	}
	
	public static void createStatusBarPermissionsNotification(Context _context, CharSequence tickerText)
	{
		NotificationManager mNotificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = R.drawable.warning;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		//notification.defaults |= Notification.DEFAULT_SOUND;
		//notification.defaults |= Notification.DEFAULT_VIBRATE;
		//notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Context context = _context;
		CharSequence contentTitle = context.getResources().getString(R.string.APP_NAME) + " - " + context.getResources().getString(R.string.PERMISSION_DENIED);
		CharSequence contentText = tickerText.toString();
		Intent notificationIntent = new Intent(context, LastErrorActivity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(Constants.AUTH_ERROR_ID, notification);
	}
	
	public static void createStatusBarAccountNotification(Context _context, CharSequence tickerText)
	{
		NotificationManager mNotificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = R.drawable.warning;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		//notification.defaults |= Notification.DEFAULT_SOUND;
		//notification.defaults |= Notification.DEFAULT_VIBRATE;
		//notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Context context = _context;
		CharSequence contentTitle = context.getResources().getString(R.string.APP_NAME) + " - " + context.getResources().getString(R.string.REFRESH_DENIED);
		CharSequence contentText = tickerText.toString();
		Intent notificationIntent = new Intent(context, AccountActivity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(Constants.AUTH_ERROR_ID, notification);
	}
}
