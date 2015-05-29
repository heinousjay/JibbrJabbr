package jj.server;

import static jj.application.AppLocation.AppBase;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.resource.MockAbstractResourceDependencies;
import jj.server.ModuleResource;

import org.junit.Ignore;
import org.junit.Test;

public class ModuleResourceTest {

	MockAbstractResourceDependencies dependencies;
	
	@Ignore
	@Test
	public void test() throws Exception {
		
		Path path = Paths.get("");
		
		new ModuleResource(dependencies = new MockAbstractResourceDependencies(AppBase, "name"), path);
	}

}
