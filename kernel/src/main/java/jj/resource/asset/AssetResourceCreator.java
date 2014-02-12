package jj.resource.asset;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

@Singleton
public class AssetResourceCreator extends AbstractResourceCreator<AssetResource> {
	
	// this class has low test coverage because it has a special path for tests.
	
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

	private Path path(String name, Object... args) {
		Path result = null;
		
		try {
			result = Asset.path(name);
		} catch (IOException e) {
			log.error("couldn't produce an asset path for {}", name);
			log.error("", e);
		}
		
		return result;
	}

	@Override
	public AssetResource create(String baseName, Object... args) throws IOException {
		if (Asset.jar == null) {
			return instanceModuleCreator.createResource(AssetResource.class, cacheKey(Asset.appPath.resolve(baseName).toUri()), baseName, Asset.appPath);
		} else {
			try (FileSystem myJarFS = FileSystems.newFileSystem(Asset.jar, null)) {
				return instanceModuleCreator.createResource(AssetResource.class, cacheKey(baseName), baseName, myJarFS.getPath("/jj/assets"));
			}
		}
	}
}
