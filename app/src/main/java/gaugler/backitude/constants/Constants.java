package gaugler.backitude.constants;

public class Constants 
{	
	public static final String SETTINGS_LAUNCH = "settings";
	public static final String ADVANCED_SETTINGS_LAUNCH = "advanced_settings";
	public static final String WIFI_SYS_PREF = "wifi_sys";
	public static final String SYS_LOC_SETTINGS_LAUNCH = "loc_sys_settings";
	public static final String RUN_NOW_LAUNCH = "runNow";
	public static final String SEND_FIRE_REQUEST = "send_fire";
	public static final String SEND_LAST_UPDATE = "send_last_update";
	public static final String EXTRAS_LAUNCH = "extras";
	public static final String POLLING_SETTINGS_LAUNCH = "polling_settings";
	public static final String REALTIME_SETTINGS_LAUNCH = "realtime_settings";
	public static final String WIFI_MODE_SETTINGS_LAUNCH = "wifi_mode_settings";
	public static final String PUSH_SETTINGS_LAUNCH = "push_settings";
	public static final String ACCURACY_SETTINGS_LAUNCH = "accuracy_settings";
	public static final String LAST_UPDATE_LAUNCH = "lastUpdate";
	public static final String LAST_ERROR_LAUNCH = "lastError";
	public static final String LAST_PUSH_LAUNCH = "lastPush";
	public static final String UPDATE_SETTINGS_LAUNCH = "update_settings";
	public static final String OFFLINE_SETTINGS_LAUNCH = "offline_settings";
	public static final String SERVER_SETTINGS_LAUNCH = "server_settings";
	public static final String MANUAL_OFFLINE_SYNC = "manual_offline_sync";
	public static final String CLEAR_OFFLINE_STORAGE = "clear_offline_history";
	public static final String DONATE_TO_LAUNCH = "donateTo";
	public static final String FAQ_LAUNCH = "faq";
	public static final String ABOUT_LAUNCH = "about";
		
	public static final int NO_MIN_CHANGE_DISTANCE = 0;
	public static final int ON_LOC_POLL_ONLY_VALUE = 0;
	public static final int NO_POLLING_INTERVAL = 99999999;
	public static final int VARIABLE_MIN_DIST = 99999999;
	public static final int MIN_MIN_DISTANACE = 20;
	
	public static final int ONE_SECOND = 1000;
	public static final int TWO_SECONDS = 2000;
	public static final int THREE_SECONDS = 3000;
	public static final int FIVE_SECONDS = 5000;
	public static final int TEN_SECONDS = 10000;
	public static final int FIFTEEN_SECONDS = 15000;
	public static final int ONE_MINUTE = 60000;
	public static final int FIVE_MINUTES = 300000;
	public static final int TEN_MINUTES = 600000;
	public static final int FIFTEEN_MINUTES = 900000;
		
	public static final String SERVICE_STARTUP_PARAM = "SERVICE_STARTUP_PARAM";
	public static final String SERVICE_PHONE_PARAM = "SERVICE_PHONE_PARAM";
	
	public static final int POLL_TIMER_FLAG = 1;
	public static final int FIRE_UPDATE_FLAG = 2;
	public static final int STEAL_UPDATE_FLAG = 3;
	public static final int RESYNC_ALARM_FLAG = 4;
	public static final int OFFLINE_SYNC_FLAG = 5;
	public static final int MANUAL_UPDATE_FLAG = 6;
	public static final int PUSH_UPDATE_FLAG = 7;
	
	public static final int POLL_UPDATE_OVER_TRUE_FLAG = 10;
	public static final int POLL_UPDATE_OVER_FALSE_FLAG = 11;
	public static final int FIRE_UPDATE_OVER_TRUE_FLAG = 12;
	public static final int FIRE_UPDATE_OVER_FALSE_FLAG = 13;
	public static final int STEAL_UPDATE_OVER_TRUE_FLAG = 14;
	public static final int STEAL_UPDATE_OVER_FALSE_FLAG = 15;
	public static final int RESYNC_UPDATE_OVER_TRUE_FLAG = 16;
	public static final int RESYNC_UPDATE_OVER_FALSE_FLAG = 17;
	public static final int MANUAL_UPDATE_OVER_TRUE_FLAG = 18;
	public static final int MANUAL_UPDATE_OVER_FALSE_FLAG = 19;
	public static final int PUSH_UPDATE_OVER_TRUE_FLAG = 20;
	public static final int PUSH_UPDATE_OVER_FALSE_FLAG = 21;
	
	public static final int START_POLL_RETRY_TIMER = 30;
	public static final int START_FIRE_RETRY_TIMER = 31;
	public static final int START_STEAL_RETRY_TIMER = 32;
	public static final int START_RESYNC_RETRY_TIMER = 33;
	public static final int START_SYNC_RETRY_TIMER = 34;
	public static final int START_MANUAL_RETRY_TIMER =35;
	public static final int START_PUSH_RETRY_TIMER = 36;
	
	public static final int REFRESH_AUTH_TOKEN_POLL = 40;
	public static final int REFRESH_AUTH_TOKEN_FIRE = 41;
	public static final int REFRESH_AUTH_TOKEN_STEAL= 42;
	public static final int REFRESH_AUTH_TOKEN_RESYNC = 43;
	public static final int REFRESH_AUTH_TOKEN_OFFSYNC = 44;
	public static final int REFRESH_AUTH_TOKEN_MANUAL = 45;
	public static final int REFRESH_AUTH_TOKEN_PUSH = 46;
	

	public static final int REAL_TIME_WIDGET_DISABLED = 0;
	public static final int REAL_TIME_WIDGET_ENABLED = 1;
	public static final int REAL_TIME_WIDGET_RUNNING = 2;
	
	public static final int ENABLED_STATUS_ID = 1;
	public static final int POLLING_STATUS_ID = 2;
	public static final int AUTH_ERROR_ID = 3;
	public static final int REALTIME_STATUS_ID = 4;
	public static final int UPDATE_REQUEST_ID = 5;
	public static final int TOKEN_ERROR_ID = 6;
	
	//public static final String SCOPE = "oauth2:https://mapt.com/auth/scopes/latitude.all.best";
	public static final String SCOPE = "oauth2:https://googleapis.com/auth/latitude.all.best";
	public static final String FAQ_URL = "http://www.downwindoutdoors.com/apps/backitude/faq.html";	

	public static final String ON_OFF_WIDGET_PARAM = "IsAppEnabled";
	public static final String REALTIME_WIDGET_PARAM = "IsRealtimeEnabled";
	public static final String FIRE_WIDGET_PARAM = "isPolling";
	public static final String PUSH_WIDGET_PARAM = "contactName";
	
	public static final String ON_OFF_WIDGET_ACTION_WIDGET_CLICKED = "ON_OFF_ACTION_WIDGET_UPDATE_FROM_WIDGET";
	public static final String ON_OFF_WIDGET_ACTION_UPDATE_WIDGET = "ON_OFF_ACTION_WIDGET_UPDATE_FROM_ACTIVITY";
	public static final String FIRE_WIDGET_ACTION_WIDGET_CLICKED = "FIRE_ACTION_WIDGET_UPDATE_FROM_WIDGET";
	public static final String FIRE_WIDGET_ACTION_UPDATE_WIDGET = "FIRE_ACTION_WIDGET_UPDATE_FROM_ACTIVITY";
	public static final String REALTIME_WIDGET_ACTION_WIDGET_CLICKED = "REALTIME_ACTION_WIDGET_UPDATE_FROM_WIDGET";
	public static final String REALTIME_WIDGET_ACTION_UPDATE_WIDGET = "REALTIME_ACTION_WIDGET_UPDATE_FROM_ACTIVITY";
	public static final String PUSH_WIDGET_ACTION_WIDGET_CLICKED = "PUSH_ACTION_WIDGET_UPDATE_FROM_WIDGET";
	public static final String PUSH_WIDGET_ACTION_UPDATE_WIDGET = "PUSH_ACTION_WIDGET_UPDATE_FROM_ACTIVITY";
	public static final String LATITUDE_WIDGET_ACTION_WIDGET_CLICKED = "LATITUDE_ACTION_WIDGET_UPDATE_FROM_WIDGET";
	public static final String LATITUDE_WIDGET_ACTION_UPDATE_WIDGET = "LATITUDE_ACTION_WIDGET_UPDATE_FROM_ACTIVITY";
	public static final String CHANGE_SETTING_BY_INTENT = "gaugler.backitude.SEND_INTENT_ACTION_CHANGE_SETTING";
	
	// TASKER integration parameters
	public static final String KEY_parameter_enabled = "enabled";
	public static final String KEY_parameter_latitude = "latitude";
	public static final String KEY_parameter_longitude = "longitude";
	public static final String KEY_parameter_priority = "priority";
	public static final String KEY_parameter_interval = "interval";
	public static final String KEY_parameter_docked = "docked";
	public static final String KEY_parameter_dockedInterval = "dockedInterval";
	public static final String KEY_parameter_sync = "sync";
	public static final String KEY_parameter_fallback = "fallback";
	
	public static final String PERMISSION_EXCEPTION = "PERMISSION_EXCEPTION";
	public static final String TOKEN_EXCEPTION = "TOKEN_EXCEPTION";
	public static final String STEAL_BUFFER_PARAM = "STEAL_BUFFER_OVER";
	public static final String POLL_BUFFER_PARAM = "POLL_BUFFER_PARAM";
	
	public static final String OFFLINE_LOCATION_DB = "OfflineLocationStorage";
	public static final String OFFLINE_LOCATION_TABLE = "Location";
	
	public static final String FORCE_BACKITUDE_UPDATE_EN = "Force Backitude Update";
	public static final String FORCE_BACKITUDE_UPDATE_ES = "Solicitud de Actualización Backitude";
	public static final String FORCE_BACKITUDE_UPDATE_PL = "Wymuś Aktualizację Backitude";
	public static final String FORCE_BACKITUDE_UPDATE_PL2 = "Wymu? Aktualizacj? Backitude";
	public static final String FORCE_BACKITUDE_UPDATE_PT = "Forçar atualização do Backitude";
	public static final String FORCE_BACKITUDE_UPDATE_ES2 = "Solicitud de Actualizaci?n Backitude";
	public static final String FORCE_BACKITUDE_UPDATE_PT2 = "For?ar atualiza??o do Backitude";
	public static final String FORCE_BACKITUDE_UPDATE_DE = "Forceer Backitude Bijwerking";
}
