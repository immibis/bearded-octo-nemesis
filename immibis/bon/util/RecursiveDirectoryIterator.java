package immibis.bon.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** TODO remove this - unused? */
public class RecursiveDirectoryIterator implements Iterable<File> {
	
	private final File root;
	private final boolean includeDirs;
	
	public RecursiveDirectoryIterator(File root, boolean includeDirs) {
		this.root = root;
		this.includeDirs = includeDirs;
	}
	
	@Override
	public Iterator<File> iterator() {
		return new Iterator<File>() {
			List<File> stack = new ArrayList<>(); {stack.add(root); skipDirs();}
			
			@Override
			public boolean hasNext() {
				return !stack.isEmpty();
			}
			
			@Override
			public File next() {
				if(stack.isEmpty())
					throw new NoSuchElementException();
				File f = stack.remove(stack.size()-1);
				if(f.isDirectory())
					stack.addAll(Arrays.asList(f.listFiles()));
				skipDirs();
				return f;
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			private void skipDirs() {
				if(includeDirs)
					return;
				while(!stack.isEmpty() && stack.get(stack.size()-1).isDirectory())
					next();
			}
		};
	}
	
	
}
