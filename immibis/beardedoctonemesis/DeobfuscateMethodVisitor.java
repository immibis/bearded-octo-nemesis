package immibis.beardedoctonemesis;

import org.objectweb.asm.*;

public class DeobfuscateMethodVisitor extends MethodVisitor {
	
	private Main main;
	//private String inClassName;

	public DeobfuscateMethodVisitor(MethodVisitor base, Main main, String inClassName) {
		super(Opcodes.ASM4, base);
		this.main = main;
		//this.inClassName = inClassName;
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
		String outOwner = main.map.getClass(owner);
		String outName = main.resolveField(owner, name, desc);
		String outDesc = main.deobfTypeDescriptor(desc);
		super.visitFieldInsn(opcode, outOwner, outName, outDesc);
	}
	public void 	visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
		for(int k = 0; k < local.length; k++)
			if(local[k] instanceof String)
				local[k] = main.map.getClass((String)local[k]);
		for(int k = 0; k < stack.length; k++)
			if(stack[k] instanceof String)
				stack[k] = main.map.getClass((String)stack[k]);
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
		if(cst instanceof Type) {
			cst = Type.getType(main.deobfTypeDescriptor(((Type) cst).getDescriptor()));
		}
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
		//if(main.map.getClass(owner).contains("EnumMobType"))
			//throw new RuntimeException("boom '"+owner+"' -> '"+main.map.getClass(owner)+"'");
		super.visitMethodInsn(opcode,
				main.map.getClass(owner),
				main.lookupInheritedMethod(owner, name, desc),
				main.deobfMethodDescriptor(desc));
	}
	public void 	visitMultiANewArrayInsn(String desc, int dims) {
		super.visitMultiANewArrayInsn(main.deobfTypeDescriptor(desc), dims);
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
		String deobf = type.charAt(0) == '[' ? main.deobfTypeDescriptor(type) : main.map.getClass(type);
		super.visitTypeInsn(opcode, deobf);
	}
	public void 	visitVarInsn(int opcode, int var) {
		super.visitVarInsn(opcode, var);
	}
}
