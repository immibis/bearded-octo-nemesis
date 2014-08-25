package immibis.bon.mcp;

import immibis.bon.Mapping;
import immibis.bon.NameSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SrgFile {
	
	public Map<String, String> classes = new HashMap<String, String>(); // name -> name
	public Map<String, String> fields = new HashMap<String, String>(); // owner/name -> name
	public Map<String, String> methods = new HashMap<String, String>(); // owner/namedesc -> name
	
	public static String getLastComponent(String s) {
		String[] parts = s.split("/");
		return parts[parts.length - 1];
	}
	
	private SrgFile() {}
	
	/** Does not close <var>r</var>. */
	public static SrgFile read(Reader r, boolean reverse) throws IOException {
		@SuppressWarnings("resource")
		Scanner in = new Scanner(r);
		SrgFile rv = new SrgFile();
		while(in.hasNextLine()) {
			if(in.hasNext("CL:")) {
				in.next();
				String obf = in.next();
				String deobf = in.next();
				if(reverse)
					rv.classes.put(deobf, obf);
				else
					rv.classes.put(obf, deobf);
			} else if(in.hasNext("FD:")) {
				in.next();
				String obf = in.next();
				String deobf = in.next();
				if(reverse)
					rv.fields.put(deobf, getLastComponent(obf));
				else
					rv.fields.put(obf, getLastComponent(deobf));
			} else if(in.hasNext("MD:")) {
				in.next();
				String obf = in.next();
				String obfdesc = in.next();
				String deobf = in.next();
				String deobfdesc = in.next();
				if(reverse)
					rv.methods.put(deobf + deobfdesc, getLastComponent(obf));
				else
					rv.methods.put(obf + obfdesc, getLastComponent(deobf));
			} else {
				in.nextLine();
			}
		}
		return rv;
	}

	@Deprecated
	public SrgFile(File f, boolean reverse) throws IOException {
		try (FileReader fr = new FileReader(f)) {
			SrgFile sf = read(new BufferedReader(fr), reverse);
			classes = sf.classes;
			fields = sf.fields;
			methods = sf.methods;
		}
	}
	
	public Mapping toMapping(NameSet fromNS, NameSet toNS) {
		Mapping m = new Mapping(fromNS, toNS);
		
		for(Map.Entry<String, String> entry : classes.entrySet()) {
			m.setClass(entry.getKey(), entry.getValue());
		}
		
		for(Map.Entry<String, String> entry : fields.entrySet()) {
			int i = entry.getKey().lastIndexOf('/');
			m.setField(entry.getKey().substring(0, i), entry.getKey().substring(i+1), entry.getValue());
		}
		
		for(Map.Entry<String, String> entry : methods.entrySet()) {
			int i = entry.getKey().lastIndexOf('(');
			String desc = entry.getKey().substring(i);
			String classandname = entry.getKey().substring(0,i);
			i = classandname.lastIndexOf('/');
			m.setMethod(classandname.substring(0,i), classandname.substring(i+1), desc, entry.getValue());
		}
		
		return m;
	}
}
