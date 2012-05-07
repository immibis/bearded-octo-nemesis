package immibis.beardedoctonemesis;

import java.io.*;
import java.util.*;

public class CsvFile {
	private Map<String, String> data = new HashMap<String, String>();
	
	public CsvFile(File f, int n_side, boolean reob) throws IOException {
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
	}

	public String get(String seargeName) {
		String r = data.get(seargeName);
		return r == null ? seargeName : r;
	}
}
