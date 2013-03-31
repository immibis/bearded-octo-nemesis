package immibis.beardedoctonemesis.gui;

public enum Side {
	Universal(immibis.beardedoctonemesis.mcp.Side.PACKAGED, "temp/client_reobf.jar"),
	Universal_old(immibis.beardedoctonemesis.mcp.Side.JOINED, "temp/client_reobf.jar"),
	Client(immibis.beardedoctonemesis.mcp.Side.CLIENT, "temp/client_reobf.jar"),
	Server(immibis.beardedoctonemesis.mcp.Side.SERVER, "temp/server_reobf.jar");
	
	private Side(immibis.beardedoctonemesis.mcp.Side mcpside, String xpath) {
		this.mcpside = mcpside;
		this.xpath = xpath;
	}
	
	public final immibis.beardedoctonemesis.mcp.Side mcpside;
	public final String xpath;
}
