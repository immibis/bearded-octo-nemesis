package immibis.bon.gui;

public enum Side {
	Universal(immibis.bon.mcp.Side.PACKAGED, "temp/client_reobf.jar"),
	Universal_old(immibis.bon.mcp.Side.JOINED, "temp/client_reobf.jar"),
	Client(immibis.bon.mcp.Side.CLIENT, "temp/client_reobf.jar"),
	Server(immibis.bon.mcp.Side.SERVER, "temp/server_reobf.jar");
	
	private Side(immibis.bon.mcp.Side mcpside, String xpath) {
		this.mcpside = mcpside;
		this.xpath = xpath;
	}
	
	public final immibis.bon.mcp.Side mcpside;
	public final String xpath;
}
