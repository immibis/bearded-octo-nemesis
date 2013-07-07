package immibis.bon.gui;

public enum Operation {
	DeobfuscateMod("Deobfuscate mod", ".deobf"),
	ReobfuscateMod("Reobfuscate mod", ".reobf"),
	ReobfuscateModSRG("Reobfuscate mod to SRG", ".srg"),
	SRGifyMod("Deobfuscate mod to SRG", ".srg");
	
	private Operation(String str, String suffix) {
		this.str = str;
		this.defaultNameSuffix = suffix;
	}
	
	private final String str;
	public String toString() {return str;}
	
	public final String defaultNameSuffix;
	
}
