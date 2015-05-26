package jj.system;

import static jj.system.ServerLocation.Virtual;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Arguments;
import jj.resource.Location;
import jj.resource.LocationResolver;

@Singleton
public class Server implements LocationResolver {

	@Inject
	Server(Arguments arguments) {
		// figure out the jar we're in? no. we'd want the capsule
		String dir = arguments.get("server-root", String.class, System.getProperty("user.dir"));
		Path path = Paths.get(dir);
		
		System.out.println(path);
	}
	
	@Override
	public Location base() {
		return ServerLocation.Root;
	}

	@Override
	public Path path() {
		
		return null;
	}

	@Override
	public boolean pathInBase(Path path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Path resolvePath(Location base, String name) {
		assert base == Virtual;
		return null;
	}

}
