package gaugler.backitude.wakeful;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.constants.SyncOptionsEnum;
import gaugler.backitude.http.OfflineSyncTask;
import gaugler.backitude.util.ZLogger;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class OfflineLocationSyncService extends WakefulService{

	private static Handler delayedStart_handler = new Handler();
	
	@Override
	protected void doWakefulWork(Intent intent) {

		try
		{
			int serviceStartupType = -1;

			if(intent!=null) {
				Bundle extras = intent.getExtras();
				if(extras!=null) {
					serviceStartupType = extras.getInt(Constants.SERVICE_STARTUP_PARAM, -1);
				}
			}

			stopRetryTimer();
			stopTimeoutCountdown();

			switch(serviceStartupType){
			case Constants.OFFLINE_SYNC_FLAG:
				if(MyWakefulService.hasWifiWakeLock) {
					ZLogger.log("WiFi wake lock acquired - Delay offline service start for " + (Constants.FIFTEEN_SECONDS/1000) + " seconds");
					Toast.makeText(this, getResources().getString(R.string.wifi_lock_connecting), Toast.LENGTH_SHORT).show();
					delayedStart_handler.postDelayed(delayedStart_runnable, Constants.FIFTEEN_SECONDS);
				} else {
					ZLogger.log("OfflineLocationSyncService Start: Perform the offline location sync task");
					startRetryTimer();
					new OfflineSyncTask(OfflineLocationSyncService.this, 0).execute();
				}
				break;
			case Constants.REFRESH_AUTH_TOKEN_OFFSYNC:
				ZLogger.log("OfflineLocationSyncService Start: Refresh Auth Token");
				//TokenRefreshTask tokenRefresher = new TokenRefreshTask();
				//tokenRefresher.start(this, getResources().getString(R.string.UPDATE_TYPE_OFFLINE_SYNC));
				break;
			case Constants.START_SYNC_RETRY_TIMER:
				ZLogger.log("OfflineLocationSyncService Start: start timeout");
				startRetryTimer();
				break;
			default:
				ZLogger.log("OfflineLocationSyncService Start: but not for any specific reason?");
				onDestroy();
			}
		}
		catch(Exception ex)
		{
			ZLogger.logException("OfflineLocationSyncService", ex, getBaseContext());
			onDestroy();
		}
	}

	@Override
	public void onDestroy() {
		ZLogger.log("OfflineLocationSyncService onDestroy: onDestroy");

		stopDelayedStart();
		stopRetryTimer();
		stopTimeoutCountdown();

		// TOGGLE the sync flag so the Activity refreshes
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean temp = prefs.getBoolean(Prefs.KEY_offlineSync_flag, false);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(Prefs.KEY_offlineSync_flag, !temp);
		editor.commit();

		super.onDestroy();
	}

	// RETRY TIMER
	private static Handler handler = new Handler();
	private void startRetryTimer()
	{
		ZLogger.log("OfflineLocationSyncService: startRetryTimer");
		handler.postDelayed(r, Constants.ONE_MINUTE);
	}

	private void stopRetryTimer()
	{
		ZLogger.log("OfflineLocationSyncService: stopRetryTimer");
		if(handler!=null) {
			try {
				handler.removeCallbacks(r);
			}
			catch (Exception ex) {
				ZLogger.logException("OfflineLocationSyncService", ex, getBaseContext());
			}
		}
	}

	private final Runnable r = new Runnable()
	{
		public void run()
		{
			ZLogger.log("OfflineLocationSyncService: Runnable callback");
			try{	
				startTimeout();
				new OfflineSyncTask(OfflineLocationSyncService.this, false).execute();
			}
			catch(Exception ex)
			{
				ZLogger.logException("OfflineLocationSyncService Runnable", ex, getBaseContext());
			}
		}
	};

	// NO RESPONSE TIMEOUT
	private static Handler handler2 = new Handler();
	private void startTimeout()
	{
		ZLogger.log("OfflineLocationSyncService: startTimeout");
		handler2.postDelayed(r2,  Constants.ONE_MINUTE);
	}

	private void stopTimeoutCountdown()
	{
		ZLogger.log("OfflineLocationSyncService: stopTimeoutCountdown");
		if(handler2!=null) {
			try {
				handler2.removeCallbacks(r2);
			}
			catch (Exception ex) {
				ZLogger.logException("OfflineLocationSyncService", ex, getBaseContext());
			}
		}
	}

	private final Runnable r2 = new Runnable()
	{
		public void run()
		{
			ZLogger.log("OfflineLocationSyncService run2: Timeout reached (turn off sync)");
			try{	
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				if(!prefs.getString(Prefs.KEY_syncOptions, SyncOptionsEnum.ANY_DATA_NETWORK.getString()).equalsIgnoreCase(SyncOptionsEnum.MANUAL_ONLY.getString())){
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString(Prefs.KEY_syncOptions, SyncOptionsEnum.MANUAL_ONLY.getString());
					editor.commit();
					ZLogger.log("OfflineLocationSyncService run2: Sync failed, turn off autosync, switched to manual only");
					Toast.makeText(OfflineLocationSyncService.this, getResources().getString(R.string.SYNC_FAIL),Toast.LENGTH_SHORT).show();
				}
				onDestroy();
			}
			catch(Exception ex)
			{
				ZLogger.logException("ReSyncUpdateService Runnable", ex, getBaseContext());
			}
		}
	};
	
	private final Runnable delayedStart_runnable = new Runnable()
	{
		public void run()
		{
			ZLogger.log("OfflineLocationSyncService: delayed polling start for WiFi wake lock retrieval");
			try{	
				startRetryTimer();
				new OfflineSyncTask(OfflineLocationSyncService.this, 0).execute();
			}
			catch(Exception ex)
			{
				ZLogger.logException("OfflineLocationSyncService delayedStart_runnable", ex, getBaseContext());
			}
		}
	};
	
	private void stopDelayedStart()
	{
		ZLogger.log("OfflineLocationSyncService: stopDelayedStart");
		if(delayedStart_handler!=null) {
			try {
				delayedStart_handler.removeCallbacks(delayedStart_runnable);
			}
			catch (Exception ex) {
				ZLogger.logException("OfflineLocationSyncService", ex, getBaseContext());
			}
		}
	}
}
