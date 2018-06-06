package gaugler.backitude.util;

import gaugler.backitude.constants.Constants;
import gaugler.backitude.widget.OnOffWidget;
import gaugler.backitude.widget.RealtimeWidget;
import android.content.Context;
import android.content.Intent;

public class UpdateWidgetHelper {


	public static void updateRealtimeWidget(Context context)
	{		
		ZLogger.log("UpdateWidgetHelper updateRealtimeWidget: method start");

		Intent uiIntent = new Intent(context, RealtimeWidget.class);
		uiIntent.setAction(Constants.REALTIME_WIDGET_ACTION_UPDATE_WIDGET);
		context.sendBroadcast(uiIntent);

	}

	public static void updateOnOffWidget(boolean appEnabled, Context context) {
		if(context!=null){
			ZLogger.log("UpdateWidgetHelper updateOnOffWidget: method start for app enabled: " + appEnabled);
			Intent uiIntent = new Intent(context, OnOffWidget.class);
			uiIntent.setAction(Constants.ON_OFF_WIDGET_ACTION_UPDATE_WIDGET);
			uiIntent.putExtra(Constants.ON_OFF_WIDGET_PARAM, appEnabled);
			context.sendBroadcast(uiIntent);
		}
	}
}
