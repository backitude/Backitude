package gaugler.backitude.http;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.MyWakefulService;
import gaugler.backitude.wakeful.OfflineLocationSyncService;
import gaugler.backitude.wakeful.ReSyncUpdateService;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;

public class TokenRefreshTask {

	public TokenRefreshTask(){ }

	private String accountName = Prefs.DEFAULT_accountName;

	private Service _service = null;
	private String _type = "";
	private boolean immediateRetry = false;
	private boolean waitingForRetry = false;
	private boolean realtimeRunning = false;
	private boolean showMessage = true;
	private boolean showSyncErrors = true;

	public void start(Service service, String type)
	{
		ZLogger.log("TokenRefreshTask start");

		immediateRetry = true;
		waitingForRetry = false;
		_type = type;
		_service = service;
		init();
	}

	private void restart() 
	{
		ZLogger.log("TokenRefreshTask restart");

		immediateRetry = false;

		startRetryTimer();	
		waitingForRetry = true;

		init();
	}

	private void retry()
	{
		ZLogger.log("TokenRefreshTask retry");

		immediateRetry = false;
		waitingForRetry = false;
		init();
	}

	private void init()
	{
		try
		{
			getAuthPrefs();

			if(accountName.length()>0){
				getGoogleAuth();
			}
			else
			{
				ZLogger.log("TokenRefreshTask init: Cannot Update Latitude, No account name");
				Toast.makeText(_service, _service.getResources().getString(R.string.account_prefs_summary), Toast.LENGTH_SHORT).show();
				exit();
			}
		}
		catch(Exception ex)
		{ 
			handleException(ex, true);
		}
	}

	public String authToken = PersistedData.DEFAULT_authToken;
	public GoogleAccountManager accountManager;

	private void getGoogleAuth()
	{
		accountManager = new GoogleAccountManager(_service);
		gotAccount();
	}

	private void gotAccount() {

		try
		{
			Account account = accountManager.getAccountByName(accountName);

			if (account != null) {
				ZLogger.log("TokenRefreshTask gotAccount: Invalidate old auth token: " + authToken);
				accountManager.invalidateAuthToken(authToken);
				authToken = null;

				determineAuthToken(account);
			}
			else
			{
				ZLogger.log("TokenRefreshTask gotAccount: Cannot obtain Account object for: " + accountName);
				exit();
			}
		}
		catch(Exception ex)
		{
			ZLogger.log("TokenRefreshTask gotAccount: " + ex.toString());
			handleException(ex, true);
		}
	}

	private void determineAuthToken(final Account account) {
		try
		{
			accountManager.getAccountManager().getAuthToken(account, Constants.SCOPE, false, new AccountManagerCallback<Bundle>() {
				@Override public void run(AccountManagerFuture<Bundle> result) {
					try {
						ZLogger.log("TokenRefreshTask determineAuthToken: start");
						Bundle bundle = result.getResult();
						ZLogger.log("TokenRefreshTask determineAuthToken: no exception");
						final String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
						final String refreshToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
						final Intent authIntent = bundle.getParcelable(AccountManager.KEY_INTENT);
						if (accountName != null && refreshToken != null) {
							ZLogger.log("TokenRefreshTask determineAuthToken: refreshed: " + refreshToken);
							final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_service.getBaseContext());
							final SharedPreferences.Editor editor = settings.edit();
							authToken = refreshToken;
							editor.putString(PersistedData.KEY_authToken, authToken);
							editor.commit();

							onAuthToken();
						} 
						else if (authIntent != null) 
						{
							ZLogger.log("TokenRefreshTask determineAuthToken: intent returned");
							/*
							try
							{
								authIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								_service.startActivity(authIntent);
							}
							catch (Exception ex) { 
								ZLogger.logException("TokenRefreshTask", ex, _service);
							}
							 */	
							if(immediateRetry)
							{
								ZLogger.log("TokenRefreshTask determineAuthToken: immediate restart");
								restart();
							}
							else if(waitingForRetry)
							{
								ZLogger.log("TokenRefreshTask determineAuthToken: Do nothing, waiting for token refresh retry");	
							}
							else
							{
								ZLogger.log("TokenRefreshTask determineAuthToken: Auth token was not able to be refreshed.");
								/*ZLogger.logException(Constants.TOKEN_EXCEPTION, new Exception(
										_service.getResources().getString(R.string.REFRESH_DENIED) + " " + _service.getResources().getString(R.string.TRY_AGAIN_LATER)
										), _service);*/
								exit();
							}
						} 
						else 
						{
							ZLogger.log("TokenRefreshTask determineAuthToken: AccountManager was unable to obtain an authToken - nothing returned");
							if(immediateRetry)
							{
								ZLogger.log("TokenRefreshTask determineAuthToken: immediate restart");
								restart();
							}
							else if(waitingForRetry)
							{
								ZLogger.log("TokenRefreshTask determineAuthToken: Do nothing, waiting for token refresh retry");	
							}
							else
							{								
								ZLogger.log("TokenRefreshTask determineAuthToken: AuthIntent is null, no retry... fail.");
								/*ZLogger.logException(Constants.TOKEN_EXCEPTION, new Exception(
								_service.getResources().getString(R.string.REFRESH_DENIED) + " " + _service.getResources().getString(R.string.TRY_AGAIN_LATER)
								), _service);*/
								exit();
							}
						}
					}
					catch (AuthenticatorException e)
					{
						ZLogger.log("TokenRefreshTask determineAuthToken: AuthenticatorException " + e.toString());
						handleException(e, false);
					}
					catch(OperationCanceledException e)
					{
						ZLogger.log("TokenRefreshTask determineAuthToken: OperationCanceledException " + e.toString());
						handleException(e, false);
					}
					catch(IOException e)
					{
						ZLogger.log("TokenRefreshTask determineAuthToken: IOException " + e.toString());
						handleException(e, false);
					}
					catch (Exception e) 
					{
						ZLogger.log("TokenRefreshTask determineAuthToken: run " + e.toString());
						handleException(e, true);
					}
				}
			}, null);
		}
		catch (Exception e) {
			ZLogger.log("TokenRefreshTask determineAuthToken: " + e.toString());
			handleException(e, true);
		}
	}

	private void onAuthToken() {
		ZLogger.log("TokenRefreshTask onAuthToken: start");
		// Stop Token refresh timer
		stop();

		//then....start another one for update
		if(_type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_POLL))){
			ZLogger.log("TokenRefreshTask onAuthToken: start retry timer for Poll before another attempt on CustomServerPostTask");
			Intent serviceIntent = new Intent(_service, MyWakefulService.class);
			serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.START_POLL_RETRY_TIMER);
			_service.getBaseContext().startService(serviceIntent);
		}
		else if(_type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_FIRE)))
		{
			ZLogger.log("TokenRefreshTask onAuthToken: start retry timer for Fire before another attempt on CustomServerPostTask");
			Intent serviceIntent = new Intent(_service, MyWakefulService.class);
			serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.START_FIRE_RETRY_TIMER);
			_service.getBaseContext().startService(serviceIntent);	
		}
		else if (_type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_STEAL)))
		{
			ZLogger.log("TokenRefreshTask onAuthToken: start retry timer for Steal before another attempt on CustomServerPostTask");
			Intent serviceIntent = new Intent(_service, MyWakefulService.class);
			serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.START_STEAL_RETRY_TIMER);
			_service.getBaseContext().startService(serviceIntent);
		}
		else if (_type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_RESYNC)))
		{
			ZLogger.log("TokenRefreshTask onAuthToken: start retry timer for Re-Sync before another attempt on CustomServerPostTask");
			Intent serviceIntent = new Intent(_service, ReSyncUpdateService.class);
			serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.START_RESYNC_RETRY_TIMER);
			_service.getBaseContext().startService(serviceIntent);
		}
		else if(_type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_MANUAL)))
		{
			ZLogger.log("TokenRefreshTask onAuthToken: start retry timer for Manual update before another attempt on CustomServerPostTask");
			Intent serviceIntent = new Intent(_service, MyWakefulService.class);
			serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.START_MANUAL_RETRY_TIMER);
			_service.getBaseContext().startService(serviceIntent);	
		}
		else if(_type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_PUSH)))
		{
			ZLogger.log("TokenRefreshTask onAuthToken: start retry timer for Push before another attempt on CustomServerPostTask");
			Intent serviceIntent = new Intent(_service, MyWakefulService.class);
			serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.START_PUSH_RETRY_TIMER);
			_service.getBaseContext().startService(serviceIntent);	
		}
		else
		{
			ZLogger.log("TokenRefreshTask onAuthToken: start retry timer for Offline Sync before another attempt on CustomServerPostTask");
			Intent serviceIntent = new Intent(_service, OfflineLocationSyncService.class);
			serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.START_SYNC_RETRY_TIMER);
			_service.getBaseContext().startService(serviceIntent);
		}

		if(_type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_OFFLINE_SYNC)))
		{
			ZLogger.log("TokenRefreshTask onAuthToken: call offline location sync task");
			try{
				new OfflineSyncTask(_service).execute();
			}
			catch (Exception e) 
			{
				ZLogger.log("TokenRefreshTask onAuthToken: " + e.toString());
				handleException(e, true);
			}
		}
		else
		{
			ZLogger.log("TokenRefreshTask onAuthToken: call location update task");
			try{
				new CustomServerPostTask(_service, _type).execute();
			}
			catch (Exception e) 
			{
				ZLogger.log("TokenRefreshTask onAuthToken: " + e.toString());
				handleException(e, true);
			}
		}
	}

	private void getAuthPrefs()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_service.getBaseContext());
		authToken = prefs.getString(PersistedData.KEY_authToken, PersistedData.DEFAULT_authToken);
		accountName = prefs.getString(Prefs.KEY_accountName, Prefs.DEFAULT_accountName);
		realtimeRunning = prefs.getBoolean(PersistedData.KEY_realtimeRunning, false);
		showMessage = prefs.getBoolean(Prefs.KEY_update_toast, true);
		showSyncErrors = prefs.getBoolean(Prefs.KEY_offlineSync_toast, true);
	}	

	private void handleException(Exception ex, boolean showNotif)
	{
		ZLogger.log("TokenRefreshTask handleException: " + ex.toString());
		if(immediateRetry)
		{
			ZLogger.log("TokenRefreshTask handleException: Immediate restart of Token refresh process");
			restart();
		}
		else if(_type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_POLL)) || _type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_FIRE)) ||  _type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_PUSH)) || _type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_OFFLINE_SYNC)) || _type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_MANUAL)))
		{
			// Don't wait on timeout for a Steal or ReSync
			if(waitingForRetry){
				ZLogger.log("TokenRefreshTask handleException: Do nothing, waiting for token refresh retry");	
			}
			else
			{
				// Already tried again...fail
				try{
					if(showMessage && !realtimeRunning && 
							(_type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_POLL)) || _type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_FIRE)) || _type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_PUSH)) || _type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_MANUAL)))
							|| (showSyncErrors && _type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_OFFLINE_SYNC)))
					){
						Toast.makeText(_service, _service.getResources().getString(R.string.TOKEN_REFRESH_FAILED), Toast.LENGTH_SHORT).show();
					}
					//if it didn't come from the getAuthToken method call
					if(showNotif){
						ZLogger.logException("TokenRefreshTask", ex, _service);
					}

				}
				catch(Exception ex1)
				{
					ZLogger.log("TokenRefreshTask handleException: exception on toast - " + ex1.toString());
				}
				exit();
			}
		}
		else
		{
			ZLogger.log("TokenRefreshTask handleException: fail on a " + _type);
			exit();
		}
	}

	private Handler handler = new Handler();
	private void startRetryTimer()
	{
		ZLogger.log("TokenRefreshTask startRetryTimer");
		handler.postDelayed(r,  Constants.ONE_MINUTE);
	}

	public void stop()
	{
		ZLogger.log("TokenRefreshTask stopRetryTimer");

		immediateRetry = false;
		waitingForRetry = false;

		if(handler!=null) {
			try {
				handler.removeCallbacks(r);
			}
			catch (Exception ex) {
				ZLogger.log("TokenRefreshTask stop: " + ex.toString());
			}
		}
	}

	private final Runnable r = new Runnable()
	{
		public void run()
		{
			ZLogger.log("TokenRefreshTask run: Runnable callback retry timer");
			retry();
		}
	};

	private void exit()
	{
		ZLogger.log("Fail Token Refresh: exit");

		stop();

		if(_service!=null){
			if(_type.equalsIgnoreCase(_service.getResources().getString(R.string.UPDATE_TYPE_OFFLINE_SYNC)))
			{
				ZLogger.log("Fail Token Refresh: Stop Offline Location Sync Service");
				Intent stopMyWakefulService = new Intent(_service, OfflineLocationSyncService.class);
				_service.stopService(stopMyWakefulService);
			}
			else
			{
				int param = Constants.POLL_UPDATE_OVER_FALSE_FLAG;

				if(_type.equals(_service.getResources().getString(R.string.UPDATE_TYPE_POLL)))
				{
					param = Constants.POLL_UPDATE_OVER_FALSE_FLAG;
				}
				else if(_type.equalsIgnoreCase(_service.getResources().getString(R.string.UPDATE_TYPE_STEAL)))
				{
					param = Constants.STEAL_UPDATE_OVER_FALSE_FLAG;
				}
				else if (_type.equalsIgnoreCase(_service.getResources().getString(R.string.UPDATE_TYPE_RESYNC)))
				{
					param = Constants.RESYNC_UPDATE_OVER_FALSE_FLAG;
				}
				else if (_type.equalsIgnoreCase(_service.getResources().getString(R.string.UPDATE_TYPE_FIRE)))
				{
					param = Constants.FIRE_UPDATE_OVER_FALSE_FLAG;
				}
				else if (_type.equalsIgnoreCase(_service.getResources().getString(R.string.UPDATE_TYPE_PUSH)))
				{
					param = Constants.PUSH_UPDATE_OVER_FALSE_FLAG;
				}
				else if (_type.equalsIgnoreCase(_service.getResources().getString(R.string.UPDATE_TYPE_MANUAL)))
				{
					param = Constants.MANUAL_UPDATE_OVER_FALSE_FLAG;
				}

				ServiceManager sm = new ServiceManager();
				sm.updateOver(_service, param);
			}
		}
	}
}
