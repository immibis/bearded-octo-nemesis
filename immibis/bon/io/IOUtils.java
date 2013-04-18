package immibis.bon.io;

import immibis.bon.ClassFormatException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class IOUtils {
	public static byte[] readStreamFully(InputStream stream) throws IOException {
		ByteArrayOutputStream temp = new ByteArrayOutputStream(Math.max(8192, stream.available()));
		byte[] buffer = new byte[8192];
		int read;
		
		while((read = stream.read(buffer)) >= 0)
			temp.write(buffer, 0, read);
		
		return temp.toByteArray();
	}
	
	public static ClassNode readClass(byte[] bytes) throws ClassFormatException {

		ClassNode cn = new ClassNode();
		try {
			new ClassReader(bytes).accept(cn, 0);
		} catch(RuntimeException e) {
			throw new ClassFormatException("Unable to load class");
		}
		
		return cn;
	}
	
	public static byte[] writeClass(ClassNode cn) {
		ClassWriter cw = new ClassWriter(0);
		cn.accept(cw);
		return cw.toByteArray();
	}
}
