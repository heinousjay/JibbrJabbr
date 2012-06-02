package jj;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Responsible for loading the kernel and its direct dependencies
 * The kernel will make its own set of classloaders to decide what
 * means what to applications.
 * @author jason
 *
 */
final class BootstrapClassLoader
		extends ClassLoader{
	
	private static final String JAR_GLOB = "*.jar";
	private static final String CLASS_FILE_FORMAT = "/%s.class";
	private static final String RESOURCE_FORMAT = "/%s";
	static {
		// does this speed up the start-up? exciting...
		registerAsParallelCapable();
	}
	
	private final Path libPath;

	BootstrapClassLoader(Path libPath) {
		// whatever loaded this class is the root of all classloaders in the system
		super(BootstrapClassLoader.class.getClassLoader());
		this.libPath = libPath;
		// the kernel uses assertions to guarantee everything is constructed correctly
		// and we want them enabled
		setDefaultAssertionStatus(true);
	}

	private DirectoryStream<Path> libJarsStream() throws IOException {
		return Files.newDirectoryStream(libPath, JAR_GLOB);
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try (DirectoryStream<Path> libDir = libJarsStream()) {
			for (Path jarPath : libDir) {
				try (FileSystem myJarFS = FileSystems.newFileSystem(jarPath, null)) {
					Path attempt = myJarFS.getPath(String.format(CLASS_FILE_FORMAT, name.replace('.', '/')));
					if (Files.exists(attempt)) {
						byte[] classBytes = Files.readAllBytes(attempt);
						return defineClass(name, classBytes, 0, classBytes.length);
					}
				}
			}
			
		} catch (IOException e) {
			System.err.printf("Something went wrong reading a class [%s]\n", name);
			e.printStackTrace();
			throw new ClassNotFoundException(name, e);
		}
		System.err.printf("Couldn't find [%s]\n", name);
		throw new ClassNotFoundException(name);
	}
	
	@Override
	protected URL findResource(String name) {
		URL result = null;
		try (DirectoryStream<Path> libJars = libJarsStream()) {
			for (Path jarPath : libJars) {
				try (FileSystem myJarFS = FileSystems.newFileSystem(jarPath, null)) {
					Path attempt = myJarFS.getPath(String.format(RESOURCE_FORMAT, name));
					if (Files.exists(attempt)) {
						result = attempt.toUri().toURL();
						break;
					}
				}
			}
		} catch (IOException e) {}
		return result;
	}
	
	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		List<URL> result = new ArrayList<>();
		try (DirectoryStream<Path> libJars = libJarsStream()) {
			for (Path jarPath : libJars) {
				try (FileSystem myJarFS = FileSystems.newFileSystem(jarPath, null)) {
					Path attempt = myJarFS.getPath(String.format(RESOURCE_FORMAT, name));
					if (Files.exists(attempt)) {
						result.add(attempt.toUri().toURL());
					}
				}
			}
		}
		return Collections.enumeration(result);
	}
}