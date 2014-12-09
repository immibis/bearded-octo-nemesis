package immibis.bon;

import immibis.bon.org.objectweb.asm.tree.ClassNode;

public class ClassReferenceData {
	
	static class MethodData {
		String name;
		String desc;
	}
	
	static class FieldData {
		String name;
		String desc;
	}

	String name;
	int access;
	String superName;
	MethodData[] methods;
	FieldData[] fields;
	String[] interfaces;
	
	public static ClassReferenceData fromClassNode(ClassNode cn) {
		ClassReferenceData cr = new ClassReferenceData();
		cr.access = cn.access;
		cr.name = cn.name;
		cr.superName = cn.superName;
		cr.interfaces = cn.interfaces.toArray(new String[cn.interfaces.size()]);
		
		cr.methods = new MethodData[cn.methods.size()];
		for(int k = 0; k < cr.methods.length; k++) {
			cr.methods[k] = new MethodData();
			cr.methods[k].name = cn.methods.get(k).name;
			cr.methods[k].desc = cn.methods.get(k).desc;
		}
		
		cr.fields = new FieldData[cn.fields.size()];
		for(int k = 0; k < cr.fields.length; k++) {
			cr.fields[k] = new FieldData();
			cr.fields[k].name = cn.fields.get(k).name;
			cr.fields[k].desc = cn.fields.get(k).desc;
		}
		
		return cr;
	}

}
