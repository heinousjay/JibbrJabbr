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
package jj.jasmine;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * A specific spec result from a jasmine run
 * @author jason
 *
 */
class Spec {
	
	final String description;
	final Suite parent;
	final LinkedHashMap<String, Spec> children = new LinkedHashMap<>();
	final ArrayList<String> failedExpectations = new ArrayList<>();
	
	String status = "pending";
	
	Spec(String id, String description, Suite parent) {
		this.description = description;
		this.parent = parent;
		if (parent != null) {
			parent.children.put(id, this);
		}
	}
	
	public boolean failed() {
		return status == "failed";
	}
	
	protected StringBuilder makeToString(StringBuilder sb, int indentation) {
		
		for (int i = 0; i < indentation; ++i) {
			sb.append(' ');
		}
		
		sb.append(description).append(" - ").append(status).append('\n');
		
		if (!failedExpectations.isEmpty()) {
			for (String fe : failedExpectations) {
				for (int i = 0; i < indentation; ++i) {
					sb.append(' ');
				}
				sb.append(" - ").append(fe).append('\n');
			}
		}
		
		for (Spec child : children.values()) {
			child.makeToString(sb, indentation + 2);
		}
		
		return sb;
	}
}