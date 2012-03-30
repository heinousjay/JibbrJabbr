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


import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	static final Path myJarPath = jarForClass(JJ.class);
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
	
	private boolean processBootstrapArgs(String[] args) {
		// we install regardless
		if (!daemonStart && args.length == 1 && "install".equals(args[0])) {
			System.out.println("⟾⟾⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾");
			System.out.println("⟾⟾");
			System.out.println("⟾⟾ JibbrJabbr installed to: ");
			System.out.println("⟾⟾ " + installer.basePath);
			System.out.println("⟾⟾");
			System.out.println("⟾⟾⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾");
			// we just stop here having performed the installation
			return true;
		}
		
		if (args.length == 1 && "env".equals(args[0])) {
			System.out.println("ENVIRONMENT:");
			for (String key : System.getenv().keySet()) {
				System.out.printf("ENV[%s] = [%s]\n", key, System.getenv(key));
			}
			System.out.println("SYSTEM PROPERTIES:");
			for (Object keyObj : System.getProperties().keySet()) {
				String key = (String)keyObj;
				System.out.printf("%s = [%s]\n", key, System.getProperty(key));
			}
			return true;
		}
		
		return false;
	}
	
	private BootstrapInstaller installer;

	public void init(String[] args) throws Exception {
		if (initialized) {
			throw new IllegalStateException("Already run once.");
		}
		initialized = true;
		if (myJarPath != null) {
			
			installer = new BootstrapInstaller(myJarPath);
			if (processBootstrapArgs(args) && !daemonStart) return;

			kernelClass = new BootstrapClassLoader(installer.libPath).loadClass(JJ_KERNEL_CLASS);
			
		} else {
			System.out.println("⟾⟾⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾ ⟾⟾");
			System.out.println("⟾⟾");
			System.out.println("⟾⟾ WARNING - this operation is not fully implemented.");
			System.out.println("⟾⟾ For best results, you should package this project");
			System.out.println("⟾⟾ and run from the resulting jar.");
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
}
