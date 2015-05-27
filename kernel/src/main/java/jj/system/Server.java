package jj.system;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJ;
import jj.configuration.Arguments;
import jj.resource.Location;
import jj.resource.LocationResolver;

@Singleton
public class Server implements LocationResolver {
	
	private final Path rootPath;

	@Inject
	Server(Arguments arguments) {
		Path myJar = JJ.jarForClass(JJ.class); // need to account for capsule? probably
		Path defaultPath = myJar == null ? Paths.get(System.getProperty("user.dir")) : myJar.getParent();
		rootPath = arguments.get("server-root", Path.class, defaultPath);
		assert rootPath != null;
	}
	
	@Override
	public Location base() {
		return ServerLocation.Root;
	}

	@Override
	public Path path() {
		return rootPath;
	}

	@Override
	public boolean pathInBase(Path path) {
		return path.startsWith(rootPath);
	}

	@Override
	public Path resolvePath(Location base, String name) {
		assert base instanceof ServerLocation;
		switch ((ServerLocation)base) {
		case Root:
		case Modules:
		case Virtual:
			return null;
		}
		
		throw new AssertionError();
	}

}
