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
package jj.webdriver;

import java.util.IllegalFormatException;

/**
 * @author jason
 *
 */
public class ByReader {
	
	private final By by;
	
	private final boolean needsResolution;
	private final String type;
	private final String value;

	public ByReader(By by) {
		this.by = by;
		validate();
		needsResolution = extractResolution();
		type = extractType();
		value = extractValue();
	}
	
	public boolean needsResolution() {
		return needsResolution;
	}
	
	public String type() {
		return type;
	}
	
	// determines if the value can be used as a format string for the given arguments
	public boolean validateValueAsFormatterFor(Object...args) {
		try {
			String.format(value, args);
			return true;
		} catch (IllegalFormatException ife) {
			return false;
		}
	}
	
	public String value() {
		return value;
	}
	
	private boolean extractResolution() {
		return !empty(by.value());
	}
	
	private String extractType() {
		if (!empty(by.value())) {
			return "id";
		}
		if (!empty(by.id())) {
			return "id";
		}
		if (!empty(by.className())) {
			return "className";
		}
		if (!empty(by.selector())) {
			return "selector";
		}
		if (!empty(by.xpath())) {
			return "xpath";
		}
		return null;
	}
	
	private String extractValue() {
		if (!empty(by.value())) {
			return by.value();
		}
		if (!empty(by.id())) {
			return by.id();
		}
		if (!empty(by.className())) {
			return by.className();
		}
		if (!empty(by.selector())) {
			return by.selector();
		}
		if (!empty(by.xpath())) {
			return by.xpath();
		}
		return null;
	}
	
	private void validate() {
		int found = 0;
		
		if (!empty(by.value())) {
			found++;
		}
		if (!empty(by.id())) {
			found++;
		}
		if (!empty(by.className())) {
			found++;
		}
		if (!empty(by.selector())) {
			found++;
		}
		if (!empty(by.xpath())) {
			found++;
		}
		if (found != 1) {
			throw new AssertionError("By annotation is not valid - exactly one attribute must be specified.");
		}
	}
	
	private boolean empty(String string) {
		return string == null || string.isEmpty();
	}
	
	
}
