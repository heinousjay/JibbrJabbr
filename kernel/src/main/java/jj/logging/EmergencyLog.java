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
package jj.logging;

/**
 * <p>
 * A component to log emergencies that haven't yet been / may not
 * ever be converted to events.  Uses the event system internally.
 * Points of use of this class should be considered carefully as
 * events.  This may be a temporary measure on the road to doing
 * that conversion, but i wanted to make sure that errors were going
 * to be logged all the time while i figured that angle out
 * 
 * @author jason
 *
 */
public interface EmergencyLog {
	
	
	void error(String message, Throwable t);
	
	void error(String message, Object...args);
	
	void warn(String message, Object...args);
}
