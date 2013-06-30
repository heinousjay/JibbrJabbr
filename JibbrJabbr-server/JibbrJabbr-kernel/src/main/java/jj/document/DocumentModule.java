package jj.document;

import jj.JJModule;

import com.google.inject.multibindings.Multibinder;



public class DocumentModule extends JJModule {

	@Override
	protected void configure() {
		Multibinder<DocumentFilter> filters = Multibinder.newSetBinder(binder(), DocumentFilter.class);
		
		filters.addBinding().to(InlineMessagesDocumentFilter.class);
		filters.addBinding().to(ResourceUrlDocumentFilter.class);
		filters.addBinding().to(ScriptHelperDocumentFilter.class);
	}
}
