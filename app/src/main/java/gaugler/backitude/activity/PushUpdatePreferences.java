package gaugler.backitude.activity;

import gaugler.backitude.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PushUpdatePreferences extends PreferenceActivity {


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.push_preferences);
	}
}