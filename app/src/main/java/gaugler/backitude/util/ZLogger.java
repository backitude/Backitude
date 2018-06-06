package gaugler.backitude.util;
import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.PersistedData;
import gaugler.backitude.constants.Prefs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;


public class ZLogger {   
	public static final boolean LOG = false;
	public static void zlog(String msg){
		Log.v("backitude", msg);
		if(LOG){
			try{			
				String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
				//String appDataPath = "Android" + File.separator + "data" + File.separator + "gaugler.backitude"  + File.separator + "logs";
				File sdDir = new File(baseDir + File.separator + "Backitude");

				if(!sdDir.exists()){
					sdDir.mkdir();
				}

				if (sdDir.canWrite()) {
					SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
					String logName = formatter.format(new Date());
					File logFile = new File (sdDir, "backitude-log-" + logName + ".txt");
					FileWriter logFileWriter = new FileWriter(logFile, true);
					BufferedWriter out = new BufferedWriter(logFileWriter);
					String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());  
					out.write("\n" + currentDateTimeString + " - " + msg + "\n");
					out.close();
				}
				else
				{
					Log.v("backitude", "Could not write to file: could not initialize");
				}
			} catch (Exception e) {
				Log.v("backitude", "Could not write to file: " + e.getMessage());
			}
		}
	}

	public static void log(String msg){
		Log.v("backitude", msg);
		if(LOG){
			try{
				String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
				//String appDataPath = "Android" + File.separator + "data" + File.separator + "gaugler.backitude"  + File.separator + "logs";
				File sdDir = new File(baseDir + File.separator + "Backitude");

				if(!sdDir.exists()){
					sdDir.mkdir();
				}

				if (sdDir.canWrite()) {
					SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
					String logName = formatter.format(new Date());
					File logFile = new File (sdDir, "backitude-log-" + logName + ".txt");
					FileWriter logFileWriter = new FileWriter(logFile, true);
					BufferedWriter out = new BufferedWriter(logFileWriter);
					String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());  
					out.write("\n" + currentDateTimeString + " - " + msg + "\n");
					out.close();
				}
				else
				{
					Log.v("backitude", "Could not write to file: could not initialize");
				}
			} catch (Exception e) {
				Log.v("backitude", "Could not write to file: " + e.getMessage());
			}
		}
	}

	public static void logException(String className, Exception e, Context context)
	{		
		zlog(className + " fail: " + e.toString());
		zlog(className + " fail: " + e.getLocalizedMessage());
		zlog(className + " fail: " + e.getMessage());
		StackTraceElement[] ex = e.getStackTrace();
		int i = 0;
		while(ex!=null && ex.length > i && i < 3)
		{
			zlog(className + " stack toString: " + ex[i].toString());
			zlog(className + " stack FileName: " + ex[i].getFileName());
			zlog(className + " stack FileName: " + ex[i].getClassName());
			zlog(className + " stack MethodName: " + ex[i].getMethodName());
			zlog(className + " stack LineNumber: " + ex[i].getLineNumber());
			i++;
		}

		if(context!=null)
		{
			String errorMsg1 = "";
			String errorMsg2 = "";
			String errorTime1 = "";
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			if(e!=null)
			{
				try {
					errorMsg1 = settings.getString(PersistedData.KEY_lastErrorMsg2, "");
					errorTime1 = settings.getString(PersistedData.KEY_lastErrorTime2, "");
					errorMsg2 = e.toString();
				}
				catch (Exception e1) {
					errorMsg2 = e1.toString();
				}

				SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
				Date date= new Date();
				String errorTime2 = sdf.format(date);

				SharedPreferences.Editor editor = settings.edit();
				editor.putString(PersistedData.KEY_lastErrorMsg1, errorMsg1);
				editor.putString(PersistedData.KEY_lastErrorTime, errorTime1);
				editor.putString(PersistedData.KEY_lastErrorMsg2, errorMsg2);
				editor.putString(PersistedData.KEY_lastErrorTime2, errorTime2);
				editor.commit();
			}

			if(settings.getBoolean(Prefs.KEY_display_error, false))
			{
				if(className.equalsIgnoreCase(Constants.PERMISSION_EXCEPTION))
				{
					WarningNotification.createStatusBarPermissionsNotification(context, context.getResources().getString(R.string.CONTACT_DEV));
				}
				else if(className.equalsIgnoreCase(Constants.TOKEN_EXCEPTION))
				{
					WarningNotification.createStatusBarAccountNotification(context, context.getResources().getString(R.string.TRY_AGAIN_LATER));
				}
				else{
					WarningNotification.createStatusBarErrorNotification(context, errorMsg2);
				}

			}

		}	
	}
}
