package immibis.beardedoctonemesis.mcp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import immibis.beardedoctonemesis.Main;
import immibis.beardedoctonemesis.Mapping;

public class McpMapping {
	
	protected SrgFile srg_file;
	protected ExcFile exc_file;
	protected Map<String, String> fields_csv;
	protected Map<String, String> methods_csv;
	protected boolean reobf;
	
	public McpMapping(File confDir, Side side, boolean reobf) throws IOException {
		srg_file = new SrgFile(new File(confDir, side.srg_name), reobf);
		exc_file = new ExcFile(new File(confDir, side.exc_name));
		fields_csv = CsvFile.read(new File(confDir, "fields.csv"), side.side_number);
		methods_csv = CsvFile.read(new File(confDir, "methods.csv"), side.side_number);
		
		this.reobf = reobf;
	}
	
	private String getCsv(Map<String, String> csv, String key) {
		if(csv.containsKey(key))
			return csv.get(key);
		return key;
	}
	
	public Mapping getMapping() {
		Mapping m = new Mapping();
		
		for(Map.Entry<String, String> e : srg_file.classes.entrySet()) {
			m.setClass(e.getKey(), e.getValue());
			//System.out.println(e.getKey()+" -> "+e.getValue());
		}
		
		for(Map.Entry<String, String> e : srg_file.methods.entrySet()) {
			String descriptor = "(" + e.getKey().split("\\(")[1];
			String name_and_class = e.getKey().split("\\(")[0];
			String method_name = SrgFile.getLastComponent(name_and_class);
			String clazz = name_and_class.substring(0, name_and_class.length() - method_name.length() - 1);
			
			if(reobf) {
				m.setMethod(clazz, getCsv(methods_csv, method_name), descriptor, e.getValue());
			} else {
				
				String obf_name = method_name;
				String srg_name = e.getValue();
				String deobf_name = getCsv(methods_csv, e.getValue());
				
				m.setMethod(clazz, obf_name, descriptor, deobf_name);
				m.setMethod(srg_file.classes.get(clazz), srg_name, m.mapMethodDescriptor(descriptor), deobf_name); // for Forge mods using SRG names
			}
		}
		
		for(Map.Entry<String, String> e : srg_file.fields.entrySet()) {
			String name = SrgFile.getLastComponent(e.getKey());
			String clazz = e.getKey().substring(0, e.getKey().length() - name.length() - 1);
			
			if(reobf) {
				m.setField(clazz, getCsv(fields_csv, name), e.getValue());
			} else {
				
				String obf_name = name;
				String srg_name = e.getValue();
				String deobf_name = getCsv(fields_csv, e.getValue());
				
				m.setField(clazz, obf_name, deobf_name);
				m.setField(srg_file.classes.get(clazz), srg_name, deobf_name); // for Forge mods using SRG names
			}
		}
		
		for(Map.Entry<String, String[]> e : exc_file.exceptions.entrySet()) {
			String descriptor = "(" + e.getKey().split("\\(")[1];
			String name_and_class = e.getKey().split("\\(")[0];
			String method_name = SrgFile.getLastComponent(name_and_class);
			String clazz = name_and_class.substring(0, name_and_class.length() - method_name.length() - 1);
			
			m.setExceptions(clazz, method_name, descriptor, Arrays.asList(e.getValue()));
		}
		
		if(reobf)
			m.addPrefix("net/minecraft/src/", "");
		else
		{
			m.setDefaultPackage("net/minecraft/src/");
			m.addPrefix("forge/", "net/minecraft/src/forge/");
		}
		
		return m;
	}
}
