package immibis.bon;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mapping {
	Map<String, String> classes = new HashMap<String, String>();
	Map<String, String> methods = new HashMap<String, String>();
	Map<String, String> fields = new HashMap<String, String>();
	Map<String, List<String>> exceptions = new HashMap<String, List<String>>();
	Map<String, String> classPrefixes = new HashMap<String, String>();
	String defaultPackage = "";

	public final NameSet fromNS, toNS;

	public Mapping(NameSet fromNS, NameSet toNS) {
		this.fromNS = fromNS;
		this.toNS = toNS;
	}

	public void addPrefix(String old, String new_) {
		classPrefixes.put(old, new_);
	}

	public String getClass(String in) {
		if (in == null) return null;
		if (in.startsWith("[L") && in.endsWith(";")) return "[L" + getClass(in.substring(2, in.length() - 1)) + ";";
		if (in.startsWith("[")) return "[" + getClass(in.substring(1));

		if (in.equals("B") || in.equals("C") || in.equals("D") || in.equals("F") || in.equals("I") || in.equals("J") || in.equals("S") || in.equals("Z")) return in;

		String ret = classes.get(in);
		if (ret != null) return ret;
		for (Map.Entry<String, String> e : classPrefixes.entrySet()) if (in.startsWith(e.getKey())) return e.getValue() + in.substring(e.getKey().length());
		if (!in.contains("/")) return defaultPackage + in;
		return in;
	}

	public List<String> getExceptions(String clazz, String method, String desc) {
		List<String> ret = exceptions.get(clazz + "/" + method + desc);
		return ret == null ? Collections.<String> emptyList() : ret;
	}

	public String getField(String clazz, String name) {
		String ret = fields.get(clazz + "/" + name);
		return ret == null ? name : ret;
	}

	public String getMethod(String clazz, String name, String desc) {
		String ret = methods.get(clazz + "/" + name + desc);
		return ret == null ? name : ret;
	}

	public String mapMethodDescriptor(String desc) {
		// some basic sanity checks, doesn't ensure it's completely valid though
		if (desc.length() == 0 || desc.charAt(0) != '(' || desc.indexOf(")") < 1) throw new IllegalArgumentException("Not a valid method descriptor: " + desc);
		int pos = 0;
		String out = "";
		while (pos < desc.length()) {
			switch (desc.charAt(pos)) {
			case 'V':
			case 'Z':
			case 'B':
			case 'C':
			case 'S':
			case 'I':
			case 'J':
			case 'F':
			case 'D':
			case '[':
			case '(':
			case ')':
				out += desc.charAt(pos);
				pos++;
				break;
			case 'L': {
				int end = desc.indexOf(';', pos);
				String obf = desc.substring(pos + 1, end);
				pos = end + 1;
				out += "L" + getClass(obf) + ";";
			}
			break;
			default:
				throw new RuntimeException("Unknown method descriptor character: " + desc.charAt(pos) + " (in " + desc + ")");
			}
		}
		return out;
	}

	public String mapTypeDescriptor(String in) {
		if (in.startsWith("[")) return "[" + mapTypeDescriptor(in.substring(1));
		if (in.startsWith("L") && in.endsWith(";")) return "L" + getClass(in.substring(1, in.length() - 1)) + ";";
		return in;
	}

	public void setClass(String in, String out) {
		classes.put(in, out);
	}

	// p must include trailing slash
	public void setDefaultPackage(String p) {
		defaultPackage = p;
	}

	public void setExceptions(String clazz, String method, String desc,
			List<String> exc) {
		exceptions.put(clazz + "/" + method + desc, exc);
	}

	public void setField(String clazz, String name, String out) {
		fields.put(clazz + "/" + name, out);
	}

	public void setMethod(String clazz, String name, String desc, String out) {
		methods.put(clazz + "/" + name + desc, out);
	}
}