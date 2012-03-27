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
package jj;

/**
 * Represents some level of Application containment,
 * coordinating the set of resources that make it up
 * - picocontainer
 * - kernel connector
 * - classloader
 * - filesystem path
 * - library jars
 * - sub applications
 * 
 * Primary responsibility is to mediate between the app
 * and kernel services, which basically is to say all
 * i/o runs through the kernel.  This class and anything
 * it controls should never block.
 * 
 * The root Application in a given hierarchy establishes
 * the thread pool for that application, and is responsible
 * for creating and maintaining its own children.
 * 
 * @author Jason Miller
 *
 */
public class Application {

}
