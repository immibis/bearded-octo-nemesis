package immibis.beardedoctonemesis;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class Main {
	public static final String MCP_BASE = "C:/Users/Alex/mcp2/fdro-test-1.2.5/";
	
	public static final int CLIENT = 0;
	public static final int SERVER = 1;
	
	public File base, input, output;
	public ExcFile exc;
	public SrgFile srg;
	public CsvFile fields, methods;
	public String[] xpathlist;
	
	public Map<String, Set<String>> supers = new HashMap<String, Set<String>>();
	
	// returns an internal name (with /'s and $'s)
	public static String fileToClass(String fn) {
		return fn.replace(File.separator, "/").replace(".class", "");
	}
	
	// expects an internal name (with /'s and $'s)
	public static String classToFile(String cn) {
		return cn+".class";
	}
	
	private void getParents(File f) throws IOException {
		ZipInputStream inZip = new ZipInputStream(new FileInputStream(f));
		while(true) {
			ZipEntry inEntry = inZip.getNextEntry();
			if(inEntry == null)
				break;
			if(!inEntry.getName().endsWith(".class"))
				continue;
			
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
	
	public void run() throws IOException {
		ZipInputStream inZip;
		ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(output));
		
		for(String xpath : xpathlist)
			getParents(new File(xpath));
		getParents(input);
		
		inZip = new ZipInputStream(new FileInputStream(input));
		
		while(true) {
			ZipEntry inEntry = inZip.getNextEntry();
			if(inEntry == null)
				break;
			
			if(!inEntry.getName().endsWith(".class")) {
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
			String newName = srg.getClassName(oldName);
			
			//if(oldName.contains("$"))
				//System.out.println(oldName+" -> "+newName);
			
			try {
			
				ClassReader cr = new ClassReader(inZip);
				
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
		inZip.close();
		outZip.close();
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
			m.exc = new ExcFile(new File(m.base, "conf/client.exc"));
			m.srg = new SrgFile(new File(m.base, "conf/client.srg"), m);
			m.fields = new CsvFile(new File(m.base, "conf/fields.csv"), CLIENT, false);
			m.methods = new CsvFile(new File(m.base, "conf/methods.csv"), CLIENT, false);
			m.xpathlist = new String[] {"C:\\users\\alex\\mcp2\\tech-1.2.5\\jars\\bin\\minecraft.jar"};
			m.run();
		} else {
			if((args.length != 8 && args.length != 9) || (!args[7].equals("deob") && !args[7].equals("reob"))) {
				System.err.println("Arguments: <input file> <output file> <exc file> <srg file> <fields.csv> <methods.csv> <side number> [deob|reob] [xpath]");
				System.err.println("  xpath is a "+File.pathSeparator+"-separated list of extra jar files to use, you normally need at least the");
				System.err.println("  minecraft jar here or it won't deobfuscate mods correctly.");
				return;
			}
			boolean reob = args[7].equals("reob");
			if(reob)
				System.err.println("You are reobfuscating, this probably won't work properly yet!");
			
			int side = Integer.parseInt(args[6]);
			Main m = new Main();
			m.base = new File(".");
			m.input = new File(/*m.base, */args[0]);
			m.output = new File(args[1]);
			m.exc = new ExcFile(new File(/*m.base, */args[2]));
			m.srg = new SrgFile(new File(/*m.base, */args[3]), m);
			m.fields = new CsvFile(new File(/*m.base, */args[4]), side, reob);
			m.methods = new CsvFile(new File(/*m.base, */args[5]), side, reob);
			m.xpathlist = (args.length < 9 ? "" : args[8]).split(File.pathSeparator);
			m.run();
		}
	}

	public String deobfTypeDescriptor(String desc) {
		if(desc.charAt(0) == '[')
			return "[" + deobfTypeDescriptor(desc.substring(1));
		if(desc.charAt(0) == 'L' && desc.charAt(desc.length() - 1) == ';')
			return "L" + srg.getClassName(desc.substring(1, desc.length() - 1)) + ";";
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
					out += "L" + srg.getClassName(obf) + ";";
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
		String deobf = srg.getMethod(owner, name, desc);
		if(deobf != null) {
			if(verbose)
				System.out.println(owner+"/"+name+desc+" -> "+deobf);
			return deobf;
		}
		Set<String> inherits = supers.get(owner);
		//if(owner.contains("RubLeaves"))
			//verbose = true;
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
		String smi = srg.getFieldName(owner, name);
		if(smi != null)
			return smi;
		//if(name.equals("bN"))// && owner.contains("BlockIC2Door"))
			//verbose = true;
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
	
}
