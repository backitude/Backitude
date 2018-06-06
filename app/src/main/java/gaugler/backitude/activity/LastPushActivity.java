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

public class LastPushActivity extends Activity {

	static final int DIALOG_PUSH_HISTORY = 0;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		  
		showDialog(DIALOG_PUSH_HISTORY);

	}

	@Override
	protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_PUSH_HISTORY:
            return launchPushHistoryDialog();
        default:
            return null;
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}
	
	 private Dialog launchPushHistoryDialog()
	    {
	    	Dialog dialog = new Dialog(LastPushActivity.this);

	    	dialog.setContentView(R.layout.push_history);
	    	dialog.setTitle(R.string.lastPush_dialog);
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
		        TextView lastPush5 = (TextView) dialog.findViewById(R.id.lastPush5);
		        lastPush5.setText(getResources().getString(R.string.LAST_PUSH_HISTORY) + " " + settings.getString(PersistedData.KEY_lastPush5, getResources().getString(R.string.NO_PUSH_HISTORY))
		        		+ " " + settings.getString(PersistedData.KEY_lastPushTime5, ""));
		        
		        TextView lastPush4 = (TextView) dialog.findViewById(R.id.lastPush4);
		        lastPush4.setText(getResources().getString(R.string.LAST_PUSH_HISTORY) + " " + settings.getString(PersistedData.KEY_lastPush4, getResources().getString(R.string.NO_PUSH_HISTORY))
		        		+ " " + settings.getString(PersistedData.KEY_lastPushTime4, ""));
		        
		        TextView lastPush3 = (TextView) dialog.findViewById(R.id.lastPush3);
		        lastPush3.setText(getResources().getString(R.string.LAST_PUSH_HISTORY) + " " + settings.getString(PersistedData.KEY_lastPush3, getResources().getString(R.string.NO_PUSH_HISTORY))
		        		+ " " + settings.getString(PersistedData.KEY_lastPushTime3, ""));
		        
		        TextView lastPush2 = (TextView) dialog.findViewById(R.id.lastPush2);
		        lastPush2.setText(getResources().getString(R.string.LAST_PUSH_HISTORY) + " " + settings.getString(PersistedData.KEY_lastPush2, getResources().getString(R.string.NO_PUSH_HISTORY))
		        		+ " " + settings.getString(PersistedData.KEY_lastPushTime2, ""));
		        
		        TextView lastPush1 = (TextView) dialog.findViewById(R.id.lastPush1);
		        lastPush1.setText(getResources().getString(R.string.LAST_PUSH_HISTORY) + " " + settings.getString(PersistedData.KEY_lastPush1, getResources().getString(R.string.NO_PUSH_HISTORY))
		        		+ " " + settings.getString(PersistedData.KEY_lastPushTime1, ""));
	    	}
	        return dialog;
	    }
}
