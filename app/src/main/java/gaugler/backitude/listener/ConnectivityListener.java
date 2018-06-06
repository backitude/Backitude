package gaugler.backitude.listener;

import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.constants.SyncOptionsEnum;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.DatabaseHelper;
import gaugler.backitude.util.ServiceHelper;
import gaugler.backitude.util.ZLogger;
import gaugler.backitude.wakeful.OfflineLocationSyncService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class ConnectivityListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		ZLogger.log("ConnectivityListener onReceive: start");
		try
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			boolean isAppEnabled = prefs.getBoolean(Prefs.KEY_appEnabled, false);

			if(isAppEnabled)
			{
				int syncOption = Integer.parseInt(prefs.getString(Prefs.KEY_syncOptions, SyncOptionsEnum.ANY_DATA_NETWORK.getString()));
				boolean isWiFiOnlyUpdates = prefs.getBoolean(Prefs.KEY_wifiOnly_enabled, false);
				boolean isWiFiModeEnabled = prefs.getBoolean(Prefs.KEY_wifi_mode, false);
				boolean isWifiModeRunning = prefs.getBoolean(PersistedData.KEY_wifiModeRunning, false);

				ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				boolean isWifiConnected = false;

				WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

				if(wifiNetInfo!=null)
				{
					ZLogger.log("ConnectivityListener onReceive: isWifiEnabled = " + wm.isWifiEnabled());
					ZLogger.log("ConnectivityListener onReceive: isAvailable = " + wifiNetInfo.isAvailable());
					ZLogger.log("ConnectivityListener onReceive: isConnectedOrConnecting = " + wifiNetInfo.isConnectedOrConnecting());
					ZLogger.log("ConnectivityListener onReceive: wm.getConnectionInfo().getSupplicantState() = " + wm.getConnectionInfo().getSupplicantState().toString());
					ZLogger.log("ConnectivityListener onReceive: Wifi Connected = " + wifiNetInfo.isConnected());		
					isWifiConnected = wifiNetInfo.isConnected();
				}

				// Check for Offline Records and a new type of connection that may result in a Sync
				if(!ServiceHelper.isMyOfflineServiceRunning(context)){
					if(DatabaseHelper.recordsExist(context)){
						if(mobNetInfo!=null)
						{
							ZLogger.log("ConnectivityListener onReceive: Mobile Connected = " + mobNetInfo.isConnected());	
							if(mobNetInfo.isConnected())
							{
								if(syncOption == SyncOptionsEnum.ANY_DATA_NETWORK.getValue() && !isWiFiOnlyUpdates)
								{
									ZLogger.log("ConnectivityListener onReceive: Sync offline records");
									Intent serviceIntent = new Intent(context, OfflineLocationSyncService.class);
									serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.OFFLINE_SYNC_FLAG);
									context.startService(serviceIntent);
									return;
								}
							}
						}
						if(isWifiConnected)
						{
							if(syncOption == SyncOptionsEnum.ANY_DATA_NETWORK.getValue() || syncOption == SyncOptionsEnum.WIFI_ONLY.getValue())
							{
								ZLogger.log("ConnectivityListener onReceive: Sync offline records");
								Intent serviceIntent = new Intent(context, OfflineLocationSyncService.class);
								serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.OFFLINE_SYNC_FLAG);
								context.startService(serviceIntent);
								return;
							}
						}
					}
				}

				// Check for Wi-Fi Connected Mode
				if(isWifiModeRunning)
				{
					ZLogger.log("ConnectivityListener onReceive: wifi mode currently running");
					if(!isWiFiModeEnabled || 
						(!isWifiConnected && (
							!wm.isWifiEnabled() || 
							!wifiNetInfo.isAvailable() || 
							wm.getConnectionInfo().getSupplicantState() == SupplicantState.SCANNING || 
							wm.getConnectionInfo().getSupplicantState() == SupplicantState.DISCONNECTED || 
							wm.getConnectionInfo().getSupplicantState().toString().equals("INTERFACE_DISABLED")
						))
					)
					{
						ZLogger.log("ConnectivityListener onReceive: wifi not connected and not available or not enabled...turn off wifi mode");
						ServiceManager sm = new ServiceManager();
						sm.startAlarms(context);
					}
					else
					{
						ZLogger.log("ConnectivityListener onReceive: there's a reason to keep wifi mode running");
					}
				}
				else
				{
					ZLogger.log("ConnectivityListener onReceive: wifi mode currently not running");
					
					if(isWiFiModeEnabled && isWifiConnected){
						ZLogger.log("ConnectivityListener onReceive: WiFi mode enabled and connected to network.. turn on wifi mode");
						ServiceManager sm = new ServiceManager();
						sm.startAlarms(context);
					}
					else
					{
						ZLogger.log("ConnectivityListener onReceive: there's a reason to keep wifi mode off");
					}
				}
			}
			else
			{
				ZLogger.log("ConnectivityListener onReceive: app disabled, do nothing");
			}
		}
		catch(Exception ex){
			ZLogger.log("ConnectivityListener onReceive: " + ex.toString());
		}
	}	
}
