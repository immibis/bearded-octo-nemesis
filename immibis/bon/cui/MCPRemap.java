package immibis.bon.cui;

import immibis.bon.ClassCollection;
import immibis.bon.ReferenceDataCollection;
import immibis.bon.Remapper;
import immibis.bon.io.ClassCollectionFactory;
import immibis.bon.io.JarWriter;
import immibis.bon.mcp.MappingFactory;
import immibis.bon.mcp.MappingLoader_MCP;
import immibis.bon.mcp.MinecraftNameSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MCPRemap extends CUIBase {
	
	private static class Timer {
		private long start;
		public Timer() {start = System.currentTimeMillis();}
		public int flip()
		{
			int rv = (int)(System.currentTimeMillis() - start);
			start = System.currentTimeMillis();
			return rv;
		}
	}
	
	@Override
	protected void run() throws Exception {
		Timer timer = new Timer();
		int readTime = 0, remapTime = 0, writeTime = 0;
		
		System.out.println("Loading MCP configuration");
		
		String mcVer = MappingLoader_MCP.getMCVer(mcpDir);
		MappingFactory.registerMCPInstance(mcVer, side, mcpDir, null);
		readTime += timer.flip();
		
		MinecraftNameSet inputNS = new MinecraftNameSet(fromType, side, mcVer);
		MinecraftNameSet outputNS = new MinecraftNameSet(toType, side, mcVer);
		
		List<ReferenceDataCollection> refs = new ArrayList<>();
		for(RefOption ro : refOptsParsed) {
			MinecraftNameSet refNS = new MinecraftNameSet(ro.type, side, mcVer);
		
			System.out.println("Loading "+ro.file);
			ClassCollection refCC = ClassCollectionFactory.loadClassCollection(refNS, ro.file, null);
			readTime += timer.flip();
			
			if(!refNS.equals(inputNS)) {
				System.out.println("Remapping "+ro.file+" ("+refNS+" -> "+inputNS+")");
				refCC = Remapper.remap(refCC, MappingFactory.getMapping((MinecraftNameSet)refCC.getNameSet(), inputNS, null), Collections.<ReferenceDataCollection>emptyList(), null);
				remapTime += timer.flip();
			}
			
			refs.add(ReferenceDataCollection.fromClassCollection(refCC));
		}
		
		System.out.println("Loading "+inFile);
		ClassCollection inputCC = ClassCollectionFactory.loadClassCollection(inputNS, inFile, null);
		readTime += timer.flip();
		
		System.out.println("Remapping "+inFile+" ("+inputNS+" -> "+outputNS+")");
		ClassCollection outputCC = Remapper.remap(inputCC, MappingFactory.getMapping((MinecraftNameSet)inputCC.getNameSet(), outputNS, null), refs, null);
		remapTime += timer.flip();
		
		System.out.println("Writing "+outFile);
		JarWriter.write(outFile, outputCC, null);
		writeTime += timer.flip();
		
		//System.out.printf("Completed in %dms (%dms read, %dms remap, %dms write)\n", readTime+remapTime+writeTime, readTime, remapTime, writeTime);
		System.out.printf("Completed in %d ms\n", readTime + remapTime + writeTime);
	}
	
	
	@Required @Option("-mcp")	public File mcpDir;
	@Required @Option("-from")	public MinecraftNameSet.Type fromType;
	@Required @Option("-to")	public MinecraftNameSet.Type toType;
	@Required @Option("-side")	public MinecraftNameSet.Side side;
	@Required @Option("-in")	public File inFile;
	@Required @Option("-out")	public File outFile;
	          @Option("-ref")	public List<String> refOpts = new ArrayList<>();
	          @Option("-refn")	public List<String> refnOpts = new ArrayList<>();
	
    private static class RefOption {
    	public MinecraftNameSet.Type type;
    	public File file;
    	
    	public RefOption(MinecraftNameSet.Type t, File f) {
    		type = t;
    		file = f;
    	}
    }
	          
	private List<RefOption> refOptsParsed = new ArrayList<>();
	
	@Override
	protected boolean checkOptions() throws Exception {
		if(!super.checkOptions())
			return false;
		
		boolean ok = true;
		
		if(!inFile.exists()) {
			System.err.println("Input file doesn't exist: " + inFile.getAbsolutePath());
			ok = false;
		}
		
		if(outFile.isDirectory()) {
			System.err.println("Output file already exists and is a directory: " + outFile.getAbsolutePath());
			ok = false;
		}
		
		if(!mcpDir.exists()) {
			System.err.println("MCP directory doesn't exist: " + mcpDir.getAbsolutePath());
			ok = false;
		}
		
		for(String s : refOpts) {
			refOptsParsed.add(new RefOption(fromType, new File(s)));
		}
		
		for(String s : refnOpts) {
			String[] p = s.split(":", 2);
			if(p.length != 2) {
				System.err.println("Missing : in -refn option: " + s);
				ok = false;
			} else {
				try {
					refOptsParsed.add(new RefOption(MinecraftNameSet.Type.valueOf(p[0]), new File(p[1])));
				} catch(EnumConstantNotPresentException e) {
					System.err.println("Invalid name type: " + p[0]);
					ok = false;
				}
			}
		}
		
		for(RefOption ro : refOptsParsed) {
			if(!ro.file.exists()) {
				System.err.println("Reference file doesn't exist: " + ro.file.getAbsolutePath());
				ok = false;
			}
		}
		
		return ok;
	}
	

	
	@Override
	protected void showUsage() {
		System.out.println("Usage:");
		System.out.println("  java -cp BON.jar immibis.bon.cui.MCPRemap <option>...");
		System.out.println("");
		System.out.println("Required options:");
		System.out.println("  -mcp <mcp dir>");
		System.out.println("       Specifies the path to the MCP directory.");
		System.out.println("  -from <source names>");
		System.out.println("       Specifies the type of names the input file will uses.");
		System.out.println("       Can be OBF or SRG or MCP.");
		System.out.println("  -to <target names>");
		System.out.println("       Specifies the type of names the output file will use.");
		System.out.println("       Can be OBF or SRG or MCP.");
		System.out.println("  -side <side>");
		System.out.println("       Can be UNIVERSAL, CLIENT or SERVER.");
		System.out.println("  -in <input file>");
		System.out.println("       Specifies the path to the input file");
		System.out.println("  -out <output file>");
		System.out.println("       Specifies the path to the output file");
		System.out.println("");
		System.out.println("Optional options:");
		System.out.println("  -ref <reference file>");
		System.out.println("       Specifies the path to a jar file or directory which the input code depends on.");
		System.out.println("       This is hard to describe exactly, but you will want to specify MCP/bin/minecraft");
		System.out.println("       as well as any mods your mod depends on (e.g. RedPowerCore when processing");
		System.out.println("       RedPowerDigital). You can use this option several times with different files.");
		System.out.println("       The file must be using the same names specified in <source names>. See -refn.");
		System.out.println("");
		System.out.println("  -refn <names>:<reference file>");
		System.out.println("       Same as -ref, but the reference file can be using obfuscated, SRG or MCP names.");
		System.out.println("       If <names> is different from <source names>, the file will be remapped automatically,");
		System.out.println("       which will take slightly longer than if the file was already remapped.");
		System.out.println("");
		System.out.println("Example command line:");
		System.out.println("  -mcp . -from OBF -to MCP -side UNIVERSAL -in RedPowerDigital.zip -out RedPowerDigital-deobf.zip -ref RedPowerCore.zip -refn MCP:bin/minecraft");
		System.out.println("       Deobfuscates RedPowerDigital.zip, saving the result in RedPowerDigital-deobf.zip.");
		System.out.println("       The current directory contains an MCP installation. RedPowerCore.zip (which is obfuscated)");
		System.out.println("       and bin/minecraft (which is not) will also be loaded.");
		System.out.println("");
		System.out.println("  -mcp . -from MCP -to OBF -side UNIVERSAL -in AwesomeMod.jar -out AwesomeMod-obf.jar -ref bin/minecraft");
		System.out.println("       Obfuscates AwesomeMod.jar, saving the result in AwesomeMod-obf.jar.");
		System.out.println("       The current directory contains an MCP installation.");
		System.out.println("");
		System.out.println("Note: If deobfuscating, you need to know if the input file is using SRG or OBF names.");
		System.out.println("The GUI gets around this by remapping twice, once with '-from OBF -to SRG', and then with '-from SRG -to MCP',");
		System.out.println("which is slower.");
		System.out.println("");
		System.out.println("Note: Automatic remapping of reference files may not work correctly if the reference file itself needs");
		System.out.println("reference files to remap correctly. (E.g. if RPDigital.zip requires RPCore.zip which requires bin/minecraft)");
		System.out.println("In this case you will need to ensure the reference files do not need remapping.");
		System.out.println("A reference file could be the output of a previous command.");
		System.out.println("");
	}
	
	public static void main(String[] args) throws Exception {
		new MCPRemap().run(args);
	}
}
