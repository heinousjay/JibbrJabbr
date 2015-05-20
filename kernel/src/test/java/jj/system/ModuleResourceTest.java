package jj.system;

import static jj.application.AppLocation.Base;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.resource.MockAbstractResourceDependencies;
import jj.system.ModuleResource;

import org.junit.Ignore;
import org.junit.Test;

public class ModuleResourceTest {

	MockAbstractResourceDependencies dependencies;
	
	@Ignore
	@Test
	public void test() throws Exception {
		
		Path path = Paths.get("");
		
		new ModuleResource(dependencies = new MockAbstractResourceDependencies(Base, "name"), path);
	}

}
