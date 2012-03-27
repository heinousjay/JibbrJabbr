/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jj.api.Version;

/**
 * <p>
 * Entry point to start the JibbrJabbr container from the command line.
 * Behaves as the root classloader for the server.
 * Also contains some container-wide constants and util methods needed
 * to start up.
 * </p>
 * 
 * <p>
 * The direct dependencies of this class are limited to inner classes,
 * commons-daemon, the jj.api package, and the JDK.
 * </p>
 * 
 * @author Jason Miller
 */
public final class JJ {
	
	// these names get defined here and then copied in the
	// classes that manage directory layouts
	public static final String SYSTEM_BASE_PATH = "system";
	public static final String LIB_PATH = "lib";
	public static final String META_INF_PATH = "META-INF";
	
	private static final String JJ_KERNEL_CLASS = "jj.Kernel";
	
	private static final String COMMONS_DAEMON_PROCESS_ID_KEY = "commons.daemon.process.id";

	private static final Pattern JAR_URL_PARSER = 
		Pattern.compile("^jar:([^!]+)!.+$");
	
	/**
	 * Get a resource path for a given Class, for instance
	 * "/jj/JJ.class" for this class.  Arrays are unwrapped
	 * to their component type, and primitives and synthetic
	 * classes are ignored.
	 * @param clazz The class
	 * @return The resource path
	 */
	public static String resourcePath(final Class<?> clazz) {
		if (clazz.isPrimitive() || clazz.isSynthetic()) return null;
		if (clazz.isArray()) return resourcePath(clazz.getComponentType());
		return resourcePath(clazz.getName());
	}
	
	/**
	 * Get a resource path for a fully qualified class name, for
	 * instance "/jj/JJ.class" for "jj.JJ"
	 * @param className
	 * @return
	 */
	public static String resourcePath(final String className) {
		return "/" + className.replace('.', '/') + ".class";
	}
	
	/**
	 * Basic utility to turn a Class instance into its URI for
	 * resource lookup.
	 * @param clazz The Class to look up
	 * @return The URI representing that class, or null if the class cannot be
	 * 		represented as a resource (for instance if it is generated)
	 */
	public static URI uri(final Class<?> clazz) {
		try {
			return clazz.getResource(resourcePath(clazz)).toURI();
		} catch (Exception e) { 
			return null;
		}
	}
	
	/**
	 * Basic utility to get a URI to the jar that contains the
	 * resource identified by the URI, or null if the resource
	 * is not in a jar.
	 * @param uri The resource URI to locate
	 * @return The Path to the jar containing the identified resource, or null
	 * 		if the resource is not in a jar.
	 */
	public static Path jarPath(URI uri) {
		try {
			Matcher m = JAR_URL_PARSER.matcher(uri.toString());
			m.matches();
			return Paths.get(new URI(m.group(1)));
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Basic utility to get a Path to the jar the contains the file that
	 * defined the given Class.
	 * @param clazz The Class to look up
	 * @return A Path to the jar containing the class file, or null if the
	 * 		jar cannot be found (for generated classes or class files not in
	 * 		a jar, for instance.)
	 */
	public static Path jarForClass(Class<?> clazz) {
		return jarPath(uri(clazz));
	}
	
	private static final Path myJarPath = jarForClass(JJ.class);
	private static boolean initialized = false;

	public static void main(String[] args) 
		throws Exception {
		try {
			new JJ(true).init(args);
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
		}
	}
	
	private Class<?> kernelClass;
	private Object kernelInstance;
	private boolean daemonStart = System.getProperty(COMMONS_DAEMON_PROCESS_ID_KEY) != null;
	
	public JJ() {
		if (initialized) {
			throw new IllegalStateException("Already run once.");
		}
		
//		for (String key : System.getProperties().stringPropertyNames()) {
//			System.err.printf("%s = %s\n", key, System.getProperty(key));
//		}
	}
	
	private JJ(boolean internal) {
		if (daemonStart) {
			throw new IllegalStateException("Main called under a daemon");
		}
	}

	public void init(String[] args) throws Exception {
		if (initialized) {
			throw new IllegalStateException("Already run once.");
		}
		initialized = true;
		if (myJarPath != null) {
			kernelClass = new BootstrapClassLoader().loadClass(JJ_KERNEL_CLASS);
			
			if (!daemonStart && args.length == 1 && "install".equals(args[0])) {
				// we just stop here.  the class loader made the install
				return;
			}
			
		} else {
			System.out.println("⟾⟾⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾");
			System.out.println("⟾⟾ WARNING - this operation is not fully implemented.");
			System.out.println("⟾⟾ For best results, you should package this project");
			System.out.println("⟾⟾ and run from the resulting jar.");
			System.out.println("⟾⟾");
			System.out.println("⟾⟾ export $JAVA_HOME=[JDK 7 home]");
			System.out.println("⟾⟾");
			System.out.println("⟾⟾ mvn clean install");
			System.out.println("⟾⟾");
			System.out.println("⟾⟾ $JAVA_HOME/bin/java -jar server/distro/target/" + Version.name + "-" + Version.version + "-all.jar");
			System.out.println("⟾⟾");
			System.out.println("⟾⟾⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾");
			
			return;
		}
		kernelInstance = kernelClass.getConstructor(args.getClass(), Boolean.TYPE).newInstance(args, daemonStart);
	}

	public void start() throws Exception {
		kernelClass.getMethod("start").invoke(kernelInstance);
	}

	public void stop() throws Exception {
		kernelClass.getMethod("stop").invoke(kernelInstance);
	}
	
	public void destroy() {
		try {
			kernelClass.getMethod("dispose").invoke(kernelInstance);
		} catch (Exception e) {
			System.err.println("Trouble shutting down");
			e.printStackTrace();
		}
	}
	
	private static final class BasicSelfInstaller {
		
		private Path myLibPath;
		
		private void install() {
			
			try (FileSystem myJarFS = FileSystems.newFileSystem(myJarPath, null)) {
				
				// basic self installation
				// - ensure the existence of the system directory and the system/lib
				// - if necessary, copy the libs from inside the jar to the lib directory
				//   for now, always do it. properly this would require version checks
				//   maybe have Maven put the classpath entries in the manifest? that might work
				//   since all the jars are named with versions
				
				// whoever owns the installation directory is going to own
				// everything we make - if we are running as root on unix
				// we need to do this or we can't read our dependencies later
				UserPrincipal owner = Files.getOwner(myJarPath);
				
				myLibPath = Files.createDirectories(myJarPath.resolveSibling(SYSTEM_BASE_PATH).resolve(LIB_PATH));
				Files.setOwner(myLibPath.getParent(), owner);
				Files.setOwner(myLibPath, owner);
				
				try (DirectoryStream<Path> libDir = 
					Files.newDirectoryStream(myJarFS.getPath(META_INF_PATH, LIB_PATH), "*.jar")) {
					for (Path jarPath : libDir) {
						Path jar = myLibPath.resolve(jarPath.getFileName().toString());
						Files.copy(jarPath, jar, COPY_ATTRIBUTES, REPLACE_EXISTING);
						Files.setOwner(jar, owner);
					}
				}
			} catch (IOException ioe) {
				throw new IllegalStateException("Could not open", ioe);
			}
		}
		
	}
	
	private static final class BootstrapClassLoader
			extends ClassLoader{
		
		static {
			// does this speed up the start-up? exciting...
			registerAsParallelCapable();
		}
		
		private final BasicSelfInstaller installer = new BasicSelfInstaller();
	
		private BootstrapClassLoader() {
			// whatever loaded this class is the root of all classloaders in the system
			super(JJ.class.getClassLoader());
			
			// this stuff does not belong here at all
			installer.install();
			
		}
	
		private DirectoryStream<Path> libJarsStream() throws IOException {
			return Files.newDirectoryStream(installer.myLibPath, "*.jar");
		}
		
		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			try (DirectoryStream<Path> libDir = libJarsStream()) {
				for (Path jarPath : libDir) {
					try (FileSystem myJarFS = FileSystems.newFileSystem(jarPath, null)) {
						Path attempt = myJarFS.getPath("/" + name.replace('.', '/') + ".class");
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
						Path attempt = myJarFS.getPath("/" + name);
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
						Path attempt = myJarFS.getPath("/" + name);
						if (Files.exists(attempt)) {
							result.add(attempt.toUri().toURL());
						}
					}
				}
			}
			return Collections.enumeration(result);
		}
	}
}
