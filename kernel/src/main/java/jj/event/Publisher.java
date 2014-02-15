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
 * Inject this to publish events.  Event objects can be of any type except
 * Object.  Due to type erasure, generics will not work unless they are reified.
 * 
 * <p>
 * They should probably be immutable in most circumstances but it's not
 * at all required.
 * 
 * @author jason
 *
 */
public interface Publisher {
	
	void publish(Object event);

}
