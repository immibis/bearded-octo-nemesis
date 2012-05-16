package immibis.beardedoctonemesis;

import java.io.*;
import java.util.*;

public class SrgFile {
	private Map<String, String> classes = new HashMap<String, String>();
	private Map<String, String> fields = new HashMap<String, String>();
	private Map<String, MethodInfo> methods = new HashMap<String, MethodInfo>();
	
	public static class MethodInfo {
		public String name;
		public String desc;
	}
	
	private static String getLastComponent(String s) {
		String[] parts = s.split("/");
		return parts[parts.length - 1];
	}
	
	public String getClassName(String obf) {
		if(obf == null)
			return null;

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
		String r = fields.get(clazz + "/" + obf);
		return r == null ? obf : r;
	}
	public MethodInfo getMethod(String clazz, String obf, String desc) {
		MethodInfo r = methods.get(clazz + "/" + obf + desc);
		if(r == null) {
			r = new MethodInfo();
			r.name = obf;
			r.desc = desc;
		}
		return r;
	}
	public SrgFile(File f) throws IOException {
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
					MethodInfo mi = new MethodInfo();
					mi.desc = deobfdesc;
					mi.name = getLastComponent(deobf);
					methods.put(obf + obfdesc, mi);
				} else {
					in.nextLine();
				}
			}
		} finally {
			in.close();
		}
	}
}
