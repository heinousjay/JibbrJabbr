package jj.server;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import javax.inject.Inject;

import jj.resource.AbstractFileResource;

public class ModuleResource extends AbstractFileResource {

	@Inject
	ModuleResource(Dependencies dependencies, Path path) throws Exception {
		super(dependencies, path, true);
		
		// we should be able to open this as a jar filesystem
		FileSystem jar = FileSystems.newFileSystem(path, null);
		System.out.println(jar);
		jar.close();
	}
}
