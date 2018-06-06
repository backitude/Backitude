package gaugler.backitude.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;

import gaugler.backitude.constants.GpsOptionsEnum;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;

public class PreferenceHelper {

	public static boolean isPollingAlarmRunning(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(PersistedData.KEY_isAlarmRunning, false);
	}

	public static boolean isReSyncAlarmRunning(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(PersistedData.KEY_isReSyncRunning, false);
	}
	
	public static void setLastPolledLocation(Context context, Location _location) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat(PersistedData.KEY_polledLocation_lat, (float)_location.getLatitude());
		editor.putFloat(PersistedData.KEY_polledLocation_long, (float)_location.getLongitude());
		editor.putLong(PersistedData.KEY_polledLocation_date, _location.getTime());	
		if(_location.hasAccuracy()){
			editor.putFloat(PersistedData.KEY_polledLocation_accur, _location.getAccuracy());
		} else {
			editor.remove(PersistedData.KEY_polledLocation_accur);
		}
		if(_location.hasSpeed()){
			editor.putFloat(PersistedData.KEY_polledLocation_speed, _location.getSpeed());
		} else {
			editor.remove(PersistedData.KEY_polledLocation_speed);
		}
		if(_location.hasAltitude()){
			ZLogger.log("PreferenceHelper setLastPolledLocation: set altitude: " + _location.getAltitude());
			editor.putFloat(PersistedData.KEY_polledLocation_altitude, (float)_location.getAltitude());
		} else {
			ZLogger.log("PreferenceHelper setLastPolledLocation: does not have altitude: " + _location.getAltitude());
			editor.remove(PersistedData.KEY_polledLocation_altitude);
		}
		if(_location.hasBearing()){
			editor.putFloat(PersistedData.KEY_polledLocation_bearing, _location.getBearing());
		} else {
			editor.remove(PersistedData.KEY_polledLocation_bearing);
		}
		editor.commit();
	}

	public static boolean hasLastPolledLocation(Service _service)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_service.getBaseContext());
		return prefs.getFloat(PersistedData.KEY_polledLocation_lat, 0) != 0 ||
		prefs.getFloat(PersistedData.KEY_polledLocation_long, 0) != 0;
	}

	public static boolean hasLastPolledLocation(Context _context)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
		return prefs.getFloat(PersistedData.KEY_polledLocation_lat, 0) != 0 ||
			prefs.getFloat(PersistedData.KEY_polledLocation_long, 0) != 0;
	}
	
	public static Location getLastPolledLocation(Context context) {
		Location lastPolledLocation = new Location(LocationManager.GPS_PROVIDER);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		lastPolledLocation.setLatitude((double)prefs.getFloat(PersistedData.KEY_polledLocation_lat, (float)lastPolledLocation.getLatitude()));
		lastPolledLocation.setLongitude((double)prefs.getFloat(PersistedData.KEY_polledLocation_long, (float)lastPolledLocation.getLongitude()));
		long temp = 0;
		try{
			temp = (prefs.getLong(PersistedData.KEY_polledLocation_date, lastPolledLocation.getTime()));
		} catch (ClassCastException ex){
			temp =(long)prefs.getFloat(PersistedData.KEY_polledLocation_date, (float)lastPolledLocation.getTime());
		}
		lastPolledLocation.setTime(temp);
		if(prefs.contains(PersistedData.KEY_polledLocation_accur)){
			lastPolledLocation.setAccuracy(prefs.getFloat(PersistedData.KEY_polledLocation_accur, lastPolledLocation.getAccuracy()));
		} else {
			lastPolledLocation.removeAccuracy();
		}
		if(prefs.contains(PersistedData.KEY_polledLocation_speed)){
			lastPolledLocation.setSpeed(prefs.getFloat(PersistedData.KEY_polledLocation_speed, lastPolledLocation.getSpeed()));
		} else {
			lastPolledLocation.removeSpeed();
		}
		if(prefs.contains(PersistedData.KEY_polledLocation_altitude)){
			lastPolledLocation.setAltitude((double)prefs.getFloat(PersistedData.KEY_polledLocation_altitude, lastPolledLocation.getSpeed()));
			ZLogger.log("PreferenceHelper getLastPolledLocation: get altitude: " + lastPolledLocation.getAltitude());
		} else {
			ZLogger.log("PreferenceHelper getLastPolledLocation: does not have altitude");
			lastPolledLocation.removeAltitude();
		}
		if(prefs.contains(PersistedData.KEY_polledLocation_bearing)){
			lastPolledLocation.setBearing(prefs.getFloat(PersistedData.KEY_polledLocation_bearing, lastPolledLocation.getBearing()));
		} else {
			lastPolledLocation.removeBearing();
		}

		return lastPolledLocation;
	}
	
	public static Location getCachedSteal(Context context) {		
		Location savedStolenLocation = new Location(LocationManager.GPS_PROVIDER);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		savedStolenLocation.setLatitude((double)prefs.getFloat(PersistedData.KEY_stolenLocation_lat, (float)savedStolenLocation.getLatitude()));
		savedStolenLocation.setLongitude((double)prefs.getFloat(PersistedData.KEY_stolenLocation_long, (float)savedStolenLocation.getLongitude()));
		savedStolenLocation.setTime(prefs.getLong(PersistedData.KEY_stolenLocation_date, (new Date()).getTime()));
		long temp = 0;
		try{
			temp = (prefs.getLong(PersistedData.KEY_stolenLocation_date, (new Date()).getTime()));
		} catch (ClassCastException ex){
			temp =(long)prefs.getFloat(PersistedData.KEY_stolenLocation_date, (float)(new Date()).getTime());
		}
		savedStolenLocation.setTime(temp);
		if(prefs.contains(PersistedData.KEY_stolenLocation_accur)){
			savedStolenLocation.setAccuracy(prefs.getFloat(PersistedData.KEY_stolenLocation_accur, 0));
		} else {
			savedStolenLocation.removeAccuracy();
		}
		if(prefs.contains(PersistedData.KEY_stolenLocation_speed)){
			savedStolenLocation.setSpeed(prefs.getFloat(PersistedData.KEY_stolenLocation_speed, savedStolenLocation.getSpeed()));
		} else {
			savedStolenLocation.removeSpeed();
		}
		if(prefs.contains(PersistedData.KEY_stolenLocation_altitude)){
			savedStolenLocation.setAltitude((double)prefs.getFloat(PersistedData.KEY_stolenLocation_altitude, (float)savedStolenLocation.getAltitude()));
		} else {
			savedStolenLocation.removeAltitude();
		}
		if(prefs.contains(PersistedData.KEY_stolenLocation_bearing)){
			savedStolenLocation.setBearing(prefs.getFloat(PersistedData.KEY_stolenLocation_bearing, savedStolenLocation.getBearing()));
		} else {
			savedStolenLocation.removeBearing();
		}
		return savedStolenLocation;
	}
	
	public static void clearCachedSteal(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(PersistedData.KEY_stolenLocation_lat);
		editor.remove(PersistedData.KEY_stolenLocation_long);
		editor.remove(PersistedData.KEY_stolenLocation_accur);
		editor.remove(PersistedData.KEY_stolenLocation_date);
		editor.remove(PersistedData.KEY_stolenLocation_speed);
		editor.remove(PersistedData.KEY_stolenLocation_altitude);
		editor.remove(PersistedData.KEY_stolenLocation_bearing);
		editor.commit();
	}

	public static boolean isGpsPollingAllowed(SharedPreferences settings) {
		int priorityOption = GpsOptionsEnum.GPS_ALL.getValue();
		try
		{
			priorityOption = Integer.parseInt(settings.getString(Prefs.KEY_gpsOption, GpsOptionsEnum.GPS_ALL.getString()));
		}
		catch(NumberFormatException ex)
		{
			priorityOption = GpsOptionsEnum.GPS_ALL.getValue();
		}
				
		return (priorityOption == GpsOptionsEnum.GPS_ALL.getValue()) ||
			(priorityOption == GpsOptionsEnum.GPS_WIFI.getValue()) ||
			(priorityOption == GpsOptionsEnum.GPS_ONLY.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_ALL.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_GPS.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_THEN_REST.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_THEN_GPS.getValue());
	}

	public static boolean isWiFiPollingAllowed(SharedPreferences settings) {
		int priorityOption = GpsOptionsEnum.GPS_ALL.getValue();
		try
		{
			priorityOption = Integer.parseInt(settings.getString(Prefs.KEY_gpsOption, GpsOptionsEnum.GPS_ALL.getString()));
		}
		catch(NumberFormatException ex)
		{
			priorityOption = GpsOptionsEnum.GPS_ALL.getValue();
		}
				
		return (priorityOption == GpsOptionsEnum.WIFI_ALL.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_GPS.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_CELL.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_ONLY.getValue()) ||
			(priorityOption == GpsOptionsEnum.GPS_WIFI.getValue()) ||
			(priorityOption == GpsOptionsEnum.GPS_ALL.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_THEN_REST.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_THEN_GPS.getValue());
	}
	
	public static boolean isNetworkPollingAllowed(SharedPreferences settings) {
		int priorityOption = GpsOptionsEnum.GPS_ALL.getValue();
		try
		{
			priorityOption = Integer.parseInt(settings.getString(Prefs.KEY_gpsOption, GpsOptionsEnum.GPS_ALL.getString()));
		}
		catch(NumberFormatException ex)
		{
			priorityOption = GpsOptionsEnum.GPS_ALL.getValue();
		}
		
		return (priorityOption == GpsOptionsEnum.WIFI_ALL.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_CELL.getValue()) ||
			(priorityOption == GpsOptionsEnum.GPS_ALL.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_THEN_REST.getValue());
	}

	public static boolean isWifiOverrideEnabled(SharedPreferences settings) {
		int priorityOption = GpsOptionsEnum.GPS_ALL.getValue();
		try
		{
			priorityOption = Integer.parseInt(settings.getString(Prefs.KEY_gpsOption, GpsOptionsEnum.GPS_ALL.getString()));
		}
		catch(NumberFormatException ex)
		{
			priorityOption = GpsOptionsEnum.GPS_ALL.getValue();
		}
		
		return (priorityOption == GpsOptionsEnum.WIFI_ALL.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_GPS.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_CELL.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_ONLY.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_THEN_REST.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_THEN_GPS.getValue());
		
	}
	
	public static boolean isGpsDelayedForWiFi(SharedPreferences settings) {
		int priorityOption = GpsOptionsEnum.GPS_ALL.getValue();
		try
		{
			priorityOption = Integer.parseInt(settings.getString(Prefs.KEY_gpsOption, GpsOptionsEnum.GPS_ALL.getString()));
		}
		catch(NumberFormatException ex)
		{
			priorityOption = GpsOptionsEnum.GPS_ALL.getValue();
		}
		
		return (priorityOption == GpsOptionsEnum.WIFI_THEN_REST.getValue()) ||
			(priorityOption == GpsOptionsEnum.WIFI_THEN_GPS.getValue());
		
	}
	
	public static boolean isAbleToBeEnabled(Context context)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		boolean gpsPollingAllowed = PreferenceHelper.isGpsPollingAllowed(settings);
		boolean networkPollingAllowed = PreferenceHelper.isNetworkPollingAllowed(settings);
		boolean wifiPollingAllowed = PreferenceHelper.isWiFiPollingAllowed(settings);
		
		ContentResolver contentResolver = context.getContentResolver();
		boolean isGpsLocEnabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER) && gpsPollingAllowed;
		
		boolean isNetworkLocEnabled = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER) && networkPollingAllowed;
		
		WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		boolean isWiFiLocEnabled = (Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.NETWORK_PROVIDER)) &&
				wifi.isWifiEnabled() && wifiPollingAllowed;	
		
		return (isGpsLocEnabled||isNetworkLocEnabled||isWiFiLocEnabled);
	}
}
