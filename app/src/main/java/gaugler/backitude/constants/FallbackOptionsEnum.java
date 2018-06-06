package gaugler.backitude.constants;

public enum FallbackOptionsEnum {
	MOST_ACCURATE_OR_REPEAT_PREVIOUS(1),
	MOST_ACCURATE_NO_REPEAT(2),
	REPEAT_PREVIOUS(3),
	DO_NOT_UPDATE(4)
	;

	private int value;

	private FallbackOptionsEnum(int val){
		value = val;
	}
	public static FallbackOptionsEnum fromString(String text) 
	{
		if (text != null) {
			for (FallbackOptionsEnum b : FallbackOptionsEnum.values()) {
				if (text.equalsIgnoreCase(b.getString())) {
					return b;
				}
			}
		}
		return FallbackOptionsEnum.MOST_ACCURATE_OR_REPEAT_PREVIOUS;
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