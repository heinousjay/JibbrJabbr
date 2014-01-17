package jj;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;

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
	private static final Permissions ALL_PERMISSIONS = new Permissions();
	
	static {
		registerAsParallelCapable();
		
		ALL_PERMISSIONS.add(new AllPermission());
		ALL_PERMISSIONS.setReadOnly();
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
				definePackageIfNeeded(name, classFile);
				CodeSource codeSource = systemJars.codeSourceForFile(classFile);
				ProtectionDomain protectionDomain = new ProtectionDomain(codeSource, ALL_PERMISSIONS);
				return defineClass(name, classBytes, 0, classBytes.length, protectionDomain);
			}
			
		} catch (Exception e) {
			System.err.printf("Something went wrong reading a class [%s]\n", name);
			e.printStackTrace();
			throw new ClassNotFoundException(name, e);
		}
		
		throw new ClassNotFoundException(name);
	}
	
	private void definePackageIfNeeded(String name, String classFile) throws Exception {
		String packageName = name.substring(0, name.lastIndexOf('.'));
		if (getPackage(packageName) == null) {
			Manifest manifest = systemJars.jarManifestForFile(classFile);
			doDefinePackage(packageName, manifest);
		}
	}
	
	private Package doDefinePackage(String packageName, Manifest manifest) {
		
		String specTitle = null,
			specVersion = null,
			specVendor = null,
			implTitle = null,
			implVersion = null,
			implVendor = null;
		
		if (manifest != null) {
			Attributes attributes = manifest.getAttributes(packageName.replace('.', '/').concat("/"));
			if (attributes != null) {
				specTitle = attributes.getValue(Name.SPECIFICATION_TITLE);
				specVersion = attributes.getValue(Name.SPECIFICATION_VERSION);
				specVendor = attributes.getValue(Name.SPECIFICATION_VENDOR);
				implTitle = attributes.getValue(Name.IMPLEMENTATION_TITLE);
				implVersion = attributes.getValue(Name.IMPLEMENTATION_VERSION);
				implVendor = attributes.getValue(Name.IMPLEMENTATION_VENDOR);
			}
			
			attributes = manifest.getMainAttributes();
			if (attributes != null) {
				specTitle = specTitle == null ? attributes.getValue(Name.SPECIFICATION_TITLE) : specTitle;
				specVersion = specVersion == null ? attributes.getValue(Name.SPECIFICATION_VERSION) : specVersion;
				specVendor = specVendor == null ? attributes.getValue(Name.SPECIFICATION_VENDOR) : specVendor;
				implTitle = implTitle == null ? attributes.getValue(Name.IMPLEMENTATION_TITLE) : implTitle;
				implVersion = implVersion == null ? attributes.getValue(Name.IMPLEMENTATION_VERSION) : implVersion;
				implVendor = implVendor == null ? attributes.getValue(Name.IMPLEMENTATION_VENDOR) : implVendor;
			}
		}
		
		return definePackage(packageName, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, null);
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