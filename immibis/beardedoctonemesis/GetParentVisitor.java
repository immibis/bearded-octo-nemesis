package immibis.beardedoctonemesis;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class GetParentVisitor extends ClassVisitor {
	
	public String name, parent; // internal names
	public String[] interfaces;
	
	public GetParentVisitor() {
		super(Opcodes.ASM4, null);
	}
	
	@SuppressWarnings("serial")
	public static class FinishedException extends RuntimeException {}
	
	@Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this.name = name;
		this.parent = superName;
		this.interfaces = interfaces;
		throw new FinishedException();
    }
    
    @Override
    public void visitOuterClass(String owner, String name, String desc) {
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    	return null;
    }

    @Override
    public void visitAttribute(Attribute attr) {
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
    	return null;
    }

    @Override
    public void visitEnd() {
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    	return null;
    }

    @Override
    public void visitSource(String source, String debug) {
    }
}
