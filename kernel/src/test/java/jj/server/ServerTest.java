package jj.server;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.configuration.Arguments;
import jj.server.APIModules;
import jj.server.Assets;
import jj.server.Server;
import jj.server.ServerLocation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServerTest {

	@Mock Arguments arguments;
	@Mock Assets assets;
	@Mock APIModules apiModules;
	
	Server server;

	@Test
	public void test() {
		Path path = Paths.get("server-root-path");
		given(arguments.get(eq("server-root"), eq(Path.class), any(Path.class))).willReturn(path);
		
		server = new Server(arguments, assets, apiModules);
		
		assertThat(server.path(), is(path));
		
		// asset resources get delegated to Assets
		server.resolvePath(ServerLocation.Assets, "a");
		verify(assets).path("a");
		
		// api module resources get delegated to APIModules
		server.resolvePath(ServerLocation.APIModules, "b");
		verify(apiModules).path("b");
	}


}
