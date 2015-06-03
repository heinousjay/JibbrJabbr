package jj.server;

import static jj.server.ServerLocation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

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

	private final APISpecs apiSpecs;
	
	@Inject
	Server(Arguments arguments, Assets assets, APIModules apiModules, APISpecs apiSpecs) {
		Path myJar = JJ.jarForClass(JJ.class); // need to account for capsule? probably
		Path defaultPath = myJar == null ? Paths.get(System.getProperty("user.dir")) : myJar.getParent();
		rootPath = arguments.get("server-root", Path.class, defaultPath);
		assert rootPath != null;
		
		this.assets = assets;
		this.apiModules = apiModules;
		this.apiSpecs = apiSpecs;
	}

	public Path resolvePath(Location base) {
		return resolvePath(base, "");
	}
	
	@Override
	public Location resolveBase(Path path) {
		assert path != null;
		path = path.normalize().toAbsolutePath();
		return path.startsWith(rootPath) && rootPath.relativize(path).startsWith("modules") ? ServerLocation.Modules : null;
	}

	@Override
	public Path resolvePath(Location base, String name) {
		assert base instanceof ServerLocation;
		switch ((ServerLocation)base) {

		case Assets:
			return assets.path(name);
			
		case APIModules:
			return apiModules.path(name);
			
		case APISpecs:
			return apiSpecs.path(name);
			
		case Root:
			return rootPath.resolve(name);
			
		case Modules:
			return rootPath.resolve("modules").resolve(name);
			
		case Virtual:
			return null;
		}
		
		throw new AssertionError();
	}

	@Override
	public List<Location> watchedLocations() {
		return Collections.singletonList(Modules);
	}
	
	@Override
	public Location specLocationFor(Location base) {
		return base == APIModules ? APISpecs : null;
	}
}
