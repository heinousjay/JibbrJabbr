package jj.resource;

import static java.util.concurrent.TimeUnit.SECONDS;
import static jj.configuration.resolution.AppLocation.Base;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import jj.App;
import jj.TreeDeleter;
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

import org.junit.After;
import org.junit.Before;
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
public class ResourceSystemIntegrationTest {
	
	@Inject ResourceFinder finder;
	
	@Inject EmbeddedHttpServer server;
	
	@Inject ResourceCache resourceCache;
	
	DocumentScriptEnvironment dse;
	HtmlResource htmlResource;
	
	ModuleScriptEnvironment mse1;
	ScriptResource scriptResource1;
	
	ModuleScriptEnvironment mse2;
	ScriptResource scriptResource2;
	
	String createDirectoriesOne = "created/1/directory/structure";
	String createDirectoriesTwo = "created/1/other/structure";
	String createDirectoriesThree = "created/2/hi/there";
	
	@After
	public void after() throws Exception {
//		System.out.println(resourceCache);
//		System.out.println(reloadedCount);
//		System.out.println(killedCount);
//		System.out.println(loadedCount);
		try {
			Files.walkFileTree(Paths.get(App.one).resolve("created"), new TreeDeleter());
		} catch (NoSuchFileException nsfe) {}
	}
	
	@Rule
	public JibbrJabbrTestServer app = new JibbrJabbrTestServer(App.one)
		.withFileWatcher()
		.injectInstance(this);

	@Test
	public void testDirectoryPreloading() throws Exception {
		assertThat(finder.findResource(DirectoryResource.class, Base, ""), is(notNullValue()));
		assertThat(finder.findResource(DirectoryResource.class, Base, "chat"), is(notNullValue()));
		assertThat(finder.findResource(DirectoryResource.class, Base, "deep"), is(notNullValue()));
		assertThat(finder.findResource(DirectoryResource.class, Base, "deep/nesting"), is(notNullValue()));
		assertThat(finder.findResource(DirectoryResource.class, Base, "modules"), is(notNullValue()));
	}

	@Test
	public void testResourceWatching() throws Throwable {
		
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
		
		assertThat(finder.findResource(DirectoryResource.class, Base, createDirectoriesOne), is(nullValue()));
		assertThat(finder.findResource(DirectoryResource.class, Base, createDirectoriesTwo), is(nullValue()));
		assertThat(finder.findResource(DirectoryResource.class, Base, createDirectoriesThree), is(nullValue()));
		
		// touch a script and wait for a reload event.
		touch(scriptResource2);
		Files.createDirectories(Paths.get(App.one).resolve(createDirectoriesOne));
		Files.createDirectories(Paths.get(App.one).resolve(createDirectoriesTwo));
		Files.createDirectories(Paths.get(App.one).resolve(createDirectoriesThree));

		// on the Mac, you will wait a while..., so if it's time to
		// make this test more things, try to only have this
		// single wait point!
		
		// wait count is 9 for the directories created above
		// + 4 kills from scriptResource2, mse2, mse1, and dse
		// + 1 reload of dse.  note that scriptResource1 and
		// html resource are left alone in the tree
		
		assertTrue(waitForCount(9 + 4 + 1));
		
		assertFalse(scriptResource2.alive());
		assertFalse(mse2.alive());
		assertFalse(mse1.alive());
		assertFalse(dse.alive());

		assertTrue(scriptResource1.alive());
		assertTrue(htmlResource.alive());
		
		assertThat(dse, is(not(sameInstance(
			finder.findResource(DocumentScriptEnvironment.class, AppLocation.Virtual, "deep/nested")
		))));
		assertThat(mse1, is(not(sameInstance(
			finder.findResource(ModuleScriptEnvironment.class, AppLocation.Virtual, "deep/module", new RequiredModule(dse, "deep/module"))
		))));
		assertThat(mse2, is(not(sameInstance(
			finder.findResource(ModuleScriptEnvironment.class, AppLocation.Virtual, "deep/nesting/module", new RequiredModule(dse, "deep/nesting/module"))
		))));
		
		assertThat(finder.findResource(DirectoryResource.class, Base, createDirectoriesOne), is(notNullValue()));
		assertThat(finder.findResource(DirectoryResource.class, Base, createDirectoriesTwo), is(notNullValue()));
		assertThat(finder.findResource(DirectoryResource.class, Base, createDirectoriesThree), is(notNullValue()));
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
	// also tests out the events indirectly
	
	boolean countEvents = false;
	CountDownLatch latch;
	AtomicInteger reloadedCount;
	AtomicInteger killedCount;
	AtomicInteger loadedCount;
	
	@Before
	public void before() {
		reloadedCount = new AtomicInteger();
		killedCount = new AtomicInteger();
		loadedCount = new AtomicInteger();
	}
	
	@Listener
	void resourceReloaded(ResourceReloaded event) {
		reloadedCount.incrementAndGet();
		if (countEvents) {
			latch.countDown();
		} 
	}
	
	@Listener
	void resourceKilled(ResourceKilled event) {
		killedCount.incrementAndGet();
		if (countEvents) {
			latch.countDown();
		}
	}
	
	@Listener
	void resourceLoaded(ResourceLoaded event) {
		if (loadedCount != null) loadedCount.incrementAndGet();
		if (countEvents) {
			latch.countDown();
		}
	}
	
	private boolean waitForCount(int count) throws Exception {
		latch = new CountDownLatch(count);
		countEvents = true;
		return latch.await(11, SECONDS);
	}
}