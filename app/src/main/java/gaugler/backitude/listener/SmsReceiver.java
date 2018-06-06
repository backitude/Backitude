package gaugler.backitude.listener;

import java.text.DateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Settings;
import android.telephony.SmsMessage;
import android.widget.Toast;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.util.PreferenceHelper;
import gaugler.backitude.util.ServiceHelper;
import gaugler.backitude.util.WarningNotification;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.MyWakefulService;

public class SmsReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		try
		{
			//ZLogger.log("SmsReceiver onReceive: start");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
			String secretSuffix = prefs.getString(Prefs.KEY_sendfire_secret, "");
			//---get the SMS message passed in---
			Bundle bundle = intent.getExtras();        
			SmsMessage[] msgs = null;
			String temp = "";
			String senderNumber = "";
			String contactName = "";
			if (bundle != null)
			{
				//---retrieve the SMS message received---
				Object[] pdus = (Object[]) bundle.get("pdus");
				msgs = new SmsMessage[pdus.length];            
				for (int i=0; i<msgs.length; i++){
					//ZLogger.log("message loop: " + i);
					msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);  
					if(msgs[i]!=null){
						//ZLogger.log(msgs[i].getOriginatingAddress());
						//ZLogger.log("Message Body: " + msgs[i].getMessageBody().toString());
						//ZLogger.log("Display Message Body: " + msgs[i].getDisplayMessageBody());
						//ZLogger.log("Email Body" + msgs[i].getEmailBody());
						//ZLogger.log("Originating Address: " + msgs[i].getDisplayOriginatingAddress());
						temp =  msgs[i].getDisplayMessageBody().trim();
						senderNumber = msgs[i].getDisplayOriginatingAddress().trim();
					}
				}
				//---display the new SMS message---
				//ZLogger.log(temp);
			}  

			//ZLogger.log("SmsReceiver onReceive: temp = " + temp);
			//ZLogger.log("SmsReceiver onReceive: secret suffix = " + secretSuffix);
			if(temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_EN + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_ES + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_PL + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_PL2 + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_PT + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_PT2 + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_ES2 + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_DE + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_EN + " " + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_ES + " " + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_PL + " " + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_PL2 + " " + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_PT + " " + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_PT2 + " " + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_ES2 + " " + secretSuffix) ||
					temp.equalsIgnoreCase(Constants.FORCE_BACKITUDE_UPDATE_DE + " " + secretSuffix)
					){
				super.abortBroadcast();

				ContentResolver localContentResolver = context.getApplicationContext().getContentResolver();
				Cursor contactLookupCursor =  
						localContentResolver.query(
								Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, 
										Uri.encode(senderNumber)), 
										new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup._ID}, 
										null, 
										null, 
										null);
				try {
					while(contactLookupCursor.moveToNext()){
						contactName = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
						//String contactId = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(PhoneLookup._ID));
						//ZLogger.log("contactMatch name: " + contactName);
						//ZLogger.log("contactMatch id: " + contactId);
					}
				}
				finally {
					contactLookupCursor.close();
				} 
				if(contactName.length()==0)
				{
					contactName = senderNumber;
				}

				String pushTime = DateFormat.getInstance().format(new Date());

				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(PersistedData.KEY_lastPush5, prefs.getString(PersistedData.KEY_lastPush4, ""));
				editor.putString(PersistedData.KEY_lastPushTime5, prefs.getString(PersistedData.KEY_lastPushTime4, ""));
				editor.putString(PersistedData.KEY_lastPush4, prefs.getString(PersistedData.KEY_lastPush3, ""));
				editor.putString(PersistedData.KEY_lastPushTime4, prefs.getString(PersistedData.KEY_lastPushTime3, ""));
				editor.putString(PersistedData.KEY_lastPush3, prefs.getString(PersistedData.KEY_lastPush2, ""));
				editor.putString(PersistedData.KEY_lastPushTime3, prefs.getString(PersistedData.KEY_lastPushTime2, ""));
				editor.putString(PersistedData.KEY_lastPush2, prefs.getString(PersistedData.KEY_lastPush1, ""));
				editor.putString(PersistedData.KEY_lastPushTime2, prefs.getString(PersistedData.KEY_lastPushTime1, ""));
				editor.putString(PersistedData.KEY_lastPush1, contactName.trim());
				editor.putString(PersistedData.KEY_lastPushTime1, pushTime);
				if(isNumeric(senderNumber)){
					editor.putString(PersistedData.KEY_pushNumber, senderNumber);
				}
				editor.commit();

				if(prefs.getBoolean(Prefs.KEY_sendfire_msg, false)){       		
					Toast.makeText(context.getApplicationContext(), contactName + " " + context.getApplicationContext().getResources().getString(R.string.UPDATE_REQUEST_SENT),  Toast.LENGTH_SHORT).show();
				}

				if(prefs.getBoolean(Prefs.KEY_sendfire_notif, false)){
					WarningNotification.createStatusBarPushNotification(context.getApplicationContext(), contactName);
				}

				if(prefs.getBoolean(Prefs.KEY_sendfire_enabled, false))
				{		

					if(isAbleToBeEnabled(context)){
						if(ServiceHelper.isNetworkAvailable(context.getApplicationContext()) || prefs.getBoolean(Prefs.KEY_offlineSync, false)){
							if(!ServiceHelper.isMyWakefulServiceRunning(context.getApplicationContext())){
								Intent serviceIntent = new Intent(context.getApplicationContext(), MyWakefulService.class);
								serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.PUSH_UPDATE_FLAG);
								if(isNumeric(senderNumber)){
									serviceIntent.putExtra(Constants.SERVICE_PHONE_PARAM, senderNumber);
								}
								context.getApplicationContext().startService(serviceIntent);
							}
							else
							{
								//ZLogger.log(context.getApplicationContext().getResources().getString(R.string.LOC_SERVICE_RUNNING));
								Toast.makeText(context.getApplicationContext(), context.getApplicationContext().getResources().getString(R.string.LOC_SERVICE_RUNNING), Toast.LENGTH_SHORT).show();
							}
						}
						else
						{
							//ZLogger.log("Cannot Fire update..no network service at this time");
							Toast.makeText(context.getApplicationContext(), context.getApplicationContext().getResources().getString(R.string.NO_FIRE_NO_NETWORK), Toast.LENGTH_SHORT).show();
						}
					}
					else
					{
						Toast.makeText(context, context.getResources().getString(R.string.NO_FIRE_NO_PROVIDER), Toast.LENGTH_SHORT).show();
					}

				}
				else
				{
					//Push updates are not enabled
				}
			}
		}
		catch(Exception ex)
		{
			ZLogger.logException("SmsReceiver", ex, context);
		}
	}

	private boolean isAbleToBeEnabled(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		boolean gpsPollingAllowed = PreferenceHelper.isGpsPollingAllowed(settings);
		boolean networkPollingAllowed = PreferenceHelper.isNetworkPollingAllowed(settings);
		boolean wifiPollingAllowed = PreferenceHelper.isWiFiPollingAllowed(settings);

		ContentResolver contentResolver = context.getContentResolver();
		boolean isGpsLocEnabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER) && gpsPollingAllowed;

		boolean isNetworkLocEnabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER) && networkPollingAllowed;

		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isWiFiLocEnabled = (Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER)) &&
				(mWifi.isAvailable() || mWifi.isConnectedOrConnecting() || mWifi.isRoaming()) && wifiPollingAllowed;

		return (isGpsLocEnabled||isNetworkLocEnabled||isWiFiLocEnabled);
	}
	
	public boolean isNumeric(String s){
	    String pattern= "^[0-9]*$";
	        if(s.matches(pattern)){
	            return true;
	        }
	        return false;   
	}
}