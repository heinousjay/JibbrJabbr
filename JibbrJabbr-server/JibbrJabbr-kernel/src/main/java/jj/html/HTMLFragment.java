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
package jj.html;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.util.CharsetUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;

/**
 * <p>
 * Coordinates all activities relating to an HTML source file, which
 * can be a document or fragment thereof.
 * </p>
 * 
 * @author jason
 *
 */
public final class HTMLFragment {
	
	public static final Tag NODE_LIST_SENTINEL_TAG = Tag.valueOf("joj.sentinel.tag");
	
	private final Path path;
	private final AtomicReference<Element> element = new AtomicReference<>();

	public HTMLFragment(final Path path) {
		assert path != null;
		assert Files.exists(path);
		this.path = path;
		
		populate();
	}
	
	public Path path() {
		return path;
	}
	
	public void reload() {
		populate();
	}
	
	public void destroy() {
		// nothing to do
	}
	
	/**
	 * If this returns NODE_LIST_SENTINEL_TAG as its Tag, then
	 * the child nodes are what counts.
	 * @return
	 */
	public Element element() {
		Element core = element.get();
		// always returns the clone, not the "canonical representation"
		// since obviously we don't want that modified
		return core == null ? null : core.clone();
	}
	
	public static boolean isStandaloneElement(Element element) {
		return !isCompleteDocument(element) && !isNodeList(element);
	}
	
	public static boolean isCompleteDocument(Element element) {
		return element instanceof Document;
	}
	
	public static boolean isNodeList(Element element) {
		return element.tag() == NODE_LIST_SENTINEL_TAG;
	}
	
	private String getRawHTML() {
		try {
			return new String(Files.readAllBytes(path), CharsetUtil.UTF_8).trim();
		} catch (IOException e) {
			// TODO - gotta make this stuff somewhat nicer
			throw new RuntimeException(e);
		}
	}
	
	private void populate() {
		Element old = element.get();
		String raw = getRawHTML();
		// does it start with a <!DOCTYPE HTML> or <html>? it's a document.  
		// for now, who cares, just get it loading
		Element parsed;
		if (raw.startsWith("<!DOCTYPE") || raw.startsWith("<html>")) {
			parsed = Parser.parse(raw, "");
		} else {
			List<Node> nodes = Parser.parseFragment(raw, null, "");
			// if there is one node and it's an element, just use that
			if (nodes.size() == 1 && nodes.get(0) instanceof Element) {
				parsed = (Element)nodes.get(0);
			} else {
				parsed = new Element(NODE_LIST_SENTINEL_TAG, "");
				for (Node node : nodes) {
					parsed.appendChild(node);
				}
			}
		}
		
		element.compareAndSet(old, parsed);
	}
	
	@Override
	public String toString() {
		return String.format("%s from %s",
			HTMLFragment.class.getName(),
			path
		);
	}
}