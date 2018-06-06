package gaugler.backitude.wakeful;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.http.CustomServerPostTask;
import gaugler.backitude.service.ServiceLocationHelper;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.PreferenceHelper;
import gaugler.backitude.util.ZLogger;

public class MyWakefulService extends WakefulService{

	private static Handler delayedStart_handler = new Handler();
	
	private static String phoneNumber = "";
	
	@Override
	protected void doWakefulWork(Intent intent) {

		try	{
			int serviceStartupType = -1;
			
			if(intent!=null) {
				Bundle extras = intent.getExtras();
				if(extras!=null) {
					serviceStartupType = extras.getInt(Constants.SERVICE_STARTUP_PARAM, -1);
					ZLogger.log("MyWakefulService doWakefulWork: serviceStartupType = " + serviceStartupType);
					phoneNumber = extras.getString(Constants.SERVICE_PHONE_PARAM);
				}
			}

			switch(serviceStartupType){
			case Constants.POLL_TIMER_FLAG:
				if(MyWakefulService.hasWifiWakeLock) {
					Toast.makeText(this, getResources().getString(R.string.wifi_lock_connecting), Toast.LENGTH_SHORT).show();
					ZLogger.log("WiFi wake lock acquired - Delay polling service start for " + (Constants.FIFTEEN_SECONDS/1000) + " seconds");
					delayedStart_handler.postDelayed(delayedStart_runnable, Constants.FIFTEEN_SECONDS);
				} else {
					ZLogger.log("Wakeful Service Start: Starting Location Polling for Timer.  WiFi wake lock was not obtained.  (Delayed start not required)");
					ServiceLocationHelper.start(this);
				}
				break;
			case Constants.FIRE_UPDATE_FLAG:
				ZLogger.log("Wakeful Service Start: Starting Location Polling for Fire Update");
				ServiceLocationHelper.start(this, true);
				break;
			case Constants.STEAL_UPDATE_FLAG:
				ZLogger.log("Wakeful Service Start: Perform Steal Update");
				Location location = PreferenceHelper.getCachedSteal(getBaseContext());
				PreferenceHelper.clearCachedSteal(getBaseContext());
				// If the saved stolen location is legit
				if(location.hasAccuracy()){
					startStealRetryTimer();
					new CustomServerPostTask(this, location, getResources().getString(R.string.UPDATE_TYPE_STEAL)).execute();
				}
				else
				{
					ZLogger.log("Wakeful Service Start: steal value is null, stop service");
					onDestroy();
				}
				break;
			case Constants.PUSH_UPDATE_FLAG:
				ZLogger.log("Wakeful Service Start: Starting Location Polling for Push Update");
				ServiceLocationHelper.start(this, true, phoneNumber);
				break;
			case Constants.MANUAL_UPDATE_FLAG:
				ZLogger.log("Wakeful Service Start: Perform Manual Update");
				Location manualLocation = PreferenceHelper.getCachedSteal(getBaseContext());
				PreferenceHelper.clearCachedSteal(getBaseContext());
				startManualRetryTimer();
				new CustomServerPostTask(this, manualLocation, getResources().getString(R.string.UPDATE_TYPE_MANUAL)).execute();
				break;
			case Constants.REFRESH_AUTH_TOKEN_POLL:
				ZLogger.log("Wakeful Service Start: Refresh Auth Token for Poll");
				stopPollRetryTimer();
				stopPollRetryTimer2();
				stopPollTimeoutCountdown();
				//TokenRefreshTask pollTokenRefresher = new TokenRefreshTask();
				//pollTokenRefresher.start(this, getResources().getString(R.string.UPDATE_TYPE_POLL));
				break;
			case Constants.REFRESH_AUTH_TOKEN_FIRE:
				ZLogger.log("Wakeful Service Start: Refresh Auth Token for Fire");
				stopFireRetryTimer();
				stopFireTimeoutCountdown();
				//TokenRefreshTask fireTokenRefresher = new TokenRefreshTask();
				//fireTokenRefresher.start(this, getResources().getString(R.string.UPDATE_TYPE_FIRE));
				break;
			case Constants.REFRESH_AUTH_TOKEN_STEAL:
				ZLogger.log("Wakeful Service Start: Refresh Auth Token for Steal");
				stopStealRetryTimer();
				stopStealTimeoutCountdown();
				//TokenRefreshTask stealTokenRefresher = new TokenRefreshTask();
				//stealTokenRefresher.start(this, getResources().getString(R.string.UPDATE_TYPE_STEAL));
				break;
			case Constants.REFRESH_AUTH_TOKEN_PUSH:
				ZLogger.log("Wakeful Service Start: Refresh Auth Token for Push");
				stopPushRetryTimer();
				stopPushTimeoutCountdown();
				//TokenRefreshTask fireTokenRefresher = new TokenRefreshTask();
				//fireTokenRefresher.start(this, getResources().getString(R.string.UPDATE_TYPE_FIRE));
				break;
			case Constants.REFRESH_AUTH_TOKEN_MANUAL:
				ZLogger.log("Wakeful Service Start: Refresh Auth Token for Manual update");
				stopManualRetryTimer();
				stopManualTimeoutCountdown();
				//TokenRefreshTask manualTokenRefresher = new TokenRefreshTask();
				//manualTokenRefresher.start(this, getResources().getString(R.string.UPDATE_TYPE_MANUAL));
				break;
			case Constants.START_POLL_RETRY_TIMER:
				ZLogger.log("Wakeful Service Start: start retry timer");
				startPollRetryTimer();
				break;
			case Constants.START_FIRE_RETRY_TIMER:
				ZLogger.log("Wakeful Service Start: start fire retry timer");
				startFireRetryTimer();
				break;
			case Constants.START_STEAL_RETRY_TIMER:
				ZLogger.log("Wakeful Service Start: start steal retry timer");
				startStealRetryTimer();
				break;
			case Constants.START_PUSH_RETRY_TIMER:
				ZLogger.log("Wakeful Service Start: start push retry timer");
				startPushRetryTimer();
				break;
			case Constants.START_MANUAL_RETRY_TIMER:
				ZLogger.log("Wakeful Service Start: start manual update retry timer");
				startManualRetryTimer();
				break;
			default:
				ZLogger.log("Wakeful Service Start: but not for any specific reason?");
				onDestroy();
			}
		}
		catch(Exception ex)
		{
			ZLogger.logException("MyWakefulService", ex, getBaseContext());
			onDestroy();
		}
	}

	@Override
	public void onDestroy() {
		stopDelayedStart();
		stopPollRetryTimer();
		stopPollRetryTimer2();
		stopFireRetryTimer();
		stopStealRetryTimer();
		stopManualRetryTimer();
		stopPollTimeoutCountdown();
		stopFireTimeoutCountdown();
		stopStealTimeoutCountdown();
		stopManualTimeoutCountdown();
		super.onDestroy();
	}


	// POLL RETRY TIMER
	private static Handler poll_handler = new Handler();
	private void startPollRetryTimer()
	{
		ZLogger.log("MyWakefulService: startPollRetryTimer");
		poll_handler.postDelayed(poll_runnable, Constants.ONE_MINUTE);
	}

	private void stopPollRetryTimer()
	{
		ZLogger.log("MyWakefulService: stopPollRetryTimer");
		if(poll_handler!=null) {
			try {
				poll_handler.removeCallbacks(poll_runnable);
			}
			catch (Exception ex) {
				ZLogger.logException("MyWakefulService", ex, getBaseContext());
			}
		}
	}

	private final Runnable poll_runnable = new Runnable()
	{
		public void run()
		{
			ZLogger.log("MyWakefulService: poll_runnable callback");
			try{	
				startPollRetryTimer2();
				new CustomServerPostTask(MyWakefulService.this, getResources().getString(R.string.UPDATE_TYPE_POLL), true).execute();
			}
			catch(Exception ex)
			{
				ZLogger.logException("MyWakefulService poll_runnable", ex, getBaseContext());
			}
		}
	};


	// POLL RETRY TIMER #2

	private static Handler poll_handler2 = new Handler();
	private void startPollRetryTimer2()
	{
		ZLogger.log("MyWakefulService: startPollRetryTimer2");
		poll_handler2.postDelayed(poll_runnable2, Constants.ONE_MINUTE);
	}

	private void stopPollRetryTimer2()
	{
		ZLogger.log("MyWakefulService: stopPollRetryTimer2");
		if(poll_handler2!=null) {
			try {
				poll_handler2.removeCallbacks(poll_runnable2);
			}
			catch (Exception ex) {
				ZLogger.logException("MyWakefulService", ex, getBaseContext());
			}
		}
	}

	private final Runnable poll_runnable2 = new Runnable()
	{
		public void run()
		{
			ZLogger.log("MyWakefulService: poll_runnable2 callback");
			try{	
				startPollTimeout();
				new CustomServerPostTask(MyWakefulService.this, getResources().getString(R.string.UPDATE_TYPE_POLL), false).execute();
			}
			catch(Exception ex)
			{
				ZLogger.logException("MyWakefulService poll_runnable2", ex, getBaseContext());
			}
		}
	};

	// NO RESPONSE POLL TIMEOUT

	private static Handler poll_timeout_handler = new Handler();
	private void startPollTimeout()
	{
		ZLogger.log("MyWakefulService: startTimeout");
		poll_timeout_handler.postDelayed(poll_runnable3,  Constants.ONE_MINUTE);
	}

	private void stopPollTimeoutCountdown()
	{
		ZLogger.log("MyWakefulService: stopPollTimeoutCountdown");
		if(poll_timeout_handler!=null) {
			try {
				poll_timeout_handler.removeCallbacks(poll_runnable3);
			}
			catch (Exception ex) {
				ZLogger.logException("MyWakefulService", ex, getBaseContext());
			}
		}
	}

	private final Runnable poll_runnable3 = new Runnable()
	{
		public void run()
		{
			ZLogger.log("MyWakefulService: poll_runnable3 callback");
			try{	
				ServiceManager sm = new ServiceManager();
				sm.updateOver(MyWakefulService.this, Constants.POLL_UPDATE_OVER_FALSE_FLAG);
			}
			catch(Exception ex)
			{
				ZLogger.logException("MyWakefulService poll_runnable3", ex, getBaseContext());
			}
		}
	};

	// FIRE RETRY TIMER
	private static Handler fire_handler = new Handler();
	private void startFireRetryTimer()
	{
		ZLogger.log("MyWakefulService: startFireRetryTimer");
		fire_handler.postDelayed(fire_runnable, Constants.ONE_MINUTE);
	}

	private void stopFireRetryTimer()
	{
		ZLogger.log("MyWakefulService: stopFireRetryTimer");
		if(fire_handler!=null) {
			try {
				fire_handler.removeCallbacks(fire_runnable);
			}
			catch (Exception ex) {
				ZLogger.logException("MyWakefulService", ex, getBaseContext());
			}
		}
	}

	private final Runnable fire_runnable = new Runnable()
	{
		public void run()
		{
			ZLogger.log("MyWakefulService: fire_runnable callback");
			try{	
				startFireTimeout();
				new CustomServerPostTask(MyWakefulService.this, getResources().getString(R.string.UPDATE_TYPE_FIRE), false).execute();
			}
			catch(Exception ex)
			{
				ZLogger.logException("MyWakefulService fire_runnable", ex, getBaseContext());
			}
		}
	};

	// NO RESPONSE FIRE TIMEOUT

	private static Handler fire_timeout_handler = new Handler();
	private void startFireTimeout()
	{
		ZLogger.log("MyWakefulService: startFireTimeout");
		fire_timeout_handler.postDelayed(fire_runnable2,  Constants.ONE_MINUTE);
	}

	private void stopFireTimeoutCountdown()
	{
		ZLogger.log("MyWakefulService: stopFireTimeoutCountdown");
		if(fire_timeout_handler!=null) {
			try {
				fire_timeout_handler.removeCallbacks(fire_runnable2);
			}
			catch (Exception ex) {
				ZLogger.logException("MyWakefulService", ex, getBaseContext());
			}
		}
	}

	private final Runnable fire_runnable2 = new Runnable()
	{
		public void run()
		{
			ZLogger.log("MyWakefulService: fire_runnable2 callback");
			try{	
				ServiceManager sm = new ServiceManager();
				sm.updateOver(MyWakefulService.this, Constants.FIRE_UPDATE_OVER_FALSE_FLAG);
			}
			catch(Exception ex)
			{
				ZLogger.logException("MyWakefulService fire_runnable2", ex, getBaseContext());
			}
		}
	};

	// STEAL RETRY TIMER
	private static Handler steal_handler = new Handler();
	private void startStealRetryTimer()
	{
		ZLogger.log("MyWakefulService: startStealRetryTimer");
		steal_handler.postDelayed(steal_runnable, Constants.ONE_MINUTE);
	}

	private void stopStealRetryTimer()
	{
		ZLogger.log("MyWakefulService: stopStealRetryTimer");
		if(steal_handler!=null) {
			try {
				steal_handler.removeCallbacks(steal_runnable);
			}
			catch (Exception ex) {
				ZLogger.logException("MyWakefulService", ex, getBaseContext());
			}
		}
	}

	private final Runnable steal_runnable = new Runnable()
	{
		public void run()
		{
			ZLogger.log("MyWakefulService: steal_runnable callback");
			try{	
				startStealTimeout();
				new CustomServerPostTask(MyWakefulService.this, getResources().getString(R.string.UPDATE_TYPE_STEAL), false).execute();
			}
			catch(Exception ex)
			{
				ZLogger.logException("MyWakefulService steal_runnable", ex, getBaseContext());
			}
		}
	};

	// NO RESPONSE STEAL TIMEOUT

	private static Handler steal_timeout_handler = new Handler();
	private void startStealTimeout()
	{
		ZLogger.log("MyWakefulService: startTimeout");
		steal_timeout_handler.postDelayed(steal_runnable2,  Constants.ONE_MINUTE);
	}

	private void stopStealTimeoutCountdown()
	{
		ZLogger.log("MyWakefulService: stopStealTimeoutCountdown");
		if(steal_timeout_handler!=null) {
			try {
				steal_timeout_handler.removeCallbacks(steal_runnable2);
			}
			catch (Exception ex) {
				ZLogger.logException("MyWakefulService", ex, getBaseContext());
			}
		}
	}

	private final Runnable steal_runnable2 = new Runnable()
	{
		public void run()
		{
			ZLogger.log("MyWakefulService: steal_runnable2 callback");
			try{	
				ServiceManager sm = new ServiceManager();
				sm.updateOver(MyWakefulService.this, Constants.STEAL_UPDATE_OVER_FALSE_FLAG);
			}
			catch(Exception ex)
			{
				ZLogger.logException("MyWakefulService steal_runnable2", ex, getBaseContext());
			}
		}
	};
	
	// MANUAL RETRY TIMER
	private static Handler manual_handler = new Handler();
	private void startManualRetryTimer()
	{
		ZLogger.log("MyWakefulService: startManualRetryTimer");
		manual_handler.postDelayed(manual_runnable, Constants.ONE_MINUTE);
	}

	private void stopManualRetryTimer()
	{
		ZLogger.log("MyWakefulService: stopManualRetryTimer");
		if(manual_handler!=null) {
			try {
				manual_handler.removeCallbacks(manual_runnable);
			}
			catch (Exception ex) {
				ZLogger.logException("MyWakefulService", ex, getBaseContext());
			}
		}
	}

	private final Runnable manual_runnable = new Runnable()
	{
		public void run()
		{
			ZLogger.log("MyWakefulService: manual_runnable callback");
			try{	
				startManualTimeout();
				new CustomServerPostTask(MyWakefulService.this, getResources().getString(R.string.UPDATE_TYPE_MANUAL), false).execute();
			}
			catch(Exception ex)
			{
				ZLogger.logException("MyWakefulService manual_runnable", ex, getBaseContext());
			}
		}
	};

	// NO RESPONSE MANUAL TIMEOUT

	private static Handler manual_timeout_handler = new Handler();
	private void startManualTimeout()
	{
		ZLogger.log("MyWakefulService: startManualTimeout");
		manual_timeout_handler.postDelayed(manual_runnable2,  Constants.ONE_MINUTE);
	}

	private void stopManualTimeoutCountdown()
	{
		ZLogger.log("MyWakefulService: stopManualTimeoutCountdown");
		if(manual_timeout_handler!=null) {
			try {
				manual_timeout_handler.removeCallbacks(manual_runnable2);
			}
			catch (Exception ex) {
				ZLogger.logException("MyWakefulService", ex, getBaseContext());
			}
		}
	}

	private final Runnable manual_runnable2 = new Runnable()
	{
		public void run()
		{
			ZLogger.log("MyWakefulService: manual_runnable2 callback");
			try{	
				ServiceManager sm = new ServiceManager();
				sm.updateOver(MyWakefulService.this, Constants.MANUAL_UPDATE_OVER_FALSE_FLAG);
			}
			catch(Exception ex)
			{
				ZLogger.logException("MyWakefulService manual_runnable2", ex, getBaseContext());
			}
		}
	};
	

	// PUSH RETRY TIMER
	private static Handler push_handler = new Handler();
	private void startPushRetryTimer()
	{
		ZLogger.log("MyWakefulService: startPushRetryTimer");
		push_handler.postDelayed(push_runnable, Constants.ONE_MINUTE);
	}

	private void stopPushRetryTimer()
	{
		ZLogger.log("MyWakefulService: stopPushRetryTimer");
		if(push_handler!=null) {
			try {
				push_handler.removeCallbacks(push_runnable);
			}
			catch (Exception ex) {
				ZLogger.logException("MyWakefulService", ex, getBaseContext());
			}
		}
	}

	private final Runnable push_runnable = new Runnable()
	{
		public void run()
		{
			ZLogger.log("MyWakefulService: push_runnable callback");
			try{	
				startPushTimeout();
				new CustomServerPostTask(MyWakefulService.this, getResources().getString(R.string.UPDATE_TYPE_PUSH), false, phoneNumber).execute();
			}
			catch(Exception ex)
			{
				ZLogger.logException("MyWakefulService push_runnable", ex, getBaseContext());
			}
		}
	};

	// NO RESPONSE PUSH TIMEOUT

	private static Handler push_timeout_handler = new Handler();
	private void startPushTimeout()
	{
		ZLogger.log("MyWakefulService: startPushTimeout");
		push_timeout_handler.postDelayed(push_runnable2,  Constants.ONE_MINUTE);
	}

	private void stopPushTimeoutCountdown()
	{
		ZLogger.log("MyWakefulService: stopPushTimeoutCountdown");
		if(push_timeout_handler!=null) {
			try {
				push_timeout_handler.removeCallbacks(push_runnable2);
			}
			catch (Exception ex) {
				ZLogger.logException("MyWakefulService", ex, getBaseContext());
			}
		}
	}

	private final Runnable push_runnable2 = new Runnable()
	{
		public void run()
		{
			ZLogger.log("MyWakefulService: push_runnable2 callback");
			try{	
				ServiceManager sm = new ServiceManager();
				sm.updateOver(MyWakefulService.this, Constants.PUSH_UPDATE_OVER_FALSE_FLAG);
			}
			catch(Exception ex)
			{
				ZLogger.logException("MyWakefulService push_runnable2", ex, getBaseContext());
			}
		}
	};
	
	
	// RANDOM
	
	private final Runnable delayedStart_runnable = new Runnable()
	{
		public void run()
		{
			ZLogger.log("MyWakefulService: start of the delayed polling from WiFi wake lock retrieval");
			try{	
				ServiceLocationHelper.start(MyWakefulService.this);
			}
			catch(Exception ex)
			{
				ZLogger.logException("MyWakefulService r", ex, getBaseContext());
			}
		}
	};
	
	private void stopDelayedStart()
	{
		ZLogger.log("MyWakefulService: stopDelayedStart from WiFi wake lock retrieval");
		if(delayedStart_handler!=null) {
			try {
				delayedStart_handler.removeCallbacks(delayedStart_runnable);
			}
			catch (Exception ex) {
				ZLogger.logException("MyWakefulService", ex, getBaseContext());
			}
		}
	}

}
