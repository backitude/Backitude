package gaugler.backitude.activity;

import gaugler.backitude.R;
import gaugler.backitude.constants.Constants;
import gaugler.backitude.constants.Prefs;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class AccuracyPreferences extends PreferenceActivity {

	CheckBoxPreference enforceAccuracy;
	ListPreference fallbackOptions_pref;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.accuracy_settings);

		enforceAccuracy = (CheckBoxPreference) findPreference(Prefs.KEY_min_accuracy);
		enforceAccuracy.setOnPreferenceChangeListener(new
				Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(!enforceAccuracy.isChecked()){
					Toast.makeText(getBaseContext(), getResources().getString(R.string.ENFORCE_ACCURACY), Toast.LENGTH_LONG).show();
				}
				return true;
			}
		});

		fallbackOptions_pref = (ListPreference) findPreference(Prefs.KEY_fallbackOptions);
		fallbackOptions_pref.setOnPreferenceChangeListener(new
				Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(newValue!=null && !newValue.toString().equals("")) {
					int new_fallback_option = 0;
					try{
						new_fallback_option = Integer.parseInt(newValue.toString());	
					}
					catch(NumberFormatException ex) {}
					switch(new_fallback_option){
					case 1:
						Toast.makeText(getBaseContext(), getResources().getString(R.string.fallbackOptions_1), Toast.LENGTH_LONG).show();
						break;
					case 2: 
						Toast.makeText(getBaseContext(), getResources().getString(R.string.fallbackOptions_2), Toast.LENGTH_LONG).show();
						break;
					case 3:
						Toast.makeText(getBaseContext(), getResources().getString(R.string.fallbackOptions_3), Toast.LENGTH_LONG).show();
						break;
					case 4:
						Toast.makeText(getBaseContext(), getResources().getString(R.string.fallbackOptions_4), Toast.LENGTH_LONG).show();
						break;
					default:
						break;
					}
				}
				return true;
			}
		});
	}

	@Override
	public void onStart()
	{
		super.onStart();

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		float minDistance = Float.parseFloat(settings.getString(Prefs.KEY_min_distance, Prefs.DEFAULT_min_distance));
		enforceAccuracy.setEnabled(minDistance==Constants.NO_MIN_CHANGE_DISTANCE);
	}


}