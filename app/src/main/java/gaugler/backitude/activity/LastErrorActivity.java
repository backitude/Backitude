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

public class LastErrorActivity extends Activity {

	static final int DIALOG_LAST_ERROR = 0;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		  
		showDialog(DIALOG_LAST_ERROR);

	}

	@Override
	protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_LAST_ERROR:
            return launchErrorHistoryDialog();
        default:
            return null;
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}
	
	 private Dialog launchErrorHistoryDialog()
	    {
	    	Dialog dialog = new Dialog(LastErrorActivity.this);

	    	dialog.setContentView(R.layout.error_history);
	    	dialog.setTitle(R.string.lastError_dialog);
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
		        TextView errorLine1 = (TextView) dialog.findViewById(R.id.errorLine1);
		        errorLine1.setText(getResources().getString(R.string.ERROR_LABEL) + " " + settings.getString(PersistedData.KEY_lastErrorMsg1, ""));
		        TextView errorTime1 = (TextView) dialog.findViewById(R.id.errorTime1);
		        errorTime1.setText(getResources().getString(R.string.ERROR_TS_LABEL) + " " + settings.getString(PersistedData.KEY_lastErrorTime, ""));
		        TextView errorLine2 = (TextView) dialog.findViewById(R.id.errorLine2);
		        errorLine2.setText(getResources().getString(R.string.ERROR_LABEL) + " " + settings.getString(PersistedData.KEY_lastErrorMsg2, ""));
		        TextView errorTime2 = (TextView) dialog.findViewById(R.id.errorTime2);
		        errorTime2.setText(getResources().getString(R.string.ERROR_TS_LABEL) + " " + settings.getString(PersistedData.KEY_lastErrorTime2, ""));
	    	}
	        return dialog;
	    }
}
