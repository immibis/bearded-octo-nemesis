package immibis.bon;

import immibis.bon.ClassReferenceData.FieldData;
import immibis.bon.ClassReferenceData.MethodData;
import immibis.bon.org.objectweb.asm.Opcodes;
import immibis.bon.org.objectweb.asm.Type;
import immibis.bon.org.objectweb.asm.tree.AbstractInsnNode;
import immibis.bon.org.objectweb.asm.tree.AnnotationNode;
import immibis.bon.org.objectweb.asm.tree.ClassNode;
import immibis.bon.org.objectweb.asm.tree.FieldInsnNode;
import immibis.bon.org.objectweb.asm.tree.FieldNode;
import immibis.bon.org.objectweb.asm.tree.FrameNode;
import immibis.bon.org.objectweb.asm.tree.InnerClassNode;
import immibis.bon.org.objectweb.asm.tree.LdcInsnNode;
import immibis.bon.org.objectweb.asm.tree.LocalVariableNode;
import immibis.bon.org.objectweb.asm.tree.MethodInsnNode;
import immibis.bon.org.objectweb.asm.tree.MethodNode;
import immibis.bon.org.objectweb.asm.tree.MultiANewArrayInsnNode;
import immibis.bon.org.objectweb.asm.tree.TryCatchBlockNode;
import immibis.bon.org.objectweb.asm.tree.TypeInsnNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Remapper {
	
	// returns actual owner of field
	// or null if the field could not be resolved
	private static String resolveField(Map<String, ClassReferenceData> refClasses, String owner, String name, String desc, Mapping m) {
		
		ClassReferenceData cn = refClasses.get(owner);
		if(cn == null) {
			return null;
		}
		
		// http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-5.html#jvms-5.4.3.2
		
		for(FieldData fn : cn.fields)
			if(fn.name.equals(name) && fn.desc.equals(desc)) {
				return owner;
			}
		
		for(String i : cn.interfaces) {
			String result = resolveField(refClasses, i, name, desc, m);
			if(result != null) {
				return result;
			}
		}
		
		return resolveField(refClasses, cn.superName, name, desc, m);
		
	}
	
	// returns [realOwner, realDesc]
	// or null if the method could not be resolved
	private static String[] resolveMethod(Map<String, ClassReferenceData> refClasses, String owner, String name, String desc, Mapping m) {
		
		ClassReferenceData cn = refClasses.get(owner);
		if(cn == null)
			return null;
		
		if((cn.access & Opcodes.ACC_INTERFACE) != 0) {
			
			// interface method resolution; http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-5.html#jvms-5.4.3.4
			
			for(MethodData mn : cn.methods) {
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
				
				for(MethodData mn : cn.methods) {
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

	public static ClassCollection remap(ClassCollection cc, Mapping m, Collection<ReferenceDataCollection> refs, IProgressListener progress) {
		
		if(!cc.getNameSet().equals(m.fromNS))
			throw new IllegalArgumentException("Input classes use nameset "+cc.getNameSet()+", but mapping is from "+m.fromNS+"; cannot apply mapping");
		
		for(ReferenceDataCollection ref : refs)
			if(!ref.getNameSet().equals(m.fromNS))
				throw new IllegalArgumentException("Reference ClassCollection uses nameset "+ref.getNameSet()+" but input uses "+m.fromNS);
		
		HashMap<String, ClassReferenceData> refClasses = new HashMap<>();
		
		for(ReferenceDataCollection refcc : refs)
			for(ClassReferenceData cn : refcc.getAllClasses())
				refClasses.put(cn.name, cn);
		for(ClassNode cn : cc.getAllClasses())
			refClasses.put(cn.name, ClassReferenceData.fromClassNode(cn));
		
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
						
						} else if(ain instanceof MultiANewArrayInsnNode) {
							MultiANewArrayInsnNode min = (MultiANewArrayInsnNode)ain;
							
							min.desc = m.getClass(min.desc);
						}
					}
				}
				
				processAnnotationList(m, mn.visibleAnnotations);
				processAnnotationList(m, mn.visibleParameterAnnotations);
				processAnnotationList(m, mn.invisibleAnnotations);
				processAnnotationList(m, mn.invisibleParameterAnnotations);
				
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
				
				// TODO: support signatures (for generics, even though Minecraft doesn't use them after obfuscation)
			}
			
			for(FieldNode fn : cn.fields) {
				fn.name = m.getField(cn.name, fn.name);
				fn.desc = m.mapTypeDescriptor(fn.desc);
				
				processAnnotationList(m, fn.invisibleAnnotations);
				processAnnotationList(m, fn.visibleAnnotations);
				
				// TODO: support signatures (for generics, even though Minecraft doesn't use them after obfuscation)
			}
			
			cn.name = m.getClass(cn.name);
			cn.superName = m.getClass(cn.superName);
			
			for(int k = 0; k < cn.interfaces.size(); k++)
				cn.interfaces.set(k, m.getClass(cn.interfaces.get(k)));
			
			processAnnotationList(m, cn.invisibleAnnotations);
			processAnnotationList(m, cn.visibleAnnotations);
			
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
	
	private static void processAnnotationList(Mapping m, List<AnnotationNode>[] array) {
		if(array != null)
			for(List<AnnotationNode> list : array)
				processAnnotationList(m, list);
	}

	private static void processAnnotationList(Mapping m, List<AnnotationNode> list) {
		if(list != null)
			for(AnnotationNode an : list)
				processAnnotation(m, an);
	}
	
	private static void processAnnotation(Mapping m, AnnotationNode an) {
		an.desc = m.getClass(an.desc);
		if(an.values != null)
			for(int k = 1; k < an.values.size(); k += 2)
				an.values.set(k, processAnnotationValue(m, an.values.get(k)));
	}

	private static Object processAnnotationValue(Mapping m, Object value) {
		if(value instanceof Type)
			return Type.getType(m.getClass(((Type)value).getDescriptor()));
		
		if(value instanceof String[]) {
			// enum value; need to remap both the enum, and the value
			String[] array = (String[])value;
			String desc = array[0], enumvalue = array[1];
			if(!desc.startsWith("L") || !desc.endsWith(";"))
				throw new AssertionError("Not a class type descriptor: "+desc);
			return new String[] {m.getClass(desc), m.getField(desc.substring(1, desc.length() - 1), enumvalue)};
		}
		
		if(value instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>)value;
			for(int k = 0; k < list.size(); k++)
				list.set(k, processAnnotationValue(m, list.get(k)));
			return value;
		}
		
		if(value instanceof AnnotationNode) {
			processAnnotation(m, (AnnotationNode)value);
			return value;
		}
		
		return value;
	}

	/*public static ClassCollection remap(ClassCollection classes, NameSet toNS, Collection<ClassCollection> refs, IProgressListener progress) throws MappingUnavailableException, IOException {
		return remap(classes, MappingFactory.getMapping(classes.getNameSet(), toNS, null), refs, progress);
	}*/

}
