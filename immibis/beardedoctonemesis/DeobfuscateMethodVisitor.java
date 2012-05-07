package immibis.beardedoctonemesis;

import org.objectweb.asm.*;

public class DeobfuscateMethodVisitor extends MethodVisitor {
	
	private Main main;

	public DeobfuscateMethodVisitor(MethodVisitor base, Main main) {
		super(Opcodes.ASM4, base);
		this.main = main;
	}
	
	public AnnotationVisitor 	visitAnnotation(String desc, boolean visible) {
		return super.visitAnnotation(desc, visible);
	}
	public AnnotationVisitor 	visitAnnotationDefault() {
		return super.visitAnnotationDefault();
	}
	public void 	visitAttribute(Attribute attr) {
		super.visitAttribute(attr);
	}
	public void 	visitCode() {
		super.visitCode();
	}
	public void 	visitEnd() {
		super.visitEnd();
	}
	public void 	visitFieldInsn(int opcode, String owner, String name, String desc) {
		String deobfOwner = main.srg.getClassName(owner);
		String seargeName = main.srg.getFieldName(owner, name);
		String deobfName = main.fields.get(seargeName);
		desc = main.deobfTypeDescriptor(desc);
		super.visitFieldInsn(opcode, deobfOwner, deobfName, desc);
	}
	public void 	visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
		for(int k = 0; k < local.length; k++)
			if(local[k] instanceof String)
				local[k] = main.srg.getClassName((String)local[k]);
		for(int k = 0; k < stack.length; k++)
			if(stack[k] instanceof String)
				stack[k] = main.srg.getClassName((String)stack[k]);
		super.visitFrame(type, nLocal, local, nStack, stack);
	}
	public void 	visitIincInsn(int var, int increment) {
		super.visitIincInsn(var, increment);
	}
	public void 	visitInsn(int opcode) {
		super.visitInsn(opcode);
	}
	public void 	visitIntInsn(int opcode, int operand) {
		super.visitIntInsn(opcode, operand);
	}
	public void 	visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
		throw new RuntimeException("invokedynamic not implemented");
		//super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}
	public void 	visitJumpInsn(int opcode, Label label) {
		super.visitJumpInsn(opcode, label);
	}
	public void 	visitLabel(Label label) {
		super.visitLabel(label);
	}
	public void 	visitLdcInsn(Object cst) {
		super.visitLdcInsn(cst);
	}
	public void 	visitLineNumber(int line, Label start) {
		super.visitLineNumber(line, start);
	}
	public void 	visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		desc = main.deobfTypeDescriptor(desc);
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}
	public void 	visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		super.visitLookupSwitchInsn(dflt, keys, labels);
	}
	public void 	visitMaxs(int maxStack, int maxLocals) {
		super.visitMaxs(maxStack, maxLocals);
	}
	public void 	visitMethodInsn(int opcode, String owner, String name, String desc) {
		SrgFile.MethodInfo smi = main.srg.getMethod(owner, name, desc);
		String seargeName = smi.name;
		//if(!smi.desc.equals(main.deobfMethodDescriptor(desc)))
		//	throw new RuntimeException("Method descriptor mismatch, SRG: "+smi.desc+", Main: "+main.deobfMethodDescriptor(desc));
		
		//desc = smi.desc;
		desc = main.deobfMethodDescriptor(desc);
		
		String deobfName = main.methods.get(seargeName);
		owner = main.srg.getClassName(owner);
		super.visitMethodInsn(opcode, owner, name, desc);
	}
	public void 	visitMultiANewArrayInsn(String desc, int dims) {
		desc = main.deobfTypeDescriptor(desc);
		super.visitMultiANewArrayInsn(desc, dims);
	}
	public AnnotationVisitor 	visitParameterAnnotation(int parameter, String desc, boolean visible) {
		return super.visitParameterAnnotation(parameter, desc, visible);
	}
	public void 	visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
		super.visitTableSwitchInsn(min, max, dflt, labels);
	}
	public void 	visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		super.visitTryCatchBlock(start, end, handler, type);
	}
	public void 	visitTypeInsn(int opcode, String type) {
		super.visitTypeInsn(opcode, main.srg.getClassName(type));
	}
	public void 	visitVarInsn(int opcode, int var) {
		super.visitVarInsn(opcode, var);
	}
}
