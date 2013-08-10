package immibis.bon.mcp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public abstract class CsvFile {
	public static Map<String, String> read(File f, int[] n_sides) throws IOException {
		Map<String, String> data = new HashMap<String, String>();
		Scanner in = new Scanner(new BufferedReader(new FileReader(f)));

		try {
			in.useDelimiter(",");
			while (in.hasNextLine()) {
				String searge = in.next();
				String name = in.next();
				String side = in.next();
				/* String desc = */ in.nextLine();
				try {
					if (sideIn(Integer.parseInt(side), n_sides)) data.put(searge, name);
				} catch (NumberFormatException e) {}
			}
		} finally {
			in.close();
		}
		return data;
	}

	static boolean sideIn(int i, int[] ar) {
		for (int n : ar) if (n == i) return true;
		return false;
	}
}