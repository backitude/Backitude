package gaugler.backitude.widget;

import gaugler.backitude.R;
import gaugler.backitude.util.ZLogger;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PushWidgetConfig extends Activity {

	private int appWidgetId;
	private static final String PREF_NAME_PREFIX_KEY = "ContactName_prefix_";
	private static final String PREF_NUM_PREFIX_KEY = "ContactNumber_prefix_";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// get the appWidgetId of the appWidget being configured
		Intent launchIntent = getIntent();
		Bundle extras = launchIntent.getExtras();
		if(extras==null){
			return;
		}
		appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		
		ZLogger.log("PushWidgetConfig onCreate: widget id = " + appWidgetId);

		// set the result for cancel first
		// if the user cancels, then the appWidget
		// should not appear
		Intent cancelResultValue = new Intent();
		cancelResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,	appWidgetId);
		setResult(RESULT_CANCELED, cancelResultValue);
		// show the user interface of configuration
		setContentView(R.layout.push_widget_config);

		// the OK button
		Button ok = (Button) findViewById(R.id.push_widget_select_button_id);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				try
				{
					//Intent intent = new Intent(Intent.ACTION_PICK, Contacts.Phones.CONTENT_URI);
					Intent intent = new Intent(Intent.ACTION_PICK, Phone.CONTENT_URI);
					startActivityForResult(intent, 1);
				}
				catch(Exception ex)
				{
					ZLogger.logException("PushWidgetConfig", ex, PushWidgetConfig.this);
				}

			}
		});

		// cancel button
		Button cancel = (Button) findViewById(R.id.push_widget_cancel_button_id);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId);
				setResult(RESULT_OK, resultValue);
				// finish closes activity
				// and sends the OK result
				// the widget will be be placed on the home screen
				finish();
			}
		});
	}

	@Override  
	public void onActivityResult(int reqCode, int resultCode, Intent data) {  
		super.onActivityResult(reqCode, resultCode, data); 
		String name = "", phoneNumber = "";
		try{
			ZLogger.log("PushWidgetConfig onActivityResult: " + resultCode);
			if (resultCode == Activity.RESULT_OK) {  
				Uri contactData = data.getData();  
				Cursor c = null;
				try{
					c =  managedQuery(contactData, null, null, null, null);  
					if (c.moveToFirst()) {
						name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
						ZLogger.log("PushWidgetConfig onActivityResult: Contact = " + name);

						String contactId = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
						ZLogger.log("PushWidgetConfig onActivityResult: ID = " + contactId);

						c = getContentResolver().query(
								Phone.CONTENT_URI,
								null,
								Phone._ID + " = ? " , new String[] {contactId}, null);

						int numberIdx = c.getColumnIndex(Phone.DATA);  

						if(c.moveToFirst()) {
							phoneNumber = c.getString(numberIdx);
							ZLogger.log("PushWidgetConfig onActivityResult: number = " + phoneNumber);
							
							final Context context = PushWidgetConfig.this;
							ZLogger.log("PushWidgetConfig onActivityResult: widget id = " + appWidgetId);

							saveTitlePref(context, appWidgetId, name, phoneNumber);

							// Push widget update to surface with newly set prefix
							AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
							PushWidget.updateAppWidget(context, appWidgetManager, appWidgetId, name);

							Intent resultValue = new Intent();
							resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
							setResult(RESULT_OK, resultValue);
							finish();
						} else {
							//WE FAILED
							ZLogger.log("PushWidgetConfig onActivityResult: number not found");
						}
					}
				}catch (Exception e) {
					ZLogger.logException("PushWidgetConfig", e, PushWidgetConfig.this);
				} finally {
					if (c!=null) {
						c.close();
					}
				}
			}
			else
			{
				ZLogger.log("PushWidgetConfig onActivityResult: Cancelled Contact selection");
			}
		}
		catch(Exception ex){
			ZLogger.logException("PushWidgetConfig", ex, PushWidgetConfig.this);
		}
	}

	static void saveTitlePref(Context context, int appWidgetId, String name, String number) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREF_NAME_PREFIX_KEY + appWidgetId, name);
		editor.putString(PREF_NUM_PREFIX_KEY + appWidgetId, number);
		editor.commit();
		ZLogger.log("PushWidgetConfig saveTitlePref: complete for id = " + appWidgetId);
	}
}
