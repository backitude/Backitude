package gaugler.backitude.wakeful;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.http.CustomServerPostTask;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.ZLogger;

public class ReSyncUpdateService extends WakefulService{

	private static Handler delayedStart_handler = new Handler();
	
	@Override
	protected void doWakefulWork(Intent intent) {

		try{
			int serviceStartupType = -1;

			if(intent!=null) {
				Bundle extras = intent.getExtras();
				if(extras!=null) {
					serviceStartupType = extras.getInt(Constants.SERVICE_STARTUP_PARAM, -1);
				}
			}

			switch(serviceStartupType){
			case Constants.RESYNC_ALARM_FLAG:
				if(MyWakefulService.hasWifiWakeLock) {
					ZLogger.log("WiFi wake lock acquired - Delay resync service start for " + (Constants.FIFTEEN_SECONDS/1000) + " seconds");
					Toast.makeText(this, getResources().getString(R.string.wifi_lock_connecting), Toast.LENGTH_SHORT).show();
					delayedStart_handler.postDelayed(delayedStart_runnable, Constants.FIFTEEN_SECONDS);
				} else {
					ZLogger.log("ReSyncUpdateService Start: Perform ReSync Update.  WiFi wake lock was not obtained (no delayed start required)");
					startRetryTimer();
					new CustomServerPostTask(this, null, getResources().getString(R.string.UPDATE_TYPE_RESYNC)).execute();
				}
				break;
			case Constants.REFRESH_AUTH_TOKEN_RESYNC:
				ZLogger.log("ReSyncUpdateService Start: Refresh Auth Token");
				stopRetryTimer();
				stopTimeoutCountdown();
				//TokenRefreshTask tokenRefresher = new TokenRefreshTask();
				//tokenRefresher.start(this, getResources().getString(R.string.UPDATE_TYPE_RESYNC));
				break;
			case Constants.START_RESYNC_RETRY_TIMER:
				ZLogger.log("ReSyncUpdateService Start: start retry timer");
				startRetryTimer();
				break;
			default:
				ZLogger.log("ReSyncUpdateService Start: but not for any specific reason?");
				onDestroy();
			}
		}
		catch (Exception ex) {
			ZLogger.logException("MyWakefulService", ex, getBaseContext());
		}
	}

	@Override
	public void onDestroy() {
		stopDelayedStart();
		stopRetryTimer();
		stopTimeoutCountdown();
		super.onDestroy();
	}

	// RETRY TIMER
	private static Handler handler = new Handler();
	private void startRetryTimer()
	{
		ZLogger.log("ReSyncUpdateService: startRetryTimer");
		handler.postDelayed(r, Constants.ONE_MINUTE);
	}

	private void stopRetryTimer()
	{
		ZLogger.log("ReSyncUpdateService: stopRetryTimer");
		if(handler!=null) {
			try {
				handler.removeCallbacks(r);
			}
			catch (Exception ex) {
				ZLogger.logException("ReSyncUpdateService", ex, getBaseContext());
			}
		}
	}

	private final Runnable r = new Runnable()
	{
		public void run()
		{
			ZLogger.log("ReSyncUpdateService: Runnable callback");
			try{	
				startTimeout();
				new CustomServerPostTask(ReSyncUpdateService.this, getResources().getString(R.string.UPDATE_TYPE_RESYNC), false).execute();
			}
			catch(Exception ex)
			{
				ZLogger.logException("ReSyncUpdateService Runnable", ex, getBaseContext());
			}
		}
	};

	// NO RESPONSE TIMEOUT

	private static Handler handler2 = new Handler();
	private void startTimeout()
	{
		ZLogger.log("ReSyncUpdateService: startTimeout");
		handler2.postDelayed(r2,  Constants.ONE_MINUTE);
	}

	private void stopTimeoutCountdown()
	{
		ZLogger.log("ReSyncUpdateService: stopTimeoutCountdown");
		if(handler2!=null) {
			try {
				handler2.removeCallbacks(r2);
			}
			catch (Exception ex) {
				ZLogger.logException("ReSyncUpdateService", ex, getBaseContext());
			}
		}
	}

	private final Runnable r2 = new Runnable()
	{
		public void run()
		{
			ZLogger.log("ReSyncUpdateService: Runnable callback");
			try{	
				ServiceManager sm = new ServiceManager();
				sm.updateOver(ReSyncUpdateService.this, Constants.RESYNC_UPDATE_OVER_FALSE_FLAG);
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
			ZLogger.log("ReSyncUpdateService: delayed resync start for WiFi wake lock retrieval");
			try{	
				startRetryTimer();
				//new OfflineSyncTask(ReSyncUpdateService.this, 0).execute();  // I think this was a bug, no idea why this line of code was here
				new CustomServerPostTask(ReSyncUpdateService.this, null, getResources().getString(R.string.UPDATE_TYPE_RESYNC)).execute();
			}
			catch(Exception ex)
			{
				ZLogger.logException("ReSyncUpdateService delayedStart_runnable", ex, getBaseContext());
			}
		}
	};
	
	private void stopDelayedStart()
	{
		ZLogger.log("ReSyncUpdateService: stopDelayedStart");
		if(delayedStart_handler!=null) {
			try {
				delayedStart_handler.removeCallbacks(delayedStart_runnable);
			}
			catch (Exception ex) {
				ZLogger.logException("ReSyncUpdateService", ex, getBaseContext());
			}
		}
	}
}
