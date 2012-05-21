package immibis.beardedoctonemesis;

import java.io.*;
import java.util.*;

public class SrgFile {
	private Map<String, String> classes = new HashMap<String, String>();
	private Map<String, String> fields = new HashMap<String, String>();
	private Map<String, String> methods = new HashMap<String, String>();
	
	private static String getLastComponent(String s) {
		String[] parts = s.split("/");
		return parts[parts.length - 1];
	}
	
	public String getClassName(String obf) {
		if(obf == null)
			return null;
		
		if(obf.charAt(0) == '[')
			return main.deobfTypeDescriptor(obf);

		// Check the class mapping list
		String r = classes.get(obf);
		if(r != null)
			return r;

		// Fix certain packages in net/minecraft/src
		if(!obf.contains("/") || obf.startsWith("forge/"))
			return "net/minecraft/src/" + obf;
		
		return obf;
	}
	public String getFieldName(String clazz, String obf) {
		return fields.get(clazz + "/" + obf);
	}
	public String getMethod(String clazz, String obf, String desc) {
		return methods.get(clazz + "/" + obf + desc);
	}
	private Main main;
	public SrgFile(File f, Main m) throws IOException {
		this.main = m;
		Scanner in = new Scanner(f);
		try {
			while(in.hasNextLine()) {
				if(in.hasNext("CL:")) {
					in.next();
					String obf = in.next();
					String deobf = in.next();
					classes.put(obf, deobf);
				} else if(in.hasNext("FD:")) {
					in.next();
					String obf = in.next();
					String deobf = in.next();
					fields.put(obf, getLastComponent(deobf));
				} else if(in.hasNext("MD:")) {
					in.next();
					String obf = in.next();
					String obfdesc = in.next();
					String deobf = in.next();
					String deobfdesc = in.next();
					methods.put(obf + obfdesc, getLastComponent(deobf));
				} else {
					in.nextLine();
				}
			}
		} finally {
			in.close();
		}
	}
}
