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
	
	private final Assets assets;
	
	private final APIModules apiModules;

	@Inject
	Server(Arguments arguments, Assets assets, APIModules apiModules) {
		Path myJar = JJ.jarForClass(JJ.class); // need to account for capsule? probably
		Path defaultPath = myJar == null ? Paths.get(System.getProperty("user.dir")) : myJar.getParent();
		rootPath = arguments.get("server-root", Path.class, defaultPath);
		assert rootPath != null;
		
		this.assets = assets;
		this.apiModules = apiModules;
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

		case Assets:
			return assets.path(name);
		case APIModules:
			return apiModules.path(name);
			
		case Root:
		case Modules:
		case Virtual:
			return null;
		}
		
		throw new AssertionError();
	}

}
