package jj;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;

/**
 * Sets up the basic installation directories when the system is started.
 * This only installs enough to bootstrap the system, the kernel takes
 * care of everything else later
 * 
 * @author jason
 *
 */
final class BootstrapInstaller {
	
	private static final String SYSTEM_BASE_PATH = "system";
	private static final String META_INF_PATH = "META-INF";
	
	final Path jarPath;
	final Path basePath;
	final Path systemPath;
	
	BootstrapInstaller(Path jjJarPath) {
		
		try {
			jarPath = jjJarPath;
			basePath = jjJarPath.getParent();
			systemPath = Files.createDirectories(basePath.resolve(SYSTEM_BASE_PATH));
			install();
		} catch (Exception e) {
			throw new IllegalStateException("Installation failed - jar at [" + jjJarPath + "] is corrupt?", e);
		}
	}
	
	private void install() throws Exception {
		
		try (FileSystem myJarFS = FileSystems.newFileSystem(jarPath, null)) {
			
			// basic self installation
			// - ensure the existence of the system directory and the system/lib
			// - if necessary, copy the libs from inside the jar to the lib directory
			//   always copy out internal jars if this is a snapshot build.
			
			// whoever owns the installation directory is going to own
			// everything we make - if we are running as root on unix
			// we need to do this or we can't read our dependencies later
			// could (and should!) just make them readable to all and writable by
			// no one?  would that work?
			UserPrincipal owner = Files.getOwner(jarPath);
			
			Files.setOwner(systemPath.getParent(), owner);
			Files.setOwner(systemPath, owner);
			
			try (DirectoryStream<Path> systemDir = 
				Files.newDirectoryStream(myJarFS.getPath(META_INF_PATH, SYSTEM_BASE_PATH), "*.jar")) {
				for (Path storedPath : systemDir) {
					
					String fileName = storedPath.getFileName().toString();
					
					Path installedPath = systemPath.resolve(fileName);
					
					if (fileName.startsWith(Version.name) && Version.snapshot) {
						Files.copy(storedPath, installedPath, COPY_ATTRIBUTES, REPLACE_EXISTING);
					} else if (!Files.exists(installedPath)){
						Files.copy(storedPath, installedPath, COPY_ATTRIBUTES);
					}
					Files.setOwner(installedPath, owner);
				}
			}
		}
	}
	
}