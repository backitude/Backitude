package gaugler.backitude.listener;

import gaugler.backitude.constants.Prefs;
import gaugler.backitude.service.ServiceManager;
import gaugler.backitude.util.ZLogger;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MyStartupIntentReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {

		ZLogger.log("MyStartupIntentReceiver received a BOOTUP complete broadcast");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		if(prefs.getBoolean(Prefs.KEY_appEnabled, false)){
				ZLogger.log("MyStartupIntentReceiver: previously enabled- start it up");
				//Intent serviceIntent = new Intent(context, MyBackgroundService.class);
				//serviceIntent.putExtra(Constants.SERVICE_STARTUP_PARAM, Constants.SERVICE_STARTUP_FLAG);
				//getBaseContext().startService(serviceIntent);
				ServiceManager appStarter = new ServiceManager();
				appStarter.startAlarms(context.getApplicationContext());
		}
	}

}