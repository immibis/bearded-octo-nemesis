package immibis.bon.mcp;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
	
	private ExcFile() {}
	
	public static ExcFile read(InputStream in) throws IOException {
		return read(new InputStreamReader(in, StandardCharsets.UTF_8));
	}
	
	/** Does not close <var>r</var>. */
	public static ExcFile read(Reader r) throws IOException {
		//example line:
		//net/minecraft/src/NetClientHandler.<init>(Lnet/minecraft/client/Minecraft;Ljava/lang/String;I)V=java/net/UnknownHostException,java/io/IOException|p_i42_1_,p_i42_2_,p_i42_3_
		
		ExcFile rv = new ExcFile();
		
		@SuppressWarnings("resource")
		Scanner in = new Scanner(r);
		while(in.hasNextLine()) {
			String line = in.nextLine();
			
			if(line.startsWith("#"))
				continue;
			
			if(line.contains("-Access="))
				continue;
			
			if(line.contains("=CL_"))
				continue;
			
			int i = line.indexOf('.');
			if(i < 0)
				continue;
			String clazz = line.substring(0, i);
			line = line.substring(i+1);
			
			i = line.indexOf('(');
			String func = line.substring(0, i);
			line = line.substring(i+1);
			
			i = line.indexOf('=');
			String desc = line.substring(0, i);
			line = line.substring(i+1);
			
			i = line.indexOf('|');
			String excs = line.substring(0, i);
			line = line.substring(i+1);
			
			if(excs.contains("CL_"))
				throw new RuntimeException(excs);
			
			rv.exceptions.put(clazz + "/" + func + desc, excs.split(","));
		}
		return rv;
	}
	
	@Deprecated
	public ExcFile(File f) throws IOException {
		try (FileReader fr = new FileReader(f)) {
			exceptions = read(fr).exceptions;
		}
	}
}
