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
/**
 * Implements the JavaScript API for the system.  Generally we're talking thin
 * wrappers around other functionality.  has some fairly specific bootstrapping
 * needs. maybe. if i get it to work like i want
 * 
 * There are two levels of API defined. There are the host functions and objects,
 * which are created as properties of the global root object (aliased as 'global')
 * with comment slashes (//) in front of their name.  This is to make it highly
 * unlikely for them to be used from client code.  These objects are implemented
 * in terms of the kernel API.
 * 
 * Then there is the user-level API, which is implemented in terms of the host
 * object API.  This layer mainly bridges the gap between the type system of
 * java and javascript.
 */
package jj.api;