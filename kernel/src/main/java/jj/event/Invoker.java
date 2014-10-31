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
package jj.event;

/**
 * <p>
 * The API into the generated listener invoking instances.
 * Implementing this statically doesn't do anything in this system,
 * it's only public because it has to be.
 * @author jason
 *
 */
public interface Invoker {
	
	/** a description of the target listener, for debugging */
	String target();
	
	/** invoke the target method */
	void invoke(Object arg);
}
