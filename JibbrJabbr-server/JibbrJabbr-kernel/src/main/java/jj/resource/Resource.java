package jj.resource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public interface Resource {
	
	String baseName();
	
	Object[] creationArgs();

	Path path();
	
	boolean needsReplacing() throws IOException;
	
	FileTime lastModified();

	String uri();
	
	String absoluteUri();
	
	String sha1();
	
	String mime();
}