package immibis.bon.mcp;

import java.io.*;
import java.util.*;

public abstract class CsvFile {
	/** Does not close <var>r</var>. */
	public static Map<String, String> read(Reader r, int[] n_sides) throws IOException {
		Map<String, String> data = new HashMap<String, String>();
		
		@SuppressWarnings("resource")
		Scanner in = new Scanner(r);
		
		in.useDelimiter(",");
		while(in.hasNextLine()) {
			String searge = in.next();
			String name = in.next();
			String side = in.next();
			/*String desc =*/ in.nextLine();
			try {
				if(sideIn(Integer.parseInt(side), n_sides)) {
					data.put(searge, name);
				}
			} catch(NumberFormatException e) {
			}
		}
		return data;
	}
	
	@Deprecated
	public static Map<String, String> read(File f, int[] n_sides) throws IOException {
		try (Reader r = new BufferedReader(new FileReader(f))) {
			return read(r, n_sides);
		}
	}

	private static boolean sideIn(int i, int[] ar) {
		for(int n : ar)
			if(n == i)
				return true;
		return false;
	}
}
