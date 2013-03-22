package immibis.beardedoctonemesis;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class DeobfuscateVisitor extends ClassVisitor {
	
	private Main main;
	private String inClassName;
	private String outClassName;
	
	public DeobfuscateVisitor(ClassVisitor base, Main main) {
		super(Opcodes.ASM4, base);
		this.main = main;
	}
	
	@Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		inClassName = name;
		outClassName = main.map.getClass(name);
		for(int k = 0; k < interfaces.length; k++)
			interfaces[k] = main.map.getClass(interfaces[k]);
        super.visit(version, access, outClassName, signature, main.map.getClass(superName), interfaces);
    }
    
    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(main.map.getClass(owner), main.map.getClass(name), desc);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitAttribute(Attribute attr) {
        super.visitAttribute(attr);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
    	String deobfName = main.resolveField(inClassName, name, desc);
        return super.visitField(access, deobfName, main.deobfTypeDescriptor(desc), signature, value);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    	String deobfName = main.lookupInheritedMethod(inClassName, name, desc);
    	
    	desc = main.deobfMethodDescriptor(desc);
    	
    	if(exceptions == null)
    		exceptions = new String[0];
    	
    	for (int i = 0; i < exceptions.length; ++i) {
            exceptions[i] = main.map.getClass(exceptions[i]);
        }
    	List<String> newExceptions = new LinkedList<String>(Arrays.asList(exceptions));
    	newExceptions.addAll(main.map.getExceptions(inClassName, name, desc));
    	
        return new DeobfuscateMethodVisitor(
        		super.visitMethod(access, deobfName, desc, signature,
        				(String[])newExceptions.toArray(new String[newExceptions.size()])),
        		main,
        		inClassName);
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
    }
}
