package gaugler.backitude.http;

import gaugler.backitude.R;
import gaugler.backitude.constants.AuthenticationOptionsEnum;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.ExportOptionsEnum;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.DatabaseHelper;
import gaugler.backitude.util.ExportHelper;
import gaugler.backitude.util.PreferenceHelper;
import gaugler.backitude.util.ServiceHelper;
import gaugler.backitude.util.ZLogger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Base64;
import android.widget.Toast;

public class CustomServerPostTask extends AsyncTask<Void, Void, Boolean> {

	//private final Latitude latitude;

	/** Global instance of the HTTP transport. */
	//private final HttpTransport HTTP_TRANSPORT;

	/** Global instance of the GSON factory. */
	//private final GsonFactory GSON_FACTORY;
	//public static final GoogleClientRequestInitializer KEY_INITIALIZER = new LatitudeRequestInitializer("AIzaSyC11cLsqHVwuYNfPq5JICz1qsl9tRTF1KY");

	private String responseCode = ""; 
	private Location _location;
	private Context _context;
	private String _type;
	private String _phoneNumber;
	private boolean _refreshToken = true;
	private boolean _retry = true;

	private String authenticationType = AuthenticationOptionsEnum.NONE.getString();
	private boolean realtimeRunning = false;
	private boolean showMessage = true;
	private String accountName = Prefs.DEFAULT_accountName;
	//private String authToken = PersistedData.DEFAULT_authToken;
	private boolean offlineStorageEnabled = false;
	private boolean wifiOnlyEnabled = true;
	private String serverUrl = Prefs.DEFAULT_server_url;
	private boolean pingBackEnabled = false;
	private String exportType = ExportOptionsEnum.NONE.getString();
	private boolean timeZone = false;

	private boolean isPermissionsProblem = false;

	// Normal fresh update attempt #1
	public CustomServerPostTask(Context context, Location location, String type) {
		_refreshToken = true;
		_retry = true;
		_location = location;
		_context = context;
		_type = type;

		getPrefs();

		/*
		GSON_FACTORY = new GsonFactory();
		HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

		latitude = new Latitude.Builder(HTTP_TRANSPORT, GSON_FACTORY, new GoogleCredential().setAccessToken(authToken))
		.setApplicationName("Backitude/4.1")
		.setLatitudeRequestInitializer(new LatitudeRequestInitializer("AIzaSyC11cLsqHVwuYNfPq5JICz1qsl9tRTF1KY") {
			@Override
			public void initializeLatitudeRequest(LatitudeRequest<?> request) {
				HttpHeaders headers = new HttpHeaders();
				headers.set("Connection", "Close");	
				request.setPrettyPrint(true);
				request.setRequestHeaders(headers);
			}
		}).build();
		 */

		ZLogger.log("CustomServerPostTask start 1: " + _type);
	}

	// After token refresh attempt #1
	public CustomServerPostTask(Service service, String type) {

		ZLogger.log("CustomServerPostTask constructor: Second attempt after token refresh");

		_refreshToken = false;
		_retry = true;
		_location = null;
		_context = service;
		_type = type;

		getPrefs();

		/*
		GSON_FACTORY = new GsonFactory();
		HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

		latitude = new Latitude.Builder(HTTP_TRANSPORT, GSON_FACTORY, new GoogleCredential().setAccessToken(authToken))
		.setApplicationName("Backitude/4.1")
		.setLatitudeRequestInitializer(new LatitudeRequestInitializer("AIzaSyC11cLsqHVwuYNfPq5JICz1qsl9tRTF1KY") {
			@Override
			public void initializeLatitudeRequest(LatitudeRequest<?> request) {
				HttpHeaders headers = new HttpHeaders();
				headers.set("Connection", "Close");	
				request.setPrettyPrint(true);
				request.setRequestHeaders(headers);
			}
		}).build();
		 */

		ZLogger.log("CustomServerPostTask token refresh 1: " + _type);
	}

	// Possible ReTry attempt after a failure
	public CustomServerPostTask(Service service, String type, boolean retry) {

		ZLogger.log("CustomServerPostTask constructor: Second attempt, retry");

		_refreshToken = true;
		_retry = retry;
		_location = null;
		_context = service;
		_type = type;

		getPrefs();

		/*
		GSON_FACTORY = new GsonFactory();
		HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

		latitude = new Latitude.Builder(HTTP_TRANSPORT, GSON_FACTORY, new GoogleCredential().setAccessToken(authToken))
		.setApplicationName("Backitude/4.1")
		.setLatitudeRequestInitializer(new LatitudeRequestInitializer("AIzaSyC11cLsqHVwuYNfPq5JICz1qsl9tRTF1KY") {
			@Override
			public void initializeLatitudeRequest(LatitudeRequest<?> request) {
				HttpHeaders headers = new HttpHeaders();
				headers.set("Connection", "Close");	
				request.setPrettyPrint(true);
				request.setRequestHeaders(headers);
			}
		}).build();
		 */

		ZLogger.log("CustomServerPostTask retry 1: " + _type);
	}

	// Initial update attempt #1 for push update
	public CustomServerPostTask(Context context, Location location, String type, String phoneNumber) {
		_refreshToken = true;
		_retry = true;
		_location = location;
		_context = context;
		_type = type;
		_phoneNumber = phoneNumber;

		getPrefs();

		/*
		GSON_FACTORY = new GsonFactory();
		HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

		latitude = new Latitude.Builder(HTTP_TRANSPORT, GSON_FACTORY, new GoogleCredential().setAccessToken(authToken))
		.setApplicationName("Backitude/4.1")
		.setLatitudeRequestInitializer(new LatitudeRequestInitializer("AIzaSyC11cLsqHVwuYNfPq5JICz1qsl9tRTF1KY") {
			@Override
			public void initializeLatitudeRequest(LatitudeRequest<?> request) {
				HttpHeaders headers = new HttpHeaders();
				headers.set("Connection", "Close");	
				request.setPrettyPrint(true);
				request.setRequestHeaders(headers);
			}
		}).build();
		 */

		ZLogger.log("CustomServerPostTask push start 1: " + _type);
	}

	// Possible ReTry attempt after a failure on push update
	public CustomServerPostTask(Service service, String type, boolean retry, String phoneNumber) {

		ZLogger.log("CustomServerPostTask constructor: Second attempt, retry");

		_refreshToken = true;
		_retry = retry;
		_location = null;
		_context = service;
		_type = type;
		_phoneNumber = phoneNumber;

		getPrefs();

		/*
		GSON_FACTORY = new GsonFactory();
		HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

		latitude = new Latitude.Builder(HTTP_TRANSPORT, GSON_FACTORY, new GoogleCredential().setAccessToken(authToken))
		.setApplicationName("Backitude/4.1")
		.setLatitudeRequestInitializer(new LatitudeRequestInitializer("AIzaSyC11cLsqHVwuYNfPq5JICz1qsl9tRTF1KY") {
			@Override
			public void initializeLatitudeRequest(LatitudeRequest<?> request) {
				HttpHeaders headers = new HttpHeaders();
				headers.set("Connection", "Close");	
				request.setPrettyPrint(true);
				request.setRequestHeaders(headers);
			}
		}).build();
		 */

		ZLogger.log("CustomServerPostTask push retry 1: " + _type);
	}


	@Override
	protected void onPreExecute() {

		ZLogger.log("CustomServerPostTask onPreExecute: Start for type " + _type);

		// CHECK THE CONTEXT
		if(_context==null)
		{
			ZLogger.log("CustomServerPostTask onPreExecute: Service is null");
			this.cancel(true);
			return;
		}

		// if location is not null, we polled a new location or stole a new location.
		// if location is null, it is either a Re-Sync and we can skip this step or the update failed
		//   on the first attempt, and is going to retry so we still want to skip this step
		if(_location!=null) {
			PreferenceHelper.setLastPolledLocation(_context, _location);

			// Internal Memory storage
			if(exportType.equals(ExportOptionsEnum.KML.getString())){
				ExportHelper.writeKML(_location);
			} else if(exportType.equals(ExportOptionsEnum.CSV.getString())){
				ExportHelper.writeCSV(_context, _location);
			} else {
				ZLogger.log("CustomServerPostTask onPreExecute: No internal memory storage specified");
			}

			// SMS Reply on Push Updates
			if(ServiceHelper.isNetworkAvailable(_context)){
				if(pingBackEnabled && _type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_PUSH)) && _phoneNumber!=null && _phoneNumber.length()>0) {
					sendSMS(_context, _location, _phoneNumber);
				}
			}
		}

		/*
		// CHECK THE ACCOUNT - no longer a requirement?
		if(accountName.length()==0)
		{
			ZLogger.log("CustomServerPostTask onPreExecute: Cannot update Latitude (Account name blank)");
			exit("Cannot update Latitude (Account name blank)", 1);
			this.cancel(true);
			return;
		}
		 */

		// Make sure there is a server to send the location to, or offline storage is enabled
		if(serverUrl.length()<=Prefs.DEFAULT_server_url.length()){
			if(_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_RESYNC)) || !offlineStorageEnabled){
				ZLogger.log("CustomServerPostTask onPreExecute: Cancel task (server url is not defined)");
				exit("Cannot send data (No destination)", 6);
				this.cancel(true);
				return;
			} else {
				ZLogger.log("CustomServerPostTask onPreExecute: server url is not defined, but offline storage is enabled");
			}
		}

		// CHECK FOR DATA NETWORK SIGNAL
		if(!ServiceHelper.isNetworkAvailable(_context)){
			ZLogger.log("CustomServerPostTask onPreExecute: No network service available at this time");
			if (_location!=null)
			{				
				// Location exists from Steal, Poll, Fire, or Push
				if(offlineStorageEnabled)
				{
					ZLogger.log("CustomServerPostTask onPreExecute: Offline storage enabled, save location and exit");
					exit("Cannot update Latitude (No network service)", 2);
					this.cancel(true);
					return;

				}
				else{
					if(_retry)
					{
						ZLogger.log("CustomServerPostTask onPreExecute: No exit strategy, wait for retry timer to fire on the service");
					}
					else
					{
						ZLogger.log("CustomServerPostTask onPreExecute: Retry already attempted or not retrying for steals.  Update failed.");
						exit("Cannot update Latitude (No network service)", 2);
					}
				}
			}
			else
			{
				ZLogger.log("CustomServerPostTask onPreExecute: No service on a resync or location repeat");
				exit("Cannot update Latitude (No network service)", 2);
			}
			this.cancel(true);
			return;
		}
		else
		{
			// DATA NETWORK SIGNAL Available but is it Wi-Fi only update mode?
			if(wifiOnlyEnabled){
				if(ServiceHelper.isConnectedToWifi(_context))
				{
					// Perform update on Poll, Fire, Re-Sync or whatever
					ZLogger.log("CustomServerPostTask onPreExecute: Wi-Fi connected for Data Saver mode");
				}
				else
				{
					ZLogger.log("CustomServerPostTask onPreExecute: Wi-Fi not available, store location for future sync");
					exit("Cannot update Latitude (No Wi-Fi service)", 5);
					this.cancel(true);
					return;
				}
			}
		}

		// CHECK TO SEE IF WE ARE RE-SYNC'ing, null location means re-sync
		if(_location==null) {
			// we're just duplicating the last update for Latitude Re-Sync
			_location = PreferenceHelper.getLastPolledLocation(_context);
		}

		// Finally, just make sure there is definitely a location to update
		if(_location==null) {
			exit("Cannot send data (No location value)", 3);
			this.cancel(true);
			return;
		}

		ZLogger.log("CustomServerPostTask onPreExecute: exit successfully, ready to update");

	}

	@Override
	protected Boolean doInBackground(Void... params) {

		try 
		{	
			ZLogger.log("CustomServerPostTask doInBackground: start for type " + _type);
			if(!this.isCancelled()){

				HttpResponse result = null;
				if(_location!=null && serverUrl.length()>Prefs.DEFAULT_server_url.length()){
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httppost = new HttpPost(serverUrl);

					ZLogger.log("CustomServerPostTask doInBackground: setup http objects");
					try {
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
						ParameterBean parms = new ParameterBean(_context);
						if(authenticationType.equals(AuthenticationOptionsEnum.BASIC_AUTH.getString())){
							ZLogger.log("CustomServerPostTask doInBackground: Basic Auth");
							httppost.setHeader("Authorization", "Basic " + Base64.encodeToString((prefs.getString(Prefs.KEY_server_user_name, Prefs.DEFAULT_server_user_name) + ":" +  prefs.getString(Prefs.KEY_server_password, Prefs.DEFAULT_server_password)).getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));
						} else if (authenticationType.equals(AuthenticationOptionsEnum.POST_PARAMS.getString())){
							ZLogger.log("CustomServerPostTask doInBackground: Credentials in params");
							parms.set_userName(prefs.getString(Prefs.KEY_server_user_name, Prefs.DEFAULT_server_user_name));
							parms.set_password(prefs.getString(Prefs.KEY_server_password, Prefs.DEFAULT_server_password));
						} else if (authenticationType.equals(AuthenticationOptionsEnum.BOTH.getString())) {
							ZLogger.log("CustomServerPostTask doInBackground: Basic Auth and POST params");
							httppost.setHeader("Authorization", "Basic " + Base64.encodeToString((prefs.getString(Prefs.KEY_server_user_name, Prefs.DEFAULT_server_user_name) + ":" +  prefs.getString(Prefs.KEY_server_password, Prefs.DEFAULT_server_password)).getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));
							parms.set_userName(prefs.getString(Prefs.KEY_server_user_name, Prefs.DEFAULT_server_user_name));
							parms.set_password(prefs.getString(Prefs.KEY_server_password, Prefs.DEFAULT_server_password));
						} else {
							ZLogger.log("CustomServerPostTask doInBackground: No credentials sent");
						}

						parms.set_latitude(String.valueOf(_location.getLatitude()));
						parms.set_longitude(String.valueOf(_location.getLongitude()));
						if(_location.hasAccuracy()){
							parms.set_accuracy(String.valueOf(_location.getAccuracy()));
						}
						if(_location.hasSpeed()){
							parms.set_speed(String.valueOf(_location.getSpeed()));
						}
						if(_location.hasAltitude()){
							parms.set_altitude(String.valueOf(_location.getAltitude()));
						}
						if(_location.hasBearing()){
							parms.set_bearing(String.valueOf(_location.getBearing()));
						}

						Date now = new Date();
						int offsetFromUtc = 0;
						if(timeZone){
							TimeZone tz = TimeZone.getDefault();
							offsetFromUtc = tz.getOffset(now.getTime());
							ZLogger.log("CustomServerPostTask doInBackground: time zone offset = " + offsetFromUtc);
						} else {
							ZLogger.log("CustomServerPostTask doInBackground: ignore time zone offset");
						}
						long locationTs = (_location.getTime() + offsetFromUtc);
						long requestTs = (System.currentTimeMillis() + (offsetFromUtc));
						parms.set_locTs(String.valueOf(locationTs));
						parms.set_reqTs(String.valueOf(requestTs));
						ZLogger.log("CustomServerPostTask param values: " + parms.toString());
						httppost.setEntity(new UrlEncodedFormEntity(parms.getNameValuePairs()));
						// Execute HTTP Post Request
						ZLogger.log("CustomServerPostTask doInBackground: Request Line before execute  " + httppost.getRequestLine());
						result = httpclient.execute(httppost);
						if(result!=null)
						{
							ZLogger.log("CustomServerPostTask response status line: " + result.getStatusLine().toString());
							responseCode = result.getStatusLine().toString();
						}
						else
						{
							ZLogger.log("CustomServerPostTask response == null");
						}

					} catch (ClientProtocolException e) {
						handleIOException(new IOException(e.toString()));
					} catch (IOException e) {
						handleIOException(e);
					}

				} else {
					ZLogger.log("CustomServerPostTask doInBackground:  location is null or serverUrl is null");
					return null;
				}

				if(result!=null)
				{
					if(result.getStatusLine().getStatusCode()==200){
						ZLogger.log("doCustomServerPostTask doInBackground: result = 200 return SUCCESS");
						return (true);
					}
					else
					{
						ZLogger.logException("CustomServerPostTask", new Exception("Custom Server POST failure: " + result.getStatusLine().toString()), _context);
						if (_retry)
						{
							ZLogger.log( "CustomServerPostTask doInBackground: return false for retry");
							return false;
						}
						else
						{
							ZLogger.log( "CustomServerPostTask doInBackground: return null to exit, no retries left");
							return null;
						}
					}
				}
				else
				{
					if (_retry)
					{
						ZLogger.log( "CustomServerPostTask doInBackground: Result is null (return false for retry)");
						return false;
					}
					else
					{
						ZLogger.log( "CustomServerPostTask doInBackground: Result is null (return null to exit)");
						return null;
					}
				}
			}
			else  // Task was Cancelled
			{
				if (_retry)
				{
					// Return false for Retry routine
					ZLogger.log( "CustomServerPostTask doInBackground: Task was canceled (return false for retry)");
					return false;
				}
				else{
					ZLogger.log( "CustomServerPostTask doInBackground: Task was canceled (return null to exit)");
					return null;
				}
			}

		} 
		catch(Exception e)
		{
			ZLogger.log("CustomServerPostTask doInBackground catch Exception" + e.toString());
			return handleIOException(new IOException(e.toString()));
		}	
	}

	@Override
	protected void onPostExecute(Boolean feed) {

		ZLogger.log("CustomServerPostTask onPostExecute: start");	
		if (feed == null) {
			ZLogger.log("CustomServerPostTask onPostExecute: feed is null");
			exit("Response feed from server is null.", 3);
			this.cancel(true);
			return;
		}
		else if(feed == false)
		{
			ZLogger.log("CustomServerPostTask onPostExecute: Just exit and wait for token refresh or retry timer");
			this.cancel(true);
			return;
		}

		ZLogger.log("CustomServerPostTask onPostExecute: Success "  + _type);

		updateMostRecentLocationValues();

		if(showMessage && 
				(
						(_type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_POLL)) && !realtimeRunning) ||
						_type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_FIRE)) || 
						_type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_PUSH)) || 
						_type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_MANUAL))
						)
				)
		{
			Toast.makeText(_context, _context.getResources().getString(R.string.SUCCESS), Toast.LENGTH_SHORT).show();
		}
		success(_type);

		ZLogger.log("CustomServerPostTask onPostExecute: return");
		this.cancel(true);

	}

	private void exit(String msg, int toastType)
	{
		ZLogger.log("CustomServerPostTask Exit: " + msg);

		toastType = storeLocation(toastType);

		if(showMessage && 
				((!realtimeRunning && _type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_POLL))) || _type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_FIRE)) || _type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_PUSH)) || _type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_MANUAL)))
				)
		{
			switch(toastType)
			{
			case 1:
				Toast.makeText(_context, _context.getResources().getString(R.string.NO_UPDATE_NO_ACCOUNT), Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(_context, _context.getResources().getString(R.string.NO_UPDATE_NO_NETWORK), Toast.LENGTH_SHORT).show();
				break;
			case 3:
				if(isPermissionsProblem)
				{
					Toast.makeText(_context, _context.getResources().getString(R.string.UPDATE_FAILED_403), Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(_context, "Backitude Failure: " + responseCode, Toast.LENGTH_LONG).show();
					//Toast.makeText(_context, _context.getResources().getString(R.string.UPDATE_FAILED), Toast.LENGTH_SHORT).show();
				}
				break;
			case 4:
				Toast.makeText(_context, _context.getResources().getString(R.string.LOCATION_STORED), Toast.LENGTH_SHORT).show();
				break;
			case 5:
				Toast.makeText(_context, _context.getResources().getString(R.string.NO_UPDATE_NO_WIFI), Toast.LENGTH_LONG).show();
				break;
			case 6:
				Toast.makeText(_context, _context.getResources().getString(R.string.NO_UPDATE_NO_DEST), Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		}

		int param = Constants.POLL_UPDATE_OVER_FALSE_FLAG;

		if(_type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_POLL)) && toastType == 4)
		{
			param = Constants.POLL_UPDATE_OVER_TRUE_FLAG;
		}
		else if(_type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_POLL)))
		{
			param = Constants.POLL_UPDATE_OVER_FALSE_FLAG;
		}
		else if(_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_STEAL)) && toastType == 4)
		{
			param = Constants.STEAL_UPDATE_OVER_TRUE_FLAG;
		}
		else if(_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_STEAL)))
		{
			param = Constants.STEAL_UPDATE_OVER_FALSE_FLAG;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_RESYNC)))
		{
			param = Constants.RESYNC_UPDATE_OVER_FALSE_FLAG;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_FIRE)) && toastType == 4)
		{
			param = Constants.FIRE_UPDATE_OVER_TRUE_FLAG;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_FIRE)))
		{
			param = Constants.FIRE_UPDATE_OVER_FALSE_FLAG;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_MANUAL)) && toastType == 4)
		{
			param = Constants.MANUAL_UPDATE_OVER_TRUE_FLAG;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_MANUAL)))
		{
			param = Constants.MANUAL_UPDATE_OVER_FALSE_FLAG;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_PUSH)) && toastType == 4)
		{
			param = Constants.PUSH_UPDATE_OVER_TRUE_FLAG;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_PUSH)))
		{
			param = Constants.PUSH_UPDATE_OVER_FALSE_FLAG;
		}

		ServiceManager sm = new ServiceManager();
		sm.updateOver(_context, param);
	}

	private void success(String msg)
	{

		ZLogger.log("CustomServerPostTask success: " + msg);
		int param = Constants.POLL_UPDATE_OVER_TRUE_FLAG;

		if(_type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_POLL)))
		{
			param = Constants.POLL_UPDATE_OVER_TRUE_FLAG;
		}
		else if(_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_STEAL)))
		{
			param = Constants.STEAL_UPDATE_OVER_TRUE_FLAG;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_RESYNC)))
		{
			param = Constants.RESYNC_UPDATE_OVER_TRUE_FLAG;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_FIRE)))
		{
			param = Constants.FIRE_UPDATE_OVER_TRUE_FLAG;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_PUSH)))
		{
			param = Constants.PUSH_UPDATE_OVER_TRUE_FLAG;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_MANUAL)))
		{
			param = Constants.MANUAL_UPDATE_OVER_TRUE_FLAG;
		}

		ServiceManager sm = new ServiceManager();
		sm.updateOver(_context, param);
	}

	private int storeLocation(int toastType) {
		ZLogger.log("CustomServerPostTask storeLocation: " + offlineStorageEnabled);
		ZLogger.log("CustomServerPostTask storeLocation: " + (_location!=null));
		if(_location!=null){

			if(offlineStorageEnabled && (_type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_POLL)) || _type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_STEAL)) || _type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_FIRE)) || _type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_PUSH)) || _type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_MANUAL))))
			{
				ZLogger.log("CustomServerPostTask exit: Offline storage enabled, save location");
				SQLiteDatabase sampleDB = null;
				try{
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
					sampleDB =  _context.openOrCreateDatabase(Constants.OFFLINE_LOCATION_DB, Context.MODE_PRIVATE, null);
					if(DatabaseHelper.isTableExists(_context, sampleDB, Constants.OFFLINE_LOCATION_TABLE)){

						Date now = new Date();
						StringBuilder sql = new StringBuilder();
						sql.append("INSERT INTO ");
						sql.append(Constants.OFFLINE_LOCATION_TABLE);
						sql.append(" (account, latitude, longitude, TimestampMs, PollingTimestampMs, accuracy, speed, altitude, bearing) ");
						sql.append(" Values ('");
						sql.append(accountName);
						sql.append("', ");
						sql.append(_location.getLatitude()).append(", ");
						sql.append(_location.getLongitude()).append(", ");
						sql.append(_location.getTime()).append(", ");
						sql.append(now.getTime()).append(", ");
						if(_location.hasAccuracy()){
							ZLogger.log("CustomServerPostTask storeLocation: accuracy = " + _location.getAccuracy());
							sql.append(_location.getAccuracy()).append(", ");
						} else {
							ZLogger.log("CustomServerPostTask storeLocation: accuracy stored as NULL"); 
							sql.append("NULL, ");
						}
						if(_location.hasSpeed()){
							sql.append(_location.getSpeed()).append(", ");
						} else {
							sql.append("NULL, ");
						}
						if(_location.hasAltitude()){
							sql.append(_location.getAltitude()).append(", ");
						} else {
							sql.append("NULL, ");
						}
						if(_location.hasBearing()){
							sql.append(_location.getBearing()).append("); ");
						} else {
							sql.append("NULL); ");
						}

						sampleDB.execSQL(sql.toString());

						// TOGGLE the sync flag so the Activity refreshes if it is active
						boolean temp = prefs.getBoolean(Prefs.KEY_offlineSync_flag, false);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putBoolean(Prefs.KEY_offlineSync_flag, !temp);
						editor.commit();

						ZLogger.log("CustomServerPostTask exit: Successfully stored for future sync");

						updateMostRecentLocationValues();

						toastType = 4;  //Display successful storage message
					}
					else
					{
						toastType = 3;  //Display update failed message	
					}
					sampleDB.close();	
				}
				catch(Exception ex){
					ZLogger.log("CustomServerPostTask onPreExecute: " + ex.toString());
					if(sampleDB!=null) { sampleDB.close();}
					toastType = 3;  //Display update failed message
				}
			}
		}
		return toastType;
	}

	private void updateMostRecentLocationValues() {

		ZLogger.log("CustomServerPostTask updateMostRecentLocationValues: start");

		try{
			// Update the values displayed on VIEW MOST RECENT LOCATION HISTORY screen

			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");

			Date now = new Date();
			int offsetFromUtc = 0;
			if(timeZone){
				TimeZone tz = TimeZone.getDefault();
				offsetFromUtc = tz.getOffset(now.getTime());
				ZLogger.log("CustomServerPostTask updateMostRecentLocationValues: time zone offset = " + offsetFromUtc);
			} else {
				ZLogger.log("CustomServerPostTask updateMostRecentLocationValues: time zone offset is ignored");
			}
			long locationTs = (_location.getTime() + offsetFromUtc);
			long requestTs = (System.currentTimeMillis() + (offsetFromUtc));
			Date locTime = new Date(locationTs);  
			Date nowDate = new Date(requestTs);
			String nowDateFormatted = sdf.format(nowDate);

			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(PersistedData.KEY_savedLocation_time, sdf.format(locTime));
			editor.putString(PersistedData.KEY_savedLocation_UpdateTime, nowDateFormatted);
			editor.putFloat(PersistedData.KEY_savedLocation_lat, (float)_location.getLatitude());
			editor.putFloat(PersistedData.KEY_savedLocation_long, (float)_location.getLongitude());
			if(_location.hasAccuracy()){
				editor.putFloat(PersistedData.KEY_savedLocation_accur, _location.getAccuracy());
			} else {
				editor.remove(PersistedData.KEY_savedLocation_accur);
			}
			if(_location.hasSpeed()){
				editor.putFloat(PersistedData.KEY_savedLocation_speed, _location.getSpeed());
			} else {
				editor.remove(PersistedData.KEY_savedLocation_speed);
			}
			if(_location.hasAltitude()){
				editor.putFloat(PersistedData.KEY_savedLocation_altitude, (long)_location.getAltitude());
			} else {
				editor.remove(PersistedData.KEY_savedLocation_altitude);
			}
			if(_location.hasBearing()){
				editor.putFloat(PersistedData.KEY_savedLocation_bearing, _location.getBearing());
			} else {
				editor.remove(PersistedData.KEY_savedLocation_bearing);
			}
			editor.putString(PersistedData.KEY_savedLocation_type, _type);
			editor.commit();

			ZLogger.log("CustomServerPostTask updateMostRecentLocationValues: Location saved in app data as last update");
		}
		catch(Exception ex){
			ZLogger.logException("CustomServerPostTask", ex, _context);
		}

	}

	private Boolean handleIOException(IOException e) {

		ZLogger.log("CustomServerPostTask handleIOException: start");
		/*
		if (e instanceof GoogleJsonResponseException) {
			GoogleJsonResponseException exception = (GoogleJsonResponseException) e;
			ZLogger.log("CustomServerPostTask handleIOException exception: " + exception.toString());

			if(exception.getStatusCode() == 401)
			{
				if(_refreshToken)
				{
					if(_type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_RESYNC)))
					{
						Intent serviceIntent = new Intent(_context, ReSyncUpdateService.class);
						serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.REFRESH_AUTH_TOKEN_RESYNC);
						_context.startService(serviceIntent);
					}
					else
					{
						Intent serviceIntent = new Intent(_context, MyWakefulService.class);
						serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, getRefreshTokenCode());
						_context.startService(serviceIntent);
					}
					ZLogger.log("CustomServerPostTask handleIOException: return false for token refresh");
					return false;
				}
				else
				{
					ZLogger.log("CustomServerPostTask handleIOException (401): Already tried refreshing token, still getting 401");
					return null;
				}
			}
			else if (_retry)
			{
				// Return false for Retry routine
				ZLogger.log("CustomServerPostTask handleIOException (" + exception.getStatusCode() + "): return false for RETRY");
				return false;
			}
			else if((exception.getStatusCode() == 403 || exception.getStatusCode() == 503) &&  (_type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_POLL)) || _type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_FIRE))|| _type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_PUSH)) || _type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_MANUAL))))
			{
				// Return null for 403/503 permissions problem
				ZLogger.log("CustomServerPostTask handleException: return null for permissions problem");
				isPermissionsProblem = true;
				if(exception.getStatusCode() == 403)
				{
					if(exception.toString().indexOf("Daily Limit Exceeded")>0)
					{
						ZLogger.logException(Constants.PERMISSION_EXCEPTION, new Exception(exception.toString() + " " + _context.getResources().getString(R.string.PERMISSION_NOTIFICATION_403B)), _context);
					}
					else
					{
						ZLogger.logException(Constants.PERMISSION_EXCEPTION, new Exception(exception.toString() + " " + _context.getResources().getString(R.string.PERMISSION_NOTIFICATION_403)), _context);			
					}
				}
				else
				{
					ZLogger.logException(Constants.PERMISSION_EXCEPTION, new Exception(exception.toString() + " " + _context.getResources().getString(R.string.PERMISSION_NOTIFICATION_503)), _context);
				}
				return null;
			}
			ZLogger.log("CustomServerPostTask handleIOException response (" + exception.getStatusCode() + ") for: " + _type);
		}
		else
		{*/
		ZLogger.log("CustomServerPostTask handleIOException: exception is " + e.toString());
		if(_retry)
		{
			// retry
			ZLogger.log("CustomServerPostTask handleIOException: return false for RETRY");
			return false;
		}
		else
		{
			ZLogger.log("CustomServerPostTask handleIOException: already attempted retry");
		}
		//}
		//else 
		//{
		//	ZLogger.log("CustomServerPostTask handleIOException handleException: no retry for type " + _type);
		//}

		//}
		ZLogger.log("CustomServerPostTask handleIOException: return null - no retry");
		return null;
	}

	private int getRefreshTokenCode() {

		if(_type.equals(_context.getResources().getString(R.string.UPDATE_TYPE_POLL)))
		{
			return Constants.REFRESH_AUTH_TOKEN_POLL;
		}
		else if(_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_STEAL)))
		{
			return Constants.REFRESH_AUTH_TOKEN_STEAL;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_FIRE)))
		{
			return Constants.REFRESH_AUTH_TOKEN_FIRE;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_PUSH)))
		{
			return Constants.REFRESH_AUTH_TOKEN_PUSH;
		}
		else if (_type.equalsIgnoreCase(_context.getResources().getString(R.string.UPDATE_TYPE_MANUAL)))
		{
			return Constants.REFRESH_AUTH_TOKEN_MANUAL;
		}
		else{
			return Constants.REFRESH_AUTH_TOKEN_POLL;
		}
	}

	private void sendSMS(Context context, Location location, String phoneNumber) {

		ZLogger.log("CustomServerPostTask sendSMS: start");

		try{
			PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(), 0);
			SmsManager sms = SmsManager.getDefault();

			StringBuilder sb = new StringBuilder();
			sb.append(context.getString(R.string.SHARE_LOCATION_BODY));
			sb.append(" http://maps.google.com/maps?q=loc:");
			sb.append(location.getLatitude());
			sb.append(",");
			sb.append(location.getLongitude());
			sb.append(" ");
			sb.append(context.getString(R.string.SHARE_LOCATION_BODY_2));
			ZLogger.log("CustomServerPostTask sendSMS: " + sb.toString());

			if(phoneNumber!=null && phoneNumber.length()>0)
				sms.sendTextMessage(phoneNumber, null, sb.toString(), pi, null);
		}
		catch(Exception e){
			ZLogger.logException("CustomServerPostTask", e, _context);
		}
		ZLogger.log("CustomServerPostTask sendSMS: end");
	}


	private void getPrefs(){
		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);

		realtimeRunning = prefs.getBoolean(PersistedData.KEY_realtimeRunning, false);
		showMessage = prefs.getBoolean(Prefs.KEY_update_toast, true);
		accountName = prefs.getString(Prefs.KEY_accountName, Prefs.DEFAULT_accountName);
		//authToken = prefs.getString(PersistedData.KEY_authToken, PersistedData.DEFAULT_authToken);
		offlineStorageEnabled = prefs.getBoolean(Prefs.KEY_offlineSync, false);
		wifiOnlyEnabled = prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false);
		serverUrl = prefs.getString(Prefs.KEY_server_url, Prefs.DEFAULT_server_url);
		pingBackEnabled = prefs.getBoolean(Prefs.KEY_sendback_enabled, false);
		authenticationType = prefs.getString(Prefs.KEY_authentication, AuthenticationOptionsEnum.NONE.getString());
		exportType = prefs.getString(Prefs.KEY_export, ExportOptionsEnum.NONE.getString());
		timeZone = prefs.getBoolean(Prefs.KEY_timezone, false);
	}
}
