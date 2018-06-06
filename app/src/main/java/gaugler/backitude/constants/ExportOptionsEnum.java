package gaugler.backitude.constants;

public enum ExportOptionsEnum {
	NONE(1),
	KML(2),
	CSV(3)
	;

	private int value;

	private ExportOptionsEnum(int val){
		value = val;
	}
	public static ExportOptionsEnum fromString(String text) 
	{
		if (text != null) {
			for (ExportOptionsEnum b : ExportOptionsEnum.values()) {
				if (text.equalsIgnoreCase(b.getString())) {
					return b;
				}
			}
		}
		return ExportOptionsEnum.NONE;
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