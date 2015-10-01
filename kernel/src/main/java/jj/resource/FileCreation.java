package jj.resource;

import java.nio.file.Path;

/**
 * Event published when a new directory has been created in a watched filesystem
 * @author jason
 */
public class FileCreation {

	public final Path path;

	public FileCreation(Path path) {
		this.path = path;
	}
}
