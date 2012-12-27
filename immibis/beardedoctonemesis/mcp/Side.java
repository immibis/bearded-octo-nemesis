package immibis.beardedoctonemesis.mcp;

public enum Side {
	PACKAGED("packaged.srg", "packaged.exc", 2), // shouldn't really be a side, but it works for now
	JOINED("joined.srg", "joined.exc", 2),
	CLIENT("client.srg", "client.exc", 0),
	SERVER("server.srg", "server.exc", 1);
	
	public static Side fromString(String s) {
		if(s.equalsIgnoreCase("joined") || s.equals("universal"))
			return JOINED;
		if(s.equalsIgnoreCase("client"))
			return CLIENT;
		if(s.equalsIgnoreCase("server"))
			return SERVER;
		if(s.equalsIgnoreCase("packaged"))
			return PACKAGED;
		return null;
	}
	
	private Side(String srg_name, String exc_name, int side_number) {
		this.srg_name = srg_name;
		this.exc_name = exc_name;
		this.side_number = side_number;
	}
	
	public final String srg_name;
	public final String exc_name;
	public final int side_number;
}
