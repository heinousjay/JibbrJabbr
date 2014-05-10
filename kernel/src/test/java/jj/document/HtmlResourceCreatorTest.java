package jj.document;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

import jj.configuration.resolution.AppLocation;
import jj.document.HtmlResource;
import jj.document.HtmlResourceCreator;
import jj.document.servable.DocumentConfiguration;
import jj.resource.AbstractResource.Dependencies;
import jj.resource.ResourceBase;

public class HtmlResourceCreatorTest extends ResourceBase<HtmlResource, HtmlResourceCreator> {

	@Override
	protected String name() {
		return "index.html";
	}
	
	protected Path path() {
		return appPath.resolve(name());
	}
	
	@Override
	protected HtmlResource resource() throws Exception {
		given(configuration.get(DocumentConfiguration.class)).willReturn(new DocumentConfiguration() {
			
			@Override
			public boolean showParsingErrors() {
				return false;
			}
			
			@Override
			public boolean removeComments() {
				return true;
			}
			
			@Override
			public boolean clientDebug() {
				return false;
			}
		});
		
		return new HtmlResource(configuration, logger, new Dependencies(cacheKey(), AppLocation.Base), name(), path());
	}
	
	@Override
	protected void resourceAssertions(HtmlResource resource) throws Exception {
		final AtomicInteger count = new AtomicInteger(0);
		resource.document().traverse(new NodeVisitor() {
			
			@Override
			public void tail(Node node, int depth) {
				if (node instanceof Comment) {
					count.incrementAndGet();
				}
			}
			
			@Override
			public void head(Node node, int depth) {
				// TODO Auto-generated method stub
				
			}
		});
		
		assertThat(count.get(), is(0));
	}
	
	@Override
	protected HtmlResourceCreator toTest() {
		return new HtmlResourceCreator(app, creator);
	}
}
