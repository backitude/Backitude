
package gaugler.backitude.activity;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import gaugler.backitude.R;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.util.ZLogger;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;

public class AccountActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		ZLogger.log("AccountActivity onCreate");
		try{
			super.onCreate(savedInstanceState);
			selectAccount();
		}
		catch(Exception ex)
		{
			ZLogger.logException("AccountActivity", ex, getBaseContext());
		}
	}

	private static final int DIALOG_ACCOUNTS = 0;
	private static final int REQUEST_AUTHENTICATE = 0;
	private Account _account = null;
	public GoogleAccountManager accountManager;

	@Override
	protected Dialog onCreateDialog(int id) {
		try{
			accountManager = new GoogleAccountManager(this);
			switch (id) {
			case DIALOG_ACCOUNTS:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.ACCOUNT_SELECTION));
				final Account[] accounts = accountManager.getAccounts();
				final int size = accounts.length;
				String[] names = new String[size];
				for (int i = 0; i < size; i++) {
					ZLogger.log("AccountActivity onCreateDialog: " + accounts[i].name);
					names[i] = accounts[i].name;
				}
				builder.setItems(names, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						ZLogger.log("AccountActivity selected Account: " + accounts[which].name);
						gotAccount(accounts[which]);
					}
				});

				builder.setOnCancelListener(
						new DialogInterface.OnCancelListener()
						{
							public void onCancel(DialogInterface dialog) 
							{
								finish();
							}
						});

				return builder.create();
			}
		}
		catch(Exception ex)
		{
			ZLogger.logException("AccountActivity", ex, getBaseContext());
		}
		return null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_AUTHENTICATE:
			if (resultCode == RESULT_OK) {
				ZLogger.log("AccountActivity onActivityResult: RESULT_OK");
				//refreshAuthToken();
			} else {
				ZLogger.log("AccountActivity else: select account again");
				selectAccount();
			}
			break;
		}
	}

	public void selectAccount() {
		ZLogger.log("AccountActivity selectAccount: show dialog next");
		showDialog(DIALOG_ACCOUNTS);
	}

	void gotAccount(final Account account) {
		
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		//String authToken = prefs.getString(PersistedData.KEY_authToken, "");
		ZLogger.log("AccountActivity gotAccount: " + (account!=null));
		_account = account;
		
		// Added this code for account name
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		final SharedPreferences.Editor editor = settings.edit();
		editor.putString(Prefs.KEY_accountName, _account.name);
		editor.commit();
		Toast.makeText(AccountActivity.this, account.name, Toast.LENGTH_LONG).show();
		/*
		if(authToken.length()>0){
			ZLogger.log("AccountActivity previous authToken exists");
			accountManager.invalidateAuthToken(authToken);
		}
		ZLogger.log("Going to try and make a Toast");
		Toast.makeText(AccountActivity.this, getResources().getString(R.string.TOKEN_REFRESH), Toast.LENGTH_SHORT).show();
		ZLogger.log("Going to call the refresh token method");
		try{
			refreshAuthToken();
		}
		catch(Exception ex)
		{
			ZLogger.logException("AccountActivity", ex, getBaseContext());
		}
		 */
		finish();
	}

/*
	private void refreshAuthToken() {
		try{
			if(_account!=null){
				ZLogger.log("AccountActivity refreshAuthToken");
				accountManager.getAccountManager().getAuthToken(_account, Constants.SCOPE, null, this, new AccountManagerCallback<Bundle>() {
					public void run(AccountManagerFuture<Bundle> result) {
						try {
							ZLogger.log("AccountActivity AccountManagerCallback");
							Bundle bundle = result.getResult();
							final String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
							final String refreshToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
							final Intent authIntent = bundle.getParcelable(AccountManager.KEY_INTENT);
							if (accountName != null && refreshToken != null) {
								ZLogger.log("AccountActivity AccountManagerCallback: accountName != null && refreshToken != null");
								final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
								final SharedPreferences.Editor editor = settings.edit();
								editor.putString(PersistedData.KEY_authToken, refreshToken);
								editor.putString(Prefs.KEY_accountName, accountName);
								editor.commit();
								ZLogger.log("Token refreshed: " + settings.getString(PersistedData.KEY_authToken, ""));
								finish();
							} else if (authIntent != null) {
								ZLogger.log("AccountActivity AccountManagerCallback: authIntent != null");
								int flags = authIntent.getFlags();
								flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
								authIntent.setFlags(flags);
								startActivityForResult(authIntent, REQUEST_AUTHENTICATE);
							} else {
								ZLogger.log("AccountActivity AccountManagerCallback: Google just aint working right now");
								finish();
							}
						}
						catch (OperationCanceledException exception) {
							ZLogger.log("AccountActivity AccountManagerCallback: OperationCanceledException, user Denied Permission");
							finish();
						} catch (AuthenticatorException exception) {
							ZLogger.logException("AccountActivity", exception, getBaseContext());
							finish();
						} catch (IOException exception) {
							ZLogger.logException("AccountActivity", exception, getBaseContext());
							finish();
						}
					}
				}, null);

			}
			else
			{
				ZLogger.log("Account selection null.");
				selectAccount();
			}
		}
		catch(Exception ex)
		{
			ZLogger.logException("AccountActivity", ex, getBaseContext());
		}
	}
	*/

}
