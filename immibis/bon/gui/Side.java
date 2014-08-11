package immibis.bon.gui;

import immibis.bon.mcp.MinecraftNameSet;

public enum Side {
	Universal(MinecraftNameSet.Side.UNIVERSAL, "bin/minecraft"),
	Client(MinecraftNameSet.Side.CLIENT, "bin/minecraft"),
	Server(MinecraftNameSet.Side.SERVER, "bin/minecraft_server");
	
	private Side(MinecraftNameSet.Side nsside, String referencePath) {
		this.nsside = nsside;
		this.referencePath = referencePath;
	}
	
	public final MinecraftNameSet.Side nsside;
	public final String referencePath;
}
