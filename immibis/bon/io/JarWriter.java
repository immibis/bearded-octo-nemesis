package immibis.bon.io;

import immibis.bon.ClassCollection;
import immibis.bon.IProgressListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.objectweb.asm.tree.ClassNode;

public class JarWriter {
	
	private static void addDirectories(String filePath, Set<String> dirs) {
		int i = filePath.lastIndexOf('/');
		if(i >= 0) {
			String dirPath = filePath.substring(0, i);
			if(dirs.add(dirPath))
				addDirectories(dirPath, dirs); // ensure all parent directories are added
		}
	}

	public static void write(File file, ClassCollection cc, IProgressListener progress) throws IOException {
		if(progress != null)
			progress.setMax(cc.getAllClasses().size() + cc.getExtraFiles().size());
		
		int files = 0;
		
		Set<String> dirs = new HashSet<>();
		
		try (JarOutputStream j_out = new JarOutputStream(new FileOutputStream(file))) {
			for(ClassNode cn : cc.getAllClasses()) {
				if(progress != null) 
					progress.set(files++);
				
				addDirectories(cn.name, dirs);
				
				j_out.putNextEntry(new JarEntry(cn.name + ".class"));
				j_out.write(IOUtils.writeClass(cn));
				j_out.closeEntry();
			}
			
			for(Map.Entry<String, byte[]> e : cc.getExtraFiles().entrySet()) {
				if(progress != null) 
					progress.set(files++);
				
				addDirectories(e.getKey(), dirs);
				
				j_out.putNextEntry(new JarEntry(e.getKey()));
				j_out.write(e.getValue());
				j_out.closeEntry();
			}
			
			for(String dirPath : dirs) {
				j_out.putNextEntry(new JarEntry(dirPath + "/"));
				j_out.closeEntry();
			}
		}
	}

}
