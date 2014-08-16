package immibis.bon.io;

import immibis.bon.ClassCollection;
import immibis.bon.ClassFormatException;
import immibis.bon.IProgressListener;
import immibis.bon.NameSet;

import java.io.File;
import java.io.IOException;

@Deprecated
public class ClassCollectionFactory {
	public static ClassCollection loadClassCollection(NameSet ns, File from, IProgressListener progress) throws IOException, ClassFormatException {
		if(from.isDirectory())
			return DirLoader.loadClassesFromDirectory(ns, from, progress);
		else
			return JarLoader.loadClassesFromJar(ns, from, progress);
	}
}
