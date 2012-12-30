package jj.request;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;

import jj.JJExecutors;
import jj.document.DocumentFilter;
import jj.document.DocumentRequest;
import jj.document.DocumentRequestProcessorImpl;
import jj.resource.Resource;
import jj.script.ScriptExecutorFactory;
import jj.webbit.JJHttpRequest;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jmock.lib.concurrent.DeterministicScheduler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.webbitserver.stub.StubHttpControl;
import org.webbitserver.stub.StubHttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class DocumentRequestProcessorImplTest {
	
	Document document;
	String baseName;

	DeterministicScheduler executor;
	@Mock ScriptExecutorFactory scriptExecutorFactory;
	@Mock JJExecutors executors;

	@Mock Resource htmlResource;
	
	@Mock JJHttpRequest httpRequest;
	StubHttpResponse httpResponse;
	StubHttpControl httpControl;

	DocumentRequest documentRequest;
	
	int filterCalls;
	
	class TestDocumentFilter implements DocumentFilter {

		private boolean io;
		private int sequence;
		
		TestDocumentFilter(boolean io, int sequence) {
			this.io = io;
			this.sequence = sequence;
		}
		
		@Override
		public boolean needsIO(DocumentRequest documentRequest) {
			return io;
		}

		@Override
		public void filter(DocumentRequest documentRequest) {
			assertThat(++filterCalls, is(sequence));
			assertTrue("not correctly sequencing calls or switching to IO thread", io == executors.isIOThread());
		}
	}
	
	private static final String MIME = "mime-type";
	
	@Before
	public void before() {
		filterCalls = 0;
		
		document = Jsoup.parse("<html><head><title>what</title></head><body></body></html>");
		baseName = "baseName";
		executor = new DeterministicScheduler();
		
		when(executors.ioExecutor()).thenReturn(executor);
		when(executors.scriptExecutorFor(baseName)).thenReturn(executor);

		when(htmlResource.baseName()).thenReturn(baseName);
		when(htmlResource.mime()).thenReturn(MIME);
		
		when(httpRequest.wallTime()).thenReturn(new BigDecimal(0));
		
		httpResponse = new StubHttpResponse();
		httpControl = new StubHttpControl();
		
		documentRequest = new DocumentRequest(htmlResource, document, httpRequest, httpResponse, httpControl);
	}

	@Test
	public void testIOFilterBehavior() {

		// given
		DocumentFilter[] filters = {
			new TestDocumentFilter(true, 4),
			new TestDocumentFilter(true, 5),
			new TestDocumentFilter(false, 1),
			new TestDocumentFilter(false, 2),
			new TestDocumentFilter(true, 6),
			new TestDocumentFilter(false, 3)
		};
		given(executors.isIOThread())
			.willReturn(false)
			.willReturn(false)
			.willReturn(false)
			.willReturn(true)
			.willReturn(true)
			.willReturn(true);
		
		DocumentRequestProcessorImpl toTest = 
			new DocumentRequestProcessorImpl(executors, documentRequest, filters);
		
		// when
		toTest.respond();
		executor.runUntilIdle();
		
		// then
		assertThat(filterCalls, is(6));
		
		// + assertions in TestDocumentFilter
		
	}
	
	@Test
	public void testWritesDocumentCorrectly() {
		// given
		DocumentRequestProcessorImpl toTest = 
				new DocumentRequestProcessorImpl(executors, documentRequest, new DocumentFilter[0]);
		
		// when
		toTest.respond();
		executor.runUntilIdle();
		
		// then
		assertThat(httpResponse.charset(), is(UTF_8));
		assertThat(httpResponse.header(HttpHeaders.Names.CACHE_CONTROL), is (HttpHeaders.Values.NO_STORE));
		assertThat(httpResponse.header(HttpHeaders.Names.CONTENT_TYPE), is(MIME));
		assertThat(httpResponse.contentsString(), is(document.toString()));
	}

}
