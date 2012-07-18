package immibis.beardedoctonemesis;

import java.util.*;

public class Mapping {
	public Map<String, String> classes = new HashMap<String, String>();
	public Map<String, String> methods = new HashMap<String, String>();
	public Map<String, String> fields = new HashMap<String, String>();
	public Map<String, List<String>> exceptions = new HashMap<String, List<String>>();
	public Map<String, String> classPrefixes = new HashMap<String, String>();
	public String defaultPackage = "";
	
	public void setClass(String in, String out) {
		classes.put(in, out);
	}
	
	public void setMethod(String clazz, String name, String desc, String out) {
		methods.put(clazz + "/" + name + desc, out);
	}
	
	public void setField(String clazz, String name, String out) {
		fields.put(clazz + "/" + name, out);
	}
	
	public void setExceptions(String clazz, String method, String desc, List<String> exc) {
		exceptions.put(clazz + "/" + method + desc, exc);
	}
	
	public String getClass(String in) {
		if(in == null)
			return null;
		if(in.startsWith("[L") && in.endsWith(";"))
			return "[L" + getClass(in.substring(2, in.length() - 1)) + ";";
		if(in.startsWith("["))
			return "[" + getClass(in.substring(1));
		
		if(in.equals("B") || in.equals("C") || in.equals("D") || in.equals("F") || in.equals("I") || in.equals("J") || in.equals("S") || in.equals("Z"))
			return in;
		
		String ret = classes.get(in);
		if(ret != null)
			return ret;
		for(Map.Entry<String, String> e : classPrefixes.entrySet())
			if(in.startsWith(e.getKey()))
				return e.getValue() + in.substring(e.getKey().length());
		if(!in.contains("/"))
			return defaultPackage + in;
		return in;
	}
	
	public String getMethod(String clazz, String name, String desc) {
		String ret = methods.get(clazz + "/" + name + desc);
		return ret == null ? name : ret;
	}
	
	public String getField(String clazz, String name) {
		String ret = fields.get(clazz + "/" + name);
		return ret == null ? name : ret;
	}
	
	public List<String> getExceptions(String clazz, String method, String desc) {
		List<String> ret = exceptions.get(clazz + "/" + method + desc);
		return ret == null ? (List<String>)Collections.EMPTY_LIST : ret;
	}
	
	public void addPrefix(String old, String new_) {
		classPrefixes.put(old, new_);
	}
	
	// p must include trailing slash
	public void setDefaultPackage(String p) {
		defaultPackage = p;
	}
}
