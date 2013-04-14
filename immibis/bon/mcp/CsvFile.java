package immibis.bon.mcp;

import java.io.*;
import java.util.*;

public abstract class CsvFile {
	public static Map<String, String> read(File f, int n_side) throws IOException {
		Map<String, String> data = new HashMap<String, String>();
		Scanner in = new Scanner(f);
		String s_side = String.valueOf(n_side);
		try {
			in.useDelimiter(",");
			while(in.hasNextLine()) {
				String searge = in.next();
				String name = in.next();
				String side = in.next();
				/*String desc =*/ in.nextLine();
				if(side.equals(s_side)) {
					data.put(searge, name);
				}
			}
		} finally {
			in.close();
		}
		return data;
	}
}
