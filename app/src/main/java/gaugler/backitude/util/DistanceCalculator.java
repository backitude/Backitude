package gaugler.backitude.util;

import gaugler.backitude.constants.PersistedData;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;

public class DistanceCalculator {

	public static boolean isBeyondDistance(Location newLocation, Context context, float minDistance)
	{
		Location lastUpdatedLocation = new Location(LocationManager.NETWORK_PROVIDER);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		lastUpdatedLocation.setLatitude((double)prefs.getFloat(PersistedData.KEY_savedLocation_lat, 0));
		lastUpdatedLocation.setLongitude((double)prefs.getFloat(PersistedData.KEY_savedLocation_long, 0));
		lastUpdatedLocation.setAccuracy(prefs.getFloat(PersistedData.KEY_savedLocation_accur, 0));

		if(newLocation.getAccuracy() >= lastUpdatedLocation.getAccuracy())
		{
			// Math.max function calls are implemented for the case where Accuracy values are negative.  
			// Some apps pass around negatively accurate location values, even though that does not make sense
			float distanceTo = newLocation.distanceTo(lastUpdatedLocation);
			ZLogger.log("DistanceCalculator isBeyondDistance: distanceBetween: " + 
					(distanceTo - Math.max(newLocation.getAccuracy(), 0) - Math.max(lastUpdatedLocation.getAccuracy(),0)) + 
							", minDistance needed: " + minDistance);
			ZLogger.log("DistanceCalculator isBeyondDistance: " + ((distanceTo - Math.max(newLocation.getAccuracy(),0) - Math.max(lastUpdatedLocation.getAccuracy(),0)) > minDistance));
			return ((distanceTo - Math.max(newLocation.getAccuracy(),0) - Math.max(lastUpdatedLocation.getAccuracy(),0)) > minDistance);
		}
		else
		{
			ZLogger.log("DistanceCalculator isBeyondDistance: true (new location is more accurate)");
			return true;
		}
	}

	public static Location isWithinCircle(Location location, Service service)
	{
		Location lastPolledLocation = PreferenceHelper.getLastPolledLocation(service.getBaseContext());
		//TODO What to do if there is no "Last Polled Location" >????
		if(location.getAccuracy() <= lastPolledLocation.getAccuracy())
		{
			// New location is at least as accurate as the previous, use it.
			ZLogger.log("DistanceCalculator isWithinCircle: Use new location, it is just as accurate");
			return location;
		}
		else
		{
			// New location is not as accurate as the previous.  Use
			// previous unless change in distance is enough to prove location is new.

			if(location.getAccuracy() > 400 &&
					(lastPolledLocation.getAccuracy() * 10) < location.getAccuracy())
			{
				// New location is not as accurate as the previous. Use
				// previous unless change in distance is enough to prove location is new.
				if(location.distanceTo(lastPolledLocation) > (2*location.getAccuracy()))
				{
					ZLogger.log("DistanceCalculator isWithinCircle: Use new location, it is too far away from previous location");
					return location;
				}
				else
				{
					ZLogger.log("DistanceCalculator isWithinCircle: Use previous location, it is more accurate and near current location");
					return lastPolledLocation;
				}
			}
			else
			{
				if(location.distanceTo(lastPolledLocation) >
					Math.max(location.getAccuracy(),0) + Math.max(lastPolledLocation.getAccuracy(),0))
				{
					ZLogger.log("DistanceCalculator isWithinCircle: Use new location, it is too far away from previous location");
					return location;
				}
				else
				{
					ZLogger.log("DistanceCalculator isWithinCircle: Use previous location, it is more accurate and near current location");
					return lastPolledLocation;
				}
			}
		}
	}
}

