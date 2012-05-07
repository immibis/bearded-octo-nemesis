package immibis.beardedoctonemesis;

import java.io.*;
import java.util.*;

public class ExcFile {
	private Map<String, Map<String, String[]>> exceptions = new HashMap<String, Map<String, String[]>>();
	
	private static String[] EMPTY_STRING_ARRAY = new String[0];
	
	// returns internal names, can return null
	public String[] getExceptionClasses(String clazz, String func, String desc) {
		Map<String, String[]> map = exceptions.get(clazz);
		if(map == null)
			return EMPTY_STRING_ARRAY;
		String[] r = map.get(func);
		if(r == null)
			return EMPTY_STRING_ARRAY;
		return r;
	}
	
	public ExcFile(File f) throws IOException {
		//example line:
		//net/minecraft/src/NetClientHandler.<init>(Lnet/minecraft/client/Minecraft;Ljava/lang/String;I)V=java/net/UnknownHostException,java/io/IOException|p_i42_1_,p_i42_2_,p_i42_3_
		
		Scanner in = new Scanner(new FileReader(f));
		try {
			while(in.hasNextLine()) {
				if(in.hasNext("#")) {
					in.nextLine();
					continue;
				}
				in.useDelimiter("\\.");
				String clazz = in.next();
				Map<String, String[]> classmap = exceptions.get(clazz);
				if(classmap == null) {
					classmap = new HashMap<String, String[]>();
					exceptions.put(clazz, classmap);
				}
				in.useDelimiter("\\(");
				String func = in.next().substring(1);
				in.useDelimiter("=");
				String desc = in.next();
				in.useDelimiter("\\|");
				String excs = in.next().substring(1);
				in.nextLine(); // skip rest of line
				classmap.put(func + desc, excs.split(","));
			}
		} finally {
			in.close();
		}
	}
}
