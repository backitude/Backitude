package gaugler.backitude.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import gaugler.backitude.constants.AuthenticationOptionsEnum;
import gaugler.backitude.constants.Prefs;
import gaugler.backitude.util.ZLogger;

public class ParameterBean {

	private Context _context = null;
	private String _userName = null;
	private String _password = null;
	private String _latitude = null;
	private String _longitude = null;
	private String _locTs = null;
	private String _reqTs = null;
	private String _accuracy = null;
	private String _speed = null;
	private String _altitude = null;
	private String _bearing = null;

	public ParameterBean(Context context){
		_context = context;

		_userName = null;
		_password = null;
		_latitude = null;
		_longitude = null;
		_locTs = null;
		_reqTs = null;
		_accuracy = null;
		_speed = null;
		_altitude = null;
		_bearing = null;
	}

	public String get_userName() {
		return _userName;
	}

	public void set_userName(String _userName) {
		this._userName = _userName;
	}

	public String get_password() {
		return _password;
	}

	public void set_password(String _password) {
		this._password = _password;
	}

	public String get_latitude() {
		return _latitude;
	}

	public void set_latitude(String _latitude) {
		this._latitude = _latitude;
	}

	public String get_longitude() {
		return _longitude;
	}

	public void set_longitude(String _longitude) {
		this._longitude = _longitude;
	}

	public String get_locTs() {
		return _locTs;
	}

	public void set_locTs(String _locTs) {
		this._locTs = _locTs;
	}

	public String get_reqTs() {
		return _reqTs;
	}

	public void set_reqTs(String _reqTs) {
		this._reqTs = _reqTs;
	}

	public String get_accuracy() {
		return _accuracy;
	}

	public void set_accuracy(String _accuracy) {
		this._accuracy = _accuracy;
	}

	public String get_speed() {
		return _speed;
	}

	public void set_speed(String _speed) {
		this._speed = _speed;
	}

	public String get_altitude() {
		return _altitude;
	}

	public void set_altitude(String _altitude) {
		this._altitude = _altitude;
	}

	public String get_bearing() {
		return _bearing;
	}

	public void set_bearing(String _bearing) {
		this._bearing = _bearing;
	}

	private int getNumberOfParameters(){

		if(_context == null){
			ZLogger.log("ParameterBean getNumberOfParameters: context is null");
			return 0;
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
		String authType = prefs.getString(Prefs.KEY_authentication, AuthenticationOptionsEnum.NONE.getString());
		// Number of parameters will be anywhere from 4 to 12
		int count = 4;  // Latitude, Longitude, TS1, TS2
		if (authType.equals(AuthenticationOptionsEnum.POST_PARAMS.getString())||authType.equals(AuthenticationOptionsEnum.BOTH.getString())){
			count = count + 2;
		}
		if(get_accuracy()!=null && prefs.getString(Prefs.KEY_server_key_accuracy, "").trim().length()>0)
		{
			count = count + 1;
		}
		if(get_altitude()!=null && prefs.getString(Prefs.KEY_server_key_altitude, "").trim().length()>0)
		{
			count = count + 1;
		}
		if(get_bearing()!=null && prefs.getString(Prefs.KEY_server_key_bearing, "").trim().length()>0)
		{
			count = count + 1;
		}
		if(get_speed()!=null && prefs.getString(Prefs.KEY_server_key_speed, "").trim().length()>0)
		{
			count = count + 1;
		}
		if(prefs.contains(Prefs.KEY_server_uid) &&  prefs.getString(Prefs.KEY_server_uid,"").length()>0 && prefs.getString(Prefs.KEY_server_key_uid, "").trim().length()>0)
		{
			count = count + 1;
		}
		if(prefs.contains(Prefs.KEY_accountName) &&  prefs.getString(Prefs.KEY_accountName,"").length()>0 && prefs.getString(Prefs.KEY_server_key_account, "").trim().length()>0)
		{
			count = count + 1;
		}
		return count;
	}

	private List<NameValuePair> nameValuePairs;
	public List<NameValuePair> getNameValuePairs(){

		if(_context == null){
			ZLogger.log("ParameterBean getNumberOfParameters: context is null");
			return new ArrayList<NameValuePair>(0);
		}

		nameValuePairs = new ArrayList<NameValuePair>(getNumberOfParameters());

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
		nameValuePairs.add(new BasicNameValuePair(prefs.getString(Prefs.KEY_server_key_latitude, Prefs.DEFAULT_server_key_latitude), get_latitude()));
		nameValuePairs.add(new BasicNameValuePair(prefs.getString(Prefs.KEY_server_key_longitude, Prefs.DEFAULT_server_key_longitude), get_longitude()));
		nameValuePairs.add(new BasicNameValuePair(prefs.getString(Prefs.KEY_server_key_loc_timestamp, Prefs.DEFAULT_server_key_loc_timestamp), get_locTs()));
		nameValuePairs.add(new BasicNameValuePair(prefs.getString(Prefs.KEY_server_key_req_timestamp, Prefs.DEFAULT_server_key_req_timestamp), get_reqTs()));

		String authType = prefs.getString(Prefs.KEY_authentication, AuthenticationOptionsEnum.NONE.getString());
		if (authType.equals(AuthenticationOptionsEnum.POST_PARAMS.getString())||authType.equals(AuthenticationOptionsEnum.BOTH.getString())){
			nameValuePairs.add(new BasicNameValuePair(prefs.getString(Prefs.KEY_server_key_username, Prefs.DEFAULT_server_key_username), prefs.getString(Prefs.KEY_server_user_name, Prefs.DEFAULT_server_user_name)));
			nameValuePairs.add(new BasicNameValuePair(prefs.getString(Prefs.KEY_server_key_password, Prefs.DEFAULT_server_key_password), prefs.getString(Prefs.KEY_server_password, Prefs.DEFAULT_server_password)));
		}
		if(get_accuracy()!=null && prefs.getString(Prefs.KEY_server_key_accuracy, "").trim().length()>0)
		{
			nameValuePairs.add(new BasicNameValuePair(prefs.getString(Prefs.KEY_server_key_accuracy, Prefs.DEFAULT_server_key_accuracy), get_accuracy()));
		}
		if(get_altitude()!=null && prefs.getString(Prefs.KEY_server_key_altitude, "").trim().length()>0)
		{
			nameValuePairs.add(new BasicNameValuePair(prefs.getString(Prefs.KEY_server_key_altitude, Prefs.DEFAULT_server_key_altitude), get_altitude()));
		}
		if(get_bearing()!=null && prefs.getString(Prefs.KEY_server_key_bearing, "").trim().length()>0)
		{
			nameValuePairs.add(new BasicNameValuePair(prefs.getString(Prefs.KEY_server_key_bearing, Prefs.DEFAULT_server_key_bearing), get_bearing()));
		}
		if(get_speed()!=null && prefs.getString(Prefs.KEY_server_key_speed, "").trim().length()>0)
		{
			nameValuePairs.add(new BasicNameValuePair(prefs.getString(Prefs.KEY_server_key_speed, Prefs.DEFAULT_server_key_speed), get_speed()));
		}
		if(prefs.contains(Prefs.KEY_server_uid) &&  prefs.getString(Prefs.KEY_server_uid,"").length()>0 && prefs.getString(Prefs.KEY_server_key_uid, "").trim().length()>0)
		{
			nameValuePairs.add(new BasicNameValuePair(prefs.getString(Prefs.KEY_server_key_uid, Prefs.DEFAULT_server_key_uid), prefs.getString(Prefs.KEY_server_uid, "")));
		}
		if(prefs.contains(Prefs.KEY_accountName) &&  prefs.getString(Prefs.KEY_accountName,"").length()>0 && prefs.getString(Prefs.KEY_server_key_account, "").trim().length()>0)
		{
			nameValuePairs.add(new BasicNameValuePair(prefs.getString(Prefs.KEY_server_key_account, Prefs.DEFAULT_server_key_account), prefs.getString(Prefs.KEY_accountName, Prefs.DEFAULT_accountName)));
		}

		return nameValuePairs;
	}

	public String toString(){
		return getNameValuePairs().toString();
	}



}
