package immibis.beardedoctonemesis;

import immibis.beardedoctonemesis.mcp.McpBukkitMapping;
import immibis.beardedoctonemesis.mcp.McpMapping;
import immibis.beardedoctonemesis.mcp.Side;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class Main {
	public static final String MCP_BASE = "C:/Users/Alex/mcp2/fdro-test-1.2.5/";
	
	public static final int CLIENT = 0;
	public static final int SERVER = 1;
	
	public File base, input, output;
	/*public ExcFile exc;
	public SrgFile srg;
	public boolean reob;
	public Map<String, String> fields, methods; // maps descriptors to final names*/
	public String[] xpathlist;
	
	public Collection<String> ignoredPrefixes = new HashSet<String>();
	
	public Map<String, Set<String>> supers = new HashMap<String, Set<String>>();
	
	public Mapping map;

	public IProgressListener progress;
	
	// returns an internal name (with /'s and $'s)
	public static String fileToClass(String fn) {
		return fn.replace(File.separator, "/").substring(0, fn.length() - 6);
	}
	
	// expects an internal name (with /'s and $'s)
	public static String classToFile(String cn) {
		return cn+".class";
	}
	
	private void getParents(File f) throws IOException {
		if(progress != null) progress.start(countZipEntries(f), "");
		
		ZipInputStream inZip = new ZipInputStream(new FileInputStream(f));
		int k = 0;
		while(true) {
			ZipEntry inEntry = inZip.getNextEntry();
			if(inEntry == null)
				break;
		
			if(progress != null) progress.set(k++);
			
			if(!inEntry.getName().endsWith(".class") || isClassIgnored(fileToClass(inEntry.getName())))
				continue;
			
			System.out.println(inEntry.getName());
			
			GetParentVisitor gpv = new GetParentVisitor();
			
			try {
				ClassReader cr = new ClassReader(inZip);
				cr.accept(gpv, 0);
				inZip.closeEntry();
			} catch(GetParentVisitor.FinishedException e) {
			}
			
			Set<String> inheritsFrom = new HashSet<String>();
			inheritsFrom.add(gpv.parent);
			for(String s : gpv.interfaces)
				inheritsFrom.add(s);
			supers.put(gpv.name, inheritsFrom);
		}
		inZip.close();
	}
	
	private int countZipEntries(File f) throws IOException {
		int n = 0;
		ZipInputStream inZip = new ZipInputStream(new FileInputStream(f));
		try {
			while(true) {
				ZipEntry inEntry = inZip.getNextEntry();
				if(inEntry == null)
					return n;
				n++;
			}
		} finally {
			inZip.close();
		}
	}

	@SuppressWarnings("unused")
	public void run() throws IOException {
		ZipInputStream inZip = null;
		ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(output));
		try {
			File parentCache = null;//new File("pcache.txt");
			if(parentCache == null || !parentCache.exists())
			{
				for(String xpath : xpathlist)
					getParents(new File(xpath));
				getParents(input);
				if(parentCache != null)
					saveParents(parentCache);
			}
			else
				loadParents(parentCache);
			
			inZip = new ZipInputStream(new FileInputStream(input));
			
			if(progress != null) progress.start(countZipEntries(input), "");
			
			int nProcessed = 0;
			while(true) {
				ZipEntry inEntry = inZip.getNextEntry();
				if(inEntry == null)
					break;
				
				if(progress != null) progress.set(nProcessed++);
				
				if(!inEntry.getName().endsWith(".class") || isClassIgnored(fileToClass(inEntry.getName()))) {
					//System.out.println("Copying "+inEntry.getName());
					ZipEntry outEntry = new ZipEntry(inEntry.getName());
					outZip.putNextEntry(outEntry);
					
					byte[] buf = new byte[1048576];
					int len = 0;
					do {
						len = inZip.read(buf);
						if(len > 0)
							outZip.write(buf, 0, len);
					} while(len > 0);
					inZip.closeEntry();
					
					outZip.closeEntry();
					continue;
				}
				
				String oldName = fileToClass(inEntry.getName());
				String newName = map.getClass(oldName);
				
				if(oldName.equals(newName))
					System.out.println(oldName);
				else
					System.out.println(oldName+" -> "+newName);
				
				try {
				
					ClassReader cr = new ClassReader(readClass(inZip));
					
					ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
					cr.accept(new DeobfuscateVisitor(cw, this), 0);
					inZip.closeEntry();
					
					ZipEntry outEntry = new ZipEntry(classToFile(newName));
					outZip.putNextEntry(outEntry);
					outZip.write(cw.toByteArray());
					outZip.closeEntry();
				} catch(Exception e) {
					System.out.println("Error occurred while processing " + oldName + " -> " + newName);
					
					throw new RuntimeException(e);
				}
			}
		} finally {
			outZip.close();
			if(inZip != null)
				inZip.close();
		}
	}
	
	private boolean isClassIgnored(String cl) {
		for(String s : ignoredPrefixes)
			if(cl.startsWith(s))
				return true;
		if(cl.startsWith("org/apache/") || cl.startsWith("org/gjt/") || cl.startsWith("org/json/"))
			return true;
		if(cl.startsWith("org/sqlite/") || cl.startsWith("org/yaml/") || cl.startsWith("org/ibex/"))
			return true;
		if(cl.startsWith("org/fusesource/") || cl.startsWith("com/avaje/") || cl.startsWith("com/google/"))
			return true;
		if(cl.startsWith("com/mysql/") || cl.startsWith("javax/") || cl.startsWith("jline/"))
			return true;
		if(cl.startsWith("joptsimple/"))
			return true;
		return false;
	}

	private void loadParents(File parentCache) throws IOException {
		Scanner s = new Scanner(parentCache);
		while(s.hasNext()) {
			String cl = s.next();
			if(cl.equals(""))
				break;
			Set<String> inheritsFrom = new HashSet<String>();
			while(true) {
				String str = s.next();
				if(str.equals("."))
					break;
				inheritsFrom.add(str);
			}
			supers.put(cl, inheritsFrom);
		}
		s.close();
	}

	private void saveParents(File parentCache) throws IOException {
		PrintWriter p = new PrintWriter(new FileOutputStream(parentCache));
		for(Map.Entry<String, Set<String>> e : supers.entrySet()) {
			p.print(e.getKey() + " ");
			for(String s : e.getValue())
				p.print(s + " ");
			p.print(". ");
		}
		p.close();
	}

	private static byte[] readClass(ZipInputStream inZip) throws IOException {
		byte[] b = new byte[100000];
		int pos = 0;
		while(true) {
			int read = inZip.read(b, pos, b.length - pos);
			if(read <= 0)
				break;
			pos += read;
			if(pos == b.length) {
				byte[] n = new byte[b.length * 2];
				for(int k = 0; k < b.length; k++)
					n[k] = b[k];
				b = n;
			}
		}
		byte[] n = new byte[pos];
		for(int k = 0; k < pos; k++)
			n[k] = b[k];
		return n;
	}

	public static void main(String[] args) throws Exception {
		if(args.length == 1 && args[0].equals("test")) {
			/*Main m = new Main();
			m.base = new File(MCP_BASE);
			m.input = new File(m.base, "jars/bin/minecraft.jar");
			m.output = new File("minecraft_deobf.jar");
			m.exc = new ExcFile(new File(m.base, "conf/client.exc"));
			m.srg = new SrgFile(new File(m.base, "conf/client.srg"));
			m.fields = new CsvFile(new File(m.base, "conf/fields.csv"), CLIENT, false);
			m.methods = new CsvFile(new File(m.base, "conf/methods.csv"), CLIENT, false);
			m.run();
			
			m = new Main();
			m.base = new File(MCP_BASE);
			m.input = new File(m.base, "jars/minecraft_server.jar");
			m.output = new File("minecraft_server_deobf.jar");
			m.exc = new ExcFile(new File(m.base, "conf/server.exc"));
			m.srg = new SrgFile(new File(m.base, "conf/server.srg"));
			m.fields = new CsvFile(new File(m.base, "conf/fields.csv"), SERVER, false);
			m.methods = new CsvFile(new File(m.base, "conf/methods.csv"), SERVER, false);
			m.run();*/
			
			Main m = new Main();
			m.base = new File(MCP_BASE);
			m.input = new File("C:\\users\\alex\\mcp2\\tech-1.2.5\\lib\\obf\\RedPowerCore-2.0pr5b1.zip");
			m.output = new File("C:\\users\\alex\\mcp2\\tech-1.2.5\\jars\\mods\\RedPowerCore-2.0pr5b1.zip");
			//m.exc = new ExcFile(new File(m.base, "conf/client.exc"));
			//m.srg = new SrgFile(new File(m.base, "conf/client.srg"), m, false);
			//m.fields = new CsvFile(new File(m.base, "conf/fields.csv"), CLIENT, false);
			//m.methods = new CsvFile(new File(m.base, "conf/methods.csv"), CLIENT, false);
			m.xpathlist = new String[] {"C:\\users\\alex\\mcp2\\tech-1.2.5\\jars\\bin\\minecraft.jar"};
			m.run();
		} else {
			if((args.length != 5 && args.length != 6) || (!args[4].equals("deob") && !args[4].equals("reob"))) {
				System.err.println("Arguments: <input file> <output file> <MCP conf dir> [client|server|joined|packaged] [deob|reob] [xpath]");
				System.err.println("  xpath is a "+File.pathSeparator+"-separated list of extra jar files to use, you normally need at least the");
				System.err.println("  minecraft jar here or it won't deobfuscate mods correctly.");
				return;
			}
			boolean reob = args[4].equals("reob");
			
			File confDir = new File(args[2]);
			
			if(!confDir.isDirectory()) {
				System.err.println(args[2]+" is not a directory.");
				return;
			}
			
			Side side = Side.fromString(args[3]);
			if(side == null) {
				System.err.println(args[3]+" is not a valid side.");
				return;
			}
			
			McpMapping mcp = new McpMapping(confDir, side, reob);
			
			Main m = new Main();
			m.base = new File(".");
			m.input = new File(args[0]);
			m.output = new File(args[1]);
			m.map = mcp.getMapping();
			m.xpathlist = (args.length < 6 ? new String[0] : args[5].split(File.pathSeparator));
			m.run();
		}
	}

	public String deobfTypeDescriptor(String desc) {
		if(desc.charAt(0) == '[')
			return "[" + deobfTypeDescriptor(desc.substring(1));
		if(desc.charAt(0) == 'L' && desc.charAt(desc.length() - 1) == ';')
			return "L" + map.getClass(desc.substring(1, desc.length() - 1)) + ";";
		return desc;
	}
	
	public String deobfMethodDescriptor(String desc) {
		// some basic sanity checks, doesn't ensure it's completely valid though
		if(desc.length() == 0 || desc.charAt(0) != '(' || desc.indexOf(")") < 1)
			throw new IllegalArgumentException("Not a valid method descriptor: " + desc);
		
		int pos = 0;
		String out = "";
		while(pos < desc.length())
		{
			switch(desc.charAt(pos))
			{
			case 'V': case 'Z': case 'B': case 'C':
			case 'S': case 'I': case 'J': case 'F':
			case 'D': case '[': case '(': case ')':
				out += desc.charAt(pos);
				pos++;
				break;
			case 'L':
				{
					int end = desc.indexOf(';', pos);
					String obf = desc.substring(pos + 1, end);
					pos = end + 1;
					out += "L" + map.getClass(obf) + ";";
				}
				break;
			default:
				throw new RuntimeException("Unknown method descriptor character: " + desc.charAt(pos) + " (in " + desc + ")");
			}
		}
		return out;
	}
	
	public String lookupInheritedMethod(String owner, String name, String desc) {
		return lookupInheritedMethod(owner, name, desc, false);
	}
	
	public String lookupInheritedField(String owner, String name) {
		return lookupInheritedField(owner, name, false);
	}

	public String lookupInheritedMethod(String owner, String name, String desc, boolean verbose) {
		String deobf = map.getMethod(owner, name, desc);
		if(deobf != null && !deobf.equals(name)) {
			if(verbose)
				System.out.println(owner+"/"+name+desc+" -> "+deobf);
			return deobf;
		}
		Set<String> inherits = supers.get(owner);
		if(verbose)
			System.out.println(owner+"/"+name+desc+" inherits from "+inherits);
		if(inherits != null) {
			for(String s : inherits) {
				deobf = lookupInheritedMethod(s, name, desc, verbose);
				if(deobf != null && !deobf.equals(name))
					return deobf;
			}
		}
		return name;
	}
	
	public String lookupInheritedField(String owner, String name, boolean verbose) {
		String smi = map.getField(owner, name);
		if(smi != null && !smi.equals(name))
			return smi;
		Set<String> inherits = supers.get(owner);
		if(verbose)
			System.out.println(owner+"/"+name+" inherits from "+inherits);
		if(inherits != null) {
			for(String s : inherits) {
				smi = lookupInheritedField(s, name, verbose);
				if(smi != null && !smi.equals(name))
					return smi;
			}
		}
		return name;
	}

	public String deobfField(String obfclass, String obffield) {
		String x = map.getField(obfclass, obffield);
		return x == null ? obffield : x;
	}
}
