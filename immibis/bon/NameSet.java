package immibis.bon;

/**
 * E.g. "1.5.1 obfuscated", "1.5.1 searge", "1.5.1 MCP" are NameSets.
 */
public class NameSet {
	public static enum Type {
		OBF,
		SRG,
		MCP
	}
	
	public static enum Side {
		UNIVERSAL,
		CLIENT,
		SERVER
	}

	
	public final Type type;
	public final String mcVersion;
	public final Side side;
	
	public NameSet(Type type, Side side, String mcVersion) {
		this.type = type;
		this.side = side;
		this.mcVersion = mcVersion;
	}
	
	@Override
	public boolean equals(Object obj) {
		try {
			NameSet ns = (NameSet)obj;
			return ns.type == type && ns.side == side && ns.mcVersion.equals(mcVersion);
			
		} catch(ClassCastException e) {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return (side.ordinal() << 8) + type.ordinal() + mcVersion.hashCode();
	}
	
	@Override
	public String toString() {
		return mcVersion+" "+type+" "+side;
	}
}
