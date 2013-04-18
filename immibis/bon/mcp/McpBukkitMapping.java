package immibis.bon.mcp;

/*import immibis.bon.Mapping;

import java.util.*;
import java.io.File;
import java.io.IOException;

public class McpBukkitMapping extends McpMapping {

	public McpBukkitMapping(File confDir, Side side, boolean reobf) throws IOException {
		super(confDir, side, reobf);
	}
	
	public Mapping getMapping() {
		Mapping m = super.getMapping();
		
		for(Map.Entry<String, String> e : new HashMap<String, String>(m.classes).entrySet()) {
			if(e.getKey().startsWith("net/minecraft/src/")) {
				String newKey = e.getKey().replace("net/minecraft/src/", "net/minecraft/server/");
				m.setClass(newKey, e.getValue());
				m.classes.remove(e.getKey());
			}
		}
		
		for(Map.Entry<String, String> e : new HashMap<String, String>(m.fields).entrySet()) {
			if(e.getKey().startsWith("net/minecraft/src/")) {
				String newKey = e.getKey().replace("net/minecraft/src/", "net/minecraft/server/");
				m.fields.put(newKey, e.getValue());
				m.fields.remove(e.getKey());
			}
		}
		
		for(Map.Entry<String, String> e : new HashMap<String, String>(m.methods).entrySet()) {
			if(e.getKey().startsWith("net/minecraft/src/")) {
				String newKey = e.getKey().replace("net/minecraft/src/", "net/minecraft/server/");
				m.methods.put(newKey, e.getValue());
				m.methods.remove(e.getKey());
			}
		}
		
		//throw new RuntimeException("boom");
		
		return m;
	}

}*/
