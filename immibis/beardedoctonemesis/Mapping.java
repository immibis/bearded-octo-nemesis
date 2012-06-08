package immibis.beardedoctonemesis;

import java.util.*;

public class Mapping {
	public Map<String, String> classes = new HashMap<String, String>();
	public Map<String, String> methods = new HashMap<String, String>();
	public Map<String, String> fields = new HashMap<String, String>();
	public Map<String, List<String>> exceptions = new HashMap<String, List<String>>();
	
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
		String ret = classes.get(in);
		return ret == null ? in : ret;
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
}
