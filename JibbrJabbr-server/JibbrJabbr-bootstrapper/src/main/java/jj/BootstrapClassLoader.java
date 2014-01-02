package jj;

import java.io.IOException;
import java.net.URL;
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
 * 
 * Most of this should actually go into a different classloader inside the kernel
 * project.  this should only really know how to find the startup class inside there
 * 
 * @author jason
 *
 */
public final class BootstrapClassLoader extends ClassLoader {

	private static final String CLASS_FILE_FORMAT = "/%s.class";
	private static final String RESOURCE_FORMAT = "/%s";
	static {
		// does this speed up the start-up? exciting...
		registerAsParallelCapable();
	}

	private final SystemJars systemJars;

	BootstrapClassLoader(Path libPath) throws IOException {
		// whatever loaded this class is the root of all classloaders in the system
		super(BootstrapClassLoader.class.getClassLoader());
		// the kernel uses assertions all over the place to ensure the state is
		// what we expect it to be
		setDefaultAssertionStatus(true);
		systemJars = new SystemJars(libPath);
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		
		try {
			String classFile = String.format(CLASS_FILE_FORMAT, name.replace('.', '/'));
			Path attempt = systemJars.pathForFile(classFile);
			if (attempt != null) {
				byte[] classBytes = Files.readAllBytes(attempt);
				return defineClass(name, classBytes, 0, classBytes.length);
			}
			
		} catch (Exception e) {
			System.err.printf("Something went wrong reading a class [%s]\n", name);
			e.printStackTrace();
			throw new ClassNotFoundException(name, e);
		}
		
		throw new ClassNotFoundException(name);
	}
	
	@Override
	protected URL findResource(String name) {
		URL result = null;
		try {
			String resourceFile = String.format(RESOURCE_FORMAT, name);
			Path attempt = systemJars.pathForFile(resourceFile);
			if (attempt != null) {
				result = attempt.toUri().toURL();
			}
		} catch (Exception e) {
			System.err.printf("Something went wrong reading a resource [%s]\n", name);
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		List<URL> result = new ArrayList<>();
		
		try {
			String resourceFile = String.format(RESOURCE_FORMAT, name);
			for (Path path : systemJars.pathsForFile(resourceFile)) {
				result.add(path.toUri().toURL());
			}
		} catch (Exception e) {
			System.err.printf("Something went wrong looking for resources by name [%s]\n", name);
			e.printStackTrace();
		}
		
		return Collections.enumeration(result);
	}
}