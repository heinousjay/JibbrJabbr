package jj.resource;

import static java.util.concurrent.TimeUnit.SECONDS;
import static jj.application.AppLocation.*;
import static jj.system.ServerLocation.Virtual;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import jj.App;
import jj.TreeDeleter;
import jj.document.DocumentScriptEnvironment;
import jj.document.HtmlResource;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.http.server.EmbeddedHttpRequest;
import jj.http.server.EmbeddedHttpServer;
import jj.script.module.JSONResource;
import jj.script.module.ModuleScriptEnvironment;
import jj.script.module.RequiredModule;
import jj.script.module.ScriptResource;
import jj.testing.JibbrJabbrTestServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Validates the various complex behaviors of the resource system
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
	
	ModuleScriptEnvironment mse3;
	JSONResource jsonResource1;
	
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
			Files.walkFileTree(Paths.get(App.module).resolve("created"), new TreeDeleter());
		} catch (NoSuchFileException nsfe) {}
	}
	
	@Rule
	public JibbrJabbrTestServer app = new JibbrJabbrTestServer(App.module)
		.withFileWatcher()
		.injectInstance(this);

	// done as a single test because at some point, the document system will be split off
	// and for now, the repl is a simple way to cause things to happen via the loader
	@Test
	public void testResourceSystem() throws Throwable {
		
		// validates that directory structures are created as expected
		DirectoryResource root = finder.findResource(DirectoryResource.class, Base, "");
		DirectoryResource deep = finder.findResource(DirectoryResource.class, Base, "deep");
		DirectoryResource nesting = finder.findResource(DirectoryResource.class, Base, "deep/nesting");
		assertThat(root, is(notNullValue()));
		assertThat(deep, is(notNullValue()));
		assertThat(nesting, is(notNullValue()));
		assertTrue(root.dependents().contains(deep));
		assertTrue(deep.dependents().contains(nesting));
		
		assertThat(server.request(new EmbeddedHttpRequest("deep/nested")).await(1, SECONDS).status().code(), is(200));
		
		dse = finder.findResource(DocumentScriptEnvironment.class, Virtual, "deep/nested");
		htmlResource = finder.findResource(HtmlResource.class, Base, "deep/nested.html");
		assertTrue(deep.dependents().contains(htmlResource));
		
		mse1 = finder.findResource(ModuleScriptEnvironment.class, Virtual, "deep/module", new RequiredModule(dse, "deep/module"));
		scriptResource1 = finder.findResource(ScriptResource.class, Base, "deep/module.js");
		assertTrue(deep.dependents().contains(scriptResource1));
		assertTrue(((AbstractResource)dse).dependents().contains(mse1));
		
		mse2 = finder.findResource(ModuleScriptEnvironment.class, Virtual, "deep/nesting/module", new RequiredModule(dse, "deep/nesting/module"));
		scriptResource2 = finder.findResource(ScriptResource.class, Base, "deep/nesting/module.js");
		assertTrue(nesting.dependents().contains(scriptResource2));
		assertTrue(((AbstractResource)dse).dependents().contains(mse2));
		
		mse3 = finder.findResource(ModuleScriptEnvironment.class, Virtual, "deep/nesting/values", new RequiredModule(dse, "deep/nesting/values"));
		jsonResource1 = finder.findResource(JSONResource.class, Base, "deep/nesting/values.json");
		assertTrue(nesting.dependents().contains(jsonResource1));
		assertTrue(((AbstractResource)dse).dependents().contains(mse3));
		
		assertTrue(dse.alive());
		assertTrue(htmlResource.alive());
		assertTrue(mse1.alive());
		assertTrue(scriptResource1.alive());
		assertTrue(mse2.alive());
		assertTrue(scriptResource2.alive());
		assertTrue(mse3.alive());
		assertTrue(jsonResource1.alive());
		
		assertThat(finder.findResource(DirectoryResource.class, Base, createDirectoriesOne), is(nullValue()));
		assertThat(finder.findResource(DirectoryResource.class, Base, createDirectoriesTwo), is(nullValue()));
		assertThat(finder.findResource(DirectoryResource.class, Base, createDirectoriesThree), is(nullValue()));
		
		final AtomicBoolean failed = new AtomicBoolean();
		
		new Thread() {
			
			public void run() {
				try {
					// touch a script and wait for a reload event.
					touch(scriptResource2);
					// and let's add some directories
					Files.createDirectories(Paths.get(App.module).resolve(createDirectoriesOne));
					Files.createDirectories(Paths.get(App.module).resolve(createDirectoriesTwo));
					Files.createDirectories(Paths.get(App.module).resolve(createDirectoriesThree));
				} catch (Exception e) {
					e.printStackTrace();
					failed.set(true);
				}
			}
		}.start();

		// on the Mac, you will wait a while..., so if it's time to
		// make this test more things, try to only have this
		// single wait point!
		
		// wait count is 9 for the directories created above
		// + 5 kills from scriptResource2, mse3, mse2, mse1, and dse
		// + 1 reload of dse.  note that scriptResource1 and jsonResource1 and
		// htmlResource are left alone in the cache, and that the
		// modules don't load again without another request
		
		assertTrue("timed out", waitForCount(9 + 5 + 1));
		assertFalse("couldn't update things correctly", failed.get());
		
		assertFalse(scriptResource2.alive());
		// should be removed upon death
		assertFalse(nesting.dependents().contains(scriptResource2));
		assertFalse(mse3.alive());
		assertFalse(mse2.alive());
		assertFalse(mse1.alive());
		assertFalse(dse.alive());

		assertTrue(jsonResource1.alive());
		assertTrue(scriptResource1.alive());
		assertTrue(htmlResource.alive());
		
		assertThat(
			finder.findResource(DocumentScriptEnvironment.class, Virtual, "deep/nested"),
			is(notNullValue())
		);
		
		assertThat(dse, is(not(sameInstance(
			finder.findResource(DocumentScriptEnvironment.class, Virtual, "deep/nested")
		))));
		
		assertThat(
			finder.findResource(ModuleScriptEnvironment.class, Virtual, "deep/module", new RequiredModule(dse, "deep/module")),
			is(nullValue())
		);
		assertThat(
			finder.findResource(ModuleScriptEnvironment.class, Virtual, "deep/nesting/module", new RequiredModule(dse, "deep/nesting/module")),
			is(nullValue())
		);
		assertThat(
			finder.findResource(ModuleScriptEnvironment.class, Virtual, "deep/nesting/values", new RequiredModule(dse, "deep/nesting/values")),
			is(nullValue())
		);
		
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
