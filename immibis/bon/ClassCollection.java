package immibis.bon;

import immibis.bon.org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClassCollection implements Cloneable {
	public ClassCollection(NameSet nameSet, Collection<ClassNode> classes) {
		this.nameSet = nameSet;
		this.classes.addAll(classes);
	}
	
	private NameSet nameSet;
	private Collection<ClassNode> classes = new ArrayList<>();
	private Map<String, byte[]> extraFiles = new HashMap<>();
	
	public Collection<ClassNode> getAllClasses() {
		return classes;
	}
	
	public NameSet getNameSet() {
		return nameSet;
	}
	
	@Override
	public ClassCollection clone() {
		try {
			ClassCollection clone = (ClassCollection)super.clone();
			clone.classes = new ArrayList<>();
			
			for(ClassNode ocn : classes) {
				// clone the ClassNode
				ClassNode ncn = new ClassNode();
				ocn.accept(ncn);
				clone.classes.add(ncn);
			}
			
			// copy map, but don't copy data
			clone.extraFiles = new HashMap<>(extraFiles);
			
			return clone;
			
		} catch(CloneNotSupportedException e) {
			throw new RuntimeException("This can't happen", e);
		}
	}
	
	public ClassCollection cloneWithNameSet(NameSet newNS) {
		ClassCollection rv = clone();
		rv.nameSet = newNS;
		return rv;
	}

	public Map<String, ClassNode> getClassMap() {
		Map<String, ClassNode> rv = new HashMap<String, ClassNode>();
		for(ClassNode cn : classes)
			rv.put(cn.name, cn);
		return rv;
	}

	public Map<String, byte[]> getExtraFiles() {
		return extraFiles;
	}

	
}
