package jj.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.JJ;

@Singleton
class AssetResourceCreator implements ResourceCreator<AssetResource> {
	
	static final Path myJar = JJ.jarForClass(AssetResourceCreator.class);
	
	// our assets should be here
	static final Path basePath;
	
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
		basePath = attempt;
	}
	
	private final Logger log = LoggerFactory.getLogger(AssetResourceCreator.class);
	
	@Override
	public Class<AssetResource> type() {
		return AssetResource.class;
	}

	@Override
	public Path toPath(String baseName, Object... args) {
		Path result = null;
		if (basePath != null) {
			result = basePath.resolve(baseName);
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
			return new AssetResource(basePath, baseName);
		} else {
			try (FileSystem myJarFS = FileSystems.newFileSystem(myJar, null)) {
				return new AssetResource(myJarFS.getPath("/jj/assets"), baseName);
			}
		}
		
	}

}
