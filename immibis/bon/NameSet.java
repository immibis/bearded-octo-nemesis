package immibis.bon;

/**
 * E.g. for Minecraft, "1.2.5 obfuscated", "1.5.1 SRG", "1.6.4 MCP" are NameSets.
 */
public abstract class NameSet {
	public abstract boolean equals(Object o);
	public abstract int hashCode();
	public abstract String toString();
}
