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

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import jj.NonBlocking;

import net.jcip.annotations.Immutable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.ParseError;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;

/**
 * <p>
 * Responsible for turning a string into a JSoup node of some type,
 * maintaining information about it such as parsing errors
 * </p>
 * 
 * @author jason
 *
 */
@Immutable
public final class HTMLFragment {
	
	
	public static final Tag NODE_LIST_SENTINEL_TAG = Tag.valueOf("jj.sentinel.tag");
	
	private static final Pattern DOC_DETECTOR =
			Pattern.compile("(?si)^\\s*(?:<!doctype |<)html>.*");
	
	private final List<ParseError> parseErrors;
	private final Element element;
	
	@NonBlocking
	HTMLFragment(final String source) {
		assert (source != null && !source.trim().isEmpty()) : "source is required";
		
		Parser parser = Parser.htmlParser();
		Document parsed = parser.setTrackErrors(100).parseInput(source, "");
		parseErrors = Collections.unmodifiableList(parser.getErrors());
		
		if (DOC_DETECTOR.matcher(source).matches()) {
			element = parsed;
		} else {
			element = parsed.select("body").first();
		}
		//contextualizeErrors(source);
	}
	
	/* *
	 * Take the JSoup parser errors and make something human readable out of them
	 * for now we don't much care, this was just a scratchpad
	private void contextualizeErrors(String source) {
		for (ParseError error : parseErrors) {
			int position = error.getPosition();
			int basePosition = Math.max(0, position - 25);
			int endPosition = Math.min(source.length(), position + 15);
			System.out.println(error.getErrorMessage());
			System.out.println(source.substring(basePosition, endPosition).replace('\t', ' ').replace('\n',	' '));
			System.out.println("                    ----^");
		}
	}
	 */
	
	/**
	 * If this returns NODE_LIST_SENTINEL_TAG as its Tag, then
	 * the child nodes are what counts.
	 * @return
	 */
	@NonBlocking
	public Element element() {
		return element == null ? null : element.clone();
	}

	@NonBlocking
	public List<ParseError> errors() {
		return parseErrors;
	}
	
	@NonBlocking
	public boolean isStandaloneElement() {
		return isStandaloneElement(element);
	}
	
	@NonBlocking
	public boolean isCompleteDocument() {
		return isCompleteDocument(element);
	}

	@NonBlocking
	public static boolean isStandaloneElement(final Element element) {
		return !isCompleteDocument(element) && !isNodeList(element);
	}

	@NonBlocking
	public static boolean isCompleteDocument(final Element element) {
		return element instanceof Document;
	}

	@NonBlocking
	public static boolean isNodeList(final Element element) {
		return element.tag() == NODE_LIST_SENTINEL_TAG;
	}
}