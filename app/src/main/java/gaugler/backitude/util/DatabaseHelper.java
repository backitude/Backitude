package gaugler.backitude.util;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.Prefs;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

public class DatabaseHelper {
	public static boolean recordsExist(Context context)
	{
		ZLogger.log("DatabaseHelper recordsExist: method start");

		boolean recordsExist = false;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String accountName = settings.getString(Prefs.KEY_accountName, "");
		
		SQLiteDatabase sampleDB = null;
		try{
			sampleDB =  context.openOrCreateDatabase(Constants.OFFLINE_LOCATION_DB, Context.MODE_PRIVATE, null);
			if(DatabaseHelper.isTableExists(context, sampleDB, Constants.OFFLINE_LOCATION_TABLE)){
				Cursor c = sampleDB.rawQuery("SELECT COUNT(id) as NumberOfRows FROM " + 
						Constants.OFFLINE_LOCATION_TABLE + 
						" WHERE account = '" + accountName + "'", null); 
				if (c != null ) { 
					if  (c.moveToFirst()) { 
						do { 
							int x = c.getInt(c.getColumnIndex("NumberOfRows")); 
							ZLogger.log("DatabaseHelper recordsExist: record count = " + x);
							recordsExist = x > 0;
						}while (c.moveToNext()); 
					} 
				}
			}
			sampleDB.close();
		}
		catch(Exception ex){
			ZLogger.logException("DatabaseHelper", new Exception(context.getResources().getString(R.string.DATABASE_ERROR)), context);	
			if(sampleDB!=null) { sampleDB.close();}
		}

		return recordsExist;
	}

	public static boolean isTableExists(Context context, SQLiteDatabase sampleDB, String tableName)
	{
		try{
			if(sampleDB == null)
			{
				ZLogger.log("DatabaseHelper isTableExists: db is null");
				return false;
			}
			if(!sampleDB.isOpen())
			{
				ZLogger.log("DatabaseHelper isTableExists: db is not open");
				return false;
			}
			Cursor cursor = sampleDB.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
			if (!cursor.moveToFirst())
			{
				ZLogger.log("DatabaseHelper isTableExists: no table found by query");
				return false;
			}
			int count = cursor.getInt(0);
			cursor.close();
			return (count > 0);
		}
		catch(Exception ex){
			ZLogger.logException("DatabaseHelper", new Exception(context.getResources().getString(R.string.DATABASE_ERROR)), context);	
			if(sampleDB!=null) { sampleDB.close();}
		}
		return false;
	}
}
