package jj.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.JJ;

@Singleton
class AssetResourceCreator extends AbstractResourceCreator<AssetResource> {
	
	static final Path myJar = JJ.jarForClass(AssetResourceCreator.class);
	
	// our assets should be here
	static final Path appPath;
	
	static {
		// just walking along to our assets directory
		Path attempt = null;
		try {
			if (myJar == null) {
				URI uri = AssetResourceCreator.class.getResource("/jj/resource/AssetResourceCreator.class").toURI();
				attempt = Paths.get(uri).getParent().getParent().resolve("assets");
			} 
			
		} catch (Exception e) {
			throw new AssertionError("couldn't locate internal assets, altering the jar?");
		}
		appPath = attempt;
	}
	
	private final Logger log = LoggerFactory.getLogger(AssetResourceCreator.class);
	
	private final ResourceInstanceModuleCreator instanceModuleCreator;
	
	@Inject
	AssetResourceCreator(final ResourceInstanceModuleCreator instanceModuleCreator) {
		this.instanceModuleCreator = instanceModuleCreator;
	}
	
	@Override
	public Class<AssetResource> type() {
		return AssetResource.class;
	}
	
	@Override
	public boolean canLoad(String name, Object... args) {
		// we load whatever
		return true;
	}

	@Override
	Path path(String baseName, Object... args) {
		Path result = null;
		if (appPath != null) {
			result = appPath.resolve(baseName);
		} else {
			try (FileSystem myJarFS = FileSystems.newFileSystem(myJar, null)) {
				result = myJarFS.getPath("/jj/assets/").resolve(baseName);
			} catch (IOException e) {
				log.error("couldn't produce an asset path for {}", baseName);
				log.error("", e);
			}
		}
		return result;
	}

	@Override
	public AssetResource create(String baseName, Object... args) throws IOException {
		if (myJar == null) {
			return instanceModuleCreator.createResource(AssetResource.class, cacheKey(appPath.resolve(baseName).toUri()), baseName, appPath);
		} else {
			try (FileSystem myJarFS = FileSystems.newFileSystem(myJar, null)) {
				return instanceModuleCreator.createResource(AssetResource.class, cacheKey(baseName), baseName, myJarFS.getPath("/jj/assets"));
			}
		}
	}
}
