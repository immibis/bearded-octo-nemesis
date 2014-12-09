package immibis.bon.io;

import immibis.bon.ClassCollection;
import immibis.bon.ClassFormatException;
import immibis.bon.ClassReferenceData;
import immibis.bon.IProgressListener;
import immibis.bon.NameSet;
import immibis.bon.ReferenceDataCollection;
import immibis.bon.org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarLoader {
	
	private static final boolean VERIFY_SIGNATURES = false;
	
	public static ClassCollection loadClassesFromJar(NameSet nameSet, File jarFile, IProgressListener progress) throws IOException, ClassFormatException {
		Collection<ClassNode> classes = new ArrayList<>();
		Map<String, byte[]> extraFiles = new HashMap<>();
		
		try (JarInputStream j_in = new JarInputStream(new FileInputStream(jarFile), VERIFY_SIGNATURES)) {
			JarEntry entry;
			
			while((entry = j_in.getNextJarEntry()) != null) {
				
				if(entry.isDirectory())
					continue;
				
				String name = entry.getName();
				
				if(name.endsWith(".class")) {
					try {
						ClassNode cn = IOUtils.readClass(IOUtils.readStreamFully(j_in));
						
						if(!name.equals(cn.name + ".class"))
							throw new ClassFormatException("Class '"+cn.name+"' has wrong path in jar file: '"+name+"'");
						
						classes.add(cn);
						
					} catch(ClassFormatException e) {
						throw new ClassFormatException("Unable to parse class file: "+name+" in "+jarFile.getName(), e);
					}
				} else {
					
					extraFiles.put(name, IOUtils.readStreamFully(j_in));
				}
			}
		}
		
		ClassCollection cc = new ClassCollection(nameSet, classes);
		cc.getExtraFiles().putAll(extraFiles);
		return cc;
	}

	public static ReferenceDataCollection loadRefDataFromJar(NameSet ns, File jarFile, IProgressListener progress) throws IOException, ClassFormatException {
		ReferenceDataCollection rv = new ReferenceDataCollection(ns);
		
		try (JarInputStream j_in = new JarInputStream(new FileInputStream(jarFile), VERIFY_SIGNATURES)) {
			JarEntry entry;
			
			while((entry = j_in.getNextJarEntry()) != null) {
				
				if(entry.isDirectory())
					continue;
				
				String name = entry.getName();
				
				if(name.endsWith(".class")) {
					try {
						ClassNode cn = IOUtils.readClass(IOUtils.readStreamFully(j_in));
						
						if(!name.equals(cn.name + ".class"))
							throw new ClassFormatException("Class '"+cn.name+"' has wrong path in jar file: '"+name+"'");
						
						rv.getAllClasses().add(ClassReferenceData.fromClassNode(cn));
						
					} catch(ClassFormatException e) {
						throw new ClassFormatException("Unable to parse class file: "+name+" in "+jarFile.getName(), e);
					}
				}
			}
		}
		
		return rv;
	}
}
