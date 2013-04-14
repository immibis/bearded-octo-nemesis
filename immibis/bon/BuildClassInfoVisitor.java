package immibis.bon;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BuildClassInfoVisitor extends ClassVisitor {
	
	public String name, parent; // internal names
	public String[] interfaces;
	
	public Map<String, String> fieldDescriptors = new HashMap<>();
	
	public BuildClassInfoVisitor() {
		super(Opcodes.ASM4, null);
	}
	
	@Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this.name = name;
		this.parent = superName;
		this.interfaces = interfaces;
    }
   
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
    	fieldDescriptors.put(name, desc);
    	
    	return null;
    }

}
