package immibis.bon.io;

import immibis.bon.ClassCollection;
import immibis.bon.IProgressListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.objectweb.asm.tree.ClassNode;

public class JarWriter {

	public static void write(File file, ClassCollection cc, IProgressListener progress) throws IOException {
		if(progress != null)
			progress.setMax(cc.getAllClasses().size() + cc.getExtraFiles().size());
		
		int files = 0;
		
		try (JarOutputStream j_out = new JarOutputStream(new FileOutputStream(file))) {
			for(ClassNode cn : cc.getAllClasses()) {
				if(progress != null) 
					progress.set(files++);
				
				j_out.putNextEntry(new JarEntry(cn.name + ".class"));
				j_out.write(IOUtils.writeClass(cn));
				j_out.closeEntry();
			}
			
			for(Map.Entry<String, byte[]> e : cc.getExtraFiles().entrySet()) {
				if(progress != null) 
					progress.set(files++);
				
				j_out.putNextEntry(new JarEntry(e.getKey()));
				j_out.write(e.getValue());
				j_out.closeEntry();
			}
		}
	}

}
