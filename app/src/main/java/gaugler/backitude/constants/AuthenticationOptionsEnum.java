package gaugler.backitude.constants;

public enum AuthenticationOptionsEnum {
	NONE(1),
	POST_PARAMS(2),
	BASIC_AUTH(3),
	BOTH(4)
	;

	private int value;

	private AuthenticationOptionsEnum(int val){
		value = val;
	}
	public static AuthenticationOptionsEnum fromString(String text) 
	{
		if (text != null) {
			for (AuthenticationOptionsEnum b : AuthenticationOptionsEnum.values()) {
				if (text.equalsIgnoreCase(b.getString())) {
					return b;
				}
			}
		}
		return AuthenticationOptionsEnum.NONE;
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