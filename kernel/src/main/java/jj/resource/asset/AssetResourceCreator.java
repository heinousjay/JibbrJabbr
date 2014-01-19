package jj.resource.asset;

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
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

@Singleton
public class AssetResourceCreator extends AbstractResourceCreator<AssetResource> {
	
	// this class has low test coverage because it has a special path for tests.
	
	static final Path myJar = JJ.jarForClass(AssetResourceCreator.class);
	
	// our assets should be here
	static final Path appPath;
	
	static {
		// just walking along to our assets directory
		Path attempt = null;
		try {
			if (myJar == null) {
				// ugly but necessary for testing, and favicon.ico will always be there
				URI uri = AssetResourceCreator.class.getResource("/jj/assets/favicon.ico").toURI();
				attempt = Paths.get(uri).getParent();
			} 
			
		} catch (Exception e) {
			throw new AssertionError("couldn't locate internal assets, altering the jar?");
		}
		appPath = attempt;
	}
	
	private final Logger log = LoggerFactory.getLogger(AssetResourceCreator.class);
	
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	AssetResourceCreator(final ResourceInstanceCreator instanceModuleCreator) {
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
	protected URI uri(String baseName, Object... args) {
		return path(baseName).toUri();
	}

	private Path path(String baseName, Object... args) {
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
