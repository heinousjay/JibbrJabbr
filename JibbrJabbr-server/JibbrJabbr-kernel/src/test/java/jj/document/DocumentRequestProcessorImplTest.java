package jj.document;


import static jj.MockJJExecutors.ThreadType.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import jj.MockJJExecutors;
import jj.ScriptExecutorFactory;
import jj.document.DocumentFilter;
import jj.document.DocumentRequest;
import jj.document.DocumentRequestProcessorImpl;
import jj.resource.Resource;
import jj.webbit.JJHttpRequest;

import org.jboss.netty.handler.codec.http.HttpHeaders;
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

	@Mock ScriptExecutorFactory scriptExecutorFactory;
	MockJJExecutors executors;

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
		}
	}
	
	private static final String MIME = "mime-type";
	
	@Before
	public void before() {
		filterCalls = 0;
		
		document = Jsoup.parse("<html><head><title>what</title></head><body></body></html>");
		document.outputSettings().prettyPrint(false);
		baseName = "baseName";

		executors = new MockJJExecutors();
		
		when(htmlResource.baseName()).thenReturn(baseName);
		when(htmlResource.mime()).thenReturn(MIME);
		
		when(httpRequest.wallTime()).thenReturn(new BigDecimal(0));
		
		httpResponse = new StubHttpResponse();
		httpControl = new StubHttpControl();
		
		documentRequest = new DocumentRequest(htmlResource, document, httpRequest, httpResponse, httpControl, false);
	}

	@Test
	public void testIOFilterBehavior() {

		// given
		Set<DocumentFilter> filters = new LinkedHashSet<>();
		filters.add(new TestDocumentFilter(true, 4));
		filters.add(new TestDocumentFilter(true, 5));
		filters.add(new TestDocumentFilter(false, 1));
		filters.add(new TestDocumentFilter(false, 2));
		filters.add(new TestDocumentFilter(true, 6));
		filters.add(new TestDocumentFilter(false, 3));
		
		executors.addThreadTypes(ScriptThread, 4);
		executors.addThreadTypes(IOThread, 2);
		
		DocumentRequestProcessorImpl toTest = 
			new DocumentRequestProcessorImpl(executors, documentRequest, filters);
		
		// when
		toTest.respond();
		executors.executor.runUntilIdle();
		
		// then
		assertThat(filterCalls, is(6));
		// TODO can make this smarter later
	}
	
	@Test
	public void testWritesDocumentCorrectly() {
		// given
		DocumentRequestProcessorImpl toTest = 
				new DocumentRequestProcessorImpl(executors, documentRequest, Collections.<DocumentFilter>emptySet());
		
		executors.isScriptThread = true;
		
		// when
		toTest.respond();
		executors.executor.runUntilIdle();
		
		// then
		assertThat(httpResponse.charset(), is(UTF_8));
		assertThat(httpResponse.header(HttpHeaders.Names.CACHE_CONTROL), is (HttpHeaders.Values.NO_STORE));
		assertThat(httpResponse.header(HttpHeaders.Names.CONTENT_TYPE), is(MIME));
		assertThat(httpResponse.contentsString(), is(document.toString()));
	}

}
