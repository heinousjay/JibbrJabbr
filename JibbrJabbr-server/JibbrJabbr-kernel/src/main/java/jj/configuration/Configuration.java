package jj.configuration;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * The central point of configuration for the system
 * @author jason
 *
 */
@Singleton
public class Configuration {
	
	private final URI baseUri;
	private final Path basePath;
	
	@Inject
	Configuration(final String[] args) {
		
		Set<String> argSet = new HashSet<>(Arrays.asList(args));
		
		baseUri = baseUri(argSet);
		basePath = basePath(argSet);
		validate(args);
	}
	
	// exactly one arg should be a URI of the form http://domain:portIfNotStandard/
	// we don't support more than that yet
	// for now we'll default to localhost:8080
	private URI baseUri(final Set<String> args) {
		URI result = null;
		for (String arg : args) {
			if (arg.startsWith("http://")) {
				try {
					result = new URI(arg);
					break;
				} catch (Exception e) {}
			}
		}
		if (result != null) {
			args.remove(result.toString());
		} else {
			result = URI.create("http://localhost:8080/");
		}
		
		return result;
	}
	
	// exactly one arg MUST be a path to the directory
	// containing what we serve
	private Path basePath(final Set<String> args) {
		Path result = null;
		for (String arg : args) {
			try {
				Path candidate = Paths.get(arg);
				if (Files.isDirectory(candidate, LinkOption.NOFOLLOW_LINKS)) {
					result = candidate;
				}
			} catch (Exception e) {}
		}
		if (result != null) {
			args.remove(result.toString());
		}
		return result;
	}
	
	
	
	private void validate(final String[] args) {
		
	}
	
	/**
	 * The base URI of the server, e.g. http://localhost:8080/
	 * @return
	 */
	public URI baseUri() {
		return baseUri;
	}
	
	/**
	 * the base path from which we serve.
	 * @return
	 */
	public Path basePath() {
		return basePath;
	}
	
	/**
	 * Flag indicating that the client should be in debug mode, which
	 * will log internal info to the script console
	 * @return
	 */
	public boolean debugClient() {
		return false;
	}
}
