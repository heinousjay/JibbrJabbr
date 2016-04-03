package jj.server;

import static jj.application.AppLocation.AppBase;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.resource.MockAbstractResourceDependencies;

import org.junit.Ignore;
import org.junit.Test;

public class ModuleResourceTest {

	MockAbstractResourceDependencies dependencies;
	
	@Ignore
	@Test
	public void test() throws Exception {
		
		Path path = Paths.get("");
		
		new ModuleResource(dependencies = new MockAbstractResourceDependencies(ModuleResource.class, AppBase, "name"), path);
	}

}
