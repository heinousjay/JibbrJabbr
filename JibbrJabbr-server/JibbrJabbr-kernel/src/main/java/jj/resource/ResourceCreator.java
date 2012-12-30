package jj.resource;

import java.io.IOException;
import java.nio.file.Path;

/**
 * represents the ability to create a resource given a baseName and
 * some unspecified arguments
 * @author jason
 *
 * @param <T>
 */
interface ResourceCreator<T extends Resource> {

	Class<T> type();
	
	Path toPath(final String baseName, final Object...args);
	
	T create(final String baseName, final Object...args) throws IOException;
}
