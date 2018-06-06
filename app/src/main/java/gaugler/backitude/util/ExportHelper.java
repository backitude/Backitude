package gaugler.backitude.util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import gaugler.backitude.R;

import android.content.Context;
import android.location.Location;
import android.os.Environment;


public class ExportHelper {   

	public static void writeKML(Location _location){
		ZLogger.log("ExportHelper writeKML: start");

		try{
			SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
			String fileName = "backitude-daily-kml-" + formatter.format(new Date()) + ".kml";
			ZLogger.log("ExportHelper writeKML: filename = " + fileName);

			String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
			ZLogger.log("ExportHelper writeKML: baseDir = " + baseDir);

			//String appDataPath = "Android" + File.separator + "data" + File.separator + "gaugler.backitude" + File.separator + "kml";
			//ZLogger.log("ExportHelper writeKML: appDataPath = " + appDataPath);
			File sdDir = new File(baseDir + File.separator + "Backitude");

			if(!sdDir.exists()){
				sdDir.mkdir();
			}

			if (sdDir.canWrite()) {
				File logFile = new File (sdDir, fileName);
				StringBuilder kml = new StringBuilder();
				if(!logFile.exists() && !logFile.isDirectory()){
					kml.append(System.getProperty("line.separator"));
					kml.append("<Style id='icon-503-ff8277'><IconStyle><color>ff7782ff</color><scale>1.1</scale><Icon><href>http://www.gstatic.com/mapspro/images/stock/503-wht-blank_maps.png</href></Icon></IconStyle></Style>");
					kml.append(System.getProperty("line.separator"));
					kml.append(System.getProperty("line.separator"));
				}
				FileWriter logFileWriter = new FileWriter(logFile, true);
				BufferedWriter out = new BufferedWriter(logFileWriter);
 
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); //2005-08-22T07:36:00Z
				Date locTime = new Date(_location.getTime());
				Date now = new Date();
				int offsetFromUtc = 0;
				//TODO OFFSET
				kml.append("<Placemark>").append(System.getProperty("line.separator"));
				kml.append("<styleUrl>#icon-503-ff8277</styleUrl>").append(System.getProperty("line.separator"));
				kml.append("<name>");
				kml.append(sdf.format(now));
				kml.append("</name>").append(System.getProperty("line.separator"));
				kml.append("<Timestamp><when>");
				kml.append(sdf.format(locTime));
				kml.append("</when></Timestamp>").append(System.getProperty("line.separator"));
				kml.append("<Point><coordinates>");
				kml.append(_location.getLongitude());
				kml.append(",");
				kml.append(_location.getLatitude());
				if(_location.hasAccuracy()){
					kml.append(",");
					kml.append(_location.getAccuracy());
				}
				kml.append("</coordinates></Point>").append(System.getProperty("line.separator"));
				kml.append("</Placemark>").append(System.getProperty("line.separator"));

				out.write("\n" + kml.toString() + "\n");
				out.close();
				ZLogger.log("ExportHelper writeKML: complete");
			}
			else
			{
				ZLogger.log("ExportHelper writeKML: can not write to directory");
			}
		} catch (Exception e) {
			ZLogger.log("ExportHelper writeKML: Exception failed to write to kml - " + e.toString());
		}

	}

	public static void writeCSV(Context context, Location _location) {
		ZLogger.log("ExportHelper writeCSV: start");

		try{
			SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
			String fileName = "backitude-daily-csv-" + formatter.format(new Date()) + ".csv";
			ZLogger.log("ExportHelper writeCSV: filename = " + fileName);

			String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
			ZLogger.log("ExportHelper writeCSV: baseDir = " + baseDir);

			File sdDir = new File(baseDir + File.separator + "Backitude");

			if(!sdDir.exists()){
				sdDir.mkdir();
			}

			if (sdDir.canWrite()) {
				File logFile = new File (sdDir, fileName);
				StringBuilder csv = new StringBuilder();
				if(!logFile.exists() && !logFile.isDirectory()){
					csv.append(System.getProperty("line.separator"));
					csv.append(context.getResources().getString(R.string.server_key_latitude_title));
					csv.append(",");
					csv.append(context.getResources().getString(R.string.server_key_longitude_title));
					csv.append(",");
					csv.append(context.getResources().getString(R.string.server_key_accuracy_title));
					csv.append(",");
					csv.append(context.getResources().getString(R.string.server_key_loc_timestamp_title));
					csv.append(",");
					csv.append(context.getResources().getString(R.string.server_key_req_timestamp_title));
					csv.append(System.getProperty("line.separator"));
				}
				FileWriter logFileWriter = new FileWriter(logFile, true);
				BufferedWriter out = new BufferedWriter(logFileWriter);
 
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); //2005-08-22T07:36:00Z
				Date locTime = new Date(_location.getTime());
				Date now = new Date();
				int offsetFromUtc = 0;
				
				csv.append(_location.getLatitude());
				csv.append(",");
				csv.append(_location.getLongitude());
				csv.append(",");
				if(_location.hasAccuracy()){
					csv.append(_location.getAccuracy());
				} else {
					csv.append("NULL");
				}
				csv.append(",");
				csv.append(sdf.format(locTime));
				csv.append(",");
				csv.append(sdf.format(now));

				out.write("\n" + csv.toString() + "\n");
				out.close();
				ZLogger.log("ExportHelper writeCSV: complete");
			}
			else
			{
				ZLogger.log("ExportHelper writeCSV: can not write to directory");
			}
		} catch (Exception e) {
			ZLogger.log("ExportHelper writeCSV: Exception failed to write to csv - " + e.toString());
		}
		
	}

}
