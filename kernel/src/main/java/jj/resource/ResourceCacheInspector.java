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
package jj.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jj.application.AppLocation;
import jj.script.Global;
import jj.script.RhinoContext;

/**
 * <p>
 * Exposes information about the Resource contents of ResourceCache to
 * the script API layer. Specifically, it mediates accessibility to the
 * data structures, and performs transformation steps to collate interesting
 * facts about the various resources.
 * 
 * <p>
 * This class is written with the assumption that it is being consumed by an
 * client using d3's force layout.  in particular, the list of links just exposes
 * tiny objects that reference the indexes in the node list, to save the trouble
 * of passing the whole world over the wire a few times, since the force layout
 * allows that optimization
 * 
 * <p>
 * Basic lifecycle is
 * <ol>
 *   <li>get an instance of this class. A single instance is a snapshot in time
 *       of the cache contents
 *   <li>call the appropriate methods to retrieve transformed information about
 *       individual resource nodes and their dependency relationships
 *   <li>discard
 * </ol>
 * 
 * <p>
 * Why not just put all this in the ResourceCacheImpl, you might ask. I'll tell you!
 * because that shouldn't be public, but the script API requires public classes,
 * and because the transformation steps can be isolated, so they should be isolated.
 * no need to give one class two jobs.
 * 
 * @author jason
 *
 */
public class ResourceCacheInspector {

	private final List<AbstractResource> resources;
	private final Provider<RhinoContext> contextProvider;
	private final ScriptableObject global;
	
	private final Map<Resource, Integer> reverseIndex = new HashMap<>();
	
	private final Scriptable nodes;
	private final Scriptable links;
	private final Scriptable types;
	private final Scriptable bases;
	
	@Inject
	ResourceCacheInspector(
		final ResourceCacheImpl resourceCache,
		final ResourceCreators resourceCreators,
		final Provider<RhinoContext> contextProvider,
		final @Global ScriptableObject global
	) {
		this.resources = resourceCache.allResources();
		this.contextProvider = contextProvider;
		this.global = global;
		
		nodes = makeNodes();
		links = makeLinks();
		types = makeTypes(resourceCreators.knownResourceTypeNames());
		bases = makeBases();
	}
	
	private Scriptable makeNodes() {
		
		try (RhinoContext context = contextProvider.get()) {
			Scriptable resultArray = context.newArray(global, resources.size());
			int index = 0;
			for (AbstractResource resource : resources) {
				reverseIndex.put(resource, index);
				Scriptable rObj = context.newObject(global);
				resource.describe(rObj);
				resultArray.put(index++, resultArray, rObj);
			}
			return resultArray;
		}
	}
	
	private Scriptable makeLinks() {
		try (RhinoContext context = contextProvider.get()) {
			// maybe no links! maybe a freakin million! WE DON'T KNOW!
			Scriptable resultArray = context.newArray(global, 0);
			int index = 0;
			for (AbstractResource r1 : resources) {
				for (AbstractResource r2 : r1.dependents()) {
					Scriptable link = context.newObject(global);
					link.put("source", link, reverseIndex.get(r1));
					link.put("target", link, reverseIndex.get(r2));
					resultArray.put(index++, resultArray, link);
				}
			}
			return resultArray;
		}
	}
	
	private Scriptable makeTypes(List<String> typeNames) {
		try (RhinoContext context = contextProvider.get()) {
			Scriptable resultArray = context.newArray(global, typeNames.size());
			int index = 0;
			for (String typeName : typeNames) {
				resultArray.put(index++, resultArray, typeName);
			}
			return resultArray;
		}
	}
	
	private Scriptable makeBases() {
		try (RhinoContext context = contextProvider.get()) {
			Scriptable resultArray = context.newArray(global, AppLocation.values().length);
			int index = 0;
			for (AppLocation baseName : AppLocation.values()) {
				resultArray.put(index++, resultArray, baseName.name());
			}
			return resultArray;
		}
	}
	
	public Scriptable nodes() {
		return nodes;
	}
	
	public Scriptable links() {
		return links;
	}
	
	public Scriptable types() {
		return types;
	}
	
	public Scriptable bases() {
		return bases;
	}
}
