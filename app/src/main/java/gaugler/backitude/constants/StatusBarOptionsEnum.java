package gaugler.backitude.constants;

public enum StatusBarOptionsEnum {
	DISPLAY_NEVER(1),
	DISPLAY_POLLING(2),
	DISPLAY_ENABLED(3),
	DISPLAY_REALTIME(4),
	DISPLAY_POLLING_REALTIME(5)
	;

	private int value;

	private StatusBarOptionsEnum(int val){
		value = val;
	}
	public static StatusBarOptionsEnum fromString(String text) 
	{
		if (text != null) {
			for (StatusBarOptionsEnum b : StatusBarOptionsEnum.values()) {
				if (text.equalsIgnoreCase(b.getString())) {
					return b;
				}
			}
		}
		return StatusBarOptionsEnum.DISPLAY_NEVER;
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
