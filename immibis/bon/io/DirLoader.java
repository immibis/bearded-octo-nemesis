package immibis.bon.io;

import immibis.bon.ClassCollection;
import immibis.bon.ClassFormatException;
import immibis.bon.ClassReferenceData;
import immibis.bon.IProgressListener;
import immibis.bon.NameSet;
import immibis.bon.ReferenceDataCollection;
import immibis.bon.org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DirLoader {
	
	public static ClassCollection loadClassesFromDirectory(NameSet ns, File dir, IProgressListener progress) throws IOException, ClassFormatException {
		Collection<ClassNode> classes = new ArrayList<>();
		Map<String, byte[]> extraFiles = new HashMap<>();
		loadFromDir("", dir, classes, extraFiles);
		ClassCollection cc = new ClassCollection(ns, classes);
		cc.getExtraFiles().putAll(extraFiles);
		return cc;
	}
	
	private static void loadFromDir(String prefix, File dir, Collection<ClassNode> result, Map<String, byte[]> extraFiles) throws IOException, ClassFormatException {
		if(dir.isDirectory()) {
			
			if(!prefix.equals(""))
				prefix += "/";
			
			for(String fn : dir.list()) {
				loadFromDir(prefix + fn, new File(dir, fn), result, extraFiles);
			}
			
		} else if(prefix.endsWith(".class")) {
			try (FileInputStream in = new FileInputStream(dir)) {
				ClassNode cn = IOUtils.readClass(IOUtils.readStreamFully(in));
				
				if(!prefix.equals(cn.name + ".class"))
					throw new ClassFormatException("Class '"+cn.name+"' has wrong path in folder: '"+prefix+"'");
				
				result.add(cn);
			}
		} else {
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (FileInputStream in = new FileInputStream(dir)) {
				byte[] buf = new byte[4096];
				while(true) {
					int read = in.read(buf);
					if(read <= 0)
						break;
					baos.write(buf, 0, read);
				}
			}
			extraFiles.put(prefix, baos.toByteArray());
		}
	}

	public static ReferenceDataCollection loadRefDataFromDirectory(NameSet ns, File dir, IProgressListener progress) throws IOException, ClassFormatException {
		ReferenceDataCollection rv = new ReferenceDataCollection(ns);
		loadFromDir("", dir, rv.getAllClasses());
		return rv;
	}
	
	private static void loadFromDir(String prefix, File dir, Collection<ClassReferenceData> result) throws IOException, ClassFormatException {
		if(dir.isDirectory()) {
			
			if(!prefix.equals(""))
				prefix += "/";
			
			for(String fn : dir.list()) {
				loadFromDir(prefix + fn, new File(dir, fn), result);
			}
			
		} else if(prefix.endsWith(".class")) {
			try (FileInputStream in = new FileInputStream(dir)) {
				ClassNode cn = IOUtils.readClass(IOUtils.readStreamFully(in));
				
				if(!prefix.equals(cn.name + ".class"))
					throw new ClassFormatException("Class '"+cn.name+"' has wrong path in folder: '"+prefix+"'");
				
				result.add(ClassReferenceData.fromClassNode(cn));
			}
		}
	}
}
