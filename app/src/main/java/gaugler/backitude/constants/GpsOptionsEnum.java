package gaugler.backitude.constants;

public enum GpsOptionsEnum {
	GPS_ALL(1),
	GPS_WIFI(2),
	GPS_ONLY(3),
	WIFI_ALL(4),
	WIFI_GPS(5),
	WIFI_CELL(6),
	WIFI_ONLY(7),
	WIFI_THEN_REST(8),
	WIFI_THEN_GPS(9)
	;

	private int value;

	private GpsOptionsEnum(int val){
		value = val;
	}
	public static GpsOptionsEnum fromString(String text) 
	{
		if (text != null) {
			for (GpsOptionsEnum b : GpsOptionsEnum.values()) {
				if (text.equalsIgnoreCase(b.getString())) {
					return b;
				}
			}
		}
		return GpsOptionsEnum.GPS_ALL;
	}


	public int getValue()
	{
		return value;
	}

	public String getString()
	{
		return String.valueOf(value);
	}
}
