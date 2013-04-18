package immibis.bon.gui;

import immibis.bon.NameSet;

public enum Side {
	Universal(NameSet.Side.UNIVERSAL, "bin/minecraft"),
	Client(NameSet.Side.CLIENT, "bin/minecraft"),
	Server(NameSet.Side.SERVER, "bin/minecraft_server");
	
	private Side(NameSet.Side nsside, String referencePath) {
		this.nsside = nsside;
		this.referencePath = referencePath;
	}
	
	public final NameSet.Side nsside;
	public final String referencePath;
}
