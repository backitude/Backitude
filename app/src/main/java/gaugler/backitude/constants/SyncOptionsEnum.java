package gaugler.backitude.constants;

public enum SyncOptionsEnum {
	MANUAL_ONLY(1),
	WIFI_ONLY(2),
	ANY_DATA_NETWORK(3)
	;

	private int value;

	private SyncOptionsEnum(int val){
		value = val;
	}
	public static SyncOptionsEnum fromString(String text) 
	{
		if (text != null) {
			for (SyncOptionsEnum b : SyncOptionsEnum.values()) {
				if (text.equalsIgnoreCase(b.getString())) {
					return b;
				}
			}
		}
		return SyncOptionsEnum.MANUAL_ONLY;
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
