package immibis.bon.mcp;

import immibis.bon.Mapping;
import immibis.bon.NameSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

	public SrgFile(File f, boolean reverse) throws IOException {
		Scanner in = new Scanner(new BufferedReader(new FileReader(f)));
		try {
			while(in.hasNextLine()) {
				if(in.hasNext("CL:")) {
					in.next();
					String obf = in.next();
					String deobf = in.next();
					if(reverse)
						classes.put(deobf, obf);
					else
						classes.put(obf, deobf);
				} else if(in.hasNext("FD:")) {
					in.next();
					String obf = in.next();
					String deobf = in.next();
					if(reverse)
						fields.put(deobf, getLastComponent(obf));
					else
						fields.put(obf, getLastComponent(deobf));
				} else if(in.hasNext("MD:")) {
					in.next();
					String obf = in.next();
					String obfdesc = in.next();
					String deobf = in.next();
					String deobfdesc = in.next();
					if(reverse)
						methods.put(deobf + deobfdesc, getLastComponent(obf));
					else
						methods.put(obf + obfdesc, getLastComponent(deobf));
				} else {
					in.nextLine();
				}
			}
		} finally {
			in.close();
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
