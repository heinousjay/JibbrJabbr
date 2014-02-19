package jj.document.servable;

import jj.JJModule;

public class DocumentServableModule extends JJModule {

	@Override
	protected void configure() {
		
		addFilterBinding().to(InlineMessagesDocumentFilter.class);
		addFilterBinding().to(ResourceUrlDocumentFilter.class);
		addFilterBinding().to(ScriptHelperDocumentFilter.class);
	}
}
