package immibis.bon.gui;

public enum Operation {
	DeobfuscateMod("Deobfuscate mod", ".deobf"),
	ReobfuscateModSRG("Reobfuscate mod", ".deobf"),
	ReobfuscateMod("Reobfuscate mod to Notch names", ".reobf.notch"),
	SRGifyMod("SRGify obfuscated mod", ".srg");
	
	private Operation(String str, String suffix) {
		this.str = str;
		this.defaultNameSuffix = suffix;
	}
	
	private final String str;
	public String toString() {return str;}
	
	public final String defaultNameSuffix;
	
}
