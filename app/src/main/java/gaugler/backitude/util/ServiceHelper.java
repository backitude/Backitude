package gaugler.backitude.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ServiceHelper {
	
	public static boolean isMyWakefulServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("gaugler.backitude.wakeful.MyWakefulService".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isReSyncUpdateServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("gaugler.backitude.wakeful.ReSyncUpdateService".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isMyOfflineServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("gaugler.backitude.wakeful.OfflineLocationSyncService".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isNetworkAvailable(Context context) {
		try{
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			return (activeNetworkInfo != null);
		}
		catch (Exception ex)
		{
			ZLogger.logException("ServiceHelper", ex, context);
		}
		return false;
	}
	
	public static boolean isConnectedToWifi(Context context){
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return (mWifi.isAvailable() && mWifi.isConnected() && !mWifi.isRoaming());
	}
}
