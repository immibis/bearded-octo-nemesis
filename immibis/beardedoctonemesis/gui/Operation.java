package immibis.beardedoctonemesis.gui;

public enum Operation {
	DeobfuscateMod("Deobfuscate mod");
	
	private Operation(String str) {
		this.str = str;
	}
	private final String str;
	public String toString() {return str;}
}
