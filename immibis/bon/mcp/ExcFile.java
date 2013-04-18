package immibis.bon.mcp;

import java.io.*;
import java.util.*;

public class ExcFile {
	public Map<String, String[]> exceptions = new HashMap<String, String[]>();
	
	private static String[] EMPTY_STRING_ARRAY = new String[0];
	
	// returns internal names, can return null
	// input uses SRG names
	public String[] getExceptionClasses(String clazz, String func, String desc) {
		String[] r = exceptions.get(clazz + "/" + func + desc);
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
				in.useDelimiter("\\(");
				String func = in.next().substring(1);
				in.useDelimiter("=");
				String desc = in.next();
				in.useDelimiter("\\|");
				String excs = in.next().substring(1);
				in.nextLine(); // skip rest of line
				exceptions.put(clazz + "/" + func + desc, excs.split(","));
			}
		} finally {
			in.close();
		}
	}
}
