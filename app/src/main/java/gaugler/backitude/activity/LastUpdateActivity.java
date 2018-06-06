package gaugler.backitude.activity;

import gaugler.backitude.R;
import gaugler.backitude.constants.PersistedData;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

public class LastUpdateActivity extends Activity {

	static final int DIALOG_LAST_UPDATE = 0;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		  
		showDialog(DIALOG_LAST_UPDATE);

	}

	@Override
	protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_LAST_UPDATE:
            return launchHistoryDialog();
        default:
            return null;
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}
	
	 private Dialog launchHistoryDialog()
	    {
	    	Dialog dialog = new Dialog(LastUpdateActivity.this);

	    	dialog.setContentView(R.layout.update_history);
	    	dialog.setTitle(R.string.lastUpdate_dialog);
	    	dialog.setCancelable(true);
	    	dialog.setOnCancelListener(new OnCancelListener() {
	    	    public void onCancel(DialogInterface dialog) {
	    	        finish();
	    	    }
	    	});
	    	dialog.setOnDismissListener(new OnDismissListener() {
	    	    public void onDismiss(DialogInterface dialog) {
	    	        finish();
	    	    }
	    	});

	    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    	if(settings!=null){
		        TextView lastUpdateLocTime = (TextView) dialog.findViewById(R.id.lastUpdate_loc_time);
		        lastUpdateLocTime.setText(getResources().getString(R.string.LAST_UPDATE_LOC_TIME) + " " + settings.getString(PersistedData.KEY_savedLocation_time, getResources().getString(R.string.LAST_UPDATE_NA)));
		        TextView lastUpdateTime = (TextView) dialog.findViewById(R.id.lastUpdate_time);
		        lastUpdateTime.setText(getResources().getString(R.string.LAST_UPDATE_TIME) + " " + settings.getString(PersistedData.KEY_savedLocation_UpdateTime, ""));
		        TextView lastUpdateLat = (TextView) dialog.findViewById(R.id.lastUpdate_lat);
		        lastUpdateLat.setText(getResources().getString(R.string.LAST_UPDATE_LAT) + " " + String.valueOf(settings.getFloat(PersistedData.KEY_savedLocation_lat, 0)));
		        TextView lastUpdateLong = (TextView) dialog.findViewById(R.id.lastUpdate_long);
		        lastUpdateLong.setText(getResources().getString(R.string.LAST_UPDATE_LONG) + " " + String.valueOf(settings.getFloat(PersistedData.KEY_savedLocation_long, 0)));

		        TextView lastUpdateAccur = (TextView) dialog.findViewById(R.id.lastUpdate_accur);
		        if(settings.contains((PersistedData.KEY_savedLocation_accur))){
		        	lastUpdateAccur.setText(getResources().getString(R.string.LAST_UPDATE_ACCUR) + " " + String.valueOf(settings.getFloat(PersistedData.KEY_savedLocation_accur, 0)));
		        }
		        else {
		        	lastUpdateAccur.setText(getResources().getString(R.string.LAST_UPDATE_ACCUR) + " (null)");
		        }
		        
		        TextView lastUpdateSpeed = (TextView) dialog.findViewById(R.id.lastUpdate_speed);
		        if(settings.contains(PersistedData.KEY_savedLocation_speed)){
		        	 lastUpdateSpeed.setText(getResources().getString(R.string.LAST_UPDATE_SPEED) + " " + String.valueOf(settings.getFloat(PersistedData.KEY_savedLocation_speed, 0)));
		        }
		        else {
		        	 lastUpdateSpeed.setText(getResources().getString(R.string.LAST_UPDATE_SPEED) + " (null)");
		        }
		        
		        TextView lastUpdateAltitude = (TextView) dialog.findViewById(R.id.lastUpdate_altitude);
		        if(settings.contains(PersistedData.KEY_savedLocation_altitude)){
		        	 lastUpdateAltitude.setText(getResources().getString(R.string.LAST_UPDATE_ALTITUDE) + " " + String.valueOf(settings.getFloat(PersistedData.KEY_savedLocation_altitude, 0)));
		        }
		        else {
		        	 lastUpdateAltitude.setText(getResources().getString(R.string.LAST_UPDATE_ALTITUDE) + " (null)");
		        }
		        
		        TextView lastUpdateBearing = (TextView) dialog.findViewById(R.id.lastUpdate_bearing);
		        if(settings.contains(PersistedData.KEY_savedLocation_bearing)){
		        	 lastUpdateBearing.setText(getResources().getString(R.string.LAST_UPDATE_BEARING) + " " + String.valueOf(settings.getFloat(PersistedData.KEY_savedLocation_bearing, 0)));
		        }
		        else {
		        	 lastUpdateBearing.setText(getResources().getString(R.string.LAST_UPDATE_BEARING) + " (null)");
		        }
		        
		        TextView lastUpdateType = (TextView) dialog.findViewById(R.id.lastUpdate_type);
		        lastUpdateType.setText(getResources().getString(R.string.LAST_UPDATE_TYPE) + " " + String.valueOf(settings.getString(PersistedData.KEY_savedLocation_type, "")));
	    	}
	        return dialog;
	    }
}
