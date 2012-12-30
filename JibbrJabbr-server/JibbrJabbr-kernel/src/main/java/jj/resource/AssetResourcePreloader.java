package jj.resource;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.JJStartup;

class AssetResourcePreloader implements JJStartup {
	
	private final Logger log = LoggerFactory.getLogger(AssetResourcePreloader.class);
	
	private final ResourceFinder finder;
	
	AssetResourcePreloader(final ResourceFinder finder) {
		this.finder = finder;
	}

	@Override
	public void start() throws Exception {
		
		log.debug("preloading internal assets");

		if (AssetResourceCreator.basePath != null) {
			doWalk(AssetResourceCreator.basePath);
		} else if (AssetResourceCreator.myJar != null) {
			try (FileSystem myJarFS = FileSystems.newFileSystem(AssetResourceCreator.myJar, null)) {
				doWalk(myJarFS.getPath("/jj/assets"));
			}
		}
		
		
		
		log.debug("preload complete");
	}
	
	private void doWalk(final Path path) throws Exception {
		Files.walkFileTree(path, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String baseName = path.relativize(file).toString();
				finder.loadResource(AssetResource.class, baseName);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				throw exc;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				if (exc != null) throw exc;
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
