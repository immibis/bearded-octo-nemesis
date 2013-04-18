package immibis.bon;

import immibis.bon.io.MappingFactory;
import immibis.bon.io.MappingFactory.MappingUnavailableException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class Remapper {
	
	// returns actual owner of field
	// or null if the field could not be resolved
	private static String resolveField(Map<String, ClassNode> refClasses, String owner, String name, String desc, Mapping m) {
		
		ClassNode cn = refClasses.get(owner);
		if(cn == null)
			return null;
		
		// http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-5.html#jvms-5.4.3.2
		
		for(FieldNode fn : cn.fields)
			if(fn.name.equals(name) && fn.desc.equals(desc) && !m.getField(owner, name).equals(name))
				return owner;
		
		for(String i : cn.interfaces) {
			String result = resolveField(refClasses, i, name, desc, m);
			if(result != null)
				return result;
		}
		
		return resolveField(refClasses, cn.superName, name, desc, m);
		
	}
	
	// returns [realOwner, realDesc]
	// or null if the method could not be resolved
	private static String[] resolveMethod(Map<String, ClassNode> refClasses, String owner, String name, String desc, Mapping m) {
		
		ClassNode cn = refClasses.get(owner);
		if(cn == null)
			return null;
		
		if((cn.access & Opcodes.ACC_INTERFACE) != 0) {
			
			// interface method resolution; http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-5.html#jvms-5.4.3.4
			
			for(MethodNode mn : cn.methods) {
				if(mn.name.equals(name) && mn.desc.equals(desc) && !m.getMethod(owner, name, desc).equals(name))
					return new String[] {owner, desc};
			}
			
			for(String i : cn.interfaces) {
				String[] result = resolveMethod(refClasses, i, name, desc, m);
				if(result != null)
					return result;
			}
			
			return null;
			
		} else {
			
			// normal method resolution; http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-5.html#jvms-5.4.3.3
			
			String originalOwner = owner;
			
			while(true) {
				
				cn = refClasses.get(owner);
				if(cn == null)
					break;
				
				for(MethodNode mn : cn.methods) {
					if(mn.name.equals(name) && mn.desc.equals(desc) && !m.getMethod(owner, name, desc).equals(name))
						return new String[] {owner, desc};
				}
				
				owner = cn.superName;
			}
			
			owner = originalOwner;
			
			while(true) {
				cn = refClasses.get(owner);
				if(cn == null)
					break;
				
				for(String i : cn.interfaces) {
					String[] result = resolveMethod(refClasses, i, name, desc, m);
					if(result != null)
						return result;
				}
				
				owner = cn.superName;
			}
			
			return null;
		}
	}

	public static ClassCollection remap(ClassCollection cc, Mapping m, Collection<ClassCollection> refs, IProgressListener progress) {
		
		if(!cc.getNameSet().equals(m.fromNS))
			throw new IllegalArgumentException("Input classes use nameset "+cc.getNameSet()+", but mapping is from "+m.fromNS+"; cannot apply mapping");
		
		for(ClassCollection ref : refs)
			if(!ref.getNameSet().equals(m.fromNS))
				throw new IllegalArgumentException("Reference ClassCollection uses nameset "+ref.getNameSet()+" but input uses "+m.fromNS);
		
		HashMap<String, ClassNode> refClasses = new HashMap<>();
		
		for(ClassCollection refcc : refs)
			for(ClassNode cn : refcc.getAllClasses())
				refClasses.put(cn.name, cn);
		for(ClassNode cn : cc.getAllClasses())
			refClasses.put(cn.name, cn);
		
		cc = cc.cloneWithNameSet(m.toNS);
		
		int classesProcessed = 0;
		
		if(progress != null)
			progress.setMax(cc.getAllClasses().size());
		
		for(ClassNode cn : cc.getAllClasses()) {
			
			if(progress != null)
				progress.set(classesProcessed++);
			
			for(MethodNode mn : cn.methods) {
				
				String[] resolvedMN = resolveMethod(refClasses, cn.name, mn.name, mn.desc, m);
				
				if(resolvedMN != null) {
					mn.name = m.getMethod(resolvedMN[0], mn.name, resolvedMN[1]);
					mn.desc = m.mapMethodDescriptor(resolvedMN[1]);
					
				} else {
					mn.name = m.getMethod(cn.name, mn.name, mn.desc);
					mn.desc = m.mapMethodDescriptor(mn.desc);
				}
				
				if(mn.instructions != null) {
					for(AbstractInsnNode ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext()) {
						
						if(ain instanceof FieldInsnNode) {
							FieldInsnNode fin = (FieldInsnNode)ain;
							
							String realOwner = resolveField(refClasses, fin.owner, fin.name, fin.desc, m);
							
							if(realOwner == null)
								realOwner = fin.owner;
							
							fin.name = m.getField(realOwner, fin.name);
							fin.desc = m.mapTypeDescriptor(fin.desc);
							fin.owner = m.getClass(realOwner);
							
						} else if(ain instanceof FrameNode) {
							FrameNode fn = (FrameNode)ain;
							
							if(fn.local != null)
								for(int k = 0; k < fn.local.size(); k++)
									if(fn.local.get(k) instanceof String)
										fn.local.set(k, m.getClass((String)fn.local.get(k)));
							
							if(fn.stack != null)
								for(int k = 0; k < fn.stack.size(); k++)
									if(fn.stack.get(k) instanceof String)
										fn.stack.set(k, m.getClass((String)fn.stack.get(k)));
							
						} else if(ain instanceof MethodInsnNode) {
							MethodInsnNode min = (MethodInsnNode)ain;
							
							String[] realOwnerAndDesc = resolveMethod(refClasses, min.owner, min.name, min.desc, m);
							
							String realOwner = realOwnerAndDesc == null ? min.owner : realOwnerAndDesc[0];
							String realDesc = realOwnerAndDesc == null ? min.desc : realOwnerAndDesc[1];
							
							min.name = m.getMethod(realOwner, min.name, realDesc);
							min.owner = m.getClass(min.owner); // note: not realOwner which could be an interface
							min.desc = m.mapMethodDescriptor(realDesc);
							
						} else if(ain instanceof LdcInsnNode) {
							LdcInsnNode lin = (LdcInsnNode)ain;
							
							if(lin.cst instanceof Type) {
								lin.cst = Type.getType(m.mapTypeDescriptor(((Type)lin.cst).getDescriptor()));
							}
							
						} else if(ain instanceof TypeInsnNode) {
							TypeInsnNode tin = (TypeInsnNode)ain;
							
							tin.desc = m.getClass(tin.desc);
						}
					}
				}
				
				for(TryCatchBlockNode tcb : mn.tryCatchBlocks) {
					if(tcb.type != null)
						tcb.type = m.getClass(tcb.type);
				}
				
				{
					Set<String> exceptions = new HashSet<>(mn.exceptions);
					exceptions.addAll(m.getExceptions(cn.name, mn.name, mn.desc));
					mn.exceptions.clear();
					for(String s : exceptions)
						mn.exceptions.add(m.getClass(s));
				}
				
				if(mn.localVariables != null)
					for(LocalVariableNode lvn : mn.localVariables)
						lvn.desc = m.mapTypeDescriptor(lvn.desc);
				
				// TODO: support annotations (even though Minecraft doesn't use them)
				// TODO: support signatures (for generics, even though Minecraft doesn't use them after obfuscation)
			}
			
			for(FieldNode fn : cn.fields) {
				fn.name = m.getField(cn.name, fn.name);
				fn.desc = m.mapTypeDescriptor(fn.desc);
				
				// TODO: support annotations (even though Minecraft doesn't use them)
				// TODO: support signatures (for generics, even though Minecraft doesn't use them after obfuscation)
			}
			
			cn.name = m.getClass(cn.name);
			cn.superName = m.getClass(cn.superName);
			
			for(int k = 0; k < cn.interfaces.size(); k++)
				cn.interfaces.set(k, m.getClass(cn.interfaces.get(k)));
			
			// TODO: support annotations (even though Minecraft doesn't use them)
			// TODO: support signatures (for generics, even though Minecraft doesn't use them after obfuscation)
			
			for(InnerClassNode icn : cn.innerClasses) {
				icn.name = m.getClass(icn.name);
				if(icn.outerName != null)
					icn.outerName = m.getClass(icn.outerName);
			}
			
			if(cn.outerMethod != null) {
				String[] resolved = resolveMethod(refClasses, cn.outerClass, cn.outerMethod, cn.outerMethodDesc, m);
				if(resolved != null) {
					cn.outerMethod = m.getMethod(resolved[0], cn.outerMethod, resolved[1]);
					cn.outerMethodDesc = m.mapMethodDescriptor(resolved[1]);
				} else {
					cn.outerMethod = m.getMethod(cn.outerClass, cn.outerMethod, cn.outerMethodDesc);
					cn.outerMethodDesc = m.mapMethodDescriptor(cn.outerMethodDesc);
				}
			}
			if(cn.outerClass != null)
				cn.outerClass = m.getClass(cn.outerClass);
		}
		
		return cc;
		
	}

	public static ClassCollection remap(ClassCollection classes, NameSet toNS, Collection<ClassCollection> refs, IProgressListener progress) throws MappingUnavailableException, IOException {
		return remap(classes, MappingFactory.getMapping(classes.getNameSet(), toNS, null), refs, progress);
	}

}
