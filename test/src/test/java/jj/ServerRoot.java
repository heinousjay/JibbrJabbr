package jj;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerRoot {

	public static final Path one;
	
	static {
		try {
			one = getPath("/test-roots/root1/");
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private static Path getPath(String p) throws URISyntaxException {
		return Paths.get(ServerRoot.class.getResource(p).toURI()).toAbsolutePath();
	}
}
