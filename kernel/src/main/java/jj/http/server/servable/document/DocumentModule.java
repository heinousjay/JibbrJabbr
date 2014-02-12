package jj.http.server.servable.document;

import jj.JJModule;

public class DocumentModule extends JJModule {

	@Override
	protected void configure() {
		
		addFilterBinding().to(InlineMessagesDocumentFilter.class);
		addFilterBinding().to(ResourceUrlDocumentFilter.class);
		addFilterBinding().to(ScriptHelperDocumentFilter.class);
	}
}
