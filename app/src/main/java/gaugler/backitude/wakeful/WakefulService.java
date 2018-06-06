package gaugler.backitude.wakeful;

import gaugler.backitude.constants.Prefs;
import gaugler.backitude.util.ZLogger;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;

abstract public class WakefulService extends Service {
	abstract protected void doWakefulWork(Intent intent);

	private static final String LOCK_NAME_STATIC="gaugler.backitude.wakeful.WakefulService";
	private static volatile PowerManager.WakeLock lockStatic=null;
	private static volatile WifiManager.WifiLock wifiLock = null;

	private static boolean wakeLockEnabled = true;
	private static boolean wifiLockEnabled = false;
	
	public static boolean hasWifiWakeLock = false;

	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		wakeLockEnabled = settings.getBoolean(Prefs.KEY_wake_lock, true);
		wifiLockEnabled = settings.getBoolean(Prefs.KEY_wifi_lock, false);

		if (lockStatic==null && wakeLockEnabled) {
			PowerManager mgr = (PowerManager)getSystemService(Context.POWER_SERVICE);

			lockStatic=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
			if(!lockStatic.isHeld()){
				lockStatic.acquire();
				ZLogger.log("WakefulService OnCreate: System Wake Lock acquired");
			}
			else
			{
				ZLogger.log("WakefulService OnCreate: System Wake Lock not needed");
			}
		}

		if(wifiLock==null && wifiLockEnabled){
			boolean skipWakeLock = false;
			WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);

			ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			skipWakeLock = !wm.isWifiEnabled()  // If wifi is not enabled, skip acquiring a wifi lock
					|| !mWifi.isAvailable()     // If wifi is not available, skip acquiring a wifi lock
					|| mWifi.isConnectedOrConnecting()   //If wifi is already connected, skip acquiring a wifi lock
					|| !wm.getConnectionInfo().getSupplicantState().equals(SupplicantState.COMPLETED); // in a connected wifi network area, sufficant state 
																							// should equal COMPLETED when device goes to sleep			
			//ZLogger.log("WakefulService OnCreate: isWifiEnabled = " + wm.isWifiEnabled());
			//ZLogger.log("WakefulService OnCreate: isAvailable = " + mWifi.isAvailable());
			//ZLogger.log("WakefulService OnCreate: isConnectedOrConnecting = " + mWifi.isConnectedOrConnecting());
			//ZLogger.log("WakefulService OnCreate: wm.getConnectionInfo().getSupplicantState() = " + wm.getConnectionInfo().getSupplicantState());

			if(skipWakeLock)
			{
				ZLogger.log("WakefulService OnCreate: Wifi wake lock is not needed because Wi-fi is not enabled, not available, or its already connected");
			}
			else
			{
				wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL , LOCK_NAME_STATIC);
				wifiLock.setReferenceCounted(true);
				if(!wifiLock.isHeld()){
					wifiLock.acquire();
					hasWifiWakeLock = true;
					ZLogger.log("WakefulService OnCreate: Wifi Lock needed and acquired");
				}
				else{
					ZLogger.log("WakefulService OnCreate: Wifi Wake Lock is needed but already held");
				}
			}

		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		super.onStartCommand(intent, flags, startId);

		try
		{
			doWakefulWork(intent);
		}
		catch(Exception ex)
		{
			ZLogger.logException("WakefulService", ex, getBaseContext());
			onDestroy();
		}

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		try {
			ZLogger.log("WakefulService onDestroy: method start");
			if(lockStatic!=null && lockStatic.isHeld()){
				lockStatic.release();
				lockStatic = null;
				ZLogger.log("WakefulService onDestroy: release system locks");
			}

			// release the WifiLock
			if (wifiLock != null && wifiLock.isHeld()) {
				wifiLock.release();
				wifiLock = null;
				hasWifiWakeLock = false;
				ZLogger.log("WakefulService onDestroy: release wifi locks");
			}
			else{
				ZLogger.log("WakefulService onDestroy: wifi lock not held");
			}
		}
		catch(Exception ex)
		{
			ZLogger.logException("WakefulService", ex, getBaseContext());
		}
		finally {
			this.stopSelf();
			super.onDestroy();
		}
	}
}