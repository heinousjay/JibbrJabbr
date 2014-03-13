package jj.document.servable;

import com.google.inject.multibindings.Multibinder;

import jj.JJModule;

public class DocumentServableModule extends JJModule {
	
	// TO DO - expose this as a binder.  gotta think on how

	private Multibinder<DocumentFilter> filters;
	
	private void addFilterBindingTo(Class<? extends DocumentFilter> filter) {
		if (filters == null) {
			filters = Multibinder.newSetBinder(binder(), DocumentFilter.class);
		}
		filters.addBinding().to(filter);
	}

	@Override
	protected void configure() {
		
		addFilterBindingTo(InlineMessagesDocumentFilter.class);
		addFilterBindingTo(ResourceUrlDocumentFilter.class);
		addFilterBindingTo(ScriptHelperDocumentFilter.class);
	}
}
