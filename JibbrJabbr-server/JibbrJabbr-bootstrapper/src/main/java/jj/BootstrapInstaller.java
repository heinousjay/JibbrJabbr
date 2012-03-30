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
	private static final String LIB_PATH = "lib";
	private static final String META_INF_PATH = "META-INF";
	
	final Path jarPath;
	final Path basePath;
	final Path systemPath;
	final Path libPath;
	
	BootstrapInstaller(Path jjJarPath) {
		
		try {
			jarPath = jjJarPath;
			basePath = jjJarPath.getParent();
			systemPath = basePath.resolve(SYSTEM_BASE_PATH);
			libPath = Files.createDirectories(systemPath.resolve(LIB_PATH));
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
			//   for now, always do it. properly this would require version checks
			//   maybe have Maven put the classpath entries in the manifest? that might work
			//   since all the jars are named with versions
			
			// whoever owns the installation directory is going to own
			// everything we make - if we are running as root on unix
			// we need to do this or we can't read our dependencies later
			UserPrincipal owner = Files.getOwner(jarPath);
			
			Files.setOwner(libPath.getParent(), owner);
			Files.setOwner(libPath, owner);
			
			try (DirectoryStream<Path> libDir = 
				Files.newDirectoryStream(myJarFS.getPath(META_INF_PATH, LIB_PATH), "*.jar")) {
				for (Path jarPath : libDir) {
					Path jar = libPath.resolve(jarPath.getFileName().toString());
					Files.copy(jarPath, jar, COPY_ATTRIBUTES, REPLACE_EXISTING);
					Files.setOwner(jar, owner);
				}
			}
		}
	}
	
}