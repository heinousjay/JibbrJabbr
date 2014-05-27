package jj.resource;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import jj.App;
import jj.configuration.resolution.AppLocation;
import jj.document.DocumentScriptEnvironment;
import jj.document.HtmlResource;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.http.server.EmbeddedHttpRequest;
import jj.http.server.EmbeddedHttpServer;
import jj.script.module.ModuleScriptEnvironment;
import jj.script.module.RequiredModule;
import jj.script.module.ScriptResource;
import jj.testing.JibbrJabbrTestServer;

import org.junit.Rule;
import org.junit.Test;

/**
 * I would like to ramp this up a bit, thinking along the lines of providing a
 * small abstract class that can act as a container and expose a wait point, so i can
 * keep it to the dang 10+ seconds but not exceed it
 * @author jason
 *
 */
@Subscriber
public class ResourceWatchServiceIntegrationTest {
	
	@Inject ResourceFinder finder;
	
	@Inject EmbeddedHttpServer server;
	
	DocumentScriptEnvironment dse;
	HtmlResource htmlResource;
	
	ModuleScriptEnvironment mse1;
	ScriptResource scriptResource1;
	
	ModuleScriptEnvironment mse2;
	ScriptResource scriptResource2;
	
	@Rule
	public JibbrJabbrTestServer app = 
		new JibbrJabbrTestServer(App.one)
		.withFileWatcher()
		.injectInstance(this);
	

	@Test
	public void test() throws Throwable {
		
		assertThat(server.request(new EmbeddedHttpRequest("deep/nested")).await(1, SECONDS).status().code(), is(200));

		dse = finder.findResource(DocumentScriptEnvironment.class, AppLocation.Virtual, "deep/nested");
		htmlResource = finder.findResource(HtmlResource.class, AppLocation.Base, "deep/nested.html");
		
		mse1 = finder.findResource(ModuleScriptEnvironment.class, AppLocation.Virtual, "deep/module", new RequiredModule(dse, "deep/module"));
		scriptResource1 = finder.findResource(ScriptResource.class, AppLocation.Base, "deep/module.js");
		
		mse2 = finder.findResource(ModuleScriptEnvironment.class, AppLocation.Virtual, "deep/nesting/module", new RequiredModule(dse, "deep/nesting/module"));
		scriptResource2 = finder.findResource(ScriptResource.class, AppLocation.Base, "deep/nesting/module.js");
		
		assertTrue(dse.alive());
		assertTrue(htmlResource.alive());
		assertTrue(mse1.alive());
		assertTrue(scriptResource1.alive());
		assertTrue(mse2.alive());
		assertTrue(scriptResource2.alive());
		
		// touch a script and wait for a reload event.
		touch(scriptResource2);

		// on the Mac, you will wait a while..., so if it's time to make this test more things, try to only have this
		// single wait point!
		assertTrue(waitForResource(dse));
		
		assertFalse(scriptResource2.alive());
		assertFalse(mse2.alive());
		assertFalse(mse1.alive());
		assertFalse(dse.alive());

		assertTrue(scriptResource1.alive());
		assertTrue(htmlResource.alive());
		
		assertThat(dse, is(not(sameInstance(finder.findResource(DocumentScriptEnvironment.class, AppLocation.Virtual, "deep/nested")))));
		assertThat(mse1, is(not(sameInstance(finder.findResource(ModuleScriptEnvironment.class, AppLocation.Virtual, "deep/module", new RequiredModule(dse, "deep/module"))))));
		assertThat(mse2, is(not(sameInstance(finder.findResource(ModuleScriptEnvironment.class, AppLocation.Virtual, "deep/nesting/module", new RequiredModule(dse, "deep/nesting/module"))))));
	}
	
	// also need a delete test!
	private void touch(FileResource resource) throws Exception {
		FileTime originalFileTime = Files.getLastModifiedTime(resource.path());
		FileTime newFileTime;
		do {
			newFileTime = FileTime.fromMillis(System.currentTimeMillis());
		} while (newFileTime.compareTo(originalFileTime) < 1);
		
		Files.setLastModifiedTime(resource.path(), newFileTime);
	}
	
	// small strictly ordered waiting mechanism
	
	AbstractResource waiting;
	CountDownLatch latch;
	
	@Listener
	void resourceLoaded(ResourceReloaded event) {
		if (waiting != null && event.matches(waiting)) {
			latch.countDown();
			waiting = null;
			latch = null;
		}
	}
	
	private boolean waitForResource(AbstractResource resource) throws Exception {
		latch = new CountDownLatch(1);
		waiting = resource;
		return latch.await(11, SECONDS);
	}
}
