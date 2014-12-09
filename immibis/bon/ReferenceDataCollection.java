package immibis.bon;

import immibis.bon.org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Similar to ClassCollection, but contains less information.
 * You can now pass ReferenceData objects (which are much smaller) instead of ClassCollection objects
 * as references when remapping.
 * This avoids having to e.g. load the entire standard library.
 */
public class ReferenceDataCollection {
	private NameSet nameset;
	private Collection<ClassReferenceData> classes = new ArrayList<>();
	
	public ReferenceDataCollection(NameSet nameset) {
		this.nameset = nameset;
	}
	
	public NameSet getNameSet() {
		return nameset;
	}
	
	public Collection<ClassReferenceData> getAllClasses() {
		return classes;
	}

	public static ReferenceDataCollection fromClassCollection(ClassCollection cc) {
		ReferenceDataCollection rdc = new ReferenceDataCollection(cc.getNameSet());
		for(ClassNode cn : cc.getAllClasses())
			rdc.classes.add(ClassReferenceData.fromClassNode(cn));
		return rdc;
	}
}
