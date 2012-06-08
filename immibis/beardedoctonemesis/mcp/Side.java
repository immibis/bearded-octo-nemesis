package immibis.beardedoctonemesis.mcp;

public enum Side {
	CLIENT("client.srg", "client.exc", 0),
	SERVER("server.srg", "server.exc", 1);
	
	public static Side fromString(String s) {
		if(s.equalsIgnoreCase("client"))
			return CLIENT;
		if(s.equalsIgnoreCase("server"))
			return SERVER;
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
