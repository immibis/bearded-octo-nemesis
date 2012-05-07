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
	private String obfname;
	
	public DeobfuscateVisitor(ClassVisitor base, Main main) {
		super(Opcodes.ASM4, base);
		this.main = main;
	}
	
	@Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		obfname = name;
		for(int k = 0; k < interfaces.length; k++)
			interfaces[k] = main.srg.getClassName(interfaces[k]);
        super.visit(version, access, main.srg.getClassName(name), signature, main.srg.getClassName(superName), interfaces);
    }
    
    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(main.srg.getClassName(owner), main.srg.getClassName(name), desc);
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
    	String seargeName = main.srg.getFieldName(obfname, name);
    	String deobfName = main.fields.get(seargeName);
        return super.visitField(access, deobfName, main.deobfTypeDescriptor(desc), signature, value);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    	SrgFile.MethodInfo smi = main.srg.getMethod(obfname, name, desc);
    	String deobfName = main.methods.get(smi.name);
    	
    	//desc = smi.desc;
    	desc = main.deobfMethodDescriptor(desc);
    	
    	if(exceptions == null)
    		exceptions = new String[0];
    	
    	String[] addExceptions = main.exc.getExceptionClasses(obfname, name, desc);
    	List<String> newExceptions = new LinkedList<String>(Arrays.asList(exceptions));
    	newExceptions.addAll(Arrays.asList(addExceptions));
    	
        return new DeobfuscateMethodVisitor(super.visitMethod(access, deobfName, desc, signature, (String[])newExceptions.toArray(new String[newExceptions.size()])), main);
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
    }
}
