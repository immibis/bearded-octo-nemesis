package immibis.bon;

/**
 * Default implementation of a NameSet.
 */
public final class SimpleNameSet extends NameSet {
	
	private final String name;
	
	public SimpleNameSet(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof SimpleNameSet && ((SimpleNameSet)o).name.equals(name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public String toString() {
		return name;
	}
}
