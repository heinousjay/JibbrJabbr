/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.i18n;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;

import jj.configuration.resolution.AppLocation;
import jj.resource.AbstractResource;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceFinder;
import jj.util.SHA1Helper;

/**
 * <p>
 * Represents a collection of {@link PropertiesResource}s that are accessed in
 * a manner similar to the {@link java.util.PropertyResourceBundle}, although
 * simpler.
 * 
 * <p>
 * Identify by a name and a {@link Locale}.  Candidate resource names are generated
 * based on this combination.  For example, given the name "index" and {@link Locale#US}
 * then an attempt will be made to load the following resources:
 * <ul>
 * 	<li>index_en_US.properties
 * 	<li>index_en.properties
 * 	<li>index.properties
 * </ul> 
 * 
 * <p>
 * Retrieve messages by key. PropertiesResources will be checked from most 
 * to least specific and the first result found will be returned. If no match is
 * found, null is returned.
 * 
 * @author jason
 *
 */
public class MessagesResource extends AbstractResource {
	
	// doubled because java8 is a pain
	private static final String __ = "_";
	private static final String EXT = ".properties";
	
	private final Locale locale;
	
	private final PropertiesResource[] propertiesResources;
	
	private final String sha;

	@Inject
	MessagesResource(
		final Dependencies dependencies,
		final Locale locale,
		final ResourceFinder resourceFinder
	) {
		super(dependencies);
		
		this.locale = locale;
		
		this.propertiesResources = findResources(resourceFinder);
		
		if (propertiesResources.length == 0) {
			throw new NoSuchResourceException(MessagesResource.class, name);
		}
		
		String[] shas = new String[propertiesResources.length];
		int count = 0;
		for (PropertiesResource r : propertiesResources) {
			shas[count++] = r.sha1();
		}
		sha = SHA1Helper.keyFor(shas);
	}
	
	private PropertiesResource[] findResources(final ResourceFinder resourceFinder) {
		ArrayList<PropertiesResource> result = new ArrayList<>(4);
		for (String candidateName : candidateNames()) {
			PropertiesResource resource =
				resourceFinder.loadResource(PropertiesResource.class, AppLocation.Base, candidateName);
			if (resource != null) {
				result.add(resource);
				resource.addDependent(this);
			} // else somehow flag that we want to know if it gets created
		}
		
		return result.toArray(new PropertiesResource[result.size()]);
	}
	
	private String[] candidateNames() {
		return new String[] {
			name + __ + locale.getLanguage() + __ + locale.getCountry() + __ + locale.getVariant() + EXT,
			name + __ + locale.getLanguage() + __ + locale.getCountry() + EXT,
			name + __ + locale.getLanguage() + EXT,
			name + EXT
		};
	}
	
	public Locale locale() {
		return locale;
	}

	@Override
	public String sha1() {
		return sha;
	}
	
	public boolean containsKey(String key) {
		boolean result = false;
		for (PropertiesResource resource : propertiesResources) {
			if (resource.properties().containsKey(key)) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	public String message(String key) {
		String result = null;
		for (PropertiesResource resource : propertiesResources) {
			if (resource.properties().containsKey(key)) {
				result = resource.properties().get(key);
				break;
			}
		}
		
		return result;
	}
	
	@Override
	protected boolean removeOnReload() {
		// this is a root resource
		return false;
	}

	@Override
	public boolean needsReplacing() throws IOException {
		// always replaced by dependencies
		return false;
	}

}
